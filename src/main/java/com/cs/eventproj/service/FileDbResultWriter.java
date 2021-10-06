package com.cs.eventproj.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cs.eventproj.api.EventConstant;
import com.cs.eventproj.api.WriterApi;
import com.cs.eventproj.model.ResultEventModel;

/**
 * File writer implementation of {@link WriterApi}
 * 
 * @author sathish
 *
 */
public class FileDbResultWriter implements WriterApi {

	private static final Logger LOG = LoggerFactory.getLogger(FileDbResultWriter.class);
	private static final String DBURL = "jdbc:hsqldb:file:logdb;hsqldb.default_table_type=cached";
	private static final String DBUSER = "SA", EMPTY = "";
	private static final String CREATE_TABLE_SQL = "CREATE TABLE LOGEVENTS (ID VARCHAR(255) PRIMARY KEY, DURATION INT NOT NULL, TYPE VARCHAR(255), HOST VARCHAR(100), ALERT VARCHAR(10));";
	private BlockingQueue<ResultEventModel> bufferedData = new ArrayBlockingQueue<>(10);
	private ScheduledExecutorService scheduleSvc = null;

	public FileDbResultWriter() {
		try {
			init();
		} catch (ClassNotFoundException e) {
			LOG.error("Error at writer init.", e);
		}
	}

	/**
	 * Pushes the data for DB writing queue, upon occupied size, runtime exception
	 * would be thrown
	 * 
	 * @param data
	 */
	@Override
	public void write(ResultEventModel data) {
		if (data == null) {
			// Shutdown indication
			LOG.info("---> Initiating writer shutdown");
			scheduleSvc.shutdown();
			return;
		}
		bufferedData.add(data);
	}

	private void init() throws ClassNotFoundException {
		try (Connection conn = DriverManager.getConnection(DBURL, DBUSER, EMPTY);
				Statement stmt = conn.createStatement()) {

			// Verify if the table already exists.
			DatabaseMetaData dbMetaData = conn.getMetaData();
			ResultSet tableRs = dbMetaData.getTables(null, null, EventConstant.TABLE_NAME, new String[] { "TABLE" });
			if (tableRs.next()) {
				LOG.info("Table already exists");
			} else {
				LOG.info("Creating table structure");
				ResultSet rs = stmt.executeQuery(CREATE_TABLE_SQL);
				rs.close();
			}

		} catch (SQLException e) {
			LOG.error(e.getMessage());
		}

		// Initiate scheduler for DB writer to read from queue
		scheduleSvc = Executors.newSingleThreadScheduledExecutor();
		scheduleSvc.execute(writeNow());
	}

	/**
	 * Writes the queued data to DB, each invocation a row of data will be added
	 * 
	 * @return {@link Runnable}
	 */
	private Runnable writeNow() {
		return () -> {
			try (Connection conn = DriverManager.getConnection(DBURL, DBUSER, EMPTY);
					Statement stmt = conn.createStatement()) {
				ResultEventModel data = null;
				int result = 0;
				while ((data = bufferedData.poll(5, TimeUnit.SECONDS)) != null) {
					String sql = String.format(
							"INSERT INTO LOGEVENTS (ID, DURATION, TYPE, HOST, ALERT) VALUES ('%s', '%d', '%s', '%s', '%s');",
							data.getId(), data.getDuration(), data.getType(), data.getHost(), data.isAlert());
					LOG.info("Adding data to table: {}", sql);
					result = 0;
					try {
						result = stmt.executeUpdate(sql);
						if (result < 1) {
							LOG.error("Failed to insert table, {}", data.getId());
						}
					} catch (SQLException sqlExcp) {
						LOG.error("Adding record id '{}' failed, reason: {}", data.getId(), sqlExcp.getMessage());
					}
					data = null;
				}
			} catch (SQLException | InterruptedException e) {
				LOG.error(e.getMessage());
			}

		};
	}

}

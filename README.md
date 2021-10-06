# CS Log Event Analyzer
For CS offline project

Project build: mvn clean install -DskipTests

Intended design points:
 - Lombok artifact is intentionally avoided due to an issue with my eclipse setup on Ubuntu OS.
 - The project are designed as independent movules for log read, lod process & log write.
 - Four Test cases would be failing in command line but would pass in the IDE, its due to the multi-thread design
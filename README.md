# incubator-flagon-tap
prerequisites: docker, maven, java

To start the Kafka stack:
`docker-compose up -d`

To compile the streams app:
`mvn clean package`

To run the streams app:
`mvn exec:java "-Dexec.mainClass=tap.Tap"`

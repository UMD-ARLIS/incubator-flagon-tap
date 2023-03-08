# incubator-flagon-tap
prerequisites: docker, java, maven

Kafka and Druid can be run seperately or together, and started in any order. To start them run:
`docker-compose -f PATH_TO_YML_FILE up -d`

To compile the streams app:
`mvn clean package`

To run the streams app:
`mvn exec:java "-Dexec.mainClass=tap.Tap"`

*Note that mvn commands must be run from within `tap/`

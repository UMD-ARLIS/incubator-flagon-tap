# incubator-flagon-tap
prerequisites: docker, maven, python

To start the Kafka stack run:
`python kafka_docker/start.py`

Start script and .yml files are derived from here:
https://developer.confluent.io/get-started/rest/


To build and run the streams app (run from flagon-tap/):
`mvn clean package`
`mvn exec:java "-Dexec.mainClass=tap.Pipe"`

maven archetype and Pipe code are taken from here:
https://kafka.apache.org/34/documentation/streams/tutorial

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.StreamsConfig.{APPLICATION_ID_CONFIG, BOOTSTRAP_SERVERS_CONFIG}
import org.apache.kafka.streams.KafkaStreams
import java.util.Properties


object Tap extends App {
  val props = new Properties()
  props.put(APPLICATION_ID_CONFIG, "Tap")
  props.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val builder: StreamsBuilder = new StreamsBuilder

  val topology = builder.build()
  val streams = new KafkaStreams(topology, props)

  streams.start()

  Runtime.getRuntime.addShutdownHook(new Thread(() => streams.close()))
}
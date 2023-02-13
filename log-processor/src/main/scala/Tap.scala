import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.kstream.Printed
import java.util.Properties

object Tap extends App {

  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "Tap")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p
  }

  val builder: StreamsBuilder = new StreamsBuilder

  val logs: KStream[String, String] = builder.stream[String, String]("RawLogsTopic")
  logs.print(Printed.toSysOut())
  logs.to("ProcessedLogsTopic")
  
  val topology = builder.build()
  val streams = new KafkaStreams(topology, props)

  streams.start()
  sleep(30)

  Runtime.getRuntime.addShutdownHook(new Thread(() => streams.close()))
}
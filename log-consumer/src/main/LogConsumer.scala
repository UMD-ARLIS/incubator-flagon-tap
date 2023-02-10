
// import java.util.Properties
// import org.apache.kafka.clients.consumer.KafkaConsumer
// import scala.collection.JavaConverters._

object LogConsumer {
    def main(args: Array[String]) = {
        println("Hello, world")
        // val props: Properties = {
        // val p = new Properties()
        //     p.put(StreamsConfig.APPLICATION_ID_CONFIG, "LogConsumer")
        //     p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        //     p
        // }

        // val consumer = new KafkaConsumer(props)
        // try {
        //     consumer.subscribe("RawLogsTopic")
        // while (true) {
        //     val records = consumer.poll(10)
        //     for (log <- records)
        //     println(log)
        // }
        // }finally {
        //     consumer.close()
        // }
    }
}
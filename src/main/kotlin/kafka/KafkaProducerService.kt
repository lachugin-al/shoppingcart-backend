package kafka

import config.Config
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Order
import mu.KotlinLogging
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

private val logger = KotlinLogging.logger {}

/**
 * Kafka Producer для отправки заказов.
 *
 * @param config Конфигурация приложения.
 */
class KafkaProducerService(config: Config) {

    private val producer: KafkaProducer<String, String>
    private val topic: String = config.kafkaTopic

    init {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBrokers.joinToString(","))
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.ACKS_CONFIG, "all") // Гарантия доставки
            put(ProducerConfig.RETRIES_CONFIG, 3) // Попытки при сбое
            put(ProducerConfig.LINGER_MS_CONFIG, 10) // Задержка для группировки сообщений
        }
        producer = KafkaProducer(props)
        logger.info { "Kafka producer initialized for topic $topic" }
    }

    /**
     * Отправляет заказ в Kafka.
     *
     * @param order Объект [Order] для отправки.
     */
    fun sendOrder(order: Order) {
        try {
            val orderJson = Json.encodeToString(order)
            val record = ProducerRecord(topic, order.orderUid, orderJson)

            producer.send(record) { metadata, exception ->
                if (exception != null) {
                    logger.error(exception) { "Failed to send order to topic $topic" }
                } else {
                    logger.info {
                        "Order sent to topic ${metadata.topic()}, partition ${metadata.partition()}, offset ${metadata.offset()}"
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error while sending order to Kafka" }
        }
    }

    /**
     * Закрывает Kafka Producer.
     */
    fun close() {
        producer.close()
        logger.info { "Kafka producer closed" }
    }
}

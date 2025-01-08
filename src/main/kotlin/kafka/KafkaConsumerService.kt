package kafka

import cache.OrderCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import model.Order
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import service.OrderService
import java.time.Duration
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * KafkaConsumerService - класс для работы с Kafka Consumer.
 *
 * @property brokers Список адресов Kafka-брокеров.
 * @property topic Название Kafka-топика для чтения сообщений.
 * @property groupId Идентификатор группы потребителей.
 * @property orderService Сервис для работы с заказами.
 * @property orderCache Кэш для хранения заказов.
 */
class KafkaConsumerService(
    private val brokers: List<String>,
    private val topic: String,
    private val groupId: String,
    private val orderService: OrderService,
    private val orderCache: OrderCache
) {

    private val consumer: KafkaConsumer<String, String>

    init {
        // Инициализация KafkaConsumer с заданными настройками
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers.joinToString(","))
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest") // Начинаем с первого сообщения
        }
        consumer = KafkaConsumer(props)
        consumer.subscribe(listOf(topic))

        logger.info { "Kafka consumer created: topic=$topic, groupId=$groupId" }
    }

    /**
     * Запускает процесс чтения сообщений из Kafka.
     *
     * @throws Exception Если возникают ошибки во время обработки сообщений.
     */
    suspend fun run() = withContext(Dispatchers.IO) {
        logger.info { "Kafka consumer started" }
        try {
            while (true) {
                // Получаем записи из Kafka-топика
                val records = consumer.poll(Duration.ofMillis(1000))
                for (record in records) {
                    val messageValue = record.value()
                    try {
                        // Десериализация JSON-сообщения в объект Order
                        val order = Json.decodeFromString<Order>(messageValue)

                        // Сохраняем заказ в базу данных через OrderService
                        orderService.saveOrder(order)

                        // Добавляем заказ в кэш
                        orderCache.set(order)

                        logger.info { "Order processed successfully: orderUID=${order.orderUid}" }
                    } catch (e: SerializationException) {
                        // Логируем ошибки десериализации JSON
                        logger.warn { "Failed to decode JSON message: ${e.message}" }
                    } catch (e: Exception) {
                        // Логируем любые другие ошибки
                        logger.warn { "Failed to process order: ${e.message}" }
                    }
                }
            }
        } catch (e: Exception) {
            // Логируем критические ошибки при обработке Kafka-сообщений
            logger.error(e) { "Error during Kafka consumer processing" }
        } finally {
            // Закрываем Kafka Consumer
            close()
        }
    }

    /**
     * Закрывает Kafka Consumer.
     *
     * @throws Exception Если возникает ошибка при закрытии.
     */
    fun close() {
        logger.info { "Closing Kafka consumer" }
        consumer.close()
    }
}

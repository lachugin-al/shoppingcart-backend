package server

import cache.OrderCache
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import kafka.KafkaProducerService
import kotlinx.serialization.json.Json
import model.Order
import mu.KotlinLogging
import util.OrderTestDataGenerator

private val logger = KotlinLogging.logger {}

/**
 * Ktor HTTP Server.
 *
 * @param port Порт для запуска сервера.
 * @param orderCache Кэш заказов.
 * @param staticDir Директория со статическими файлами.
 * @param kafkaProducer Kafka продюсер для отправки заказов.
 */
class KtorHttpServer(
    private val port: Int,
    private val orderCache: OrderCache,
    private val staticDir: String,
    private val kafkaProducer: KafkaProducerService
) {

    private val server = embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true })
        }
        routing {
            route("/api") {
                get("/order/{id}") {
                    val orderId = call.parameters["id"]
                    if (orderId.isNullOrBlank()) {
                        call.respondText("Order ID is required", status = HttpStatusCode.BadRequest)
                        return@get
                    }

                    logger.info { "Received order request for ID: $orderId" }

                    val order = orderCache.get(orderId)
                    if (order == null) {
                        call.respondText("Order not found", status = HttpStatusCode.NotFound)
                    } else {
                        call.respond(HttpStatusCode.OK, order)
                    }
                }

                get("/orders") {
                    call.respond(orderCache.getAll())
                }

                post("/send-test-order") {
                    val testOrder: Order = OrderTestDataGenerator.generateOrder()
                    logger.info { "Generated test order: ${testOrder.orderUid}" }

                    kafkaProducer.sendOrder(testOrder)
                    logger.info { "Test order sent to Kafka: ${testOrder.orderUid}" }

                    call.respondText("Test order sent successfully")
                }
            }

            staticResources("/", "web")
        }
    }

    fun start() {
        logger.info { "Starting Ktor HTTP server on port $port" }
        server.start(wait = true)
    }
}
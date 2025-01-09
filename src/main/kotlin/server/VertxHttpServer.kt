package server

import cache.OrderCache
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Класс VertxHttpServer на основе Vert.x для работы с заказами.
 *
 * @param port Порт для запуска сервера.
 * @param orderCache Кэш для доступа к заказам.
 * @param staticDir Директория для статических файлов (например, index.html).
 */
class VertxHttpServer(
    private val port: Int,
    private val orderCache: OrderCache,
    private val staticDir: String
) {
    val vertxOptions = io.vertx.core.VertxOptions()
        .setWorkerPoolSize(64) // Увеличить количество потоков для обработки задач
        .setEventLoopPoolSize(16) // Увеличить количество потоков для цикла событий

    private val vertx = Vertx.vertx(vertxOptions)
    private val router: Router = Router.router(vertx)

    /**
     * Запускает HTTP-сервер.
     */
    fun start() {
        setupRoutes()

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(port) { result ->
                if (result.succeeded()) {
                    logger.info { "HTTP server started on port $port" }
                } else {
                    logger.error(result.cause()) { "Failed to start HTTP server" }
                }
            }
    }

    /**
     * Настраивает маршруты для обработки запросов.
     */
    private fun setupRoutes() {
        // Маршрут для получения заказа по ID
        router.get("/order/:id").handler { ctx ->
            val orderId = ctx.pathParam("id")
            logger.info { "Received request for order: $orderId" }

            val order = orderCache.get(orderId)
            if (order != null) {
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(order.toJson())
            } else {
                ctx.response()
                    .setStatusCode(404)
                    .end("Order not found")
            }
        }

        // Обработка статических файлов
        router.route("/*").handler(StaticHandler.create(staticDir))
    }

    /**
     * Преобразует объект в JSON с использованием kotlinx.serialization.
     */
    private val jsonSerializer = Json {
        encodeDefaults = true
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    private inline fun <reified T> T.toJson(): String {
        return jsonSerializer.encodeToString(this)
    }

    /**
     * Останавливает сервер.
     */
    fun stop() {
        vertx.close { result ->
            if (result.succeeded()) {
                logger.info { "HTTP server stopped" }
            } else {
                logger.error(result.cause()) { "Failed to stop HTTP server" }
            }
        }
    }
}

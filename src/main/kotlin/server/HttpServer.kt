package server

import cache.OrderCache
import mu.KotlinLogging
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

/**
 * Класс Server представляет HTTP-сервер для работы с заказами.
 *
 * @param port порт для запуска сервера.
 * @param orderCache кэш для доступа к заказам.
 * @param staticDir директория для статических файлов (например, index.html).
 */
class HttpServer(
    private val port: Int,
    private val orderCache: OrderCache,
    private val staticDir: String
) {

    private val server: HttpServer = HttpServer.create(InetSocketAddress(port), 0).apply {
        executor = Executors.newFixedThreadPool(16)
        createContext("/order/") { exchange -> handleGetOrderByID(exchange) }
        createContext("/") { exchange -> handleStatic(exchange) }
    }

    init {
        logger.info { "HTTP server initialized on port $port" }
        if (staticDir.isNotEmpty()) {
            logger.info { "Static content will be served from directory: $staticDir" }
        }
    }

    /**
     * Запускает сервер и блокируется до завершения работы.
     */
    fun start() {
        logger.info { "HTTP server is starting" }
        server.start()
    }

    /**
     * Останавливает сервер.
     *
     * @param delay задержка перед остановкой сервера (в миллисекундах).
     */
    fun stop(delay: Int = 0) {
        logger.info { "HTTP server is stopping" }
        server.stop(delay)
        logger.info { "HTTP server stopped" }
    }

    /**
     * Обрабатывает запросы на получение заказа по ID.
     */
    private fun handleGetOrderByID(exchange: HttpExchange) {
        @Suppress("TooGenericExceptionCaught")
        with(exchange) {
            try {
                if (requestMethod != "GET") {
                    sendResponse("Method Not Allowed", 405)
                    return
                }

                val orderID = requestURI.path.removePrefix("/order/")
                if (orderID.isBlank()) {
                    sendResponse("Order ID is required", 400)
                    return
                }

                logger.info { "Received order request for ID: $orderID" }
                val order = orderCache.get(orderID)
                if (order == null) {
                    sendResponse("Order not found", 404)
                    return
                }

                sendResponse(order.toJson(), 200, "application/json")
            } catch (e: Exception) {
                logger.error(e) { "Failed to process order request" }
                sendResponse("Internal Server Error", 500)
            }
        }
    }

    /**
     * Обрабатывает запросы на получение статических файлов.
     */
    private fun handleStatic(exchange: HttpExchange) {
        with(exchange) {
            @Suppress("TooGenericExceptionCaught")
            try {
                val filePath = if (requestURI.path == "/") "index.html" else requestURI.path.removePrefix("/")
                val file = File(staticDir, filePath)
                if (!file.exists() || file.isDirectory) {
                    sendResponse("File not found", 404)
                    return
                }

                logger.info { "Serving static file: ${file.absolutePath}" }
                val responseBytes = file.readBytes()
                sendResponse(responseBytes, 200, "text/html")
            } catch (e: Exception) {
                logger.error(e) { "Failed to serve static file" }
                sendResponse("Internal Server Error", 500)
            }
        }
    }

    /**
     * Отправляет ответ HTTP.
     */
    private fun HttpExchange.sendResponse(response: String, statusCode: Int, contentType: String = "text/plain") {
        sendResponse(response.toByteArray(), statusCode, contentType)
    }

    /**
     * Отправляет ответ HTTP.
     */
    private fun HttpExchange.sendResponse(responseBytes: ByteArray, statusCode: Int, contentType: String) {
        responseHeaders.add("Content-Type", contentType)
        sendResponseHeaders(statusCode, responseBytes.size.toLong())
        responseBody.use { it.write(responseBytes) }
    }
}

/**
 * Преобразует объект Order в JSON.
 */
inline fun <reified T> T.toJson(): String {
    return Json.encodeToString(this)
}

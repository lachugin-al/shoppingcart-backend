package app

import cache.OrderCache
import config.ConfigLoader
import db.initDB
import kafka.KafkaConsumerService
import kafka.KafkaProducerService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import mu.KotlinLogging
import repository.DeliveriesRepositoryImpl
import repository.PaymentsRepositoryImpl
import repository.ItemsRepositoryImpl
import repository.OrdersRepositoryImpl
import server.HttpServer
import service.OrderServiceImpl
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

fun main() = runBlocking {
    logger.info { "Starting application..." }

    // Загружаем конфигурацию
    val config = ConfigLoader.loadConfig()
    logger.info { "Configuration loaded successfully" }

    // Создаем контекст для управления завершением работы приложения
    val applicationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val shutdownSignal = CompletableDeferred<Unit>()

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "Shutdown signal received. Waiting for ${config.shutdownTimeout.seconds} seconds..." }
        runBlocking {
            shutdownSignal.complete(Unit)
            applicationScope.cancel()
            withTimeoutOrNull(config.shutdownTimeout.toMillis()) {
                applicationScope.coroutineContext[Job]?.join()
            }
            logger.info { "Graceful shutdown complete" }
        }
    })

    @Suppress("TooGenericExceptionCaught")
    try {
        // Инициализация БД
        val database = initDB(config)
        logger.info { "Database initialized successfully" }

        // Создание репозиториев
        val deliveriesRepo = DeliveriesRepositoryImpl(database)
        val paymentsRepo = PaymentsRepositoryImpl(database)
        val itemsRepo = ItemsRepositoryImpl(database)
        val ordersRepo = OrdersRepositoryImpl(
            connection = database,
            deliveryRepository = deliveriesRepo,
            paymentRepository = paymentsRepo,
            itemsRepository = itemsRepo
        )

        // Инициализация кэша
        val orderCache = OrderCache()
        orderCache.loadFromDB(
            connection = database,
            ordersRepo = ordersRepo,
            deliveriesRepo = deliveriesRepo,
            paymentsRepo = paymentsRepo,
            itemsRepo = itemsRepo
        )
        logger.info { "Cache initialized successfully with ${orderCache.size()} orders" }

        // Инициализация сервисов
        val orderService = OrderServiceImpl(
            connection = database,
            ordersRepo = ordersRepo,
            deliveriesRepo = deliveriesRepo,
            paymentsRepo = paymentsRepo,
            itemsRepo = itemsRepo
        )

        // Инициализация Kafka Producer
        val kafkaProducer = KafkaProducerService(config)

        // Запуск Kafka-консьюмера
        val kafkaConsumerService = KafkaConsumerService(
            brokers = config.kafkaBrokers,
            topic = config.kafkaTopic,
            groupId = config.kafkaGroupId,
            orderService = orderService,
            orderCache = orderCache
        )

        val kafkaJob = applicationScope.launch {
            try {
                kafkaConsumerService.run()
            } catch (e: Exception) {
                logger.error(e) { "Kafka consumer stopped with an error" }
                shutdownSignal.complete(Unit)
            }
        }

        // Запуск HTTP-сервера
        val httpServer = HttpServer(
            port = config.httpPort,
            orderCache = orderCache,
            staticDir = "src/main/resources/web",
            kafkaProducer = kafkaProducer
        )

        val httpServerJob = applicationScope.launch {
            try {
                httpServer.start()
            } catch (e: Exception) {
                logger.error(e) { "HTTP server stopped with an error" }
                shutdownSignal.complete(Unit)
            }
        }

        // Ожидание сигнала завершения
        shutdownSignal.await()

        // Остановка компонентов с учетом shutdownTimeout
        logger.info { "Application shutting down..." }
        withTimeoutOrNull(config.shutdownTimeout.toMillis()) {
            kafkaJob.cancelAndJoin()
            httpServerJob.cancelAndJoin()
            applicationScope.coroutineContext[Job]?.join()
        }

        logger.info { "Application stopped successfully" }
    } catch (e: Exception) {
        logger.error(e) { "Fatal error occurred during application startup" }
        exitProcess(1)
    }
}

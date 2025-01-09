package cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import model.Order
import repository.DeliveriesRepository
import repository.ItemsRepository
import repository.OrdersRepository
import repository.PaymentsRepository
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Кэш для хранения заказов в памяти.
 */
class OrderCache {

    private val cache = ConcurrentHashMap<String, Order>() // Потокобезопасный словарь для хранения заказов

    /**
     * Загружает все заказы из базы данных в кэш.
     *
     * @param connection подключение к базе данных.
     * @param ordersRepo репозиторий заказов.
     * @param deliveriesRepo репозиторий доставок.
     * @param paymentsRepo репозиторий оплат.
     * @param itemsRepo репозиторий товаров.
     */
    suspend fun loadFromDB(
        connection: Connection,
        ordersRepo: OrdersRepository,
        deliveriesRepo: DeliveriesRepository,
        paymentsRepo: PaymentsRepository,
        itemsRepo: ItemsRepository
    ) {
        logger.info { "Starting to load orders into cache" }

        @Suppress("TooGenericExceptionCaught")
        try {
            // Получаем все идентификаторы заказов
            val orderUIDs = getAllOrderUIDs(connection)
            logger.info { "Fetched ${orderUIDs.size} order UIDs from the database" }

            // Загружаем каждый заказ
            for (uid in orderUIDs) {
                try {
                    val order = loadFullOrder(uid, ordersRepo, deliveriesRepo, paymentsRepo, itemsRepo)
                    cache[uid] = order
                    logger.info { "Order $uid successfully added to cache" }
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to load order $uid into cache" }
                }
            }

            logger.info { "Finished loading orders into cache. Total cached orders: ${cache.size}" }
        } catch (e: Exception) {
            logger.error(e) { "Error loading orders into cache" }
            throw e
        }
    }

    /**
     * Возвращает заказ из кэша по его уникальному идентификатору.
     *
     * @param orderUID уникальный идентификатор заказа.
     * @return объект [Order] или `null`, если заказ не найден.
     */
    fun get(orderUID: String): Order? {
        return cache[orderUID]?.also {
            logger.debug { "Order $orderUID retrieved from cache" }
        } ?: run {
            logger.warn { "Order $orderUID not found in cache" }
            null
        }
    }

    /**
     * Добавляет или обновляет заказ в кэше.
     *
     * @param order объект [Order] для добавления в кэш.
     */
    fun set(order: Order) {
        cache[order.orderUid] = order
        logger.info { "Order ${order.orderUid} added or updated in cache" }
    }

    /**
     * Получает список всех order_uid из таблицы orders.
     *
     * @param connection подключение к базе данных.
     * @return список order_uid.
     */
    private suspend fun getAllOrderUIDs(connection: Connection): List<String> = withContext(Dispatchers.IO) {
        val query = "SELECT order_uid FROM orders"
        val orderUIDs = mutableListOf<String>()

        connection.prepareStatement(query).use { statement ->
            statement.executeQuery().use { resultSet ->
                while (resultSet.next()) {
                    orderUIDs.add(resultSet.getString("order_uid"))
                }
            }
        }

        return@withContext orderUIDs
    }

    /**
     * Загружает полный заказ из базы данных.
     *
     * @param orderUID уникальный идентификатор заказа.
     * @param ordersRepo репозиторий заказов.
     * @param deliveriesRepo репозиторий доставок.
     * @param paymentsRepo репозиторий оплат.
     * @param itemsRepo репозиторий товаров.
     * @return объект [Order].
     */
    private suspend fun loadFullOrder(
        orderUID: String,
        ordersRepo: OrdersRepository,
        deliveriesRepo: DeliveriesRepository,
        paymentsRepo: PaymentsRepository,
        itemsRepo: ItemsRepository
    ): Order = withContext(Dispatchers.IO) {
        val order = ordersRepo.getByID(orderUID)
            ?: throw IllegalArgumentException("Order with UID $orderUID not found")

        val delivery = deliveriesRepo.getByOrderID(orderUID)
            ?: throw IllegalArgumentException("Delivery for order $orderUID not found")

        val payment = paymentsRepo.getByOrderID(orderUID)
            ?: throw IllegalArgumentException("Payment for order $orderUID not found")

        val items = itemsRepo.getByOrderID(orderUID)

        return@withContext order.copy(
            delivery = delivery,
            payment = payment,
            items = items
        )
    }

    // Возвращает количество заказов в кэше
    fun size(): Int {
        return cache.size
    }
}

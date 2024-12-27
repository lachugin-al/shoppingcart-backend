package service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.*
import mu.KotlinLogging
import repository.*
import java.sql.Connection
import java.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Интерфейс OrderService определяет бизнес-логику для работы с заказами.
 */
interface OrderService {
    suspend fun saveOrder(order: Order)
    suspend fun getOrderByID(orderUID: String): Order?
}

/**
 * Реализация OrderService.
 */
class OrderServiceImpl(
    private val connection: Connection,
    private val ordersRepo: OrdersRepository,
    private val deliveriesRepo: DeliveriesRepository,
    private val paymentsRepo: PaymentsRepository,
    private val itemsRepo: ItemsRepository
) : OrderService {

    /**
     * Сохраняет заказ в базе данных в рамках транзакции.
     */
    override suspend fun saveOrder(order: Order) {
        // Проверяем обязательные поля
        require(order.orderUid.isNotBlank()) { "orderUid cannot be empty" }
        require(order.items.isNotEmpty()) { "Order must contain at least one product" }
        require(order.delivery.name.isNotBlank() && order.delivery.phone.isNotBlank()) { "Delivery must contain a name and phone number" }

        // Устанавливаем текущее время для dateCreated, если оно не указано
        val orderToSave = if (order.dateCreated == Instant.EPOCH) {
            order.copy(dateCreated = Instant.now())
        } else {
            order
        }

        // Выполняем сохранение данных в транзакции
        withContext(Dispatchers.IO) {
            connection.use { conn ->
                conn.autoCommit = false
                try {
                    // Сохраняем заказ
                    ordersRepo.insert(orderToSave)

                    // Сохраняем доставку
                    deliveriesRepo.insert(orderToSave.delivery, orderToSave.orderUid)

                    // Сохраняем платеж
                    paymentsRepo.insert(orderToSave.payment, orderToSave.orderUid)

                    // Сохраняем товары
                    itemsRepo.insert(orderToSave.items, orderToSave.orderUid)

                    // Подтверждаем транзакцию
                    conn.commit()
                    logger.info { "Order ${orderToSave.orderUid} saved successfully" }
                } catch (e: Exception) {
                    conn.rollback()
                    logger.error(e) { "Order saving error ${orderToSave.orderUid}" }
                    throw e
                }
            }
        }
    }

    /**
     * Получает заказ и связанные данные из базы данных.
     */
    override suspend fun getOrderByID(orderUID: String): Order? = withContext(Dispatchers.IO) {
        val order = ordersRepo.getByID(orderUID) ?: return@withContext null
        val delivery = deliveriesRepo.getByOrderID(orderUID)
            ?: throw IllegalArgumentException("Delivery was not found for the order: $orderUID")
        val payment = paymentsRepo.getByOrderID(orderUID)
            ?: throw IllegalArgumentException("Payment was not found for the order: $orderUID")
        val items = itemsRepo.getByOrderID(orderUID)

        // Создаем новый объект Order с заполненными данными
        order.copy(
            delivery = delivery,
            payment = payment,
            items = items
        )
    }

    /**
     * Валидирует структуру заказа.
     */
    private fun validateOrder(order: Order) {
        require(order.orderUid.isNotBlank()) { "Order UID cannot be empty" }
        require(order.items.isNotEmpty()) { "Order must contain at least one element" }
        require(order.delivery.name.isNotBlank() && order.delivery.phone.isNotBlank()) {
            "Delivery data is incorrect"
        }
    }
}

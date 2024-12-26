package repository

import model.Order
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Интерфейс OrdersRepository определяет методы для взаимодействия с таблицей 'orders'.
 */
interface OrdersRepository {

    /**
     * Добавляет новую запись о заказе в таблицу 'orders'.
     *
     * @param order объект заказа.
     * @throws SQLException в случае ошибки выполнения запроса.
     */
    @Throws(SQLException::class)
    fun insert(order: Order)

    /**
     * Получает запись о заказе по уникальному идентификатору.
     *
     * @param orderUID уникальный идентификатор заказа.
     * @return объект [Order], если запись найдена, иначе `null`.
     * @throws SQLException в случае ошибки выполнения запроса.
     */
    @Throws(SQLException::class)
    fun getByID(orderUID: String): Order?
}

/**
 * Реализация OrdersRepository для работы с базой данных.
 *
 * @param connection объект подключения к базе данных [Connection].
 */
class OrdersRepositoryImpl(
    private val connection: Connection,
    private val deliveryRepository: DeliveriesRepository,
    private val paymentRepository: PaymentsRepository,
    private val itemsRepository: ItemsRepository
) : OrdersRepository {

    /**
     * Добавляет новую запись о заказе в таблицу 'orders'.
     */
    override fun insert(order: Order) {
        val query = """
            INSERT INTO orders (order_uid, track_number, entry, locale, internal_signature, customer_id, delivery_service, shardkey, sm_id, date_created, oof_shard)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, order.orderUid)
                statement.setString(2, order.trackNumber)
                statement.setString(3, order.entry)
                statement.setString(4, order.locale)
                statement.setString(5, order.internalSignature)
                statement.setString(6, order.customerId)
                statement.setString(7, order.deliveryService)
                statement.setString(8, order.shardkey)
                statement.setInt(9, order.smId)
                statement.setTimestamp(10, java.sql.Timestamp.from(order.dateCreated))
                statement.setString(11, order.oofShard)
                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            throw SQLException("Error inserting order with UID: ${order.orderUid}", e)
        }
    }

    /**
     * Получает запись о заказе по уникальному идентификатору.
     */
    override fun getByID(orderUID: String): Order? {
        val query = """
            SELECT order_uid, track_number, entry, locale, internal_signature, customer_id, delivery_service, shardkey, sm_id, date_created, oof_shard
            FROM orders WHERE order_uid = ?
        """.trimIndent()
        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, orderUID)
                val resultSet = statement.executeQuery()
                return if (resultSet.next()) {
                    mapRowToOrder(resultSet)
                } else {
                    null
                }
            }
        } catch (e: SQLException) {
            throw SQLException("Error fetching order with UID: $orderUID", e)
        }
    }

    /**
     * Маппит строку результата запроса в объект [Order].
     */
    private fun mapRowToOrder(resultSet: ResultSet): Order {
        val orderUid = resultSet.getString("order_uid")

        val delivery = deliveryRepository.getByOrderID(orderUid)
            ?: throw IllegalArgumentException("Delivery not found for orderUID: $orderUid")

        val payment = paymentRepository.getByOrderID(orderUid)
            ?: throw IllegalArgumentException("Payment not found for orderUID: $orderUid")

        val items = itemsRepository.getByOrderID(orderUid)

        return Order(
            orderUid = orderUid,
            trackNumber = resultSet.getString("track_number"),
            entry = resultSet.getString("entry"),
            locale = resultSet.getString("locale"),
            internalSignature = resultSet.getString("internal_signature"),
            customerId = resultSet.getString("customer_id"),
            deliveryService = resultSet.getString("delivery_service"),
            shardkey = resultSet.getString("shardkey"),
            smId = resultSet.getInt("sm_id"),
            dateCreated = resultSet.getTimestamp("date_created").toInstant(),
            oofShard = resultSet.getString("oof_shard"),
            delivery = delivery,
            payment = payment,
            items = items // Здесь можно подключить загрузку items
        )
    }
}

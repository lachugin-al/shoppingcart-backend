package repository

import model.Payment
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Интерфейс PaymentsRepository определяет методы для взаимодействия с таблицей 'payments'.
 */
interface PaymentsRepository {

    /**
     * Добавляет новую запись о платеже в таблицу 'payments'.
     *
     * @param payment объект платежа.
     * @param orderUID уникальный идентификатор заказа.
     * @throws SQLException в случае ошибки выполнения запроса.
     */
    @Throws(SQLException::class)
    fun insert(payment: Payment, orderUID: String)

    /**
     * Получает запись о платеже по уникальному идентификатору заказа.
     *
     * @param orderUID уникальный идентификатор заказа.
     * @return объект [Payment], если запись найдена.
     * @throws SQLException в случае ошибки выполнения запроса.
     */
    @Throws(SQLException::class)
    fun getByOrderID(orderUID: String): Payment?
}

/**
 * Реализация PaymentsRepository для работы с базой данных.
 *
 * @param connection объект подключения к базе данных [Connection].
 */
class PaymentsRepositoryImpl(private val connection: Connection) : PaymentsRepository {

    /**
     * Добавляет новую запись о платеже в таблицу 'payments'.
     */
    override fun insert(payment: Payment, orderUID: String) {
        val query = """
            INSERT INTO payments (order_uid, transaction, request_id, currency, provider, amount, payment_dt, bank, delivery_cost, goods_total, custom_fee)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        connection.prepareStatement(query).use { statement ->
            statement.setString(1, orderUID)
            statement.setString(2, payment.transaction)
            statement.setString(3, payment.requestId)
            statement.setString(4, payment.currency)
            statement.setString(5, payment.provider)
            statement.setInt(6, payment.amount)
            statement.setLong(7, payment.paymentDt)
            statement.setString(8, payment.bank)
            statement.setInt(9, payment.deliveryCost)
            statement.setInt(10, payment.goodsTotal)
            statement.setInt(11, payment.customFee)
            statement.executeUpdate()
        }
    }

    /**
     * Получает запись о платеже по уникальному идентификатору заказа.
     */
    override fun getByOrderID(orderUID: String): Payment? {
        val query = """
            SELECT transaction, request_id, currency, provider, amount, payment_dt, bank, delivery_cost, goods_total, custom_fee
            FROM payments WHERE order_uid = ?
        """.trimIndent()
        connection.prepareStatement(query).use { statement ->
            statement.setString(1, orderUID)
            val resultSet = statement.executeQuery()
            return if (resultSet.next()) {
                mapRowToPayment(resultSet)
            } else {
                null
            }
        }
    }

    /**
     * Маппит строку результата запроса в объект [Payment].
     */
    private fun mapRowToPayment(resultSet: ResultSet): Payment {
        return Payment(
            transaction = resultSet.getString("transaction"),
            requestId = resultSet.getString("request_id"),
            currency = resultSet.getString("currency"),
            provider = resultSet.getString("provider"),
            amount = resultSet.getInt("amount"),
            paymentDt = resultSet.getLong("payment_dt"),
            bank = resultSet.getString("bank"),
            deliveryCost = resultSet.getInt("delivery_cost"),
            goodsTotal = resultSet.getInt("goods_total"),
            customFee = resultSet.getInt("custom_fee")
        )
    }
}

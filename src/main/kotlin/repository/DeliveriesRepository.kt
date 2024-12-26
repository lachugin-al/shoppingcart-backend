package repository

import model.Delivery
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Интерфейс DeliveriesRepository определяет методы для взаимодействия с таблицей 'deliveries'.
 */
interface DeliveriesRepository {
    /**
     * Добавляет новую запись о доставке в таблицу 'deliveries'.
     *
     * @param delivery объект доставки.
     * @param orderUID уникальный идентификатор заказа.
     * @throws SQLException в случае ошибки выполнения запроса.
     */
    @Throws(SQLException::class)
    fun insert(delivery: Delivery, orderUID: String)

    /**
     * Получает запись о доставке по уникальному идентификатору заказа.
     *
     * @param orderUID уникальный идентификатор заказа.
     * @return объект [Delivery], если запись найдена, иначе `null`.
     * @throws SQLException в случае ошибки выполнения запроса.
     */
    @Throws(SQLException::class)
    fun getByOrderID(orderUID: String): Delivery?
}

/**
 * Реализация DeliveriesRepository для работы с базой данных.
 *
 * @param connection объект подключения к базе данных [Connection].
 */
class DeliveriesRepositoryImpl(private val connection: Connection) : DeliveriesRepository {

    /**
     * Добавляет новую запись о доставке в таблицу 'deliveries'.
     */
    override fun insert(delivery: Delivery, orderUID: String) {
        val query = """
            INSERT INTO deliveries (order_uid, name, phone, zip, city, address, region, email)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        connection.prepareStatement(query).use { statement ->
            statement.setString(1, orderUID)
            statement.setString(2, delivery.name)
            statement.setString(3, delivery.phone)
            statement.setString(4, delivery.zip)
            statement.setString(5, delivery.city)
            statement.setString(6, delivery.address)
            statement.setString(7, delivery.region)
            statement.setString(8, delivery.email)
            statement.executeUpdate()
        }
    }

    /**
     * Получает запись о доставке по уникальному идентификатору заказа.
     */
    override fun getByOrderID(orderUID: String): Delivery? {
        val query = """
            SELECT name, phone, zip, city, address, region, email
            FROM deliveries WHERE order_uid = ?
        """.trimIndent()
        connection.prepareStatement(query).use { statement ->
            statement.setString(1, orderUID)
            val resultSet = statement.executeQuery()
            return if (resultSet.next()) {
                mapRowToDelivery(resultSet)
            } else {
                null
            }
        }
    }

    /**
     * Маппит строку результата запроса в объект [Delivery].
     */
    private fun mapRowToDelivery(resultSet: ResultSet): Delivery {
        return Delivery(
            name = resultSet.getString("name"),
            phone = resultSet.getString("phone"),
            zip = resultSet.getString("zip"),
            city = resultSet.getString("city"),
            address = resultSet.getString("address"),
            region = resultSet.getString("region"),
            email = resultSet.getString("email")
        )
    }
}

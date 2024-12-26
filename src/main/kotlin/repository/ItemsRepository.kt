package repository

import model.Item
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Интерфейс ItemsRepository определяет методы для взаимодействия с таблицей 'items'.
 */
interface ItemsRepository {

    /**
     * Добавляет несколько записей о товарах в таблицу 'items'.
     *
     * @param items список объектов [Item], представляющих товары.
     * @param orderUID уникальный идентификатор заказа.
     * @throws SQLException в случае ошибки выполнения запроса.
     */
    @Throws(SQLException::class)
    fun insert(items: List<Item>, orderUID: String)

    /**
     * Получает все записи о товарах, связанных с указанным orderUID.
     *
     * @param orderUID уникальный идентификатор заказа.
     * @return список объектов [Item], если записи найдены.
     * @throws SQLException в случае ошибки выполнения запроса.
     */
    @Throws(SQLException::class)
    fun getByOrderID(orderUID: String): List<Item>
}

/**
 * Реализация ItemsRepository для работы с базой данных.
 *
 * @param connection объект подключения к базе данных [Connection].
 */
class ItemsRepositoryImpl(private val connection: Connection) : ItemsRepository {

    /**
     * Добавляет несколько записей о товарах в таблицу 'items'.
     */
    override fun insert(items: List<Item>, orderUID: String) {
        val query = """
            INSERT INTO items (order_uid, chrt_id, track_number, price, rid, name, sale, size, total_price, nm_id, brand, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        try {
            connection.prepareStatement(query).use { statement ->
                for (item in items) {
                    statement.setString(1, orderUID)
                    statement.setInt(2, item.chrtId)
                    statement.setString(3, item.trackNumber)
                    statement.setInt(4, item.price)
                    statement.setString(5, item.rid)
                    statement.setString(6, item.name)
                    statement.setInt(7, item.sale)
                    statement.setString(8, item.size)
                    statement.setInt(9, item.totalPrice)
                    statement.setInt(10, item.nmId)
                    statement.setString(11, item.brand)
                    statement.setInt(12, item.status)
                    statement.addBatch() // Добавляем в батч
                }
                statement.executeBatch() // Выполняем все запросы батчем
            }
        } catch (e: SQLException) {
            throw SQLException(
                "Error inserting items for Order UID: $orderUID. Items: ${items.joinToString(", ") { it.toString() }}",
                e
            )
        }
    }

    /**
     * Получает все записи о товарах, связанных с указанным orderUID.
     */
    override fun getByOrderID(orderUID: String): List<Item> {
        val query = """
            SELECT chrt_id, track_number, price, rid, name, sale, size, total_price, nm_id, brand, status
            FROM items WHERE order_uid = ?
        """.trimIndent()
        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, orderUID)
                val resultSet = statement.executeQuery()
                return mapResultSetToItems(resultSet)
            }
        } catch (e: SQLException) {
            throw SQLException("Error fetching items for Order UID: $orderUID", e)
        }
    }

    /**
     * Маппит результаты запроса в список объектов [Item].
     */
    private fun mapResultSetToItems(resultSet: ResultSet): List<Item> {
        val items = mutableListOf<Item>()
        while (resultSet.next()) {
            items.add(
                Item(
                    chrtId = resultSet.getInt("chrt_id"),
                    trackNumber = resultSet.getString("track_number"),
                    price = resultSet.getInt("price"),
                    rid = resultSet.getString("rid"),
                    name = resultSet.getString("name"),
                    sale = resultSet.getInt("sale"),
                    size = resultSet.getString("size"),
                    totalPrice = resultSet.getInt("total_price"),
                    nmId = resultSet.getInt("nm_id"),
                    brand = resultSet.getString("brand"),
                    status = resultSet.getInt("status")
                )
            )
        }
        return items
    }
}

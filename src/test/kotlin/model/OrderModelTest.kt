package model

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Тесты для проверки корректной сериализации и десериализации модели [Order].
 */
class OrderModelTest {

    /**
     * Тест проверяет, что JSON, содержащий данные о заказе, корректно десериализуется в объект [Order].
     *
     * Цели теста:
     * 1. Убедиться, что поле даты `date_created` корректно преобразуется в объект [Instant].
     * 2. Проверить корректность маппинга остальных полей объекта.
     * 3. Убедиться, что вложенные объекты, такие как [Delivery], [Payment], [Item], также правильно десериализуются.
     */
    @Test
    fun `should correctly deserialize Order JSON with Instant`() {
        // Исходный JSON для теста
        val json = """
            {
               "order_uid": "test-01",
               "track_number": "TRACKNUMBER1",
               "entry": "TEST",
               "delivery": {
                  "name": "Test Testov",
                  "phone": "+79110000000",
                  "zip": "2639802",
                  "city": "Saint Petersburg",
                  "address": "Moskovsaya 1",
                  "region": "Saint Petersburg",
                  "email": "test@test.com"
               },
               "payment": {
                  "transaction": "test-01",
                  "request_id": "",
                  "currency": "USD",
                  "provider": "payer",
                  "amount": 1816,
                  "payment_dt": 1637907726,
                  "bank": "alpha",
                  "delivery_cost": 1000,
                  "goods_total": 316,
                  "custom_fee": 0
               },
               "items": [
                  {
                     "chrt_id": 1934910,
                     "track_number": "TRACKNUMBER1",
                     "price": 451,
                     "rid": "test-a01",
                     "name": "Box",
                     "sale": 30,
                     "size": "0",
                     "total_price": 323,
                     "nm_id": 2389211,
                     "brand": "Boxoffice",
                     "status": 202
                  }
               ],
               "locale": "en",
               "internal_signature": "",
               "customer_id": "test",
               "delivery_service": "deliveler",
               "shardkey": "1",
               "sm_id": 1,
               "date_created": "2024-12-26T01:01:01Z",
               "oof_shard": "1"
            }
        """.trimIndent()

        // Десериализация JSON в объект Order
        val jsonDecoder = Json { ignoreUnknownKeys = true } // Игнорируем неизвестные ключи в JSON
        val order: Order = jsonDecoder.decodeFromString(json)

        // Проверка корректности преобразования поля даты
        val expectedDate = Instant.parse("2024-12-26T01:01:01Z")
        assertEquals(
            expectedDate,
            order.dateCreated,
            "Поле dateCreated не соответствует ожидаемому значению"
        )

        // Проверка основных полей объекта Order
        assertEquals(
            "test-01",
            order.orderUid,
            "Поле orderUid не соответствует ожидаемому значению"
        )
        assertEquals(
            "TRACKNUMBER1",
            order.trackNumber,
            "Поле trackNumber не соответствует ожидаемому значению"
        )
        assertEquals(
            "TEST",
            order.entry,
            "Поле entry не соответствует ожидаемому значению"
        )

        // Проверка вложенного объекта Delivery
        assertEquals(
            "Test Testov",
            order.delivery.name,
            "Поле name объекта Delivery не соответствует ожидаемому значению"
        )
        assertEquals(
            "+79110000000",
            order.delivery.phone,
            "Поле phone объекта Delivery не соответствует ожидаемому значению"
        )

        // Проверка вложенного объекта Payment
        assertEquals(
            1816,
            order.payment.amount,
            "Поле amount объекта Payment не соответствует ожидаемому значению"
        )

        // Проверка списка объектов Item
        assertEquals(
            "Box",
            order.items.first().name,
            "Поле name первого объекта Item не соответствует ожидаемому значению"
        )
        assertEquals(
            1934910,
            order.items.first().chrtId,
            "Поле chrtId первого объекта Item не соответствует ожидаемому значению"
        )
    }
}

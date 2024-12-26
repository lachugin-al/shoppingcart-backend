package model

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrderModelTest {

    @Test
    fun `should correctly deserialize Order JSON`() {
        // Тестовый JSON
        val json = """
            {
               "order_uid": "test-01",
               "track_number": "TRACKNUMBER1",
               "entry": "TEST",
               "delivery": {
                  "name": "Test Testov",
                  "phone": "+9710000000",
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
                  },
                  {
                     "chrt_id": 1934911,
                     "track_number": "TRACKNUMBER2",
                     "price": 451,
                     "rid": "test-a02",
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
               "sm_id": "01",
               "date_created": "2024-12-26T01:01:01Z",
               "oof_shard": "1"
            }
        """.trimIndent()

        // Десериализация JSON
        val jsonDecoder = Json { ignoreUnknownKeys = true } // Игнорируем лишние поля, если будут
        val order: Order = jsonDecoder.decodeFromString(json)

        // Проверки основных полей
        assertEquals("test-01", order.orderUid)
        assertEquals("TRACKNUMBER1", order.trackNumber)
        assertEquals("TEST", order.entry)

        // Проверка вложенных объектов
        assertEquals("Test Testov", order.delivery.name)
        assertEquals("+9710000000", order.delivery.phone)
        assertEquals(1816, order.payment.amount)
        assertEquals(1934910, order.items.first().chrtId)
        assertEquals(1934911, order.items[1].chrtId)
        assertEquals("Box", order.items.first().name)

        // Проверка даты
        assertEquals("2024-12-26T01:01:01Z", order.dateCreated)
    }
}
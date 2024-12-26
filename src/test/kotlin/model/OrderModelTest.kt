package model

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class OrderModelTest {

    @Test
    fun `should correctly deserialize Order JSON with Instant`() {
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

        val jsonDecoder = Json { ignoreUnknownKeys = true }
        val order: Order = jsonDecoder.decodeFromString(json)

        // Проверка даты
        val expectedDate = Instant.parse("2024-12-26T01:01:01Z")
        assertEquals(expectedDate, order.dateCreated)

        // Проверка других полей
        assertEquals("test-01", order.orderUid)
        assertEquals("TRACKNUMBER1", order.trackNumber)
        assertEquals("TEST", order.entry)
        assertEquals("Test Testov", order.delivery.name)
        assertEquals(1816, order.payment.amount)
        assertEquals("Box", order.items.first().name)
    }
}

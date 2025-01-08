package cache

import kotlinx.coroutines.runBlocking
import model.Delivery
import model.Item
import model.Payment
import model.Order
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import repository.DeliveriesRepository
import repository.ItemsRepository
import repository.OrdersRepository
import repository.PaymentsRepository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Тесты для проверки функциональности класса [OrderCache].
 */
class MockedOrderCacheTest {

    // Моки для базового функционала работы с базой данных и репозиториями
    private val mockConnection: Connection = mock()
    private val mockPreparedStatement: PreparedStatement = mock()
    private val mockResultSet: ResultSet = mock()

    // Моки для репозиториев
    private val mockOrdersRepository: OrdersRepository = mock()
    private val mockDeliveriesRepository: DeliveriesRepository = mock()
    private val mockPaymentsRepository: PaymentsRepository = mock()
    private val mockItemsRepository: ItemsRepository = mock()

    // Экземпляр тестируемого кэша
    private val cache = OrderCache()

    /**
     * Тест проверяет загрузку данных из базы данных в кэш.
     *
     * Процедура теста:
     * 1. Создаются моки для работы с базой данных и репозиториями.
     * 2. Настраиваются моки для имитации получения данных из базы данных.
     * 3. Проверяется, что после загрузки данные корректно добавлены в кэш.
     */
    @Test
    fun `should load data into cache from DB`() = runBlocking {
        // Исходные данные для теста
        val orderUID = "test-01"
        val order = Order(
            orderUid = orderUID,
            trackNumber = "TRACKNUMBER1",
            entry = "TEST",
            delivery = Delivery(
                name = "Test Testov",
                phone = "+9710000000",
                zip = "2639802",
                city = "Saint Petersburg",
                address = "Moskovsaya 1",
                region = "Saint Petersburg",
                email = "test@test.com"
            ),
            payment = Payment(
                transaction = "test-01",
                requestId = "",
                currency = "USD",
                provider = "payer",
                amount = 1816,
                paymentDt = 1637907726,
                bank = "alpha",
                deliveryCost = 1000,
                goodsTotal = 316,
                customFee = 0
            ),
            items = listOf(
                Item(
                    chrtId = 1934910,
                    trackNumber = "TRACKNUMBER1",
                    price = 451,
                    rid = "test-a01",
                    name = "Box",
                    sale = 30,
                    size = "0",
                    totalPrice = 323,
                    nmId = 2389211,
                    brand = "Boxoffice",
                    status = 202
                )
            ),
            locale = "en",
            internalSignature = "",
            customerId = "test",
            deliveryService = "deliveler",
            shardkey = "1",
            smId = 1,
            dateCreated = java.time.Instant.parse("2024-12-26T01:01:01Z"),
            oofShard = "1"
        )

        // Настройка мока для запроса order_uid из базы данных
        whenever(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(true, false) // Имитация одной строки результата
        whenever(mockResultSet.getString("order_uid")).thenReturn(orderUID)

        // Настройка мока для репозиториев
        whenever(mockOrdersRepository.getByID(orderUID)).thenReturn(order)
        whenever(mockDeliveriesRepository.getByOrderID(orderUID)).thenReturn(order.delivery)
        whenever(mockPaymentsRepository.getByOrderID(orderUID)).thenReturn(order.payment)
        whenever(mockItemsRepository.getByOrderID(orderUID)).thenReturn(order.items)

        // Загрузка данных в кэш
        cache.loadFromDB(
            mockConnection,
            mockOrdersRepository,
            mockDeliveriesRepository,
            mockPaymentsRepository,
            mockItemsRepository
        )

        // Проверка: данные должны быть в кэше
        val cachedOrder = cache.get(orderUID)
        assertNotNull(cachedOrder, "Order must be in the cache")
        assertEquals(order, cachedOrder, "Cached order must match the original order")
    }
}

import kotlinx.coroutines.runBlocking
import model.Delivery
import model.Item
import model.Payment
import model.Order
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.times
import repository.DeliveriesRepository
import repository.ItemsRepository
import repository.OrdersRepository
import repository.PaymentsRepository
import service.OrderService
import service.OrderServiceImpl
import java.sql.Connection
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

/**
 * Тесты для [OrderServiceImpl].
 */
class MockedOrderServiceTest {

    // Моки для зависимостей
    private val mockConnection: Connection = mock()
    private val mockOrdersRepository: OrdersRepository = mock()
    private val mockDeliveriesRepository: DeliveriesRepository = mock()
    private val mockPaymentsRepository: PaymentsRepository = mock()
    private val mockItemsRepository: ItemsRepository = mock()

    // Тестируемый сервис
    private val service: OrderService = OrderServiceImpl(
        connection = mockConnection,
        ordersRepo = mockOrdersRepository,
        deliveriesRepo = mockDeliveriesRepository,
        paymentsRepo = mockPaymentsRepository,
        itemsRepo = mockItemsRepository
    )

    @Test
    fun `should save order successfully`() = runBlocking {
        // Данные для теста
        val order = createTestOrder()

        // Настройка моков
        doNothing().whenever(mockOrdersRepository).insert(order)
        doNothing().whenever(mockDeliveriesRepository).insert(order.delivery, order.orderUid)
        doNothing().whenever(mockPaymentsRepository).insert(order.payment, order.orderUid)
        doNothing().whenever(mockItemsRepository).insert(order.items, order.orderUid)

        // Вызов метода
        service.saveOrder(order)

        // Проверка вызовов методов репозиториев
        verify(mockOrdersRepository, times(1)).insert(order)
        verify(mockDeliveriesRepository, times(1)).insert(order.delivery, order.orderUid)
        verify(mockPaymentsRepository, times(1)).insert(order.payment, order.orderUid)
        verify(mockItemsRepository, times(1)).insert(order.items, order.orderUid)
    }

    @Test
    fun `should throw error when order is invalid`() = runBlocking {
        // Пустой UID заказа
        val invalidOrder = createTestOrder(orderUID = "")

        // Ожидаем исключение IllegalArgumentException
        val exception = assertFailsWith<IllegalArgumentException> {
            service.saveOrder(invalidOrder)
        }

        // Проверка сообщения об ошибке
        assertEquals("orderUid cannot be empty", exception.message)
    }

    @Test
    fun `should get order by ID successfully`() = runBlocking {
        // Данные для теста
        val orderUID = "test-01"
        val order = createTestOrder(orderUID)
        val delivery = order.delivery
        val payment = order.payment
        val items = order.items

        // Настройка моков
        whenever(mockOrdersRepository.getByID(orderUID)).thenReturn(order)
        whenever(mockDeliveriesRepository.getByOrderID(orderUID)).thenReturn(delivery)
        whenever(mockPaymentsRepository.getByOrderID(orderUID)).thenReturn(payment)
        whenever(mockItemsRepository.getByOrderID(orderUID)).thenReturn(items)

        // Вызов метода
        val fetchedOrder = service.getOrderByID(orderUID)

        // Проверка результата
        assertNotNull(fetchedOrder)
        assertEquals(orderUID, fetchedOrder.orderUid)
        assertEquals(delivery, fetchedOrder.delivery)
        assertEquals(payment, fetchedOrder.payment)
        assertEquals(items, fetchedOrder.items)
    }

    @Test
    fun `should return null when order is not found`() = runBlocking {
        // Настройка моков
        val orderUID = "non-existent"
        whenever(mockOrdersRepository.getByID(orderUID)).thenReturn(null)

        // Вызов метода
        val fetchedOrder = service.getOrderByID(orderUID)

        // Проверка результата
        assertNull(fetchedOrder)
    }

    /**
     * Создает тестовый заказ.
     */
    private fun createTestOrder(orderUID: String = "test-01"): Order {
        return Order(
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
            dateCreated = Instant.parse("2024-12-26T01:01:01Z"),
            oofShard = "1"
        )
    }
}

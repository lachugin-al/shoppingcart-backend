package repository

import model.Delivery
import model.Item
import model.Order
import model.Payment
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import kotlin.test.assertEquals

class MockedRepositoryIntegrationTest {

    private val mockConnection: Connection = mock()
    private val mockPreparedStatement: PreparedStatement = mock()
    private val mockResultSet: ResultSet = mock()

    private val mockDeliveriesRepository: DeliveriesRepository = mock()
    private val mockPaymentsRepository: PaymentsRepository = mock()
    private val mockItemsRepository: ItemsRepository = mock()
    private val ordersRepository =
        OrdersRepositoryImpl(mockConnection, mockDeliveriesRepository, mockPaymentsRepository, mockItemsRepository)

    /**
     * Проверяет корректную вставку и получение данных заказа.
     */
    @Test
    fun `test insert and fetch order with mocks`() {
        // Исходные данные
        val orderUID = "test-01"
        val delivery = Delivery(
            name = "Test Testov",
            phone = "+9710000000",
            zip = "2639802",
            city = "Saint Petersburg",
            address = "Moskovsaya 1",
            region = "Saint Petersburg",
            email = "test@test.com"
        )
        val payment = Payment(
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
        )
        val items = listOf(
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
        )
        val order = Order(
            orderUid = orderUID,
            trackNumber = "TRACKNUMBER1",
            entry = "TEST",
            delivery = delivery,
            payment = payment,
            items = items,
            locale = "en",
            internalSignature = "",
            customerId = "test",
            deliveryService = "deliveler",
            shardkey = "1",
            smId = 1,
            dateCreated = Instant.parse("2024-12-26T01:01:01Z"),
            oofShard = "1"
        )

        // Мокирование репозиториев
        whenever(mockDeliveriesRepository.getByOrderID(orderUID)).thenReturn(delivery)
        whenever(mockPaymentsRepository.getByOrderID(orderUID)).thenReturn(payment)
        whenever(mockItemsRepository.getByOrderID(orderUID)).thenReturn(items) // Настраиваем items

        // Мокирование вставки данных
        whenever(mockConnection.prepareStatement(any())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(1)

        // Мокирование получения данных
        whenever(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(true).thenReturn(false)
        whenever(mockResultSet.getString("order_uid")).thenReturn(orderUID)
        whenever(mockResultSet.getString("track_number")).thenReturn(order.trackNumber)
        whenever(mockResultSet.getString("entry")).thenReturn(order.entry)
        whenever(mockResultSet.getString("locale")).thenReturn(order.locale)
        whenever(mockResultSet.getString("internal_signature")).thenReturn(order.internalSignature)
        whenever(mockResultSet.getString("customer_id")).thenReturn(order.customerId)
        whenever(mockResultSet.getString("delivery_service")).thenReturn(order.deliveryService)
        whenever(mockResultSet.getString("shardkey")).thenReturn(order.shardkey)
        whenever(mockResultSet.getInt("sm_id")).thenReturn(order.smId)
        whenever(mockResultSet.getTimestamp("date_created")).thenReturn(java.sql.Timestamp.from(order.dateCreated))
        whenever(mockResultSet.getString("oof_shard")).thenReturn(order.oofShard)

        // Вставка данных
        ordersRepository.insert(order)

        // Получение данных
        val fetchedOrder = ordersRepository.getByID(orderUID)

        // Проверка данных заказа
        assertEquals(order.orderUid, fetchedOrder?.orderUid, "UID заказа не совпадает")
        assertEquals(order.trackNumber, fetchedOrder?.trackNumber, "Track Number не совпадает")
        assertEquals(order.entry, fetchedOrder?.entry, "Entry не совпадает")
        assertEquals(order.locale, fetchedOrder?.locale, "Locale не совпадает")
        assertEquals(order.internalSignature, fetchedOrder?.internalSignature, "Internal Signature не совпадает")
        assertEquals(order.customerId, fetchedOrder?.customerId, "Customer ID не совпадает")
        assertEquals(order.deliveryService, fetchedOrder?.deliveryService, "Delivery Service не совпадает")
        assertEquals(order.shardkey, fetchedOrder?.shardkey, "Shardkey не совпадает")
        assertEquals(order.smId, fetchedOrder?.smId, "SM ID не совпадает")
        assertEquals(order.dateCreated, fetchedOrder?.dateCreated, "Дата создания не совпадает")
        assertEquals(order.oofShard, fetchedOrder?.oofShard, "Oof Shard не совпадает")

        // Проверка доставки
        assertEquals(order.delivery, fetchedOrder?.delivery, "Доставка не совпадает")

        // Проверка оплаты
        assertEquals(order.payment, fetchedOrder?.payment, "Платеж не совпадает")

        // Проверка элементов заказа
        assertEquals(order.items, fetchedOrder?.items, "Элементы заказа не совпадают")
    }
}

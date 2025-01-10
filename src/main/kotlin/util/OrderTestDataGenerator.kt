package util

import io.github.serpro69.kfaker.Faker
import model.Delivery
import model.Item
import model.Order
import model.Payment
import java.time.Instant
import java.util.*

/**
 * Утилитарный класс для генерации тестовых данных.
 */
object OrderTestDataGenerator {
    private val faker = Faker()

    /**
     * Генерирует тестовый объект `Order`.
     *
     * @return объект [Order] с тестовыми данными.
     */
    fun generateOrder(): Order {
        val orderUid = UUID.randomUUID().toString()
        return Order(
            orderUid = orderUid,
            trackNumber = faker.code.asin(),
            entry = faker.lorem.words(),
            delivery = generateDelivery(),
            payment = generatePayment(orderUid),
            items = generateItems(),
            locale = faker.address.countryCode(),
            internalSignature = faker.code.asin(),
            customerId = UUID.randomUUID().toString(),
            deliveryService = faker.company.name(),
            shardkey = faker.random.randomString(1),
            smId = faker.random.nextInt(1, 100),
            dateCreated = Instant.now(),
            oofShard = faker.random.randomString(1)
        )
    }

    /**
     * Генерирует тестовый объект `Delivery`.
     *
     * @return объект [Delivery].
     */
    private fun generateDelivery(): Delivery {
        return Delivery(
            name = faker.name.name(),
            phone = faker.phoneNumber.phoneNumber(),
            zip = faker.address.postcode(),
            city = faker.address.city(),
            address = faker.address.streetAddress(),
            region = faker.address.state(),
            email = faker.internet.email()
        )
    }

    /**
     * Генерирует тестовый объект `Payment`.
     *
     * @param orderUid UID заказа для привязки платежа.
     * @return объект [Payment].
     */
    private fun generatePayment(orderUid: String): Payment {
        return Payment(
            transaction = orderUid,
            requestId = faker.code.asin(),
            currency = faker.currency.code(),
            provider = faker.company.name(),
            amount = faker.random.nextInt(100, 10000),
            paymentDt = Instant.now().epochSecond,
            bank = faker.company.name(),
            deliveryCost = faker.random.nextInt(10, 500),
            goodsTotal = faker.random.nextInt(50, 5000),
            customFee = faker.random.nextInt(0, 100)
        )
    }

    /**
     * Генерирует список тестовых объектов `Item`.
     *
     * @return список объектов [Item].
     */
    private fun generateItems(): List<Item> {
        return List(faker.random.nextInt(1, 5)) {
            Item(
                chrtId = faker.random.nextInt(1000, 9999),
                trackNumber = faker.code.asin(),
                price = faker.random.nextInt(100, 1000),
                rid = UUID.randomUUID().toString(),
                name = faker.commerce.productName(),
                sale = faker.random.nextInt(0, 50),
                size = faker.random.randomString(1),
                totalPrice = faker.random.nextInt(100, 2000),
                nmId = faker.random.nextInt(100000, 999999),
                brand = faker.company.name(),
                status = faker.random.nextInt(1, 3)
            )
        }
    }
}
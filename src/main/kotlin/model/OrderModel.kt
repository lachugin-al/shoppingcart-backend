package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Представляет информацию о доставке заказа.
 */
@Serializable
data class Delivery(
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String,
    @SerialName("zip") val zip: String,
    @SerialName("city") val city: String,
    @SerialName("address") val address: String,
    @SerialName("region") val region: String,
    @SerialName("email") val email: String
)

/**
 * Представляет информацию об оплате заказа.
 */
@Serializable
data class Payment(
    @SerialName("transaction") val transaction: String,
    @SerialName("request_id") val requestId: String?,
    @SerialName("currency") val currency: String,
    @SerialName("provider") val provider: String,
    @SerialName("amount") val amount: Int,
    @SerialName("payment_dt") val paymentDt: Long,
    @SerialName("bank") val bank: String,
    @SerialName("delivery_cost") val deliveryCost: Int,
    @SerialName("goods_total") val goodsTotal: Int,
    @SerialName("custom_fee") val customFee: Int
)

/**
 * Представляет отдельный элемент в составе заказа.
 */
@Serializable
data class Item(
    @SerialName("chrt_id") val chrtId: Int,
    @SerialName("track_number") val trackNumber: String,
    @SerialName("price") val price: Int,
    @SerialName("rid") val rid: String,
    @SerialName("name") val name: String,
    @SerialName("sale") val sale: Int,
    @SerialName("size") val size: String,
    @SerialName("total_price") val totalPrice: Int,
    @SerialName("nm_id") val nmId: Int,
    @SerialName("brand") val brand: String,
    @SerialName("status") val status: Int
)

/**
 * Представляет весь заказ со всеми связанными сущностями.
 */
@Serializable
data class Order(
    @SerialName("order_uid") val orderUid: String,
    @SerialName("track_number") val trackNumber: String,
    @SerialName("entry") val entry: String,
    @SerialName("delivery") val delivery: Delivery,
    @SerialName("payment") val payment: Payment,
    @SerialName("items") val items: List<Item>,
    @SerialName("locale") val locale: String,
    @SerialName("internal_signature") val internalSignature: String?,
    @SerialName("customer_id") val customerId: String,
    @SerialName("delivery_service") val deliveryService: String,
    @SerialName("shardkey") val shardkey: String,
    @SerialName("sm_id") val smId: Int,
    @SerialName("date_created") val dateCreated: String,
    @SerialName("oof_shard") val oofShard: String
)
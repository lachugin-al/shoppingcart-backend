package util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Сериализатор для java.time.Instant.
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString()) // Сериализация в формат ISO-8601
    }

    override fun deserialize(decoder: Decoder): Instant {
        val value = decoder.decodeString()
        return try {
            Instant.parse(value) // Десериализация ISO-8601
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Неверный формат даты: $value", e)
        }
    }
}

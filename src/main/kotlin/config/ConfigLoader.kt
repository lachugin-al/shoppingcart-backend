package config

import io.github.cdimascio.dotenv.dotenv
import java.time.Duration

/**
 * Загрузчик конфигурации из `.env` файла или переменных окружения.
 */
object ConfigLoader {

    // Инициализация dotenv для загрузки `.env` файла
    private val dotenv = dotenv {
        directory = "./"         // Путь к .env файлу (по умолчанию корневая директория)
        ignoreIfMissing = false  // Бросать ошибку, если файл отсутствует
    }

    /**
     * Загружает конфигурацию приложения.
     *
     * @return объект [Config] с параметрами конфигурации.
     * @throws IllegalArgumentException если какие-либо параметры отсутствуют или некорректны.
     */
    fun loadConfig(): Config {
        return Config(
            dbHost = getEnvOrThrow("DB_HOST"),
            dbPort = getEnvOrThrow("DB_PORT").toIntOrThrow("DB_PORT"),
            dbUser = getEnvOrThrow("DB_USER"),
            dbPassword = getEnvOrThrow("DB_PASSWORD"),
            dbName = getEnvOrThrow("DB_NAME"),

            kafkaBrokers = getEnvOrThrow("KAFKA_BROKERS").split(","),
            kafkaTopic = getEnvOrThrow("KAFKA_TOPIC"),
            kafkaGroupId = getEnvOrThrow("KAFKA_GROUP_ID"),

            httpPort = getEnvOrThrow("HTTP_PORT").toIntOrThrow("HTTP_PORT"),

            shutdownTimeout = Duration.parse("PT${getEnvOrThrow("SHUTDOWN_TIMEOUT")}")
        )
    }

    /**
     * Получает значение из `.env` файла или переменной окружения. Если значение отсутствует, выбрасывает исключение.
     *
     * @param key Имя переменной.
     * @return Значение переменной окружения или из `.env` файла.
     * @throws IllegalArgumentException если переменная отсутствует.
     */
    private fun getEnvOrThrow(key: String): String {
        return dotenv[key] ?: System.getenv(key)
        ?: throw IllegalArgumentException("Environment variable not found: $key")
    }

    /**
     * Преобразует строку в [Int], выбрасывая исключение с понятным сообщением при ошибке.
     *
     * @param name Название параметра для сообщений об ошибках.
     * @return Значение [Int].
     * @throws IllegalArgumentException если строка не является числом.
     */
    private fun String.toIntOrThrow(name: String): Int {
        return this.toIntOrNull() ?: throw IllegalArgumentException("Invalid value for $name: $this")
    }
}

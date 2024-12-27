package config

import java.time.Duration

/**
 * Конфигурационный класс, содержащий параметры для приложения.
 */
data class Config(
    val dbHost: String,            // Хост базы данных
    val dbPort: Int,               // Порт базы данных
    val dbUser: String,            // Имя пользователя базы данных
    val dbPassword: String,        // Пароль пользователя базы данных
    val dbName: String,            // Имя базы данных

    val kafkaBrokers: List<String>, // Список брокеров Kafka
    val kafkaTopic: String,        // Топик Kafka для обработки заказов
    val kafkaGroupId: String,      // Группа потребителей Kafka

    val httpPort: String,          // Порт HTTP-сервера

    val shutdownTimeout: Duration  // Таймаут завершения работы приложения
)

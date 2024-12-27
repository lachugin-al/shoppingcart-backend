package db

import config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

private val logger = KotlinLogging.logger {}

/**
 * Инициализирует подключение к базе данных с использованием конфигурации.
 *
 * @param config Конфигурация приложения [Config].
 * @return Соединение с базой данных [Connection].
 * @throws Exception в случае ошибки подключения или выполнения миграций.
 */
suspend fun initDB(config: Config): Connection {
    // Формирование строки подключения (DSN)
    val dsn = "jdbc:postgresql://${config.dbHost}:${config.dbPort}/${config.dbName}" +
            "?user=${config.dbUser}&password=${config.dbPassword}"

    return withContext(Dispatchers.IO) {
        try {
            // Установление соединения
            val connection = DriverManager.getConnection(dsn)
            logger.info { "The database connection is established." }

            // Запуск миграций
            runMigrations(connection)

            connection
        } catch (e: Exception) {
            logger.error(e) { "Database connection error." }
            throw e
        }
    }
}

/**
 * Выполняет миграции из директории `src/main/resources/migrations`.
 *
 * @param connection Соединение с базой данных [Connection].
 * @throws Exception в случае ошибки выполнения миграций.
 */
private suspend fun runMigrations(connection: Connection) {
    val migrationsDir = "src/main/resources/migrations"

    withContext(Dispatchers.IO) {
        try {
            // Чтение SQL-файлов из директории миграций
            val files = Files.newDirectoryStream(Paths.get(migrationsDir), "*.sql").toList()
            for (file in files) {
                logger.info { "Applying migration: ${file.fileName}" }
                val sql = Files.readString(file)

                // Выполнение SQL-скрипта
                connection.createStatement().use { it.execute(sql) }
            }
            logger.info { "Migrations have been successfully applied." }
        } catch (e: Exception) {
            logger.error(e) { "Migration execution error." }
            throw e
        }
    }
}

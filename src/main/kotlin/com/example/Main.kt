package com.example

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import org.flywaydb.core.Flyway
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

fun setupDatabaseDataSource(schema: String): DataSource {
    val imageName = DockerImageName.parse("postgres:14-alpine")
    val postgresql: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>(imageName)
    postgresql.start()

    val dataSource = PGSimpleDataSource().apply {
        setURL(postgresql.jdbcUrl)
        user = postgresql.username
        password = postgresql.password
        currentSchema = schema
        setProperty("stringtype", "unspecified")
    }

    val flyway = Flyway.configure()
        .loggers("slf4j")
        .dataSource(dataSource)
        .locations("classpath:db/migration/$schema")
        .schemas(schema)
        .load()

    flyway.migrate()

    return dataSource
}

fun main() {
    val schema = "pizza"
    println("setting up database for $schema")

    val dataSource = setupDatabaseDataSource(schema = schema)
    val database = PizzaDatabase(driver = dataSource.asJdbcDriver())

    println("creating a pizza")
    val margheritaId = database.pizzaQueries.pizzaCreateWorkaround(
        name = "Margherita",
        diameter_inches = 12.toBigDecimal(),
        weight_pounds = 1.2.toBigDecimal(),
        sauce_name = "red",
    ).executeAsOne()

    println("retrieving the pizza")
    val pizzaGetResult = database.pizzaQueries.pizzaGet(id = margheritaId).executeAsOne()
    println("🍕: ${pizzaGetResult.name} with ${pizzaGetResult.sauce_name} sauce. ${pizzaGetResult.diameter_inches}\" diameter / ${pizzaGetResult.wieght_pounds}lb.")
}

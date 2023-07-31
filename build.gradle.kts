plugins {
    kotlin("jvm") version "1.8.21"
    application
    id("app.cash.sqldelight") version "2.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    runtimeOnly("ch.qos.logback:logback-classic:1.4.8")
    implementation("app.cash.sqldelight:jdbc-driver:2.0.0")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation(platform("org.testcontainers:testcontainers-bom:1.18.3"))
    implementation("org.testcontainers:postgresql")
    implementation("org.flywaydb:flyway-core:9.21.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}

val databaseName = "PizzaDatabase"
val schema = "pizza"
val databaseClassPackage = "com.example"

sqldelight {
    databases {
        create(databaseName) {
            val migrationsDir = File(buildDir, "resources/main/db/migration/$schema")
            packageName.set(databaseClassPackage)
            dialect("app.cash.sqldelight:postgresql-dialect:2.0.0")
            srcDirs("src/main/sql", "src/main/migrations")
            deriveSchemaFromMigrations.set(true)
            migrationOutputDirectory.set(migrationsDir)
            migrationOutputFileFormat.set(".sql")
        }
    }
}

tasks {
    val generateMigrationsTask = "generateMain${databaseName}Migrations"
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        dependsOn(generateMigrationsTask)
    }
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/sql")
        java.srcDir("src/main/migrations")
    }
}

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
    testImplementation(kotlin("test"))

    runtimeOnly(platform("org.testcontainers:testcontainers-bom:1.18.3"))
    runtimeOnly("org.testcontainers:postgresql")
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

val schema = "issue4349"

sqldelight {
    databases {
        create("Database") {
            val migrationsDir = File(buildDir, "resources/main/db/migration/$schema")
            packageName.set("com.example")
            dialect("app.cash.sqldelight:postgresql-dialect:2.0.0")
            srcDirs("src/main/sql", "src/main/migrations")
            deriveSchemaFromMigrations.set(true)
            migrationOutputDirectory.set(migrationsDir)
            migrationOutputFileFormat.set(".sql")
        }
    }
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/sql")
        java.srcDir("src/main/migrations")
    }
}

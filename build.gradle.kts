plugins {
    id("java")
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
       id("org.liquibase.gradle") version "2.2.2"
}

group = "dev.favware"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

configurations.configureEach {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-json")
    exclude(group = "org.springframework", module = "spring-webmvc")
    exclude(group = "com.sun.xml.bind")
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
                "changelogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
                "classpath" to "src/main/resources",
                "url" to "jdbc:postgresql://localhost:5432/gpadmin",
                "username" to "gpadmin",
                "password" to "gpadmin",
                "driver" to "org.postgresql.Driver",
                "database-changelog-table-name" to "DATABASECHANGELOG_REPRO",
                "database-changelog-lock-table-name" to "DATABASECHANGELOGLOCK_REPRO"
        )
    }
    runList = "main"
}

repositories {
    mavenCentral()
}

val springBootVersion = "3.2.5"
val lombokVersion = "1.18.32"

dependencies {
    //  Spring
    implementation("org.springframework.boot:spring-boot-starter-parent:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter:${springBootVersion}")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:${springBootVersion}")

    // Utilities
    implementation("com.google.guava:guava:33.1.0-jre")

    // Database
    runtimeOnly("org.postgresql:postgresql:42.7.3")
    implementation("org.postgresql:r2dbc-postgresql:1.0.5.RELEASE")

    // Lombok
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    // Dependencies required for running liquibase for tests
    liquibaseRuntime("org.postgresql:postgresql:42.7.3")
    liquibaseRuntime("org.liquibase:liquibase-core:4.27.0")
    liquibaseRuntime("info.picocli:picocli:4.7.5")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

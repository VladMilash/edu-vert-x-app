import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.mvo"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "5.0.0"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "com.mvo.edu_vert_x_app.MainVerticle"
val launcherClassName = "io.vertx.launcher.application.VertxApplication"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation("io.vertx:vertx-core:5.0.0")
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-launcher-application")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-pg-client")
  //implementation("io.vertx:vertx-web-api-contract:3.5.3.CR1")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
  testImplementation("org.mockito:mockito-core:5.18.0")
  implementation("org.flywaydb:flyway-core:11.8.2")
  implementation("org.flywaydb:flyway-database-postgresql:11.8.2")
  implementation("org.postgresql:postgresql:42.7.5")

  testImplementation("org.testcontainers:testcontainers:1.19.3")
  testImplementation("org.testcontainers:postgresql:1.19.3")
  testImplementation("org.testcontainers:junit-jupiter:1.19.3")
  testImplementation("org.testcontainers:r2dbc:1.19.3")

  implementation("org.slf4j:slf4j-api:2.0.17")
  implementation("org.slf4j:slf4j-simple:2.0.17")

  implementation("org.projectlombok:lombok:1.18.38")
  annotationProcessor("org.projectlombok:lombok:1.18.38")

  implementation("com.ongres.scram:scram-client:2.1")
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf(mainVerticleName)
}


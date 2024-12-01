plugins {
    id("io.micronaut.library") version "4.4.4"
    id("com.gradleup.shadow") version "9.0.0-beta2"
}

version = "0.1.0"
group = "com.mostlynobody.aoc.y24"

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")

    implementation("com.amazonaws:aws-lambda-java-events")
    implementation("io.micronaut.aws:micronaut-aws-lambda-events-serde")
    implementation("io.micronaut.aws:micronaut-function-aws")
    implementation("io.micronaut.crac:micronaut-crac")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("software.amazon.awssdk:dynamodb")

    runtimeOnly("ch.qos.logback:logback-classic")

    testImplementation("com.amazonaws:DynamoDBLocal:2.5.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.mockito:mockito-core:2.16.0")
    testImplementation("org.mockito:mockito-inline:2.16.0")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:localstack:1.20.4")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveVersion.set("")
    archiveClassifier.set("lambda")
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

dependencies {
    implementation(project(":shared"))
}

micronaut {
    runtime("lambda_java")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.mostlynobody.aoc.y24.*")
    }
}

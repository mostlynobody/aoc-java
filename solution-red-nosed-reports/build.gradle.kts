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

    runtimeOnly("ch.qos.logback:logback-classic")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
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

plugins {
    id("java-library")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("io.micronaut.serde:micronaut-serde-jackson:2.13.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.1")

    annotationProcessor("io.micronaut.serde:micronaut-serde-processor:2.13.0")
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

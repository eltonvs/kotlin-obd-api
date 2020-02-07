import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
}

group = "com.github.eltonvs.obd"
version = "0.0.1"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
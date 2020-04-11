import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val publicationName = "kotlin-obd-api"

plugins {
    kotlin("jvm") version "1.3.61"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

group = "com.github.eltonvs"
version = "1.1.1"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}

bintray {
    user = project.findProperty("bintrayUser").toString()
    key = project.findProperty("bintrayKey").toString()
    dryRun = false
    publish = true
    setPublications(publicationName)
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = project.name
        desc = "A Kotlin OBD-II API for reading engine data"
        websiteUrl = "https://github.com/eltonvs/${project.name}.git"
        issueTrackerUrl = "https://github.com/eltonvs/${project.name}/issues"
        vcsUrl = "https://github.com/eltonvs/${project.name}.git"
        version.vcsTag = "v${project.version}"
        setLicenses("Apache-2.0")
        setLabels("kotlin", "obd", "vehicle", "car", "obd-api")
        publicDownloadNumbers = true
    })
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
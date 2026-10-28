import org.gradle.api.GradleException
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

// Versions are never committed: git tags (vX.Y.Z or vX.Y.Z-pre.N) are the source of truth.
// Resolution order, first match wins:
//   1. JitPack (JITPACK=true): the version JitPack was asked for (env VERSION), verbatim.
//   2. GitHub Actions tag runs (GITHUB_REF_TYPE=tag): GITHUB_REF_NAME without the leading "v".
//   3. The highest SemVer v-tag pointing exactly at HEAD, without the leading "v".
//   4. 0.0.0-SNAPSHOT for every other build (branches, PRs, local work).
val semVer = Regex("""^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(-[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?$""")

val isJitPack =
    providers
        .environmentVariable("JITPACK")
        .map { it.toBoolean() }
        .getOrElse(false)

val jitPackVersion =
    providers
        .environmentVariable("VERSION")
        .map { it.trim() }
        .filter { isJitPack && it.isNotEmpty() }

val githubTagVersion =
    providers
        .environmentVariable("GITHUB_REF_TYPE")
        .filter { it == "tag" }
        .flatMap { providers.environmentVariable("GITHUB_REF_NAME") }
        .map { tag ->
            val candidate = tag.removePrefix("v")
            if (!tag.startsWith("v") || !semVer.matches(candidate)) {
                throw GradleException("Tag '$tag' is not a release tag (expected vMAJOR.MINOR.PATCH[-PRERELEASE]).")
            }
            candidate
        }

val headTagVersion =
    providers
        .exec {
            // versionsort.suffix makes v2.0.0 sort above v2.0.0-rc.1 when both point at HEAD.
            commandLine("git", "-c", "versionsort.suffix=-", "tag", "--points-at", "HEAD", "--list", "v*", "--sort=-v:refname")
            isIgnoreExitValue = true
        }.standardOutput.asText
        .map { output ->
            output
                .lineSequence()
                .map { it.trim().removePrefix("v") }
                .firstOrNull { semVer.matches(it) }
                .orEmpty()
        }.filter { it.isNotEmpty() }

val publicationVersion: String =
    jitPackVersion.orNull
        ?: githubTagVersion.orNull
        ?: runCatching { headTagVersion.orNull }.getOrNull()
        ?: "0.0.0-SNAPSHOT"

// JitPack keeps serving the historical com.github.eltonvs coordinates; everything else uses
// the Maven Central namespace.
group = if (isJitPack) "com.github.eltonvs" else "io.github.eltonvs"
version = publicationVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

ktlint {
    version.set(libs.versions.ktlint.engine)
    ignoreFailures.set(false)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    ignoreFailures = false
    config.setFrom(files("$rootDir/detekt.yml"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Test>().configureEach {
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }

    addTestListener(
        object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) = Unit

            override fun beforeTest(testDescriptor: TestDescriptor) = Unit

            override fun afterTest(
                testDescriptor: TestDescriptor,
                result: TestResult,
            ) = Unit

            override fun afterSuite(
                suite: TestDescriptor,
                result: TestResult,
            ) {
                if (suite.parent == null) {
                    logger.lifecycle(
                        "Test summary for $path: " +
                            "${result.testCount} executed, " +
                            "${result.successfulTestCount} succeeded, " +
                            "${result.failedTestCount} failed, " +
                            "${result.skippedTestCount} skipped",
                    )
                    if (result.testCount == 0L) {
                        throw GradleException(
                            "No tests were executed for task $path. " +
                                "Check source sets and CI task configuration.",
                        )
                    }
                }
            }
        },
    )
}

val hasSigningKey =
    listOf("signingInMemoryKey", "signing.keyId", "signing.gnupg.keyName")
        .any { providers.gradleProperty(it).isPresent }

mavenPublishing {
    coordinates(group.toString(), "kotlin-obd-api", publicationVersion)
    // The release workflow runs publishAndReleaseToMavenCentral; publishToMavenCentral alone
    // leaves a validated deployment in the Central Portal for a manual "Publish" click.
    publishToMavenCentral()
    // Signing is required by Maven Central only. JitPack and local publishToMavenLocal builds
    // have no key and must keep working, so only sign when a key is configured.
    if (hasSigningKey) {
        signAllPublications()
    }
    // POM metadata comes from the POM_* entries in gradle.properties.
}

tasks.withType<PublishToMavenRepository>().configureEach {
    if (name.endsWith("ToMavenCentralRepository")) {
        val version = publicationVersion
        val signed = hasSigningKey
        doFirst {
            if (version.endsWith("-SNAPSHOT")) {
                throw GradleException("Refusing to publish $version to Maven Central: build from a release tag.")
            }
            if (!signed) {
                throw GradleException("Refusing to publish unsigned artifacts to Maven Central: configure a signing key.")
            }
        }
    }
}

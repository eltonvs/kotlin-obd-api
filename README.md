<p align="center">
  <img width="300px" src="img/kotlin-obd-api-logo.png" />
</p>

<h1 align="center">Kotlin OBD API</h1>

[![GitHub release](https://img.shields.io/github/v/release/eltonvs/kotlin-obd-api)](https://github.com/eltonvs/kotlin-obd-api/releases)
[![CI Status](https://github.com/eltonvs/kotlin-obd-api/actions/workflows/ci.yml/badge.svg)](https://github.com/eltonvs/kotlin-obd-api/actions/workflows/ci.yml)
[![Maintainability](https://qlty.sh/gh/eltonvs/projects/kotlin-obd-api/maintainability.svg)](https://qlty.sh/gh/eltonvs/projects/kotlin-obd-api)
[![GitHub license](https://img.shields.io/github/license/eltonvs/kotlin-obd-api)](https://github.com/eltonvs/kotlin-obd-api/blob/master/LICENSE)
[![Open Source](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://opensource.org/)


A lightweight and developer-driven Kotlin OBD-II (ELM327) library for any Kotlin/JVM project to query and parse OBD commands.

Written in pure Kotlin and platform agnostic with a simple and easy-to-use interface, so you can hack your car without any hassle. :blue_car:

Use it to read and parse vehicle diagnostics over Bluetooth, Wi-Fi, or USB:

- Live telemetry (RPM, speed, throttle position, MAF, temperatures, pressure and more)
- Diagnostic Trouble Codes (DTC): current, pending and permanent
- VIN and monitor status commands
- Adapter-level AT commands for ELM327 setup

The API is connection-agnostic and receives an `InputStream` and an `OutputStream`, so you can integrate it with your own Bluetooth, Wi-Fi, or USB transport.

## Installation

### Gradle (Kotlin DSL)

In your root `build.gradle.kts` file:
```kotlin
repositories {
  maven("https://jitpack.io")
}

dependencies {
  implementation("com.github.eltonvs:kotlin-obd-api:1.4.1")
}
```

### Gradle (Groovy)

In your root `build.gradle` file:
```gradle
repositories {
  maven { url 'https://jitpack.io' }
}

dependencies {
  // Kotlin OBD API
  implementation 'com.github.eltonvs:kotlin-obd-api:1.4.1'
}
```

### Maven

Add JitPack to the repositories section:
```xml
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Add the dependency:
```xml
<dependency>
  <groupId>com.github.eltonvs</groupId>
  <artifactId>kotlin-obd-api</artifactId>
  <version>1.4.1</version>
</dependency>
```

### Manual

You can download a jar from GitHub's [releases page](https://github.com/eltonvs/kotlin-obd-api/releases).

## Quickstart

Get an `InputStream` and an `OutputStream` from your connection interface and create an `ObdDeviceConnection` instance.

```kotlin
import com.github.eltonvs.obd.command.Switcher
import com.github.eltonvs.obd.command.at.ResetAdapterCommand
import com.github.eltonvs.obd.command.at.SetEchoCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import java.io.InputStream
import java.io.OutputStream

suspend fun readObd(inputStream: InputStream, outputStream: OutputStream) {
    val obdConnection = ObdDeviceConnection(inputStream, outputStream)

    // Typical ELM327 setup
    obdConnection.run(ResetAdapterCommand())
    obdConnection.run(SetEchoCommand(Switcher.OFF))

    val rpm = obdConnection.run(RPMCommand())
    val vin = obdConnection.run(VINCommand(), useCache = true)
    val troubleCodes = obdConnection.run(TroubleCodesCommand())

    println("RPM: ${rpm.value} ${rpm.unit}")
    println("VIN: ${vin.value}")
    println("DTC: ${troubleCodes.value.ifBlank { "none" }}")
}
```

`run` parameters:
- `command`: any `ObdCommand`
- `useCache` (default `false`): reuses previous raw responses for identical commands
- `delayTime` (default `0`): delay in milliseconds after sending command
- `maxRetries` (default `5`): read polling retries before giving up

Runtime note: call `run()` from a background coroutine context (for example `Dispatchers.IO`). On Android, do not call it from the main thread.

Concurrency note: each `ObdDeviceConnection` instance is a serialized command channel guarded by a coroutine `Mutex`. Reuse one instance per physical connection.

The returned object is an `ObdResponse` with:

| Attribute | Type | Description |
| :- | :- | :- |
| `command` | `ObdCommand` | The command passed to the `run` method |
| `rawResponse` | `ObdRawResponse` | This class holds the raw data returned from the car |
| `value` | `String` | The parsed value |
| `unit` | `String` | The unit from the parsed value (for example: `Km/h`, `RPM`) |


`ObdRawResponse` attributes:

| Attribute | Type | Description |
| :- | :- | :- |
| `value` | `String` | The raw value (hex) |
| `elapsedTime` | `Long` | The elapsed time (in milliseconds) to run the command |
| `processedValue` | `String` | The raw (hex) value without whitespaces, colons or any other "noise" |
| `bufferedValue` | `IntArray` | The raw (hex) value as a `IntArray` |


## Extending the library

Create a custom command by extending `ObdCommand` and overriding the required fields:
```kotlin
class CustomCommand : ObdCommand() {
    // Required
    override val tag = "CUSTOM_COMMAND"
    override val name = "Custom Command"
    override val mode = "01"
    override val pid = "FF"

    // Optional
    override val defaultUnit = ""
    override val handler = { response: ObdRawResponse ->
        "Calculated value from ${response.processedValue}"
    }
}
```


## Commands

Here is a short list of supported commands. For the full list, see [SUPPORTED_COMMANDS.md](SUPPORTED_COMMANDS.md).

- Available Commands
- Vehicle Speed
- Engine RPM
- DTC Number
- Trouble Codes (Current, Pending and Permanent)
- Throttle Position
- Fuel Pressure
- Timing Advance
- Intake Air Temperature
- Mass Air Flow Rate (MAF)
- Engine Run Time
- Fuel Level Input
- MIL ON/OFF
- Vehicle Identification Number (VIN)

NOTE: Support for those commands will vary from car to car.

## LLM Context

This repository includes LLM-focused docs for coding assistants and agents:

- [llms.txt](llms.txt): short machine-readable index for quick retrieval
- [LLM_CONTEXT.md](LLM_CONTEXT.md): API conventions, command examples, and decision guide


## Contributing

Want to help or have something to add to the repo? Found an issue in a specific feature?

- Open an issue to explain the problem you want to solve: [Open an issue](https://github.com/eltonvs/kotlin-obd-api/issues)
- After discussion, open a PR (or draft PR for larger contributions): [Current PRs](https://github.com/eltonvs/kotlin-obd-api/pulls)
- Run local verification before opening a PR: `./gradlew clean test ktlintCheck detekt`
- Auto-format Kotlin sources when needed: `./gradlew ktlintFormat`


## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/eltonvs/kotlin-obd-api/tags).


## Authors

- **Elton Viana** - Initial work - Also created the [java-obd-api](https://github.com/eltonvs/java-obd-api)

See also the list of [contributors](https://github.com/eltonvs/kotlin-obd-api/contributors) who participated in this project.


## License

This project is licensed under the Apache 2.0 License - See the [LICENCE](LICENSE) file for more details.


## Acknowledgments

- **Paulo Pires** - Creator of the [obd-java-api](https://github.com/pires/obd-java-api), on which the initial steps were based.
- **[SmartMetropolis Project](http://smartmetropolis.imd.ufrn.br/)** (Digital Metropolis Institute - UFRN, Brazil) - Backed and sponsored the project development during the initial steps.
- **[Ivanovitch Silva](https://github.com/ivanovitchm)** - Helped a lot during the initial steps and with the OBD research.

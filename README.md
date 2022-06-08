<p align="center">
  <img width="300px" src="img/kotlin-obd-api-logo.png" />
</p>

<h1 align="center">Kotlin OBD API</h1>

[![GitHub version](https://badge.fury.io/gh/eltonvs%2Fkotlin-obd-api.svg)](https://badge.fury.io/gh/eltonvs%2Fkotlin-obd-api)
[![CI Status](https://github.com/eltonvs/kotlin-obd-api/workflows/CI/badge.svg)](https://github.com/eltonvs/kotlin-obd-api/actions?query=workflow%3ACI)
[![Maintainability](https://api.codeclimate.com/v1/badges/e70f6ab78bdae20de178/maintainability)](https://codeclimate.com/github/eltonvs/kotlin-obd-api/maintainability)
[![codebeat badge](https://codebeat.co/badges/6af3e4ad-1171-4868-871f-9e5e6be63df9)](https://codebeat.co/projects/github-com-eltonvs-kotlin-obd-api-master)
[![GitHub license](https://img.shields.io/github/license/eltonvs/kotlin-obd-api)](https://github.com/eltonvs/kotlin-obd-api/blob/master/LICENSE)
[![Open Source](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://opensource.org/)


A lightweight and developer-driven API to query and parse OBD commands.

Written in pure Kotlin and platform agnostic with a simple and easy to use interface, so you can hack your car without any hassle. :blue_car:

This is a flexible API that allows developers to plug-in to any connection interface (Bluetooth, Wifi, USB...). By default we use an `ObdDeviceConnection` that receives an `InputStream` and an `OutputStream` as parameters (so if you can get this from your connection interface, you're good to go :thumbsup:).


## Installation


### Gradle

In your root `build.gradle` file, at the end of repositories:
```gradle
repositories {
  ...
  maven { url 'https://jitpack.io' }
}
```

Add the dependency
```gradle
dependencies {
  ...

  // Kolin OBD API
  implementation 'com.github.eltonvs:kotlin-obd-api:1.1.2'
}
```

### Maven

Add jitpack to the repositories section
```xml
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Add the dependency
```xml
<dependency>
  <groupId>com.github.eltonvs</groupId>
  <artifactId>kotlin-obd-api</artifactId>
  <version>1.1.2</version>
</dependency>
```

### Manual

You can download a jar from GitHub's [releases page](https://github.com/eltonvs/kotlin-obd-api/releases).

## Basic Usage

Get an `InputStream` and an `OutputStream` from your connection interface and create an `ObdDeviceConnection` instance.

```kotlin
// Create ObdDeviceConnection instance
val obdConnection = ObdDeviceConnection(inputStream, outputStream)
```

With this, you're ready to run any command you want, just pass the command instance to the `.run` method. This command accepts 3 parameters: `command`, `useCache` (default = `false`) and `delayTime` (default = `0`).

```kotlin
// Retrieving OBD Speed Command
val response = obdConnection.run(SpeedCommand())

// Using cache (use with caution)
val cachedResponse = obdConnection.run(VINCommand(), useCache = true)

// With a delay time - with this, the API will wait 500ms after executing the command
val delayedResponse = obdConnection(RPMCommand(), delayTime = 500L)
```

The retuned object is a `ObdResponse` and has the following attributes:

| Attribute | Type | Description |
| :- | :- | :- |
| `command` | `ObdCommand` | The command passed to the `run` method |
| `rawResponse` | `ObdRawResponse` | This class holds the raw data returned from the car |
| `value` | `String` | The parsed value |
| `unit` | `String` | The unit from the parsed value (e.g.: `Km/h`, `RPM`, ... |


The `ObdRawResponse` has the following attributes:

| Attribute | Type | Description |
| :- | :- | :- |
| `value` | `String` | The raw value (hex) |
| `elapsedTime` | `Long` | The elapsed time (in milliseconds) to run the command |
| `processedValue` | `String` | The raw (hex) value without whitespaces, colons or any other "noise" |
| `bufferedValue` | `IntArray` | The raw (hex) value as a `IntArray` |


## Extending the library

It's easy to add a custom command using this library, all you need to do is create a class extending the `ObdCommand` class and overriding the following methods:
```kotlin
class CustomCommand : ObdCommand() {
    // Required
    override val tag = "CUSTOM_COMMAND"
    override val name "Custom Command"
    override val mode = "01"
    override val pid = "FF"

    // Optional
    override val defaultUnit = ""
    override val handler = { it: ObdRawResponse -> "Calculations to parse value from ${it.processedValue}" }
}
```


## Commands

Here are a handul list of the main supported commands (sensors). For a full list, see [here](SUPPORTED_COMMANDS.md).

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


## Contributing

Want to help or have something to add to the repo? problem on a specific feature?

-   Open an issue to explain the issue you want to solve  [Open an issue](https://github.com/eltonvs/kotlin-obd-api/issues)
-   After discussion to validate your ideas, you can open a PR or even a draft PR if the contribution is a big one  [Current PRs](https://github.com/eltonvs/kotlin-obd-api/pulls)


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

# LLM Context for `kotlin-obd-api`

This document helps coding assistants correctly choose and apply this library for OBD-II tasks.

## Project Summary

- Name: `kotlin-obd-api`
- Language: Kotlin (JVM)
- Package: `com.github.eltonvs:kotlin-obd-api` (JitPack)
- Primary purpose: query and parse OBD-II / ELM327 commands from Kotlin apps
- Typical targets: Android and JVM desktop/server apps that already manage transport sockets/streams

## When to Use

Use this library when the task involves:
- Reading OBD-II telemetry (RPM, speed, MAF, temperatures, pressures, etc.)
- Reading VIN
- Reading or clearing trouble codes (DTC)
- Executing ELM327 AT setup commands

Do not use this library when the task is:
- Managing Bluetooth pairing/UI (not provided here)
- Implementing low-level transport drivers (you provide `InputStream`/`OutputStream`)
- Rendering vehicle-specific PID catalogs beyond standard supported commands

## Core Types and API

### Connection

`ObdDeviceConnection(inputStream, outputStream, ioDispatcher = Dispatchers.IO)`

### Execute command

```kotlin
suspend fun run(
    command: ObdCommand,
    useCache: Boolean = false,
    delayTime: Long = 0,
    maxRetries: Int = 5,
): ObdResponse
```

Execution model:
- `run` is `suspend`
- Commands are serialized per connection instance (internal `Mutex`)
- Cache is keyed by command class + raw command when `useCache = true`

### Response types

- `ObdResponse`
  - `command: ObdCommand`
  - `rawResponse: ObdRawResponse`
  - `value: String`
  - `unit: String`
- `ObdRawResponse`
  - `value: String`
  - `elapsedTime: Long`
  - `processedValue: String`
  - `bufferedValue: IntArray`

## Recommended Initialization Flow (ELM327)

```kotlin
obdConnection.run(ResetAdapterCommand())
obdConnection.run(SetEchoCommand(Switcher.OFF))
obdConnection.run(SetLineFeedCommand(Switcher.OFF))
obdConnection.run(SetHeadersCommand(Switcher.OFF))
```

Depending on adapter/car, protocol selection may be explicit:

```kotlin
obdConnection.run(SelectProtocolCommand(ObdProtocols.AUTO))
```

## Common Implementation Patterns

### Read live telemetry

```kotlin
val rpm = obdConnection.run(RPMCommand())
val speed = obdConnection.run(SpeedCommand())
```

### Read VIN once and cache

```kotlin
val vin = obdConnection.run(VINCommand(), useCache = true)
```

### Read and clear DTC

```kotlin
val currentCodes = obdConnection.run(TroubleCodesCommand())
obdConnection.run(ResetTroubleCodesCommand())
```

## Error Handling

The library throws command/response-specific runtime exceptions (subclasses of `BadResponseException`), including:
- `NoDataException`
- `UnableToConnectException`
- `BusInitException`
- `MisunderstoodCommandException`
- `UnknownErrorException`
- `UnSupportedCommandException`

For robust apps, catch `BadResponseException` around command execution and apply retries/fallback based on command criticality.

## Concurrency and Android Notes

- Call `run()` from a background coroutine context (for example `Dispatchers.IO`)
- Reuse one `ObdDeviceConnection` per physical adapter/session
- Do not execute commands in parallel against the same connection instance

## Command Coverage

For the full command matrix (AT commands + Mode 01/03/04/07/09/0A), see:
- [`SUPPORTED_COMMANDS.md`](./SUPPORTED_COMMANDS.md)

## References

- README: [`README.md`](./README.md)
- Source entrypoint: [`src/main/kotlin/com/github/eltonvs/obd/connection/ObdDeviceConnection.kt`](./src/main/kotlin/com/github/eltonvs/obd/connection/ObdDeviceConnection.kt)

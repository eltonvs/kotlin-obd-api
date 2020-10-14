# Supported Commands

Full list of supported commands.

## `AT` Commands (ELM327)
| Command | Name | Description |
| :- | :- | :-|
| `Z` | `RESET_ADAPTER` | Reset OBD Adapter |
| `WS` | `WARM_START` | OBD Warm Start |
| `SI` | `SLOW_INITIATION` | OBD Slow Initiation |
| `LP` | `LOW_POWER_MODE` | OBD Low Power Mode |
| `BD` | `BUFFER_DUMP` | OBD Buffer Dump |
| `BI` | `BYPASS_INITIALIZATION` | OBD Bypass Initialization Sequence |
| `PC` | `PROTOCOL_CLOSE` | OBD Protocol Close |
| `DP` | `DESCRIBE_PROTOCOL` | Describe Protocol |
| `DPN` | `DESCRIBE_PROTOCOL_NUMBER` | Describe Protocol Number |
| `IGN` | `IGNITION_MONITOR` | Ignition Monitor |
| `RN` | `ADAPTER_VOLTAGE` | OBD Adapter Voltage |
| `SP {X}` | `SELECT_PROTOCOL_{X}` | Select Protocol where `{X}` is an `ObdProtocols` constant |
| `AT {X}` | `SET_ADAPTIVE_TIMING_{X}` | Set Adaptive Timing Control where `{X}` is an `AdaptiveTimingMode` constant |
| `E{X}` | `SET_ECHO_{X}` | Set Echo where `{X}` is a `Switcher` constant |
| `H{X}` | `SET_HEADERS_{X}` | Set Headers where `{X}` is a `Switcher` constant |
| `L{X}` | `SET_LINE_FEED_{X}` | Set Line Feed where `{X}` is a `Switcher` constant |
| `S{X}` | `SET_SPACES_{X}` | Set Spaces where `{X}` is a `Switcher` constant |
| `ST {X}` | `SET_TIMEOUT` | Set Timeout where `{X}` is an `Int` value |

## Mode 01
| Command | Name | Description |
| -- | :- | :-|
| `00`, `20`, `40`, `60`, `80` | `AVAILABLE_COMMANDS_{RANGE}` | Available PIDs for each range, where `{RANGE}` is an `AvailablePIDsRanges` constant |
| `01` | `DTC_NUMBER` | Diagnostic Trouble Codes Number |
| `01` | `MIL_ON` | MIL ON/OFF |
| `01` | `MONITOR_STATUS_SINCE_CODES_CLEARED` | Monitor Status Since Codes Cleared |
| `04` | `ENGINE_LOAD` | Engine Load |
| `05` | `ENGINE_COOLANT_TEMPERATURE` | Engine Coolant Temperature |
| `06` | `SHORT_TERM_BANK_1` | Short Term Fuel Trim Bank 1 |
| `07` | `SHORT_TERM_BANK_2` | Short Term Fuel Trim Bank 2 |
| `08` | `LONG_TERM_BANK_1` | Long Term Fuel Trim Bank 1 |
| `09` | `LONG_TERM_BANK_2` | Long Term Fuel Trim Bank 2 |
| `0A` | `FUEL_PRESSURE` | Fuel Pressure |
| `0B` | `INTAKE_MANIFOLD_PRESSURE` | Intake Manifold Pressure |
| `0C` | `ENGINE_RPM` | Engine RPM |
| `0D` | `SPEED` | Vehicle Speed |
| `0E` | `TIMING_ADVANCE` | Timing Advance |
| `0F` | `AIR_INTAKE_TEMPERATURE` | Air Intake Temperature |
| `10` | `MAF` | Mass Air Flow |
| `11` | `THROTTLE_POSITION` | Throttle Position |
| `1F` | `ENGINE_RUNTIME` | Engine Runtime |
| `21` | `DISTANCE_TRAVELED_MIL_ON` | Distance traveled with MIL on |
| `22` | `FUEL_RAIL_PRESSURE` | Fuel Rail Pressure |
| `23` | `FUEL_RAIL_GAUGE_PRESSURE` | Fuel Rail Gauge Pressure |
| `2C` | `COMMANDED_EGR` | Commanded EGR |
| `2D` | `EGR_ERROR` | EGR Error |
| `2F` | `FUEL_LEVEL` | Fuel Level |
| `31` | `DISTANCE_TRAVELED_AFTER_CODES_CLEARED` | Distance traveled since codes cleared |
| `33` | `BAROMETRIC_PRESSURE` | Barometric Pressure |
| `34` | `OXYGEN_SENSOR_1` | Oxygen Sensor 1 |
| `35` | `OXYGEN_SENSOR_2` | Oxygen Sensor 2 |
| `36` | `OXYGEN_SENSOR_3` | Oxygen Sensor 3 |
| `37` | `OXYGEN_SENSOR_4` | Oxygen Sensor 4 |
| `38` | `OXYGEN_SENSOR_5` | Oxygen Sensor 5 |
| `39` | `OXYGEN_SENSOR_6` | Oxygen Sensor 6 |
| `3A` | `OXYGEN_SENSOR_7` | Oxygen Sensor 7 |
| `3B` | `OXYGEN_SENSOR_8` | Oxygen Sensor 8 |
| `41` | `MONITOR_STATUS_CURRENT_DRIVE_CYCLE` | Monitor Status Current Drive Cycle |
| `42` | `CONTROL_MODULE_VOLTAGE` | Control Module Power Supply |
| `43` | `ENGINE_ABSOLUTE_LOAD` | Engine Absolute Load |
| `44` | `COMMANDED_EQUIVALENCE_RATIO` | Fuel-Air Commanded Equivalence Ratio |
| `45` | `RELATIVE_THROTTLE_POSITION` | Relative Throttle Position |
| `46` | `AMBIENT_AIR_TEMPERATURE` | Ambient Air Temperature |
| `4D` | `TIME_TRAVELED_MIL_ON` | Time run with MIL on |
| `4E` | `TIME_SINCE_CODES_CLEARED` | Time since codes cleared |
| `51` | `FUEL_TYPE` | Fuel Type |
| `52` | `ETHANOL_LEVEL` | Ethanol Level |
| `5C` | `ENGINE_OIL_TEMPERATURE` | Engine Oil Temperature |
| `5E` | `FUEL_CONSUMPTION_RATE` | Fuel Consumption Rate |


## Mode 03

| Name | Description |
| :- | :- |
| `TROUBLE_CODES` | Trouble Codes |


## Mode 04

| Name | Description |
| :- | :- |
| `RESET_TROUBLE_CODES` | Reset Trouble Codes |


## Mode 07

| Name | Description |
| :- | :- |
| `PENDING_TROUBLE_CODES` | Pending Trouble Codes |


## Mode 09

| Command | Name | Description |
| :- | :- | :-|
| `02` | `VIN` | Vehicle Identification Number (VIN) |


## Mode 0A

| Name | Description |
| :- | :- |
| `PERMANENT_TROUBLE_CODES` | Permanent Trouble Codes |

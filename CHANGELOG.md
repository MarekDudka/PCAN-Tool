# Changelog

All notable changes to PCAN-Tool are documented here.

## 1.0.0

- Initial Kotlin Multiplatform desktop app (Linux, Windows) for PEAK-System PCAN-Basic adapters:
  live message viewer, id/type filtering, recording and CSV export.
- `pcan-basic` module: JNA binding to the PCAN-Basic C API for classic CAN (`CAN_Initialize`,
  `CAN_Read`, `CAN_Write`, `CAN_FilterMessages`, `CAN_GetValue`/`CAN_SetValue`,
  `CAN_GetErrorText`).
- Unified Trace view: RX and TX messages interleaved chronologically in one table with a Dir
  column, filterable by id range and frame kind. Filter and recording controls only shown while
  the Trace tab is active.
- Overview tab: live per-id stats (latest payload, count, cycle time) for RX and TX separately.
- Send a message manually or configure cyclic (periodic) TX messages via right-click on the TX
  area, each independently switchable on/off.
- CSV export of a recorded session (Start/Stop/Clear/Export).
- Vendored PEAK's `peak-linux-driver` kernel module source (GPLv2) under `third-party/`, with
  build/load/udev-rule instructions, since PCAN-Basic on Linux needs it loaded before
  `CAN_Initialize` will succeed.
- Packaging: `.deb` and `.msi` via Compose Desktop's Gradle plugin, plus a `.AppImage` via
  `scripts/build-appimage.sh` (AppImage isn't one of the formats jpackage produces natively).
- PEAK-System's End User Software License Agreement (governing the separately-installed
  PCAN-Basic library, not this project's own code) kept for reference at
  `licenses/PEAK-SYSTEM-EULA.txt`.

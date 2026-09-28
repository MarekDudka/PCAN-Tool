# PCAN-Tool

A Kotlin Multiplatform desktop application (Linux, Windows) for PEAK-System PCAN-Basic CAN
adapters: live message viewer, id/type filtering, recording and CSV export.

## Modules

- `core` — platform-independent domain model: `CanMessage`, `MessageFilter`, `MessageStatsTable`,
  `RecordingSession`, `CsvExporter`. Pure Kotlin, built with the Kotlin Multiplatform plugin
  (currently targeting `jvm()` only) so it stays reusable if more targets are added later.
- `pcan-basic` — a JNA binding to the PCAN-Basic C API (classic CAN only: `CAN_Initialize`,
  `CAN_Read`, `CAN_Write`, `CAN_FilterMessages`, `CAN_GetValue`/`CAN_SetValue`,
  `CAN_GetErrorText`), plus a small Kotlin-friendly wrapper (`PcanConnection`,
  `PcanChannel`, `PcanBaudRate`).
- `app` — the Compose Multiplatform Desktop UI: connection bar, filter panel, recording controls,
  and Trace/Overview views.

## System requirements to run PCAN-Tool

Everything below is about *using* a built `.deb`/`.AppImage`/`.msi` — see
[Building installers](#building-installers) if you're building from source instead.

- **Java: nothing to install.** The packaged app bundles its own jlinked runtime, so no separate
  JRE/JDK is required on the machine running it.
- **PCAN-Basic: install separately, not bundled.** It's proprietary software from PEAK-System —
  get it from
  https://www.peak-system.com/products/software/development-packages/pcan-basic/ — and it must be
  discoverable on the OS's native library search path or the app can't find it (it reports this
  clearly at connect time rather than crashing on startup):
  - **Windows**: install the PCAN driver package for your adapter (e.g. "PCAN-USB"), which
    installs `PCANBasic.dll`.
  - **Linux**: install `libpcanbasic.so`, on `LD_LIBRARY_PATH` or in a standard location such as
    `/usr/lib` (with the unversioned `libpcanbasic.so` symlink JNA needs alongside whatever
    versioned file — e.g. `libpcanbasic.so.4` — actually ships).
- **Linux only — PEAK's `pcan` kernel module, loaded and actually claiming your adapter.** This is
  usually the fiddliest part; full instructions (building it, the udev rule for non-root
  permissions, and the in-kernel `peak_usb` driver conflict that silently stops `pcan.ko` from
  claiming the device) are in
  [`third-party/peak-linux-driver-8.20.0`](third-party/peak-linux-driver-8.20.0)'s own README.
  Without it loaded and actually bound, connecting fails with `PCAN_ERROR_NODRIVER` even though
  the adapter shows up fine in `lsusb`.

Installing and using PCAN-Basic is governed by PEAK-System's own End User Software License
Agreement, not this project's license. A copy is kept at
[`licenses/PEAK-SYSTEM-EULA.txt`](licenses/PEAK-SYSTEM-EULA.txt) (source:
https://www.peak-system.com/support/eula/) for reference — always treat the live page as
authoritative, since PEAK-System can update it.

## Running

```
./gradlew run
```

## Building installers

```
./gradlew :app:packageDeb    # Linux .deb
./gradlew :app:packageMsi    # Windows .msi (run on Windows, or cross-compile per Gradle docs)
./scripts/build-appimage.sh  # Linux AppImage (portable, no install needed)
```

The AppImage isn't a Gradle task because Compose Multiplatform's packaging only wraps whatever
jpackage itself supports (deb/rpm/msi/dmg), and AppImage isn't one of those. The script instead
wraps `:app:createDistributable`'s output (which it runs first if needed) in the AppDir layout
`appimagetool` expects, fetching that tool on first run. Output lands at
`app/build/compose/binaries/main/appimage/PCAN-Tool-x86_64.AppImage`.

## Scope

Classic CAN (11/29-bit, up to 8 data bytes) only — no CAN FD. Filtering happens in software
(`MessageFilter`, applied to both the live view and recording); the hardware range filter
(`CAN_FilterMessages`) is wired into `pcan-basic` but not used by the UI, since it can't express
the standard/extended/remote-frame filtering the UI needs on its own.

## License

This software is licensed under the [MIT License](LICENSE). It utilizes the PCAN-Basic API by
PEAK-System Technik GmbH. PCAN-Basic components remain the property of PEAK-System and are
subject to their respective usage terms and hardware requirements (see
[`licenses/PEAK-SYSTEM-EULA.txt`](licenses/PEAK-SYSTEM-EULA.txt)); PCAN-Basic itself is not
distributed with this software. The vendored kernel driver source under
[`third-party/peak-linux-driver-8.20.0`](third-party/peak-linux-driver-8.20.0) is PEAK-System's
own code under its own GPLv2/LGPLv2.1 licenses, not MIT.

# third-party

Vendored source PCAN-Tool does not need at build/run time, but that's useful to keep alongside the
project for anyone setting up a Linux machine to talk to PCAN-Basic hardware.

## `peak-linux-driver-8.20.0/driver`

The `pcan` kernel module source from PEAK-System's `peak-linux-driver` v8.20.0 (GPLv2 — see
`LICENSE.gpl` in that folder). It provides the CAN channel devices that PCAN-Basic's
`libpcanbasic.so` needs on Linux; without it loaded, `CAN_Initialize` fails with
`PCAN_ERROR_NODRIVER`.

Only the `driver/` subtree is included here (not `libpcanbasic`, `lib`, `test`, or the `.chm`/PDF
docs from the upstream release) — this repo doesn't build or ship those, so there's no reason to
carry their weight. Get the full package from
https://www.peak-system.com/products/software/development-packages/pcan-basic/ if you need them.

Build and load it:

```
cd third-party/peak-linux-driver-8.20.0/driver
make clean && make
sudo insmod pcan.ko
lsmod | grep pcan
ls /dev/pcan*
```

`libpcanbasic.so.4` itself (the library PCAN-Tool's `pcan-basic` module loads via JNA) is a
separate install — see the main [README](../README.md#prerequisite-pcan-basic-native-library).

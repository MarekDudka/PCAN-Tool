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

### Build

Requires kernel headers for your running kernel and a matching gcc:

```
ls /lib/modules/$(uname -r)/build   # must exist; if not: sudo apt install linux-headers-$(uname -r)
cd third-party/peak-linux-driver-8.20.0/driver
make clean
make
```

Build **only inside this `driver/` folder**, not a parent directory that also has `test`/`lib`
targets — those pull in `libpopt-dev` for command-line parsing, which this project doesn't need
just to get `pcan.ko`.

A successful build ends with `LD [M] pcan.ko` (and usually `BTF [M] pcan.ko`) and produces
`pcan.ko` right there in `driver/`. It's gitignored (kernel-specific binary), so it won't show up
after a fresh clone — you always rebuild it locally.

### Load it

```
sudo insmod pcan.ko
lsmod | grep pcan               # should list "pcan"
ls -la /dev/pcan*                # should list at least one channel device
```

If instead you get `PCAN_ERROR_NODRIVER` ("The driver is not loaded") from PCAN-Basic while the
USB adapter is visible in `lsusb`, this step hasn't been done (or didn't survive a reboot — see
below).

If `insmod` fails with `Unknown symbol in module`, this driver's PCI/I2C support (built in by
default alongside USB support) references `i2c_bit_add_bus`, which lives in the separate
`i2c-algo-bit` kernel module — it's not missing, just not loaded yet:

```
sudo modprobe i2c-algo-bit
sudo insmod pcan.ko
```

If `insmod` succeeds and `lsmod | grep pcan` shows it loaded, but `/dev/pcan*` still doesn't
exist, check what's actually bound to your adapter's USB interface:

```
for d in /sys/bus/usb/devices/*; do
    [ "$(cat "$d/idVendor" 2>/dev/null)" = "0c72" ] || continue
    for i in "$d"/*/; do [ -e "${i}driver" ] && echo "$(basename "$i") -> $(readlink -f "${i}driver")"; done
done
```

Modern kernels ship an in-tree SocketCAN driver (`peak_usb`) for the same PEAK USB adapters, and
it auto-loads on plug-in — a CAN interface can only be claimed by one driver at a time, so if
`peak_usb` grabs it first (you'll see a `can0`-style interface and `peak_usb` in the list above
instead of `pcan`), the out-of-tree `pcan.ko` never gets a chance even though it's loaded. The
vendored `udev/blacklist-peak.conf` stops the in-tree driver from auto-loading:

```
sudo cp third-party/peak-linux-driver-8.20.0/driver/udev/blacklist-peak.conf /etc/modprobe.d/
sudo rmmod peak_usb   # only unloads it now; the blacklist only prevents future auto-loads
```

Then unplug and replug the adapter so it re-enumerates and `pcan.ko` (already loaded, now
unopposed) claims it. Re-check with the `driver ->` loop above — it should now say `pcan`.

`insmod` only loads the module for the current boot; it does **not** persist. Either re-run it
after every reboot, or install [DKMS](https://github.com/dell/dkms) (`sudo apt install dkms`) and
register it once so it rebuilds/reloads automatically across kernel updates:

```
sudo dkms add third-party/peak-linux-driver-8.20.0/driver
sudo dkms install peak-linux-driver/8.20.0
```

(the vendored `driver/dkms.conf` already has the right `MAKE`/module name for this).

### Set up the udev rule

By default the USB device node PEAK's driver creates is only writable by `root`, so a normal user
can't open the channel — PCAN-Basic surfaces this as `PCAN_ERROR_RESOURCE` ("A resource (FIFO,
Client, timeout) cannot be created") from `CAN_Initialize`, even though the channel shows up as
available. The vendored `udev/45-pcan.rules` fixes that:

```
sudo cp third-party/peak-linux-driver-8.20.0/driver/udev/45-pcan.rules /etc/udev/rules.d/
sudo udevadm control --reload-rules
sudo udevadm trigger
```

Unplug and replug the adapter (or `sudo udevadm trigger` alone is usually enough) and re-check
`ls -la /dev/pcan*` — the group/permissions should now allow your user account access.

### Verify PCAN-Basic itself sees it

Once the module is loaded, confirm PCAN-Basic (not just the kernel) considers the channel
available, using PCAN-USB 1's `PCAN_CHANNEL_CONDITION`:

```
python3 -c "
import ctypes
lib = ctypes.CDLL('libpcanbasic.so')
lib.CAN_GetValue.restype = ctypes.c_uint32
lib.CAN_GetValue.argtypes = [ctypes.c_uint16, ctypes.c_uint8, ctypes.c_void_p, ctypes.c_uint32]
buf = ctypes.c_uint32(0)
status = lib.CAN_GetValue(0x51, 0x0D, ctypes.byref(buf), 4)
print(f'status=0x{status:x} condition=0x{buf.value:x}')  # condition 0x1 = available
"
```

`libpcanbasic.so.4` itself (the library PCAN-Tool's `pcan-basic` module loads via JNA) is a
separate install — see the main [README](../README.md#prerequisite-pcan-basic-native-library) for
that, including the `libpcanbasic.so` symlink JNA needs.

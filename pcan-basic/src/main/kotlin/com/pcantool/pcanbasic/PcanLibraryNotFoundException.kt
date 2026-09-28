package com.pcantool.pcanbasic

/**
 * Thrown when the proprietary PCAN-Basic native library (PCANBasic.dll on Windows,
 * libpcanbasic.so on Linux) can't be found on the system library path. PCAN-Tool never bundles
 * it: it must be installed separately from PEAK-System (bundled with their PCAN drivers).
 */
class PcanLibraryNotFoundException(cause: Throwable) : Exception(
    "PCAN-Basic native library not found. Install PEAK-System's PCAN drivers " +
        "(which include PCANBasic.dll / libpcanbasic.so) and ensure it is on the system library path.",
    cause,
)

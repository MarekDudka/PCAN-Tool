#!/bin/sh
# Builds a portable AppImage from the Compose Desktop app image, using PEAK's own icon (the
# jpackage-generated one already in the distributable, or the default Compose icon).
#
# Compose Multiplatform's Gradle plugin only knows how to produce .deb/.rpm/.msi/.dmg (whatever
# jpackage supports natively) — AppImage isn't one of those, so this wraps the existing
# `:app:createDistributable` output (bin/ + lib/, a self-contained app + bundled JRE) in the
# AppDir layout appimagetool expects, instead of teaching Gradle a format it has no support for.
#
# Usage: ./scripts/build-appimage.sh
# Requires network access the first time, to fetch appimagetool.

set -eu

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
APP_IMAGE_DIR="$ROOT_DIR/app/build/compose/binaries/main/app/PCAN-Tool"
OUT_DIR="$ROOT_DIR/app/build/compose/binaries/main/appimage"
TOOL_DIR="$ROOT_DIR/build/tools"
APPIMAGETOOL="$TOOL_DIR/appimagetool-x86_64.AppImage"
APPDIR="$OUT_DIR/AppDir"

# Gradle 8.12 (this project's wrapper version) doesn't run on very new JDKs (e.g. 25) — pin a
# known-compatible one if present, rather than silently inheriting whatever `java` the invoking
# shell happens to default to. Without this, running the script from a plain terminal can fail at
# the Gradle step while a JetBrains-configured IDE terminal (with its own Gradle JVM setting)
# succeeds, and — because that failure happens after `set -eu` but a stale AppImage from an
# earlier successful run is still sitting there — it's easy to not notice the rebuild didn't
# happen and run outdated code without any obvious error.
#
# Separately: whichever JDK builds the jlinked runtime gets bundled straight into the AppImage,
# and that choice affects more than compatibility — on at least Debian's OpenJDK 21 package,
# AWT's native MenuBar silently fails to render under this app's window manager (no error, no
# crash, the File/Help bar is just missing), while Amazon Corretto's build renders it fine. Any
# `~/.jdks/*` entry (how JetBrains IDEs install JDKs) is preferred first since that's most likely
# to be a non-Debian-packaged build; the openjdk-21 path is a compatibility-only fallback.
JAVA_HOME=""
for jdks_dir in "$HOME"/.jdks/*; do
    if [ -x "$jdks_dir/bin/java" ]; then
        JAVA_HOME="$jdks_dir"
        break
    fi
done
if [ -z "$JAVA_HOME" ]; then
    for candidate in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-17-openjdk-amd64; do
        if [ -d "$candidate" ]; then
            JAVA_HOME="$candidate"
            break
        fi
    done
fi
if [ -n "$JAVA_HOME" ]; then
    export JAVA_HOME
    echo "Using JAVA_HOME=$JAVA_HOME"
fi

# Always let Gradle decide whether a rebuild is needed — its own up-to-date checks handle that
# correctly (and cheaply, no-opping if nothing changed); a "does the binary already exist" check
# here would not, since it'd skip rebuilding after source changes and silently repackage stale
# code into the AppImage.
(cd "$ROOT_DIR" && ./gradlew :app:createDistributable --console=plain)

mkdir -p "$TOOL_DIR"
if [ ! -x "$APPIMAGETOOL" ]; then
    echo "Fetching appimagetool..."
    curl -sL --fail -o "$APPIMAGETOOL" \
        https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
    chmod +x "$APPIMAGETOOL"
fi

rm -rf "$APPDIR"
mkdir -p "$APPDIR/usr"
cp -r "$APP_IMAGE_DIR/bin" "$APP_IMAGE_DIR/lib" "$APPDIR/usr/"

cp "$APP_IMAGE_DIR/lib/PCAN-Tool.png" "$APPDIR/pcan-tool.png"

cat > "$APPDIR/AppRun" <<'EOF'
#!/bin/sh
HERE="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec "$HERE/usr/bin/PCAN-Tool" "$@"
EOF
chmod +x "$APPDIR/AppRun"

cat > "$APPDIR/pcan-tool.desktop" <<'EOF'
[Desktop Entry]
Type=Application
Name=PCAN-Tool
Comment=Viewer, filter, recorder and CSV exporter for PEAK-System PCAN-Basic adapters
Exec=AppRun
Icon=pcan-tool
Categories=Development;Electronics;
Terminal=false
EOF

mkdir -p "$OUT_DIR"
(cd "$OUT_DIR" && ARCH=x86_64 "$APPIMAGETOOL" "$APPDIR")

echo "AppImage written to $OUT_DIR"

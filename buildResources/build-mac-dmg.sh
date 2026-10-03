#!/bin/bash
# Builds a macOS dmg installer with jpackage from the jlink runtime image.
# Expects `mvn javafx:jlink` to have been run (target/tuntikirjaus exists).
# Must be run on macOS: the runtime image contains platform specific JRE and JavaFX natives.
# Output: target/dist/Tuntikirjaus-<version>.dmg
set -euo pipefail

cd -- "$(dirname -- "$0")/.."

RUNTIME_IMAGE=target/tuntikirjaus
BUILD_DIR=target/dmg-build
DIST_DIR=target/dist

if [ ! -d "$RUNTIME_IMAGE" ]; then
    echo "Runtime image $RUNTIME_IMAGE not found, run 'mvn javafx:jlink' first" >&2
    exit 1
fi

# jpackage on macOS accepts only 1-3 numeric components (e.g. 1.1.9), so drop suffixes like -BETA or -SNAPSHOT
POM_VERSION=$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)
APP_VERSION=${POM_VERSION%%-*}

rm -rf "$BUILD_DIR" "$DIST_DIR"
mkdir -p "$BUILD_DIR/icon.iconset" "$DIST_DIR"

# Convert png icon to icns
for size in 16 32 128 256 512; do
    sips -z $size $size buildResources/tuntikirjausResized.png --out "$BUILD_DIR/icon.iconset/icon_${size}x${size}.png" > /dev/null
done
iconutil -c icns "$BUILD_DIR/icon.iconset" -o "$BUILD_DIR/Tuntikirjaus.icns"

jpackage \
    --type dmg \
    --name Tuntikirjaus \
    --app-version "$APP_VERSION" \
    --mac-package-identifier com.sirvja.tuntikirjaus \
    --runtime-image "$RUNTIME_IMAGE" \
    --module com.sirvja.tuntikirjaus/com.sirvja.tuntikirjaus.Launcher \
    --java-options "--enable-native-access=javafx.graphics,org.xerial.sqlitejdbc" \
    --icon "$BUILD_DIR/Tuntikirjaus.icns" \
    --dest "$DIST_DIR"

echo "Created $(ls "$DIST_DIR"/*.dmg)"

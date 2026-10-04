#!/bin/bash
# Builds a Windows msi installer with jpackage from the jlink runtime image.
# Expects `mvn javafx:jlink` to have been run (target/tuntikirjaus exists).
# Must be run on Windows (e.g. Git Bash) with WiX Toolset installed: the runtime image contains
# platform specific JRE and JavaFX natives, and jpackage uses WiX to create the msi.
# Output: target/dist/Tuntikirjaus-<version>.msi
set -euo pipefail

cd -- "$(dirname -- "$0")/.."

RUNTIME_IMAGE=target/tuntikirjaus
DIST_DIR=target/dist

if [ ! -d "$RUNTIME_IMAGE" ]; then
    echo "Runtime image $RUNTIME_IMAGE not found, run 'mvn javafx:jlink' first" >&2
    exit 1
fi

# msi versions must be numeric (e.g. 1.1.9), so drop suffixes like -BETA or -SNAPSHOT
POM_VERSION=$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)
APP_VERSION=${POM_VERSION%%-*}

rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

# The upgrade uuid must stay the same between versions so that a new msi replaces the old installation
jpackage \
    --type msi \
    --name Tuntikirjaus \
    --app-version "$APP_VERSION" \
    --vendor "Janne Sirviö" \
    --runtime-image "$RUNTIME_IMAGE" \
    --module com.sirvja.tuntikirjaus/com.sirvja.tuntikirjaus.Launcher \
    --java-options "--enable-native-access=javafx.graphics,org.xerial.sqlitejdbc" \
    --icon buildResources/tuntikirjaus.ico \
    --win-upgrade-uuid 223afe7e-1649-43a9-880e-b16c31784fd0 \
    --win-per-user-install \
    --win-dir-chooser \
    --win-menu \
    --win-shortcut \
    --dest "$DIST_DIR"

echo "Created $(ls "$DIST_DIR"/*.msi)"

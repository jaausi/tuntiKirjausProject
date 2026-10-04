#!/bin/bash
# Builds a Debian/Ubuntu deb installer with jpackage from the jlink runtime image.
# Expects `mvn javafx:jlink` to have been run (target/tuntikirjaus exists).
# Must be run on Linux with dpkg-deb and fakeroot installed: the runtime image contains
# platform specific JRE and JavaFX natives.
# JavaFX needs GTK 3 at runtime, so it is declared as a dependency (package name differs between releases).
# Output: target/dist/tuntikirjaus_<version>_<arch>.deb (installs to /opt/tuntikirjaus)
set -euo pipefail

cd -- "$(dirname -- "$0")/.."

RUNTIME_IMAGE=target/tuntikirjaus
DIST_DIR=target/dist

if [ ! -d "$RUNTIME_IMAGE" ]; then
    echo "Runtime image $RUNTIME_IMAGE not found, run 'mvn javafx:jlink' first" >&2
    exit 1
fi

# jpackage accepts only numeric version components (e.g. 1.1.9), so drop suffixes like -BETA or -SNAPSHOT
POM_VERSION=$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)
APP_VERSION=${POM_VERSION%%-*}

rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

jpackage \
    --type deb \
    --name Tuntikirjaus \
    --app-version "$APP_VERSION" \
    --description "Vaivattomaan tuntien kirjaamiseen" \
    --vendor "Janne Sirviö" \
    --runtime-image "$RUNTIME_IMAGE" \
    --module com.sirvja.tuntikirjaus/com.sirvja.tuntikirjaus.Launcher \
    --java-options "--enable-native-access=javafx.graphics,org.xerial.sqlitejdbc" \
    --icon buildResources/tuntikirjausResized.png \
    --linux-package-name tuntikirjaus \
    --linux-deb-maintainer "jaausi@outlook.com" \
    --linux-package-deps "libgtk-3-0t64 | libgtk-3-0" \
    --linux-app-category office \
    --linux-menu-group "Office" \
    --linux-shortcut \
    --dest "$DIST_DIR"

echo "Created $(ls "$DIST_DIR"/*.deb)"

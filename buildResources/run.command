#!/bin/bash
# Inherit user's PATH so Selenium can find browser drivers (chromedriver, geckodriver etc.)
export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"

cd -- "$(dirname -- "$BASH_SOURCE")"
cd bin
./java -Xdock:icon=tuntikirjausResized.png --enable-native-access=javafx.graphics,org.xerial.sqlitejdbc -m com.sirvja.tuntikirjaus/com.sirvja.tuntikirjaus.Launcher
exit 0

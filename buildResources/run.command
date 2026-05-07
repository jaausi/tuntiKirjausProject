#!/bin/bash
# Aseta käyttäjän PATH jotta Selenium löytää selainajurit (chromedriver, geckodriver jne.)
# App bundle -ympäristössä PATH on rajoitettu, joten lisätään yleisimmät hakemistot
export PATH="/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/local/sbin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"

cd -- "$(dirname -- "$BASH_SOURCE")"
cd bin
./java -Xdock:icon=tuntikirjausResized.png --enable-native-access=javafx.graphics,org.xerial.sqlitejdbc -m com.sirvja.tuntikirjaus/com.sirvja.tuntikirjaus.Launcher
exit 0

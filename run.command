#!/bin/bash
cd -- "$(dirname -- "$BASH_SOURCE")"
cd bin
./java -Xdock:icon=tuntikirjausResized.png -m com.sirvja.tuntikirjaus/com.sirvja.tuntikirjaus.Launcher
exit 0

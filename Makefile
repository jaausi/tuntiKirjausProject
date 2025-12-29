build: build-jar
build-standalone: build-jar-with-jre

################################
# Update existing installation
################################
update: build-jar copy-jar-to-app-dir

################################
# Install application to home folder to be used with external jre
################################
install: build-jar create-app-dir-and-database-dir copy-jar-to-app-dir create-shell-alias

build-jar:
	mvn clean package

create-app-dir-and-database-dir:
	mkdir -pv ~/tuntikirjaus/database

copy-jar-to-app-dir:
	cp target/tuntiKirjaus-*-SNAPSHOT.jar ~/tuntikirjaus/tuntikirjaus.jar

create-shell-alias:
	echo "Create a shell alias for tuntikirjaus application: alias tuntikirjaus='cd ~/tuntikirjaus; java -jar ~/tuntikirjaus/tuntikirjaus.jar'"

################################
# Create mac compatible standalone application file
################################
build-mac-dmg: build-jar-with-jre copy-run-command-script create-app-bundle copy-built-binaries-to-app-bundle copy-modified-plist-file-to-app-bundle create-dmg-file-from-app-bundle
build-mac-dmg-with-clean: build-mac-dmg remove-app-bundle remove-built-resources

build-jar-with-jre:
	mvn clean javafx:jlink

copy-run-command-script:
	cp buildResources/run.command target/tuntikirjaus/run.command
	chmod +x target/tuntikirjaus/run.command
	cp buildResources/tuntikirjausResized.png target/tuntikirjaus/bin/

create-app-bundle:
	appify target/tuntikirjaus/run.command Tuntikirjaus.app buildResources/tuntikirjausResized.png

copy-built-binaries-to-app-bundle:
	cp -r target/tuntikirjaus/* Tuntikirjaus.app/Contents/MacOS/

copy-modified-plist-file-to-app-bundle:
	cp buildResources/Info.plist Tuntikirjaus.app/Contents/Info.plist

create-dmg-file-from-app-bundle:
	hdiutil create -volname Tuntikirjaus -srcfolder ./Tuntikirjaus.app -ov -format UDZO Tuntikirjaus.dmg

remove-app-bundle:
	rm -rf ./Tuntikirjaus.app

remove-built-resources:
	mvn clean

################################
# Create linux compatible application bundle
################################

build-linux-tar-package: build-jar create-linux-app-folder copy-stuff-to-linux-app-folder package-folder-into-tar-file
build-linux-tar-package-with-clean: build-linux-tar-package remove-app-folder remove-built-resources

create-linux-app-folder:
	mkdir ./tuntikirjaus-app

copy-stuff-to-linux-app-folder:
	cp target/tuntiKirjaus-*.jar ./tuntikirjaus-app/tuntikirjaus.jar
	cp buildResources/Makefile ./tuntikirjaus-app/Makefile
	cp buildResources/run.sh ./tuntikirjaus-app/run.sh
	cp buildResources/tuntikirjaus.desktop ./tuntikirjaus-app/tuntikirjaus.desktop
	cp buildResources/tuntikirjausResized.png ./tuntikirjaus-app/tuntikirjausResized.png

package-folder-into-tar-file:
	tar -czvf tuntikirjaus.tar.gz ./tuntikirjaus-app

remove-app-folder:
	rm -rf ./tuntikirjaus-app
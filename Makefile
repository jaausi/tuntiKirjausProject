build: build-jar
build-standalone: build-jar-with-jre

# Update existing installation
update: build-jar copy-jar-to-app-dir

# Install application to home folder to be used with external jre
install: build-jar create-app-dir-and-database-dir copy-jar-to-app-dir create-shell-alias

build-jar:
	mvn clean package

create-app-dir-and-database-dir:
	mkdir -pv ~/tuntikirjaus/database

copy-jar-to-app-dir:
	cp target/tuntiKirjaus-*-SNAPSHOT.jar ~/tuntikirjaus/tuntikirjaus.jar

create-shell-alias:
	echo "Create a shell alias for tuntikirjaus application: alias tuntikirjaus='cd ~/tuntikirjaus; java -jar ~/tuntikirjaus/tuntikirjaus.jar'"

# Create mac compatible standalone application file
build-mac-dmg: build-jar-with-jre copy-run-command-script create-app-bundle copy-built-binaries-to-app-bundle create-dmg-file-from-app-bundle
build-mac-dmg-with-clean: build-jar-with-jre copy-run-command-script create-app-bundle copy-built-binaries-to-app-bundle create-dmg-file-from-app-bundle remove-app-bundle remove-built-resources

build-jar-with-jre:
	mvn clean javafx:jlink

copy-run-command-script:
	cp run.command target/tuntikirjaus/run.command

create-app-bundle:
	appify target/tuntikirjaus/run.command TuntikirjausApp.app

copy-built-binaries-to-app-bundle:
	cp -r target/tuntikirjaus/* TuntikirjausApp.app/Contents/MacOS/

create-dmg-file-from-app-bundle:
	hdiutil create -volname TuntikirjausApp -srcfolder ./TuntikirjausApp.app -ov -format UDZO Tuntikirjaus.dmg

remove-app-bundle:
	rm -rf ./TuntikirjausApp.app

remove-built-resources:
	mvn clean

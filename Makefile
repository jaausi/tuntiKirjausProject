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

################################
# Test Kieku exporter against a fake Kieku page (requires Chrome)
################################
test-kieku:
	mvn test -Pbrowser-tests -Dtest=KiekuExporterFakePageTest

test-kieku-visible:
	mvn test -Pbrowser-tests -Dtest=KiekuExporterFakePageTest -Dkieku.headless=false -Dkieku.keepOpenMillis=10000

create-app-dir-and-database-dir:
	mkdir -pv ~/tuntikirjaus/database

copy-jar-to-app-dir:
	cp target/tuntiKirjaus-*.jar ~/tuntikirjaus/tuntikirjaus.jar

create-shell-alias:
	echo "Create a shell alias for tuntikirjaus application: alias tuntikirjaus='cd ~/tuntikirjaus; java -jar ~/tuntikirjaus/tuntikirjaus.jar'"

################################
# Create mac compatible standalone application file
################################
build-mac-dmg: build-jar-with-jre create-dmg-with-jpackage
build-mac-dmg-with-clean: build-mac-dmg copy-dmg-to-project-root remove-built-resources

build-jar-with-jre:
	mvn clean javafx:jlink

create-dmg-with-jpackage:
	./buildResources/build-mac-dmg.sh

copy-dmg-to-project-root:
	cp target/dist/Tuntikirjaus-*.dmg ./Tuntikirjaus.dmg

remove-built-resources:
	mvn clean

################################
# Create linux compatible application bundle
################################

# TODO
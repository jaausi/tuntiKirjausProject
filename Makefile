build: build-jar

install: build-jar create-app-dir-and-database-dir copy-jar-to-app-dir create-shell-alias

build-jar:
	mvn package

create-app-dir-and-database-dir:
	mkdir -pv ~/tuntikirjaus/database

copy-jar-to-app-dir:
	cp target/tuntiKirjaus-*-SNAPSHOT.jar ~/tuntikirjaus/tuntikirjaus.jar

create-shell-alias:
	echo "Create a shell alias for tuntikirjaus application: alias tuntikirjaus='cd ~/tuntikirjaus; java -jar ~/tuntikirjaus/tuntikirjaus.jar'"
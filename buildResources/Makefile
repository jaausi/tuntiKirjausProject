#############
# Installs Tuntikirjaus application to /opt/tuntikirjaus
#############

install: create-app-dir copy-jar-to-app-dir copy-icon-to-app-dir copy-run-script-to-app-dir create-desktop-file create-folder-for-user-data

create-app-dir:
	mkdir /opt/tuntikirjaus

copy-jar-to-app-dir:
	cp tuntikirjaus.jar /opt/tuntikirjaus/tuntikirjaus.jar

copy-icon-to-app-dir:
	cp tuntikirjausResized.png /opt/tuntikirjaus/tuntikirjausResized.png

copy-run-script-to-app-dir:
	cp run.sh /opt/tuntikirjaus/run.sh
	chmod +x run.sh

create-desktop-file:
	cp tuntikirjaus.desktop /usr/local/share/applications/tuntikirjaus.desktop

create-folder-for-user-data:
	mkdir ~/tuntikirjaus

############
# Uninstalls Tuntikirjaus application
############

uninstall: remove-desktop-file remove-jar-from-app-dir remove-app-dir conditionally-remove-user-data

remove-desktop-file:
	rm /usr/local/share/applications/tuntikirjaus.desktop

remove-jar-from-app-dir:
	rm /opt/tuntikirjaus/tuntikirjaus.jar

remove-app-dir:
	rmdir /opt/tuntikirjaus

conditionally-remove-user-data:
	@echo -n "Do you want to remove the user data also? [y/N]: " && read ans && if [ $${ans:-'Y'} = 'n' ]; then \
		printf $(_ERROR) "Not removing the user data" ; \
		exit 1 ; \
	else \
		printf $(_SUCCESS) "Removing the user data..." ; \
		rm -rf ~/tuntikirjaus \
		exit 0; \
	fi
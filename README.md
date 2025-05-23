# Tuntikirjaus system (Hour Recording system)

## General

The idea of this project came up of the headache to fill up worked hours to Tiima. Long story short, Tiima didn't support filling hours with the precision that was required in our company. With Tuntikirjaus Java FX application it is possible to save worked hours quickly and efficiently while working, and at the end of the day or at the beginning of next day hours can be exported to Tiima (for now manually, but in the future maybe automatically).

## How to Run the Application

Requires Java 21.

Run the project (if you don't want to lose fed data, leave out "clean")
```
mvn clean javafx:run  
```

## How to run the dependency check

Set you api key to environment variables to variable NVDAPIKEY.

Then run:
```
mvn verify
```

## How to Install the application

```
# This command:
#   1) builds new jar from sources, 
#   2) creates folder tuntikirjaus under user home dir, 
#   3) creates folder for database under tuntikirjaus folder, 
#   4) copies built jar to tuntikirjaus folder and 
#   5) gives alias to be saved in .bash_aliases file for easy use of application.
make install
```

## How to build executable package for Mac OS

```
# This command:
#   1) builds new jar from sources and includes jre in the package, 
#   2) creates app bundle, 
#   3) creates dmg-file from app bundle, 
#   4) removes temporary resources used to build the dmg-file 
make build-mac-dmg-with-clean
```

## How to Use the Application

### Normal Use

1. Press 'Uusi päivä' button at the start of your day to generate new day for hour records.
2. Set start time of your task that you are going to work on or have worked on.
3. Set the topic for the work that you did or do for the task. (If you hover over the field more specific instructions will appear)
4. Press 'Tallenna taulukkoon' when you want to save the record specified in the fields. (This step can be replaced by pressing the enter on the keyboard, when the 'Aihe' field is active.

<img width="904" alt="tuntiKirjaus_normalUse" src="https://user-images.githubusercontent.com/37043090/188315478-06a0e701-f454-48ab-a259-859a15a814c2.png">

Here is also a video of the normal use of the application:



https://user-images.githubusercontent.com/37043090/188315776-4488e235-9259-4a24-90da-83d7d24c9ec2.mov



### Reporting Hours

One frequently asked question was also that how many hours there was put in a certain task or a project. To make it easier to answer that question the reporting section was made. Through reporting section you can gather reports from the tasks that you have worked on.



https://user-images.githubusercontent.com/37043090/188315991-cd4d5319-2501-4c20-83d3-b1eaf5be827f.mov




## How to Develop the Application

If you want a new feature or discover a bug in application, create a freeform issue. If you want to implement a feature, an issue or a bugfix, create a pull request about the changes to be commited.

All created releases will launch a jar package build.

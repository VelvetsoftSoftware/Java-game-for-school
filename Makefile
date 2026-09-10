compile:
	if not exist bin mkdir bin
	javac -d bin main.java Window.java

run: compile
	java -cp bin main

jar: compile
	if not exist build mkdir build
	jar cfe build/test.jar main -C bin .

build: jar
	if exist release\testgame rmdir /s /q release\testgame
	if not exist release mkdir release
	jpackage --input build --name testgame --main-jar test.jar --main-class main --type app-image --dest release

clean:
	if exist bin rmdir /s /q bin
	if exist build rmdir /s /q build
	if exist release rmdir /s /q release
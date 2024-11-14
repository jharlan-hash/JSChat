#!/bin/bash

javac ./src/JarDrop.java ./src/Client.java ./src/Server.java
java -cp ./src/ JarDrop $1 $2 $3 $4
rm ./src/*.class> /dev/null 2>&1 

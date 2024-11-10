#!/bin/bash

rm ./src/*.class
javac ./src/JarDrop.java
java -cp ./src/ JarDrop $1

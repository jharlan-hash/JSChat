#!/bin/bash

javac ./src/*.java
java -cp ./src/ ChatUtils $1 $2 $3 $4
rm ./src/*.class> /dev/null 2>&1 

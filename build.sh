#!/bin/bash

javac ./src/chatUtils.java ./src/Client.java ./src/Server.java
java -cp ./src/ chatUtils $1 $2 $3 $4
rm ./src/*.class> /dev/null 2>&1 

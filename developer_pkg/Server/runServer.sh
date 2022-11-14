#!/bin/bash

set -e

javac -cp .:./lib/sqlite-jdbc-3.23.1.jar:./lib/json-simple-1.1.1.jar Server.java
java -cp .:./lib/sqlite-jdbc-3.23.1.jar:./lib/json-simple-1.1.1.jar Server

#!/bin/bash

echo "=== COMPILING ==="
javac RX.java
javac TX.java

echo "=== RUNNING ==="
java RX > output_java_RX.txt &
java TX > output_java_TX.txt 
kill -9 $!

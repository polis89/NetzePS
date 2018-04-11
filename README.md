# NetzePS

# To compile java 
javac TX.java
javac RX.java
# To run java
java TX
java RX

# To compile c
make RX
make TX
# To run c
./RX <port> <buffer size>
	default:
./RX 4711 512

./TX <port> <packet samount>
	default:
./TX 4711 100
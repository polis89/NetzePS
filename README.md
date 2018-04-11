# NetzePS

# To compile java 
javac RX.java
javac TX.java
# To run java
java RX <port> <buffer size>
	default:
	java RX 4711 1024
	maximal buffer size = 8192 bytes.
java TX <port> <packets amount> <delay>
	default:
	java TX 1000 0

# To compile c
make RX
make TX
# To run c
./RX <port> <buffer size>
	default:
./RX 4711 512

./TX <port> <packet samount> <send delay>
	default:
./TX 4711 100 0
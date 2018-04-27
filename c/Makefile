CC=gcc
CFLAGS=-I.

TX: TX.o
		$(CC) -o TX TX.o -lm

RX: RX.o
		$(CC) -o RX RX.o -I.

clean:
		rm -f *.o
		rm -f TX
		rm -f RX
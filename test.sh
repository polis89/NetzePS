#!/bin/bash

if [ $# -lt 5 ]
  then
    echo "Usage: test cc/cj/jc/jj port packet_amount buffer_size delay"
    exit 1
fi

echo "=== TEST ==="
if [ $1 = "cc" ]
  then
    RX="./RX $2 $4 &"
    TX="./TX $2 $3 $5"
elif [ $1 = "cj" ]
	then
    RX="./RX $2 $4 &"
    TX="java TX $2 $3 $5"
elif [ $1 = "jc" ]
	then
    RX="java RX $2 $4 &"
    TX="./TX $2 $3 $5"
elif [ $1 = "jj" ]
	then
    RX="java RX $2 $4 &"
    TX="java TX $2 $3 $5"
fi

echo "Packets Amount: $3"
echo "Buffer Size: $4"
echo "Delay: $5"
eval $RX > output_RX.txt
if [ $1 = "jc" ]
	then
		sleep 2
fi
eval $TX > output_TX.txt 
kill -9 $!

echo "=== RESULTS ==="
tail -n 3 output_RX.txt



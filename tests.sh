#!/bin/bash

# echo "=== COMPILING ==="
# javac RX.java
# javac TX.java
# make RX
# make TX

# echo "=== C to C tests ==="
# ./test.sh cc 4000 100 1024 0 
# wait
# ./test.sh cc 4000 1000 1024 0 
# wait
# ./test.sh cc 4000 10000 1024 0 
# wait

# echo "=== C to Java tests ==="
# ./test.sh cj 4000 100 1024 0 
# wait
# ./test.sh cj 4000 1000 1024 0 
# wait
# ./test.sh cj 4000 10000 1024 0 
# wait

# echo "=== Java to C tests ==="
# ./test.sh jc 4000 100 1024 0 
# wait
# ./test.sh jc 4000 1000 1024 0 
# wait
# ./test.sh jc 4000 10000 1024 0 
# wait

# echo "=== Java to Java tests ==="
# ./test.sh jj 4000 100 1024 0 
# wait
# ./test.sh jj 4000 1000 1024 0 
# wait
# ./test.sh jj 4000 10000 1024 0 
# wait
# for ((number=0;number < 10;number++))
# {
# 	java TX 4000 10000 2000
# 	sleep 1
# }

# TEST TX.c -> RX.c
echo "==================================================" >> presentation/c_to_c.txt
for ((number=0;number < 10;number++)){
	cd c/
	./RX 4700 4711 > outRX.txt &
	./TX 4700 4711 65000 100 50 "to_send_100kb.jpg" | tail -n 3 >> ../presentation/c_to_c.txt
	cd ../
	echo "===" >> presentation/c_to_c.txt
	echo $number
	sleep 2
}
echo "==================================================" >> presentation/c_to_c.txt

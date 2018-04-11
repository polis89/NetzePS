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
for ((number=0;number < 10;number++))
{
	java TX 4000 10000 2000
	sleep 1
}

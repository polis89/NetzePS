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
# echo "==================================================" >> presentation/c_to_c.txt
# for ((number=0;number < 5;number++)){
# 	cd c/
# 	./RX 4722 > outRX.txt &
# 	./TX "127.0.0.1" 4722 10000 50 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/c_to_c.txt
# 	cd ../
# 	echo "===" >> presentation/c_to_c.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/c_to_c.txt
# for ((number=0;number < 5;number++)){
# 	cd c/
# 	./RX 4722 > outRX.txt &
# 	./TX "127.0.0.1" 4722 65000 50 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/c_to_c.txt
# 	cd ../
# 	echo "===" >> presentation/c_to_c.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/c_to_c.txt

# TEST TX.c -> RX.java

# echo "./TX 4711 10000 200 to_send_100kb.jpg" >> presentation/c_to_java.txt
# echo "==================================================" >> presentation/c_to_java.txt
# for ((number=0;number < 3;number++)){
# 	cd java/
# 	java RX 4711 > outRX.txt &
# 	cd ../c
# 	sleep 5
# 	./TX "127.0.0.1" 4722 10000 200 "to_send_100kb.jpg" | tail -n 3 >> ../presentation/c_to_java.txt
# 	cd ../
# 	echo "===" >> presentation/c_to_java.txt
# 	echo $number
# 	sleep 3
# }
# echo "==================================================" >> presentation/c_to_java.txt

# echo "./TX 4711 10000 200 to_send_1mb.jpg" >> presentation/c_to_java.txt
# echo "==================================================" >> presentation/c_to_java.txt
# for ((number=0;number < 3;number++)){
# 	cd java/
# 	java RX 4711 > outRX.txt &
# 	cd ../c
# 	sleep 5
# 	./TX "127.0.0.1" 4722 10000 1000 "to_send_1mb.jpg" | tail -n 3 >> ../presentation/c_to_java.txt
# 	cd ../
# 	echo "===" >> presentation/c_to_java.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/c_to_java.txt

# echo "./TX 4711 65000 200 to_send_1mb.jpg" >> presentation/c_to_java.txt
# echo "==================================================" >> presentation/c_to_java.txt
# for ((number=0;number < 3;number++)){
# 	cd java/
# 	java RX 4711 > outRX.txt &
# 	cd ../c
# 	sleep 5
# 	./TX "127.0.0.1" 4722 65000 5000 "to_send_1mb.jpg" | tail -n 3 >> ../presentation/c_to_java.txt
# 	cd ../
# 	echo "===" >> presentation/c_to_java.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/c_to_java.txt

# echo "./TX 4711 10000 200 to_send_10mb.jpg" >> presentation/c_to_java.txt
# echo "==================================================" >> presentation/c_to_java.txt
# for ((number=0;number < 3;number++)){
# 	cd java/
# 	java RX 4711 > outRX.txt &
# 	cd ../c
# 	sleep 5
# 	./TX "127.0.0.1" 4722 10000 1000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/c_to_java.txt
# 	cd ../
# 	echo "===" >> presentation/c_to_java.txt
# 	echo $number
# 	sleep 15
# }
# echo "==================================================" >> presentation/c_to_java.txt

# echo "./TX 4711 65000 200 to_send_10mb.jpg" >> presentation/c_to_java.txt
# echo "==================================================" >> presentation/c_to_java.txt
# for ((number=0;number < 3;number++)){
# 	cd java/
# 	java RX 4711 > outRX.txt &
# 	cd ../c
# 	sleep 5
# 	./TX "127.0.0.1" 4722 65000 5000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/c_to_java.txt
# 	cd ../
# 	echo "===" >> presentation/c_to_java.txt
# 	echo $number
# 	sleep 15
# }
# echo "==================================================" >> presentation/c_to_java.txt

# TEST TX.java -> RX.c

# echo "TX  4711 10000 1000 to_send_100kb.jpg" >> presentation/java_to_c.txt
# echo "==================================================" >> presentation/java_to_c.txt
# for ((number=0;number < 3;number++)){
# 	cd c/
# 	./RX 4722 > outRX.txt &
# 	cd ../java
# 	sleep 2
# 	java TX "127.0.0.1" 4722 10000 1000 "to_send_100kb.jpg" | tail -n 3 >> ../presentation/java_to_c.txt
# 	cd ../
# 	echo "===" >> presentation/java_to_c.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/java_to_c.txt

# echo "TX  4711 10000 1000 to_send_1mb.jpg" >> presentation/java_to_c.txt
# echo "==================================================" >> presentation/java_to_c.txt
# for ((number=0;number < 3;number++)){
# 	cd c/
# 	./RX 4722 > outRX.txt &
# 	cd ../java
# 	sleep 2
# 	java TX "127.0.0.1" 4722 10000 1000 "to_send_1mb.jpg" | tail -n 3 >> ../presentation/java_to_c.txt
# 	cd ../
# 	echo "===" >> presentation/java_to_c.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/java_to_c.txt

# echo "TX  4711 65000 2000 to_send_1mb.jpg" >> presentation/java_to_c.txt
# echo "==================================================" >> presentation/java_to_c.txt
# for ((number=0;number < 3;number++)){
# 	cd c/
# 	./RX 4722 > outRX.txt &
# 	cd ../java
# 	sleep 2
# 	java TX "127.0.0.1" 4722 65000 2000 "to_send_1mb.jpg" | tail -n 3 >> ../presentation/java_to_c.txt
# 	cd ../
# 	echo "===" >> presentation/java_to_c.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/java_to_c.txt

# echo "TX  4711 10000 1000 to_send_10mb.jpg" >> presentation/java_to_c.txt
# echo "==================================================" >> presentation/java_to_c.txt
# for ((number=0;number < 3;number++)){
# 	cd c/
# 	./RX 4722 > outRX.txt &
# 	cd ../java
# 	sleep 2
# 	java TX "127.0.0.1" 4722 10000 1000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/java_to_c.txt
# 	cd ../
# 	echo "===" >> presentation/java_to_c.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/java_to_c.txt

# echo "TX  4711 65000 2000 to_send_10mb.jpg" >> presentation/java_to_c.txt
# echo "==================================================" >> presentation/java_to_c.txt
# for ((number=0;number < 3;number++)){
# 	cd c/
# 	./RX 4722 > outRX.txt &
# 	cd ../java
# 	sleep 2
# 	java TX "127.0.0.1" 4722 65000 2000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/java_to_c.txt
# 	cd ../
# 	echo "===" >> presentation/java_to_c.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/java_to_c.txt

# TEST TX.java -> RX.java

# echo "TX 4711 1000 1000 to_send_100kb.jpg" >> presentation/java_to_java.txt
# echo "==================================================" >> presentation/java_to_java.txt
# for ((number=0;number < 3;number++)){
# 	cd java/
# 	java RX 4711 > outRX.txt &
# 	sleep 2
# 	java TX "127.0.0.1" 4711 1000 1000 "to_send_100kb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
# 	cd ../
# 	# echo "===" >> presentation/java_to_java.txt
# 	echo $number
# 	sleep 5
# }
# echo "==================================================" >> presentation/java_to_java.txt

echo "TX 4711 10000 1000 to_send_100kb.jpg" >> presentation/java_to_java.txt
echo "==================================================" >> presentation/java_to_java.txt
for ((number=0;number < 3;number++)){
	cd java/
	java RX 4711 > outRX.txt &
	sleep 2
	java TX "127.0.0.1" 4711 10000 1000 "to_send_100kb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
	cd ../
	# echo "===" >> presentation/java_to_java.txt
	echo $number
	sleep 5
}
echo "==================================================" >> presentation/java_to_java.txt
echo "TX 4711 10000 1000 to_send_1mb.jpg" >> presentation/java_to_java.txt
echo "==================================================" >> presentation/java_to_java.txt
for ((number=0;number < 3;number++)){
	cd java/
	java RX 4711 > outRX.txt &
	sleep 2
	java TX "127.0.0.1" 4711 10000 1000 "to_send_1mb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
	cd ../
	# echo "===" >> presentation/java_to_java.txt
	echo $number
	sleep 5
}
echo "==================================================" >> presentation/java_to_java.txt
echo "TX 4711 65000 1000 to_send_1mb.jpg" >> presentation/java_to_java.txt
echo "==================================================" >> presentation/java_to_java.txt
for ((number=0;number < 3;number++)){
	cd java/
	java RX 4711 > outRX.txt &
	sleep 2
	java TX "127.0.0.1" 4711 65000 1000 "to_send_1mb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
	cd ../
	# echo "===" >> presentation/java_to_java.txt
	echo $number
	sleep 5
}
echo "==================================================" >> presentation/java_to_java.txt
echo "TX 4711 10000 1000 to_send_10mb.jpg" >> presentation/java_to_java.txt
echo "==================================================" >> presentation/java_to_java.txt
for ((number=0;number < 1;number++)){
	cd java/
	java RX 4711 > outRX.txt &
	sleep 2
	java TX "127.0.0.1" 4711 10000 1000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
	cd ../
	# echo "===" >> presentation/java_to_java.txt
	echo $number
	sleep 15
}
echo "==================================================" >> presentation/java_to_java.txt
echo "TX 4711 65000 1000 to_send_10mb.jpg" >> presentation/java_to_java.txt
echo "==================================================" >> presentation/java_to_java.txt
for ((number=0;number < 1;number++)){
	cd java/
	java RX 4711 > outRX.txt &
	sleep 2
	java TX "127.0.0.1" 4711 65000 1000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
	cd ../
	# echo "===" >> presentation/java_to_java.txt
	echo $number
}
echo "==================================================" >> presentation/java_to_java.txt

# echo "TX 4700 4711 65000 100 5000 to_send_10mb.jpg" >> presentation/java_to_java.txt
# echo "==================================================" >> presentation/java_to_java.txt
# for ((number=0;number < 5;number++)){
# 	cd java/
# 	java RX 4700 4711 > outRX.txt &
# 	sleep 2
# 	java TX 4700 4711 65000 100 5000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
# 	cd ../
# 	echo "===" >> presentation/java_to_java.txt
# 	echo $number
# 	sleep 30
# }
# echo "==================================================" >> presentation/java_to_java.txt

# echo "TX 4700 4711 65000 200 1000 to_send_10mb.jpg" >> presentation/java_to_java.txt
# echo "==================================================" >> presentation/java_to_java.txt
# for ((number=0;number < 5;number++)){
# 	cd java/
# 	java RX 4700 4711 > outRX.txt &
# 	sleep 2
# 	java TX 4700 4711 65000 200 1000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
# 	cd ../
# 	echo "===" >> presentation/java_to_java.txt
# 	echo $number
# 	sleep 30
# }
# echo "==================================================" >> presentation/java_to_java.txt

# echo "TX 4700 4711 65000 200 5000 to_send_10mb.jpg" >> presentation/java_to_java.txt
# echo "==================================================" >> presentation/java_to_java.txt
# for ((number=0;number < 5;number++)){
# 	cd java/
# 	java RX 4700 4711 > outRX.txt &
# 	sleep 2
# 	java TX 4700 4711 65000 200 5000 "to_send_10mb.jpg" | tail -n 3 >> ../presentation/java_to_java.txt
# 	cd ../
# 	echo "===" >> presentation/java_to_java.txt
# 	echo $number
# 	sleep 30
# }
# echo "==================================================" >> presentation/java_to_java.txt


# for ((number=0;number < 1;number++)){
# 	cd java/
# 	java RX 4700 4711 > outRX.txt &
# 	cd ../c
# 	sleep 5
# 	./TX 4700 4711 65000 200 5000 "to_send_10mb.jpg"  > outTX.txt
# 	cd ../
# 	echo "===" >> presentation/c_to_java.txt
# 	echo $number
# 	sleep 5
# }


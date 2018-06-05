/**
 * RX
 * Usage: RX <portTX> <portRX>
 *
 * @author Dmitrii Polianskii, Lukas Lamminger
 *
 */

#include <stdio.h> 
#include <stdlib.h> 
#include <arpa/inet.h>
#include <sys/socket.h>
#include <unistd.h>
#include <string.h>
#include <stdbool.h>
#include <math.h> //pow
#include <inttypes.h>
#include "libcrc-2.0/include/checksum.h"
#include "libcrc-2.0/include/crc32.c"

typedef struct{
    char *data;
    int size;
    bool recieved;
} Packet;

typedef struct{
    Packet **packets;
    int allocated;
} DataRecieved;

 
unsigned short portRX = 4711; //Default port
int bufferSize = 2048; //Length of buffer
int sock; //Socket descriptor
int summ = 0; //Summ recieve
int summLost = 0; //Summ lost
int packets_amount = -1; //-1 for unbestimmt
int packet_payload = -1; //-1 for unbestimmt
int dataSize = 0;
unsigned char *file_name = "output.jpg";
DataRecieved *dataRecieved;
uint32_t  crc_32_calc; //CRC32
unsigned char crc_32_val[4];

void die(char *s)
{
    perror(s);
    exit(1);
}

void checkCRC32(){
    unsigned char prev_byte;
    FILE *fp;
    int ch;

    prev_byte = 0;
    fp = fopen( file_name, "rb" );
    if ( fp != NULL ) {
        while( ( ch=fgetc( fp ) ) != EOF ) {
            crc_32_calc = update_crc_32( crc_32_calc, (unsigned char) ch);
            prev_byte = (unsigned char) ch;
        }
        fclose( fp );
    }

    else printf( "%s : cannot open file\n", file_name );

    crc_32_calc        ^= 0xffffffffL;

    printf( "crc_32_calc = 0x%08" PRIX32 "  \n", crc_32_calc);
    if(crc_32_val[4] == (unsigned char)crc_32_calc &&
        crc_32_val[3] == (unsigned char)(crc_32_calc >> 8) &&
        crc_32_val[2] == (unsigned char)(crc_32_calc >> 16) &&
        crc_32_val[1] == (unsigned char)(crc_32_calc >> 24) ){
        printf("crc_32 is correct\n");
    }else{
        printf("crc_32 is NOT correct\n");
    }
}

void assembleFile(){
    printf("\n=== Assemble file ===\n");
    dataSize += packet_payload * (packets_amount-1);
    printf("File Size: %d\n", dataSize);
    // char fileString[dataSize+1]; // for \0
    char *fileString = (char *)malloc((dataSize+1)*sizeof(unsigned char));    
    int packetSize = packet_payload;
    for(int i = 0; i < packets_amount; i++){
        if(i == packets_amount - 1)
            packetSize = dataSize - packet_payload * (packets_amount-1);
        for(int j = 0; j < packetSize; j++){
            fileString[i*packet_payload + j] = dataRecieved->packets[i]->data[j];
        }
        // printf("=== seq_num: %d ===\n", i);
        // printf("=== Sizeof packet %d ===\n", dataRecieved->packets[i]->size);
    }
    // printf("=== Last paket size %zu ===\n", strlen(dataRecieved->packets[packets_amount-1]->data));
    // printf("=== File size %zu ===\n", strlen(fileString));
    printf("CRC32: 0x%02" PRIX16 " 0x%02" PRIX16 "  0x%02" PRIX16 " 0x%02" PRIX16 "\n", fileString[dataSize-4], fileString[dataSize-3], fileString[dataSize-2], fileString[dataSize-1]);
    crc_32_val[1] = fileString[dataSize-4];
    crc_32_val[2] = fileString[dataSize-3];
    crc_32_val[3] = fileString[dataSize-2];
    crc_32_val[4] = fileString[dataSize-1];

    FILE *fileptr;
    fileptr = fopen(file_name, "w");
    fwrite(fileString, (dataSize-4) * sizeof(char), 1, fileptr); 
    // fprintf(fileptr,"%s",fileString); 
    fclose(fileptr); 
    printf("=== File Ready ===\n");
    checkCRC32();
}
 
int main(int argc, char *argv[])
{
    if (argc < 2)
        fputs ("usage: RX <portRX>\n", stderr);
    else {
        portRX = strtol(argv[1], NULL, 10);
    }
    struct sockaddr_in addr_me, addr_to_send; //Socket Address for Internet
    socklen_t addrlen = sizeof(addr_to_send); 
    int slen = sizeof(addr_me); 
    int recv_len;
    int seq_num;
    int i;
    unsigned char buf[bufferSize]; //Buffer to recieve
     
  	addr_me.sin_family = AF_INET;
	addr_me.sin_port = htons(portRX);
	addr_me.sin_addr.s_addr = htonl(INADDR_ANY);
    // addr_to_send.sin_family = AF_INET;
    // addr_to_send.sin_port = htons(portTX);
    // addr_to_send.sin_addr.s_addr = inet_addr("127.0.0.1");

    //Get new socket
    if ((sock=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1){
        die("on open error");
    }
     
    //bind socket to port
    if( bind(sock , (struct sockaddr*)&addr_me, sizeof(addr_me) ) == -1){
        die("bind");
    }

    //Make new DataBuffer
    int allocated = 50000;
    dataRecieved = (DataRecieved *) malloc (sizeof (DataRecieved));
    dataRecieved->allocated = allocated;
    dataRecieved->packets = (Packet **) calloc (dataRecieved->allocated, sizeof (Packet *));
    for(int i = 0; i < allocated; i++){
        Packet *p = (Packet *) malloc(sizeof(Packet));
        p->size = 0;
        p->recieved = false;
        dataRecieved->packets[i] = p;
    }

    //keep listening for data
    while(1)
    {
        fflush(stdout);
         
        //receive data
        if ((recv_len = recvfrom(sock, buf, bufferSize, 0, (struct sockaddr *) &addr_to_send, &addrlen)) == -1)
        {
            die("recvfrom()");
        }
         
        printf("=== RECIEVED ===\n");
        printf("got something %s\n",inet_ntoa(addr_to_send.sin_addr));
        printf("got something %d\n",addr_to_send.sin_port);
        if(packet_payload == -1)
            packet_payload = recv_len - 4;

        // printf("Length: %d\n", recv_len);

        printf("Datagram (first 10 bytes): ");
		for (i = 0; i < 10; i++)
		{
		    if (i > 0) printf(":");
		    printf("%02d", buf[i]);
		}
		printf("\n");

        unsigned char ack_data[5];   
        ack_data[0] = buf[0] & 0b01111111; //Remove LastFragmentbit
        ack_data[1] = buf[1];
        ack_data[2] = buf[2];
        ack_data[3] = buf[3];
        ack_data[4] = '\0';

            
        seq_num = buf[3] + buf[2] * 256 + buf[1] * pow(256.0, 2) + ack_data[0] * pow(256.0, 3);

        printf("Ack: %d\n", seq_num);

        if(seq_num >= dataRecieved->allocated){
            int newAlloc = (seq_num >= dataRecieved->allocated * 2) ? (seq_num + 200) : dataRecieved->allocated * 2;
            dataRecieved->allocated = newAlloc;
            dataRecieved->packets = (Packet **) realloc(dataRecieved->packets, dataRecieved->allocated * sizeof(Packet *));
        }

        int sent_bytes = sendto(sock, ack_data, 4, 0, (struct sockaddr*) &addr_to_send, addrlen);
        printf("Sendet bytes: %d\n", sent_bytes);


        if((buf[0] & 0b10000000) == 0b10000000){
            //Last Fragment
            packets_amount = seq_num + 1;
            dataSize = recv_len - 4;
            printf("Last Packet\n");
            printf("packets_amount: %d\n", packets_amount);
            printf("dataSize: %d\n", dataSize);
        }
        int packetSize = recv_len - 4;

        if(!dataRecieved->packets[seq_num]->recieved){
            dataRecieved->packets[seq_num]->size = packetSize;
            dataRecieved->packets[seq_num]->data = (char *)malloc((packetSize)*sizeof(char));
            for(int i = 0; i < packetSize; i++){
                dataRecieved->packets[seq_num]->data[i] = buf[i+4];
            }
            dataRecieved->packets[seq_num]->recieved = true;
            printf("Length: %d\n", recv_len);
            printf("Sequenz Nummer: %d\n", seq_num);
            printf("Packets recieved: %d\n", ++summ);
        }

        if(packets_amount == summ){
            //Send full pack
            // ack_data[0] = 0b11111111;

            // for(int i = 0; i < 4; i++){
            //     printf("Send full packet\n");
            //     // int sent_bytes = sendto(sock, ack_data, 4, 0, (const struct sockaddr*) &addr_to_send, slen);
            //     usleep(10);    
            // }

            assembleFile();

            break;
            for (int i = 0; i < packets_amount; i++){
                free(dataRecieved->packets[i]);
            }
            free(dataRecieved->packets);
            free(dataRecieved);
        }

    }
 
    return 0;
}
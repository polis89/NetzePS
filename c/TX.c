/**
 * TX
 * Usage: TX <portTX> <portRX> <packet_size> <packet_block_size> <send_delay> <file_name>
 * Default: TX 4700 4711 1000 100 0 to_send.jpg
 *
 * @author Dmitrii Polianskii, Lukas Lamminger
 *
 */

#include <stdio.h> 
#include <stdlib.h> 
#include <string.h> 
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <math.h> 
#include <stdbool.h>
#include <time.h>
#include <inttypes.h>
#include "libcrc-2.0/include/checksum.h"
#include "libcrc-2.0/include/crc32.c"

typedef struct{
    int seq_num;
    char *data;
} Packet;
 
//Default params
unsigned short portTX = 4700; //Default port
unsigned short portRX = 4711; //Default port
int packet_size = 1000; 
int packet_block_size = 100; //Amount of packets between delay 
int delay = 200; //Delay between blocks of packet
unsigned char *file_name = "to_send3.jpg";

int packet_payload, packets_amount; 
int sock, sock_for_acks; //Socket descriptors
long filelen;
Packet **packets_to_send;
uint32_t crc_32_val; //CRC32

struct sockaddr_in addr_me, addr_to_send; //Socket Address for Internet
int slen = sizeof(addr_to_send); 
int slenme = sizeof(addr_me); 
char *datagram;
unsigned char buf[1024]; //Buffer for Acks

// For Speed tests
struct timespec tstart={0,0}, tend={0,0};
double timeEnd;
double timeStart;
 
void die(char *s)
{
    perror(s);
    exit(1);
}

int getCRC32(){
    unsigned char prev_byte;
    FILE *fp;
    int ch;

    prev_byte = 0;
    fp = fopen( file_name, "rb" );
    if ( fp != NULL ) {
        while( ( ch=fgetc( fp ) ) != EOF ) {
            crc_32_val = update_crc_32( crc_32_val, (unsigned char) ch);
            prev_byte = (unsigned char) ch;
        }
        fclose( fp );
    }

    else printf( "%s : cannot open file\n", file_name );

    crc_32_val        ^= 0xffffffffL;

    printf( "CRC32              = 0x%08" PRIX32 "  /  %" PRIu32 "\n"
            , crc_32_val,         crc_32_val     );
    return 0;
}

void sendPacket(int index){
    printf("=== SEND ===\n");
    printf("seq_num: %d\n", index);

    unsigned int seq_num_0 = (index >> 24) & 0xFF;
    unsigned int seq_num_1 = (index >> 16) & 0xFF;
    unsigned int seq_num_2 = (index >> 8) & 0xFF;
    unsigned int seq_num_3 = index & 0xFF;
    int packet_size = packet_payload + 5; //1 bit for \0

    //Last fragment
    if(index == packets_amount-1){
        int chb = 0b10000000; //Bitmask
        seq_num_0 = seq_num_0 | chb; //Set first bit in 1
        packet_size = filelen - (packets_amount-1) * packet_payload + 5;
    }
    unsigned char data[packet_size];
    strcpy(data, "xxxx");
    for(int i = 0; i < packet_size; i++){
        data[i+4] = packets_to_send[index]->data[i];
    }

    data[0] = seq_num_0;
    data[1] = seq_num_1;
    data[2] = seq_num_2;
    data[3] = seq_num_3;

    // int i;
    // for (i = 0; i < 10; i++)
    // {
    //     if (i > 0) printf(":");
    //     printf("%02d", data[i]);
    // }
    // printf("\n");

    int sent_bytes = sendto(sock, data, packet_size-1, 0, (const struct sockaddr*) &addr_to_send, slen);

    printf("Packet gesendet. Bytes: %d\n", sent_bytes);
}

void sendFile(){
    //Acknowlegments array
    int packets_sended = 0;
    bool ack[packets_amount]; 
    for(int i = 0; i < packets_amount; i++){
        ack[i] = false;
    }

    int index_iterator = 0; //Index of next packet to send
    int attempt = 0;

    //Sending Loop
    while(packets_sended < packets_amount && attempt < 100){
        //Send block of packets loop
        int count = 0; 
        while(count < packet_block_size){
            //Find next non-sended packet
            bool loopEnd = false;
            while(ack[index_iterator]){
                index_iterator++;
                if(index_iterator == packets_amount){
                    index_iterator = 0;
                    loopEnd = true;
                    attempt++;
                    printf("Attempt: %d\n", attempt);
                    break;
                }
            }
            if(loopEnd)
                break;
            sendPacket(index_iterator);
            if(index_iterator == packets_amount - 1)
                break;
            index_iterator++;
            count++;
        }
        //Waiting for acks
        fd_set set;
        struct timeval tv;
        int delay_pro_packet = delay/packets_amount;
        int sec =  delay_pro_packet/1000000;
        tv.tv_sec = sec;
        tv.tv_usec = delay - sec*1000000;
        for(int i = 0; i < packets_amount; i++){
            if(packets_amount == packets_sended)
                break;
            FD_ZERO(&set); 
            FD_SET(sock_for_acks, &set); 
            int rv = select(sock_for_acks + 1, &set, NULL, NULL, &tv);
            if (rv != 0) {
                int nBytes = recvfrom(sock_for_acks, &buf, 1024, 0, (struct sockaddr *) &addr_me, &slenme);
                if(buf[0] == 0b11111111){
                    printf("Receive all\n");
                    break;
                }
                int received_seq_num = buf[3] + buf[2] * 256 + buf[1] * pow(256.0, 2) + buf[0] * pow(256.0, 3);
                printf("=== Ack: %d ===\n", received_seq_num);
                if(!ack[received_seq_num]){
                    ack[received_seq_num] = true;
                    packets_sended++;
                }
                printf("packets_sended: %d\n", packets_sended);
            }
        }
    }
}

void readFileInBuffer(){
    FILE *fileptr;

    fileptr = fopen(file_name, "rb");  // Open the file in binary mode
    fseek(fileptr, 0, SEEK_END);          // Jump to the end of the file
    filelen = ftell(fileptr);             // Get the current byte offset in the file
    rewind(fileptr);                      // Jump back to the beginning of the file
    getCRC32();
    printf("File size: %zu Bytes\n", filelen);
    filelen += 4; //For crc32
    packets_amount = 1 + filelen / packet_payload;
    packets_to_send = (Packet **) calloc (packets_amount, sizeof (Packet *));

    printf("Packets amount: %d\n", packets_amount);

    
    unsigned char *fileBuff = (char *)malloc((filelen)*sizeof(unsigned char));              
    int read = fread(fileBuff, 1, filelen-4, fileptr);
    fileBuff[filelen-1] = crc_32_val;
    fileBuff[filelen-2] = crc_32_val >> 8;
    fileBuff[filelen-3] = crc_32_val >> 16;
    fileBuff[filelen-4] = crc_32_val >> 24;
    printf("CRC32 for File: 0x%02" PRIX16 " 0x%02" PRIX16 " 0x%02" PRIX16 " 0x%02" PRIX16 "\n", fileBuff[filelen-4], fileBuff[filelen-3], fileBuff[filelen-2], fileBuff[filelen-1]);

    for(int i = 0; i < packets_amount; i++){
        Packet *p = (Packet *) malloc(sizeof(Packet));
        p->seq_num = i;
        p->data = (char *)malloc((packet_payload+1)*sizeof(char));
        for(int j = 0; j < packet_payload; j++){
            p->data[j] = fileBuff[i*packet_payload + j];
        }
        p->data[packet_payload] = '\0';
        if(i == packets_amount-1){
            p->data[(filelen%packet_payload)] = '\0';
        }
        packets_to_send[i] = p;
    }
    fclose(fileptr); // Close the file
}

void initUPDSocket(){
     
    addr_me.sin_family = AF_INET;
    addr_me.sin_port = htons(portTX);
    addr_me.sin_addr.s_addr = htonl(INADDR_ANY);

    addr_to_send.sin_family = AF_INET;
    addr_to_send.sin_port = htons(portRX);
    addr_to_send.sin_addr.s_addr = inet_addr("127.0.0.1");

    //Get new socket
    // AF_INET -> IPv4
    // SOCK_DGRAM -> datagram service
    // IPPROTO_UDP -> for UDP
    if ((sock=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
    {
        die("on open error");
    }
    if ((sock_for_acks=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
    {
        die("on open error");
    }
     
    //bind socket to port
    if( bind(sock_for_acks , (struct sockaddr*)&addr_me, sizeof(addr_me) ) == -1)
    {
        die("bind");
    }
}

void printPackets(){
    for(int i = 0; i < packets_amount; i++){
        printf("Packet seq_num: %d\n", packets_to_send[i]->seq_num);
        printf("Packet data: %s\n", packets_to_send[i]->data);
    }
}

double calculateSpeed() {
    double time = timeEnd - timeStart;

    return 8 * (filelen-4) / time / 1000000.0;
}

int main(int argc, char *argv[])
{
    switch(argc) {
        case 7:
            file_name = argv[6];
        case 6:
            portTX = strtol(argv[1], NULL, 10);
            portRX = strtol(argv[2], NULL, 10);
            packet_size = strtol(argv[3], NULL, 10);
            packet_block_size = strtol(argv[4], NULL, 10);
            // packetsAmount = strtol(argv[2], NULL, 10);
            delay = strtol(argv[5], NULL, 10);
            break;
        default : 
            fputs ("usage: TX <portTX> <portRX> <packet_size> <packet_block_size> <send_delay> <file_name>\n", stderr);
    }
    packet_payload = packet_size - 4; 

    readFileInBuffer();
    initUPDSocket();
    // printPackets();

    clock_gettime(CLOCK_MONOTONIC, &tstart);
    sendFile();
    clock_gettime(CLOCK_MONOTONIC, &tend);
    timeEnd = (double)tend.tv_sec + 1.0e-9*tend.tv_nsec;
    timeStart = (double)tstart.tv_sec + 1.0e-9*tstart.tv_nsec;
    double speed = calculateSpeed();
    printf("Time elapsed: %.2lf s\n", timeEnd - timeStart);
    printf("Communication speed: %.2lf Mbps\n", speed);
 
    return 0;
}
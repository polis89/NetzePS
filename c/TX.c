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

typedef struct{
    int seq_num;
    char *data;
} Packet;
 
unsigned char *file_name = "to_send.jpg"; //Default file name to send
unsigned short portTX = 4700; //Default port
unsigned short portRX = 4711; //Default port
int packet_size = 1000; //Default size in bytes
int packet_payload; 
int packet_block_size = 100; //Default packet block size 
int packets_amount; 
int delay = 200; //Delay between blocks of packet
int sock, sock_for_acks; //Socket descriptor
Packet **packets_to_send;


struct sockaddr_in addr_me, addr_to_send; //Socket Address for Internet
int slen = sizeof(addr_to_send); 
int slenme = sizeof(addr_me); 
char *datagram;
char buf[1024]; //Buffer for Acks
 
void die(char *s)
{
    perror(s);
    exit(1);
}

void sendPacket(int index){
    printf("=== SEND ===\n");
    printf("seq_num: %d\n", index);
    unsigned char data[5 + packet_payload];
    strcpy(data, "xxxx");
    strcat(data, packets_to_send[index]->data);

    unsigned int seq_num_0 = (index >> 24) & 0xFF;
    unsigned int seq_num_1 = (index >> 16) & 0xFF;
    unsigned int seq_num_2 = (index >> 8) & 0xFF;
    unsigned int seq_num_3 = index & 0xFF;
    //Last fragment bit
    if(index == packets_amount-1){
        int chb = 0b10000000; //Bitmask
        seq_num_0 = seq_num_0 | chb;
    }
    unsigned char seq_num[4] = {seq_num_0, seq_num_1, seq_num_2, seq_num_3};
    data[0] = seq_num_0;
    data[1] = seq_num_1;
    data[2] = seq_num_2;
    data[3] = seq_num_3;

    // printf("Sequenz Nummer: %02d %02d %02d %02d\n", seq_num_0, seq_num_1, seq_num_2,  seq_num_3);
    // printf("Datagram: ");

    int i;
    for (i = 0; i < 10; i++)
    {
        if (i > 0) printf(":");
        printf("%02d", data[i]);
    }
    printf("\n");


    int sent_bytes = sendto(sock, data, 4 + packet_payload, 0, (const struct sockaddr*) &addr_to_send, slen);

    // printf("Packet gesendet. Bytes: %d\n", sent_bytes);

    // usleep(delay);  //without this sleep, not all of the package are sent =(
}

void sendFile(){
    //Acknowlegments array
    int packets_sended = 0;
    bool ack[packets_amount]; 
    for(int i = 0; i < packets_amount; i++){
        ack[i] = false;
    }

    int index_iterator = 0; //Index of next packet to send
     
    //Sending Loop
    while(packets_sended < packets_amount){
        //Send block of packets loop
        printf("packets_sended: %d\n", packets_sended);
        int count = 0; 
        while(count < packet_block_size){
            //Find next non-sended packet
            bool loopEnd = false;
            while(ack[index_iterator]){
                index_iterator++;
                if(index_iterator == packets_amount){
                    index_iterator = 0;
                    printf("loopEnd = true");
                    loopEnd = true;
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
            if (rv == 0) {
                printf("%s\n", "timeout on response");
            } else {
                int nBytes = recvfrom(sock_for_acks, &buf, 1024, 0, (struct sockaddr *) &addr_me, &slenme);
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
    long filelen;

    fileptr = fopen(file_name, "rb");  // Open the file in binary mode
    fseek(fileptr, 0, SEEK_END);          // Jump to the end of the file
    filelen = ftell(fileptr);             // Get the current byte offset in the file
    rewind(fileptr);                      // Jump back to the beginning of the file

    packets_amount = 1 + filelen / packet_payload;
    packets_to_send = (Packet **) calloc (packets_amount, sizeof (Packet *));

    printf("packets_amount: %d\n", packets_amount);
    printf("filelen: %zu\n", filelen);
    printf("packet_payload: %d\n", packet_payload);

    for(int i = 0; i < packets_amount; i++){
        Packet *p = (Packet *) malloc(sizeof(Packet));
        p->seq_num = i;
        p->data = (char *)malloc((packet_payload+1)*sizeof(char));
        int read = fread(p->data, 1, packet_payload, fileptr);
        if(read < packet_payload){
            p->data[read] = '\0';
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

int main(int argc, char *argv[])
{
    switch(argc) {
        case 7:
            file_name = argv[5];
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
    sendFile();
 
    return 0;
}
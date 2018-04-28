/**
 * RX
 * Usage: RX <port> <buffer size>
 *
 * @author Dmitrii Polianskii, Lukas Lamminger
 *
 */

#include <stdio.h> 
#include <stdlib.h> 
#include <arpa/inet.h>
#include <sys/socket.h>
#include <string.h>
#include <math.h> //pow

typedef struct{
    char *data;
} Packet;

typedef struct{
    Packet **packets;
    int allocated;
} DataRecieved;

 
unsigned short portTX = 4700; //Default port
unsigned short portRX = 4711; //Default port
int bufferSize = 2048; //Length of buffer
int sock; //Socket descriptor
int summ = 0; //Summ recieve
int summLost = 0; //Summ lost
int packets_amount = -1; //-1 for unbestimmt
int packet_payload = -1; //-1 for unbestimmt
unsigned char *file_name = "output.txt";
DataRecieved *dataRecieved;

void die(char *s)
{
    perror(s);
    exit(1);
}

void assembleFile(){
    printf("=== assembleFile ===\n");
    int filesize = packet_payload * packets_amount;
    char fileString[filesize];
    for(int i = 0; i < packets_amount; i++){
        strcat(fileString, dataRecieved->packets[i]->data);
    }

    FILE *fileptr;
    fileptr = fopen(file_name, "w");
    fprintf(fileptr,"%s",fileString); 
    fclose(fileptr); 
}
 
int main(int argc, char *argv[])
{
    if (argc < 3)
        fputs ("usage: RX <portTX> <portRX>\n", stderr);
    else {
        portTX = strtol(argv[1], NULL, 10);
        portRX = strtol(argv[2], NULL, 10);
    }
    struct sockaddr_in addr_me, addr_to_send; //Socket Address for Internet
    int slen = sizeof(addr_me); 
    int recv_len;
    int seq_num;
    int i;
    unsigned char buf[bufferSize]; //Buffer to recieve
     
  	addr_me.sin_family = AF_INET;
	addr_me.sin_port = htons(portRX);
	addr_me.sin_addr.s_addr = htonl(INADDR_ANY);

    addr_to_send.sin_family = AF_INET;
    addr_to_send.sin_port = htons(portTX);
    addr_to_send.sin_addr.s_addr = inet_addr("127.0.0.1");

    //Get new socket
    // AF_INET -> IPv4
    // SOCK_DGRAM -> datagram service
    // IPPROTO_UDP -> for UDP
    if ((sock=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
    {
        die("on open error");
    }
     
    //bind socket to port
    if( bind(sock , (struct sockaddr*)&addr_me, sizeof(addr_me) ) == -1)
    {
        die("bind");
    }

    //Make new DataBuffer
    dataRecieved = (DataRecieved *) malloc (sizeof (DataRecieved));
    dataRecieved->allocated = 10000;
    dataRecieved->packets = (Packet **) calloc (dataRecieved->allocated, sizeof (Packet *));

    //keep listening for data
    while(1)
    {
        fflush(stdout);
         
        //receive data
        if ((recv_len = recvfrom(sock, buf, bufferSize, 0, (struct sockaddr *) &addr_me, &slen)) == -1)
        {
            die("recvfrom()");
        }
         
        printf("=== RECIEVED ===\n");

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

            
        seq_num = buf[3] + buf[2] * 256 + buf[1] * pow(256.0, 2) + ack_data[0] * pow(256.0, 3);

        if(seq_num >= dataRecieved->allocated){
            int newAlloc = (seq_num >= dataRecieved->allocated * 2) ? (seq_num + 200) : dataRecieved->allocated * 2;
            dataRecieved->allocated = newAlloc;
            dataRecieved->packets = (Packet **) realloc(dataRecieved->packets, dataRecieved->allocated * sizeof(Packet *));
        }
        Packet *p = (Packet *) malloc(sizeof(Packet));
        p->data = (char *)malloc((packet_payload+1)*sizeof(char));
        strncpy(p->data, buf + 4, packet_payload + 4);
        dataRecieved->packets[seq_num] = p;



        int sent_bytes = sendto(sock, ack_data, 4, 0, (const struct sockaddr*) &addr_to_send, slen);

        if((buf[0] & 0b10000000) == 0b10000000){
            //Last Fragment
            packets_amount = seq_num + 1;
            printf("Last Packet\n");
            printf("packets_amount: %d\n", packets_amount);
        }

		printf("Sequenz Nummer: %d\n", seq_num);

        printf("Packets recieved: %d\n", ++summ);

        if(packets_amount == summ){
            assembleFile();

            for (int i = 0; i < packets_amount; i++){
                free(dataRecieved->packets[i]);
            }
            free(dataRecieved->packets);
            free(dataRecieved);

            dataRecieved = (DataRecieved *) malloc (sizeof (DataRecieved));
            dataRecieved->allocated = 10000;
            dataRecieved->packets = (Packet **) calloc (dataRecieved->allocated, sizeof (Packet *));
            summ = 0;
            packets_amount = -1;
            packet_payload = -1;
        }

    }
 
    return 0;
}
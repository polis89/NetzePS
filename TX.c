#include<stdio.h> 
#include<stdlib.h> 
#include<string.h> 
#include<arpa/inet.h>
#include<sys/socket.h>
#include <math.h> //pow
 
#define BUFLEN 512  //Max length of buffer
#define PORT 4711   //The port on which to listen for incoming data
#define PACK_NUM 10000   //Number of packets
 
void die(char *s)
{
    perror(s);
    exit(1);
}
 
int main(void)
{
    struct sockaddr_in addr_me, addr_to_send; //Socket Address for Internet
    unsigned char *message = "Hello from TX.c";
    char *datagram;
    int count = 0;
    int slen = sizeof(addr_to_send); 
    int sock; //Socket descriptor
    int i;
     
    addr_me.sin_family = AF_INET;
    addr_me.sin_port = htons(INADDR_ANY);
    addr_me.sin_addr.s_addr = htonl(INADDR_ANY);

  	addr_to_send.sin_family = AF_INET;
	addr_to_send.sin_port = htons(PORT);
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
     
    //keep listening for data
    while(count < PACK_NUM)
    {
        printf("=========\n");
        printf("Sequenz Nummer: %d\n", count);

        unsigned char data[5 + strlen(message)];
        strcpy(data, "xxxx");
        strcat(data, message);

        unsigned int seq_num_0 = (count >> 24) & 0xFF;
        unsigned int seq_num_1 = (count >> 16) & 0xFF;
        unsigned int seq_num_2 = (count >> 8) & 0xFF;
        unsigned int seq_num_3 = count & 0xFF;
        unsigned char seq_num[4] = {seq_num_0, seq_num_1, seq_num_2, seq_num_3};
        data[0] = seq_num_0;
        data[1] = seq_num_1;
        data[2] = seq_num_2;
        data[3] = seq_num_3;

        // printf("Sequenz Nummer: %02d %02d %02d %02d\n", seq_num_0, seq_num_1, seq_num_2,  seq_num_3);
        printf("Datagram: ");

        for (i = 0; i < 4 + strlen(message); i++)
        {
            if (i > 0) printf(":");
            printf("%02d", data[i]);
        }
        printf("\n");


        int sent_bytes = sendto(sock, data, 4 + strlen(message), 0, (const struct sockaddr*) &addr_to_send, slen);

		printf("Packet gesendet. Bytes: %d\n", sent_bytes);

        count++;

        usleep(100);  //without this sleep, not all of the package are sent =(
    }
 
    return 0;
}
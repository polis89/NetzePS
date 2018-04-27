/**
 * RX
 * Usage: RX <port> <buffer size>
 *
 * @author Dmitrii Polianskii, Lukas Lamminger
 *
 */

#include<stdio.h> 
#include<stdlib.h> 
#include<arpa/inet.h>
#include<sys/socket.h>
#include <math.h> //pow
 
unsigned short portTX = 4700; //Default port
unsigned short portRX = 4711; //Default port
int bufferSize = 1024; //Length of buffer
int sock; //Socket descriptor
int summ = 0; //Summ recieve
int summLost = 0; //Summ lost

void die(char *s)
{
    perror(s);
    exit(1);
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
    int seq_num, seq_num_prev = 0;
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
     
    //keep listening for data
    while(1)
    {
		printf("=== RECIEVED ===\n");
        fflush(stdout);
         
        //receive data
        if ((recv_len = recvfrom(sock, buf, bufferSize, 0, (struct sockaddr *) &addr_me, &slen)) == -1)
        {
            die("recvfrom()");
        }
         
        // printf("Length: %d\n", recv_len);

        printf("Datagram: ");
		for (i = 0; i < recv_len; i++)
		{
		    if (i > 0) printf(":");
		    printf("%02d", buf[i]);
		}
		printf("\n");

        unsigned char ack_data[5];   
        ack_data[0] = buf[0];
        ack_data[1] = buf[1];
        ack_data[2] = buf[2];
        ack_data[3] = buf[3];
            
        int sent_bytes = sendto(sock, ack_data, 4, 0, (const struct sockaddr*) &addr_to_send, slen);

		seq_num = buf[3] + buf[2] * 256 + buf[1] * pow(256.0, 2) + buf[0] * pow(256.0, 3);

        if(seq_num_prev < seq_num && seq_num_prev + 1 != seq_num){
            //Lost of packet is detected only in the middle of the stream. 
            //If Lost occures at the end of the stream, its will not be detected.
            summLost += seq_num - seq_num_prev + 1;
        }
        seq_num_prev = seq_num;

		printf("Sequenz Nummer: %d\n", seq_num);
		// printf("Message: ");
		// for (i = 4; i < recv_len; i++)
		// {
		//     printf("%c", buf[i]);
		// }
		// printf("\n");

        printf("Packets recieved: %d\n", ++summ);
		// printf("Packets lost: %d\n", summLost);

    }
 
    return 0;
}
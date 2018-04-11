#include<stdio.h> 
#include<stdlib.h> 
#include<arpa/inet.h>
#include<sys/socket.h>
#include <math.h> //pow
 
unsigned short port = 4711; //Default port
int bufferSize = 512; //Length of buffer
int sock; //Socket descriptor

void die(char *s)
{
    perror(s);
    exit(1);
}
 
int main(int argc, char *argv[])
{
    if (argc < 3)
        fputs ("usage: RX <port> <buffer size>\n", stderr);
    else {
        port = strtol(argv[1], NULL, 10);
        bufferSize = strtol(argv[2], NULL, 10);
    }
    struct sockaddr_in addr; //Socket Address for Internet
    int slen = sizeof(addr); 
    int recv_len;
    int seq_num;
    int i;
    int summ = 0;
    unsigned char buf[bufferSize]; //Buffer to recieve
     
  	addr.sin_family = AF_INET;
	addr.sin_port = htons(port);
	addr.sin_addr.s_addr = htonl(INADDR_ANY);

    //Get new socket
    // AF_INET -> IPv4
    // SOCK_DGRAM -> datagram service
    // IPPROTO_UDP -> for UDP
    if ((sock=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
    {
        die("on open error");
    }
     
    //bind socket to port
    if( bind(sock , (struct sockaddr*)&addr, sizeof(addr) ) == -1)
    {
        die("bind");
    }
     
    //keep listening for data
    while(1)
    {
		printf("=========\n");
        fflush(stdout);
         
        //receive data
        if ((recv_len = recvfrom(sock, buf, bufferSize, 0, (struct sockaddr *) &addr, &slen)) == -1)
        {
            die("recvfrom()");
        }
         
        //print details of the client/peer and the data received
        printf("Length: %d\n", recv_len);


		for (i = 0; i < recv_len; i++)
		{
		    if (i > 0) printf(":");
		    printf("%02d", buf[i]);
		}
		printf("\n");

		seq_num = buf[3] + buf[2] * 256 + buf[1] * pow(256.0, 2) + buf[0] * pow(256.0, 3);

		printf("Sequenz Nummer: %d\n", seq_num);
		printf("Message: ");
		for (i = 4; i < recv_len; i++)
		{
		    printf("%c", buf[i]);
		}
		printf("\n");

		printf("Packets recieved: %d\n", ++summ);

    }
 
    return 0;
}

/**

 *

 * Usage: java TX <ipadress> <portRX> <packet_size> <send_delay> <file_name>

 * 

 *

 * @author Dmitrii Polianskii, Lukas Lamminger

 */




import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;

import java.io.DataInputStream;

import java.io.File;

import java.io.FileInputStream;



import java.io.IOException;



import java.net.*;



import java.util.*;

import java.util.Collections;





import java.util.concurrent.*;

import java.util.zip.CRC32;



class TX

{



  //Default params

  private static int portTX = 4700;

  private static int portRX = 4711;

  private static int msgsize = 1000;

  private static int delay = 500;
  
  private static String ip_adress = "127.0.0.1";

  private static String fileName = "BaseImage.jpg";



  // Acks

  private static boolean[] ack_seq;



  static ArrayList<DatagramPacket> packets;

  private static byte[] contentToTransmit;

  private static int anz_pakete;

  private static DatagramSocket dSocket, dSocketAck;

  private static InetAddress ia;





  public static void main( String[] args ) throws IOException, InterruptedException {

  

	 if (args.length > 0){

	   ip_adress = args[0];

	}
	  

    if (args.length > 1){

      portRX = Integer.parseInt(args[1]);

    }

    if (args.length > 2){

        msgsize = Integer.parseInt(args[2]);

      }
  
    if (args.length > 3){

        delay = Integer.parseInt(args[3]);

      }

    if (args.length > 4){

      fileName = args[4];

    }



    packets = new ArrayList<DatagramPacket>();

    dSocket = new DatagramSocket(portTX);

   // dSocketAck = new DatagramSocket(portTX);

    ia = InetAddress.getByName(ip_adress);

   

    byte[] fileContent = readFile();

    System.out.println("File size = " + fileContent.length);



    CRC32 crc = new CRC32();

    crc.update(fileContent);

    byte[] checksum = convertIntTo4Bytes(crc.getValue());



    System.out.print("CRC32: " + Arrays.toString(checksum));

    System.out.println(" / " + crc.getValue());



    contentToTransmit = new byte[fileContent.length + 4];

    System.arraycopy(fileContent, 0, contentToTransmit, 0, fileContent.length);

    System.arraycopy(checksum, 0, contentToTransmit, fileContent.length, 4);



   // System.out.println(" contentToTransmit length " + contentToTransmit.length);



    anz_pakete = 1 + contentToTransmit.length/(msgsize-4);  //-4 for seq_num

    //System.out.println(" anz_pakete " + anz_pakete);



    ack_seq = new boolean[anz_pakete];



    long start = System.nanoTime();

    sendFile();

    long end = System.nanoTime();

    long microseconds = TimeUnit.NANOSECONDS.toMicros(end - start);



    double speed = 8 * (double)(contentToTransmit.length-4) / microseconds ;

    System.out.println("File size: "+ (contentToTransmit.length-4) +" Bytes");

    System.out.printf("Time elapsed: %.2f s\n", microseconds / 1000000.0);

    System.out.printf("Communication speed: %.2f Mbps\n",speed);



  }



  private static void sendFile() throws IOException, InterruptedException{



    int packets_sended = 0;



    int index_iterator = 0; //Index of next packet to send

    int attempt = 0; 



    //Sending Loop

    while(packets_sended < anz_pakete && attempt < 100){
    	

        sendPacket(packets_sended);

        //Waiting for acks

        byte[] buf = new byte[1000];

        dSocket.setSoTimeout((delay/1000) > 0 ? delay/1000 : 1);   // set the timeout in millisecounds.


        while(true){        // recieve data until timeout

            try {

                DatagramPacket ack_packet = new DatagramPacket(buf, buf.length);

                dSocket.receive(ack_packet);

                DataInputStream ack_dis = new DataInputStream(new ByteArrayInputStream(ack_packet.getData())); 

                int ack_sequenznummer = ack_dis.readInt();

               System.out.println("=== Ack: " + ack_sequenznummer + " ===");

                if(ack_sequenznummer == packets_sended){

                    packets_sended++;
                    if(packets_sended == anz_pakete )
                      break;
                }

            }

            catch (SocketTimeoutException e) {
            	
            	
                // timeout exception.

                System.out.println("Timeout reached!!! " + e);

                break;

            }

        }

    }

  }

  



  private static void sendPacket(int index) throws IOException, InterruptedException{



  //  System.out.println( "=== Send ===" );

  //  System.out.println( "seq_num: " + index);

    byte[] sequenzNum = convertIntTo4Bytes(index); //Make sequense to 4 bytes

    int payloadSize = msgsize - 4;



    //Last packet

    if(index == anz_pakete-1){

      payloadSize = contentToTransmit.length - (msgsize - 4) * (anz_pakete - 1);

   //   System.out.println( "Last packet payload: " + payloadSize);

      sequenzNum[0] |= (1 << 7);

    //  System.out.println( "Last packet payload: " + payloadSize);

      // System.out.println(Arrays.toString(Arrays.copyOfRange(contentToTransmit, index * payloadSize, index * payloadSize + payloadSize)));

    }

    byte[] packetData = new byte[payloadSize + 4];

    //Copy seq_num in packet

    System.arraycopy(sequenzNum, 0, packetData, 0, 4); 

    //Copy data in packet

    System.arraycopy(contentToTransmit, index * (msgsize - 4), packetData, 4, payloadSize); 

    if(index == anz_pakete-1){

    //  System.out.println("CRC32 received:" + packetData[packetData.length - 4] + " "+ packetData[packetData.length - 3] + " " + packetData[packetData.length - 2] + " " + packetData[packetData.length - 1]);  

    }



    DatagramPacket packet = new DatagramPacket( packetData, packetData.length, ia, portRX ); 
   // packet.setPort(portTX);


    for(int i = 0; i < 10; i++) {

      if(i != 0)

      //  System.out.print(":");

      if(packetData[i] < 0) {

      //  System.out.print(packetData[i] + 256); //Fix 2-complement +256

      } else {

       // System.out.print(packetData[i]); 

      }

    }

   // System.out.println();

    dSocket.send( packet );

  }



  private static byte[] readFile() throws IOException{



    File file = new File(fileName);

   

    FileInputStream fis = new FileInputStream(file);

    //create FileInputStream which obtains input bytes from a file in a file system

    //FileInputStream is meant for reading streams of raw bytes such as image data. For reading streams of characters, consider using FileReader.



    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    byte[] buf = new byte[1024];

    try {

        for (int readNum; (readNum = fis.read(buf)) != -1;) 

        {

            //Writes to this byte array output stream

            bos.write(buf, 0, readNum); 

            

        }

    } catch (IOException ex) {

       

    }



    return bos.toByteArray();

  }



  private static byte[] convertIntTo4Bytes(long value){



    byte [] data = new byte[4];

    data[3] = (byte) value;

    data[2] = (byte) (value >>> 8);

    data[1] = (byte) (value >>> 16);

    data[0] = (byte) (value >>> 24);



    return data;



  }

}

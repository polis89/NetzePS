/**
 *
 * Usage: java TX <portTX> <portRX> <packet_size> <packet_block_size> <send_delay> <file_name>
 * Default: TX 4700 4711 1000 100 0 to_send.jpg
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
  private static int ack_port = 4700;
  private static int port = 4711;
  private static int msgsize = 1000;
  private static int packet_block_size = 100;
  private static int delay = 200;
  private static String fileName = "to_send.jpg";

  // Acks
  private static boolean[] ack_seq;

  static ArrayList<DatagramPacket> packets;
  static ArrayList<Byte> bytes;
  private static byte[] contentToTransmit;
  private static int anz_pakete;
  private static DatagramSocket dSocket, dSocketAck;
  private static InetAddress ia;


  public static void main( String[] args ) throws IOException, InterruptedException {
  
    if (args.length > 0){
      ack_port = Integer.parseInt(args[0]);
    }
  
    if (args.length > 1){
      port = Integer.parseInt(args[1]);
    }
  
    if (args.length > 2){
      msgsize = Integer.parseInt(args[2]);
    }
    
    if (args.length > 3){
      packet_block_size = Integer.parseInt(args[3]);
    }
    
    if (args.length > 4){
      delay = Integer.parseInt(args[4]);
    }

    if (args.length > 5){
      fileName = args[2];
    }

    packets = new ArrayList<DatagramPacket>();
    bytes = new ArrayList<Byte>();
    dSocket = new DatagramSocket();
    dSocketAck = new DatagramSocket(ack_port);
    ArrayList<Byte> bytes = new ArrayList<Byte>();
    ia = InetAddress.getByName( "127.0.0.1" );
   
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

    System.out.println(" contentToTransmit length " + contentToTransmit.length);

    anz_pakete = 1 + contentToTransmit.length/(msgsize-4);  //-4 for seq_num
    System.out.println(" anz_pakete " + anz_pakete);
    ack_seq = new boolean[anz_pakete];

    sendFile();
    //   long TimeEnd = System.currentTimeMillis();

  }

  private static void sendFile() throws IOException, InterruptedException{

    int packets_sended = 0;

    int index_iterator = 0; //Index of next packet to send
    int attempt = 0; 

    //Sending Loop
    while(packets_sended < anz_pakete && attempt < 100){
        //Send block of packets loop
        int count = 0; 
        while(count < packet_block_size){
            //Find next non-sended packet
            boolean loopEnd = false;
            while(ack_seq[index_iterator]){
                index_iterator++;
                if(index_iterator == anz_pakete){
                    index_iterator = 0;
                    loopEnd = true;
                    attempt++;
                    System.out.println("Attempt: "+ attempt);
                    break;
                }
            }
            if(loopEnd)
                break;
            sendPacket(index_iterator);
            if(index_iterator == anz_pakete - 1)
                break;
            index_iterator++;
            count++;
        }

        //Waiting for acks
        int delay_pro_packet = delay/anz_pakete;

        byte[] buf = new byte[1000];
        dSocketAck.setSoTimeout((delay/1000) > 0 ? delay/1000 : 1);   // set the timeout in millisecounds.

        while(true){        // recieve data until timeout
            try {
                DatagramPacket ack_packet = new DatagramPacket(buf, buf.length);
                dSocketAck.receive(ack_packet);
                DataInputStream ack_dis = new DataInputStream(new ByteArrayInputStream(ack_packet.getData())); 
                int ack_sequenznummer = ack_dis.readInt();
                System.out.println("=== Ack: " + ack_sequenznummer + " ===");
                if(!ack_seq[ack_sequenznummer]){
                    ack_seq[ack_sequenznummer] = true;
                    packets_sended++;
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

    System.out.println( "=== Send ===" );
    System.out.println( "seq_num: " + index);
    byte[] sequenzNum = convertIntTo4Bytes(index); //Make sequense to 4 bytes
    int payloadSize = msgsize - 4;

    //Last packet
    if(index == anz_pakete-1){
      payloadSize = contentToTransmit.length - (msgsize - 4) * (anz_pakete - 1);
      System.out.println( "Last packet payload: " + payloadSize);
      sequenzNum[0] |= (1 << 7);
    }
    byte[] packetData = new byte[payloadSize + 4];
    //Copy seq_num in packet
    System.arraycopy(sequenzNum, 0, packetData, 0, 4); 
    //Copy data in packet
    System.arraycopy(contentToTransmit, index * payloadSize, packetData, 4, payloadSize); 

    DatagramPacket packet = new DatagramPacket( packetData, packetData.length, ia, port ); 

    for(int i = 0; i < 10; i++) {
      if(i != 0)
        System.out.print(":");
      if(packetData[i] < 0) {
        System.out.print(packetData[i] + 256); //Fix 2-complement +256
      } else {
        System.out.print(packetData[i]); 
      }
    }
    System.out.println();
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

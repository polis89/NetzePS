/**

 * TX

 * Usage: java TX <port> <packets amount> <delay>

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


import java.util.concurrent.*;
import java.util.zip.CRC32;

class TX
{

  private static int port = 4711;
  
  private static int ack_port = 4712;

  private static int delay = 0;
  
  private static int packet_block_size = 10;
  
  private static int msgsize = 15;
  
  private static boolean[] ack_seq;
  
  static ArrayList<DatagramPacket> packets;


  public static void main( String[] args ) throws IOException, InterruptedException
  {
	 packets = new ArrayList<DatagramPacket>();
	ArrayList<Byte> bytes = new ArrayList<Byte>();
	long TotalByteAmount = 0;
	long TotalTime = 0;
	
	
    if (args.length > 0)
    {
      port = Integer.parseInt(args[0]);
    }
    
    if (args.length > 1)
    {
      delay = Integer.parseInt(args[2]);
    }

    if (args.length > 2)
    {
    	packet_block_size = Integer.parseInt(args[2]);
    }
    
    if (args.length > 3)
    {
    	msgsize = Integer.parseInt(args[2]);
    }
    
    String s = "BaseImage.jpg";
	File file = new File(s);
	 
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

    byte[] fileContent = bos.toByteArray();
   
	
    InetAddress ia = InetAddress.getByName( "127.0.0.1" );


    int fileContent_count = 0;
    long count = 0;

    int anz_pakete = fileContent.length/(msgsize-4);  //

    ack_seq = new boolean[anz_pakete];
    
    while ( count < anz_pakete )
    {
      long TimeBegin = System.currentTimeMillis();
      System.out.println( "======" );
      

      byte[] raw = new byte[msgsize-4];
      for(int i = 0 ; i<raw.length ;i++)
      {
    	  raw[i] = fileContent[fileContent_count+i];
    	  
    	  
      }
      fileContent_count += msgsize-4;


      byte[] data;
      
      
      data = new byte[raw.length + 4]; //added bytes for Sequenznummer
   
      TotalByteAmount += data.length;

      byte[] sequenzNum = convertIntTo4Bytes(count); //Make sequense to 4 bytes
      
     
      System.out.println("Sequenz Nummer: " + count);


      for(int i = 0; i < sequenzNum.length; i++)
      {

        data[i] = sequenzNum[i]; //In output these bytes can be with '-' sign due Java convention. But in reality they are plain bytes

      }

      if(count!=anz_pakete-1)
    	  data[0] = 0;
      if(count==anz_pakete-1)
    	  data[0] = 1;
      if(count!=anz_pakete-1)
      {
	      for(int i = 0; i < raw.length; i++)
	      {
	        data[i+4] = raw[i];
	        bytes.add(raw[i]);     
	      }
      }
      if(count==anz_pakete-1)
      {
    	  
    	  for(int i = 0; i < raw.length-4; i++)
	      {
	        data[i+4] = raw[i];
	        bytes.add(raw[i]);     
	      }
    	  
    	  byte[] bytes_arr = new byte[bytes.size()];
    	  
    	  for(int k = 0 ;k <bytes_arr.length;k++)
    	  {
    		  bytes_arr[k] = bytes.get(k);
    	  }

    	  CRC32 crc = new CRC32();
    	  crc.update(bytes_arr);
    	  byte[] checksum = convertIntTo4Bytes(crc.getValue());
  
    	  for(int i = 0; i < 4; i++)
          {

            data[data.length-4+i] = checksum[i];

          }
      }
      
 
      DatagramPacket packet = new DatagramPacket( data, data.length, ia, port ); 

      packets.add(packet);
      
      
      
      System.out.print( "DatagramPacket: ");

      for(int i = 0; i < data.length; i++)
      {
    
        if(i != 0)

          System.out.print(":");

        if(data[i] < 0)
        {
          System.out.print(data[i]); //Fix 2-complement +256
          
        }
        else
        {
          System.out.print(data[i]); 
          
        }
      }

      System.out.println();

      DatagramSocket dSocket = new DatagramSocket(port);

      dSocket.send( packet );

      System.out.println( "Paket gesendet" );
      long TimeEnd = System.currentTimeMillis();
      TotalTime += TimeEnd - TimeBegin;
      if(count==anz_pakete-1)
      {
    	  System.out.println("Speed:" + (TotalByteAmount/TotalTime) + " B/ms");
    	  for(int i = 0;i<ack_seq.length;i++)
    	  {
    		  if(ack_seq[i]==false)
    			  dSocket.send(packets.get(i));
    	  }
      }

      count++;
      
      
      
      DatagramPacket ack_packet = new DatagramPacket(buf, buf.length);
      
      if(count%packet_block_size == 0) 
      {
    	  DatagramSocket Socket = new DatagramSocket(ack_port);
    	  TimeUnit.MICROSECONDS.sleep(delay);

		 Socket.receive(ack_packet);
		  
		  DataInputStream ack_dis = new DataInputStream(new ByteArrayInputStream(ack_packet.getData())); 
	      
		  int ack_sequenznummer = ack_dis.readInt();
		  
		  ack_seq[ack_sequenznummer] = true;
      }
    }

  }



  private static byte[] convertIntTo4Bytes(long value)
  {

    byte [] data = new byte[4];

    data[3] = (byte) value;

    data[2] = (byte) (value >>> 8);

    data[1] = (byte) (value >>> 16);

    data[0] = (byte) (value >>> 24);

    return data;

  }

}

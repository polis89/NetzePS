/**
 * TX
 * Usage: java TX <port> <packets amount> <delay>
 *
 * @author Dmitrii Polianskii, Lukas Lamminger
 */

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.*;
import java.util.concurrent.*;


class TX
{
  private static int port = 4711;
  private static int packetsAmount = 1000;
  private static int delay = 0;

  public static void main( String[] args ) throws IOException, InterruptedException
  {


    if (args.length > 0){
      port = Integer.parseInt(args[0]);
    }
    if (args.length > 1){
      packetsAmount = Integer.parseInt(args[1]);
    }
    if (args.length > 2){
      delay = Integer.parseInt(args[2]);
    }

    InetAddress ia = InetAddress.getByName( "127.0.0.1" );

    long count = 0;
    int anz_pakete = packetsAmount;
    
    while ( count < anz_pakete )
    {
      System.out.println( "======" );
      
      String s = "Hello frome TX.java"; //String '01010' represents not bites, but just a string
      byte[] raw = s.getBytes();
      byte[] data = new byte[raw.length + 4]; //added bytes for Sequenznummer
      byte[] sequenzNum = convertIntTo4Bytes(count); //Make sequense to 4 bytes

      System.out.println("Sequenz Nummer: " + count);


      for(int i = 0; i < sequenzNum.length; i++){
        data[i] = sequenzNum[i]; //In output these bytes can be with '-' sign due Java convention. But in reality they are plain bytes
      }
      for(int i = 0; i < raw.length; i++){
        data[i+4] = raw[i];
      }


      DatagramPacket packet = new DatagramPacket( data, data.length, ia, port ); 
      System.out.print( "DatagramPacket: ");
      for(int i = 0; i < data.length; i++){
        if(i != 0)
          System.out.print(":");
        if(data[i] < 0)
          System.out.print(data[i] + 256); //Fix 2-complement
        else
          System.out.print(data[i]); 
      }
      System.out.println();

      DatagramSocket dSocket = new DatagramSocket();

      dSocket.send( packet );

      System.out.println( "Paket gesendet" );
      count++;
      TimeUnit.MICROSECONDS.sleep(delay);

      
    }
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


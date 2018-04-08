// package TX_j;

import java.io.IOException;
import java.net.*;
import java.util.*;


//Parameter sollen irgendwo eingegeben werden koennen
//byte->string bzw umgekehrt fuer C
//Messen?
class TX
{
  private static final int PORT = 4711;
  private static final int DEFAULT_PACKETS_AMOUNT = 1000;

  public static void main( String[] args ) throws IOException, InterruptedException
  {
    InetAddress ia = InetAddress.getByName( "127.0.0.1" );
    
    long count = 0;
    int anz_pakete = DEFAULT_PACKETS_AMOUNT;
    
    while ( count < anz_pakete )
    {
      System.out.println( "======" );
      
      String s = "Hello world"; //String '01010' represents not bites, but just a string
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


      DatagramPacket packet = new DatagramPacket( data, data.length, ia, PORT ); 
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

      
    }
  }

  private static byte[] convertIntTo4Bytes(long value){
    byte [] data = new byte[4];
    data[3] = (byte) value;
    data[2] = (byte) (value >>> 8);
    data[1] = (byte) (value >>> 16);
    data[0] = (byte) (value >>> 32);
    return data;
  }
}


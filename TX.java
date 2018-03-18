// package TX_j;

import java.io.IOException;
import java.net.*;
import java.util.*;


//Parameter sollen irgendwo eingegeben werden koennen
//byte->string bzw umgekehrt fuer C
//Messen?
class TX
{
  public static void main( String[] args ) throws IOException,
                  InterruptedException
  {
    InetAddress ia = InetAddress.getByName( "127.0.0.1" );
    
    int count = 0;
    int anz_pakete= 100;
    
    while ( count<anz_pakete )
    {
      String s = "00101";
      byte[] raw = s.getBytes();
     

      DatagramPacket packet = new DatagramPacket( raw, raw.length, ia, 4711 );

      DatagramSocket dSocket = new DatagramSocket();

      dSocket.send( packet );

      System.out.println( "Paket gesendet" );
      count++;

      
    }
  }
}


/**
 * RX
 * Usage: java RX <port> <buffer size>
 *
 * @author Dmitrii Polianskii, Lukas Lamminger
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class RX{
	private static final int MAX_BUF_SIZE = 8192;
  private static int port = 4711; //Default port
	private static byte[] buf = new byte[1024]; //Default buffer

	public static void main(String[] args) throws IOException, InterruptedException{

		if (args.length > 0){
			port = Integer.parseInt(args[0]);
		}
		if (args.length > 1){
			int size = Integer.parseInt(args[1]);
			if(size > MAX_BUF_SIZE){
				size = MAX_BUF_SIZE;
			}
			buf = new byte[size];
		}

		DatagramSocket socket = new DatagramSocket(port);
		// int packetSize = s.getBytes().length + 4;
		// byte[] buf = new byte[packetSize];

		long count = 0;
		long lostPackets = 0;
		int lastSeqNum = 0;
		//Listen Loop
		while(true){
			System.out.println("==========");
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData())); //Make input stream from datagramm
			int sequenzeNumInt = dis.readInt();
			System.out.println("Sequenz Nummer: " + sequenzeNumInt);
      if(lastSeqNum < sequenzeNumInt && lastSeqNum + 1 != sequenzeNumInt){
          //Lost of packet is detected only in the middle of the stream. 
          //If Lost occures at the end of the stream, its will not be detected.
          lostPackets += sequenzeNumInt - lastSeqNum + 1;
      }
      lastSeqNum = sequenzeNumInt;
			StringBuilder sb = new StringBuilder();
			while(dis.available() > 0){
				sb.append((char)dis.readByte());
			}
			System.out.println("Data: " + sb.toString());
			System.out.println("Recieved: " + ++count + " packets.");
			System.out.println("Lost: " + lostPackets + " packets.");

		}
	}
}
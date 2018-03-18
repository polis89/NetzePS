
import java.io.*;
import java.net.*;
import java.util.*;

public class RX{
  private static final int PORT = 4711;

	public static void main(String[] args) throws IOException, InterruptedException{
		// Vielleicht muss man packets size auch durch args einsetzen
		String s = "Hello world";
		DatagramSocket socket = new DatagramSocket(PORT);
		int packetSize = s.getBytes().length + 4;
		byte[] buf = new byte[packetSize];

		long count = 0;
		//Listen Loop
		while(true){
			System.out.println("==========");
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData())); //Make input stream from datagramm
			int sequenzeNumInt = dis.readInt();
			System.out.println("Sequense num: " + sequenzeNumInt);
			StringBuilder sb = new StringBuilder();
			while(dis.available() > 0){
				sb.append((char)dis.readByte());
			}
			System.out.println("Data: " + sb.toString());
			System.out.println("Recieved: " + ++count + " packets.");

		}
	}
}
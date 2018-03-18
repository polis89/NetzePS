
import java.io.IOException;
import java.net.*;
import java.util.*;

public class RX{
  private static final int PORT = 4711;

	public static void main(String[] args) throws IOException, InterruptedException{
		// Vielleicht muss man packets size auch durch args einsetzen
		String s = "Hello world";
		DatagramSocket socket = new DatagramSocket(PORT);
		byte[] buf = new byte[s.getBytes().length + 4];

		//Listen Loop
		while(true){
			DatagramPacket packet = packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			System.out.println(Arrays.toString(packet.getData()));
		}
	}
}
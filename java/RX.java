/**
 * RX
 * Usage: RX <portTX> <portRX>
 *
 * @author Dmitrii Polianskii, Lukas Lamminger
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class RX{
	private static final int MAX_BUF_SIZE = 8192;
  private static int portAck = 4700; //Default port to send acks
  private static int port = 4711; //Default port
	private static byte[] buf = new byte[1_000_000]; //Default buffer
	private static HashMap<Integer, ArrayList<Byte>> recievedData = new HashMap<>();
	private static ArrayList<Boolean> acks = new ArrayList<>();
	private static int packets_amount = -1; //-1 for unbestimmt
	private static int packet_payload = -1; //-1 for unbestimmt

  private static InetAddress ia;

	public static void main(String[] args) throws IOException, InterruptedException{

		ia = InetAddress.getByName( "127.0.0.1" );

		if (args.length > 0){
			portAck = Integer.parseInt(args[0]);
		}
		if (args.length > 1){
			port = Integer.parseInt(args[1]);
		}

		DatagramSocket socket = new DatagramSocket(port);
		DatagramSocket dSocketAck = new DatagramSocket();
		// int packetSize = s.getBytes().length + 4;
		// byte[] buf = new byte[packetSize];

		long count = 0;
		long packet_recieved = 0;

		//Listen Loop
		while(packets_amount != packet_recieved){
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			System.out.println("=== Recieve ===");

			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData())); //Make input stream from datagramm
			int sequenzeNumInt = dis.readInt();

			if(sequenzeNumInt < 0){ //with 2-complement first bit makes int negative
				System.out.println("Last packet");
      	sequenzeNumInt &= (0b0111111111111111); //Remove first bit
      	packets_amount = sequenzeNumInt + 1;
			}else if (packet_payload < 0){
				// packet_payload unbestimmt
				packet_payload = packet.getLength() - 4;
			}
			System.out.println("Sequenz Nummer: " + sequenzeNumInt);
			if(acks.size() <= sequenzeNumInt){
				int size_ack = acks.size();
				for(int i = 0; i < sequenzeNumInt - size_ack + 1;i++)
					acks.add(false);
			}
			if(!acks.get(sequenzeNumInt)){
				acks.set(sequenzeNumInt, true);
				packet_recieved++;
				ArrayList<Byte> dataFromDatagram = new ArrayList<>();
				for(int i = 0; i < packet.getLength() - 4; i++){
					dataFromDatagram.add(dis.readByte());
				}
				recievedData.put(sequenzeNumInt, dataFromDatagram);
			}
			byte[] axkData = convertIntTo4Bytes(sequenzeNumInt);
    	DatagramPacket ack_packet = new DatagramPacket( axkData, axkData.length, ia, portAck ); 
    	dSocketAck.send( ack_packet );

		}

		System.out.println("packet_payload: " + packet_payload);
		System.out.println("recievedData size (in packets): " + recievedData.size());

		assembleFile();
	}

  private static byte[] convertIntTo4Bytes(long value){

    byte [] data = new byte[4];
    data[3] = (byte) value;
    data[2] = (byte) (value >>> 8);
    data[1] = (byte) (value >>> 16);
    data[0] = (byte) (value >>> 24);

    return data;

  }

  private static void assembleFile(){


  }
}
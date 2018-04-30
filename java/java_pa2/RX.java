import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;

import java.net.*;

import java.util.*;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;



public class RX{

	private static final int MAX_BUF_SIZE = 8192;

  private static int port = 4711; //Default port
  
  private static int ack_port = 4700;

  private static byte[] buf = new byte[1024]; //Default buffer
	
  static int packets_amount = -1; //-1 for unbestimmt
  
  static int summ = 0; //Summ recieve
  
  static ArrayList<ReceivedData> DataReceived = new ArrayList<ReceivedData>();

	public static void main(String[] args) throws IOException, InterruptedException
	{
		
		if (args.length > 0)
		{

			port = Integer.parseInt(args[0]);

		}

		if (args.length > 1)
		{

			int size = Integer.parseInt(args[1]);

			if(size > MAX_BUF_SIZE){

				size = MAX_BUF_SIZE;

			}

			buf = new byte[size];
		}

		ArrayList<Byte> Image = new ArrayList<Byte>();
	

		DatagramSocket socket = new DatagramSocket(port);

		// int packetSize = s.getBytes().length + 4;

		// byte[] buf = new byte[packetSize];

		long count = 0;

		long lostPackets = 0;

		int lastSeqNum = 0;

		//Listen Loop

	
		ArrayList<Integer> MsgBinary = new ArrayList<Integer>();
		InetAddress ia = InetAddress.getByName( "127.0.0.1" );
		while(true)
		{
			 
			 System.out.println("==========");

			 DatagramPacket packet = new DatagramPacket(buf, buf.length);

			 socket.receive(packet);
			 
			 summ++;

			 DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData())); //Make input stream from datagramm

			 System.out.println(packet.getLength());
			 byte[] temp = packet.getData();
			 byte[] data = new byte[temp.length-4];
			 for(int i = 0;i<data.length;i++)
			 {
				 data[i] = temp[i+4];
			 }
			 int sequenzeNumInt = dis.readInt();
		 
			 
			 byte[] sequenzeNummer_buf = convertIntTo4Bytes(sequenzeNumInt);
			 
			 byte[] ack_data = new byte[5];   
			 
			 ack_data[0] = (byte) (sequenzeNummer_buf[0] & 0b01111111); //Remove LastFragmentbit

		     ack_data[1] = sequenzeNummer_buf[1];

		     ack_data[2] = sequenzeNummer_buf[2];

		     ack_data[3] = sequenzeNummer_buf[3];

		     ack_data[4] = '\0';

		     DatagramPacket seq_ack = new DatagramPacket(ack_data,ack_data.length,ia,ack_port);
		     
			 socket.send(seq_ack);
				
			 sequenzeNumInt = fromByteArray(ack_data);
			 			 
			 ReceivedData d = new ReceivedData(sequenzeNumInt,data);
			 DataReceived.add(d);	
					 
			 if((sequenzeNummer_buf[0] & 0b10000000) == 0b10000000)
			 {
					packets_amount = sequenzeNumInt  + 1;
			 }	
			 
			 System.out.println("Sequenz Nummer: " + sequenzeNumInt);

		      if(lastSeqNum < sequenzeNumInt && lastSeqNum + 1 != sequenzeNumInt)
		      {
		
		          lostPackets += sequenzeNumInt - lastSeqNum + 1;
		
		      }
		
		      lastSeqNum = sequenzeNumInt;
				 
			 System.out.print("Data: "  );
			 
			 System.out.println("");
			
			System.out.println("Recieved: " + ((++count)-1) + " packets.");
		
			System.out.println("Lost: " + lostPackets + " packets.");
		
			
			
				if(packets_amount == summ)
				{
					Collections.sort(DataReceived);
					List<byte[]> img_al = new ArrayList<byte[]>();
					
					for(int i =0;i<DataReceived.size()-1;i++)
					{
						
						img_al.add(DataReceived.get(i).data);
						
					}
					
					List<Byte> img = new ArrayList<Byte>();
					for(int i = 0;i<img_al.size();i++)
					{
						byte[] tempor =  img_al.get(i);
						for(int j = 0;j<tempor.length;j++)
						{
							
							img.add(tempor[j]);
						}
					}
					
					byte[] img_bytes = new byte[img.size()];
					for(int i = 0;i<img_al.size();i++)
					{
						img_bytes[i] = img.get(i);		
					}
	
									
					convertByteToImage(img_bytes);
					
					byte[] crcvalue = new byte[4];
					for(int i = 0;i<4;i++)
					{
						crcvalue[i] = Image.get(i+Image.size()-4);
					}
					CRC32 crc = new CRC32();
					crc.update(img_bytes);
				   
				    System.out.println("CRC32:   " + crc.getValue());
				    System.out.println("Checksum:" + fromByteArray(crcvalue));   
				}
			}
		}
	
	
	 
	 private static void convertByteToImage(byte[] bytearray) throws IOException
	    {
		   ByteArrayInputStream bis = new ByteArrayInputStream(bytearray);
	       java.util.Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpg");
	 
	        
	        ImageReader reader = (ImageReader) readers.next();
	        Object source = bis; 
	        ImageInputStream iis = ImageIO.createImageInputStream(source); 
	        reader.setInput(iis, true);
	        ImageReadParam param = reader.getDefaultReadParam();
	 
	        Image image = reader.read(0, param);
	        
	 
	        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
	        

	        Graphics2D g2 = bufferedImage.createGraphics();
	        g2.drawImage(image, null, null);

	        File imageFile = new File("C:\\Users\\luk___000\\\\Desktop\\bildnvs\\New_Image.jpg");
	        ImageIO.write(bufferedImage, "jpg", imageFile);
	    }
	 

	 
	 private static int[] inttobyte(byte b)
	  {
		  String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	      int[] arr = new int[s1.length()];
	      for(int i =0;i<s1.length();i++)
	      { 
	      	 arr[i] = Integer.parseInt(s1.substring(i, i+1));
	      }
	      return arr;
	  }
	 
	 private static int fromByteArray(byte[] bytes)
	 {
	     return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
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
	 
	 static class ReceivedData implements Comparable<ReceivedData>
	 {
		 Integer seq_nr;
		 byte[] data;
		 
		 public ReceivedData(int number, byte[] bytes)
		 {
			 this.seq_nr = number;
			 this.data = bytes;		 
		 }

		public int compareTo(ReceivedData arg0) {
			
			return this.seq_nr.compareTo(arg0.seq_nr);
		}
	 }
	
}
	 

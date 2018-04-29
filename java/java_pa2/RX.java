
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
  
  private static int ack_port = 4712;

	private static byte[] buf = new byte[1024]; //Default buffer

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
		while(true)
		{
	
			 System.out.println("==========");

			 DatagramPacket packet = new DatagramPacket(buf, buf.length);

			 socket.receive(packet);

			 DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.getData())); //Make input stream from datagramm

			 System.out.println(packet.getLength());
			 byte[] temp = packet.getData();
			 int sequenzeNumInt = dis.readInt();
			 
			 byte[] sequenzNummer = convertIntTo4Bytes(sequenzeNumInt);
			 InetAddress ia = InetAddress.getByName( "127.0.0.1" );
			 DatagramPacket seq_ack = new DatagramPacket(sequenzNummer,sequenzNummer.length,ia,ack_port);
			 socket.send(seq_ack);
			 
			 if(temp[0]==1)
			 {
				 sequenzeNumInt = (int) (sequenzeNumInt - Math.pow(2, 24));
				 
			 }
			

			 System.out.println("Sequenz Nummer: " + sequenzeNumInt);

		      if(lastSeqNum < sequenzeNumInt && lastSeqNum + 1 != sequenzeNumInt)
		      {
		
		          lostPackets += sequenzeNumInt - lastSeqNum + 1;
		
		      }
		
		      lastSeqNum = sequenzeNumInt;
		
			  
			
			 
			 System.out.print("Data: "  );
			 for(int i = 4;i<packet.getLength();i++)
			 {
				 Image.add(temp[i]);
				 System.out.print(temp[i] + ":"  );
			 }
			 System.out.println("");
			
					
		
			System.out.println("Recieved: " + ((++count)-1) + " packets.");
		
			System.out.println("Lost: " + lostPackets + " packets.");
		
			
				if(temp[0]==1)
				{
					for(int i =0;i<Image.size();i++)
					{
						int[] tem = inttobyte(Image.get(i));
						for(int j = 0;j<tem.length;j++)
						{
							MsgBinary.add(tem[j]);
						}
					}
					
					byte[] img = new byte[Image.size()-4];
					
					for(int i = 0;i<img.length;i++)
					{
						img[i] = Image.get(i);
					}
					convertByteToImage(img);
					byte[] crcvalue = new byte[4];
					for(int i = 0;i<4;i++)
					{
						crcvalue[i] = Image.get(i+Image.size()-4);
					}
					CRC32 crc = new CRC32();
					crc.update(img);
				   
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

	        File imageFile = new File("New_Image.jpg");
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
	
}
	 

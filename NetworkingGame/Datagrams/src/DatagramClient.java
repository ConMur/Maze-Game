import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DatagramClient
{

	private static final int PORT = 7781;

	public static void main(String args[])
	{
		try
		{
			// set up
			MulticastSocket socket = new MulticastSocket(PORT);
			InetAddress group = InetAddress.getByName("228.5.6.7");
			socket.joinGroup(group);
			
			DatagramPacket packet;
			
			for (int msg = 0; msg < 5; msg++)
			{
				byte[] buf = new byte[256];

				// get response
				packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				// display response
				String received = new String(packet.getData(), 0,
						packet.getLength());
				System.out.println("Quote of the Moment: " + received);
			}

			socket.leaveGroup(group);
			socket.close();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}

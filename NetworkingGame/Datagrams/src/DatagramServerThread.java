import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class DatagramServerThread extends Thread
{
	private DatagramSocket socket;
	private boolean running;

	private final int PORT = 7780;
	private final int CLIENT_PORT = 7781;

	public DatagramServerThread()
	{
		this("DatagramServer");
	}

	public DatagramServerThread(String name)
	{
		super(name);

		running = true;
		try
		{
			socket = new DatagramSocket(PORT);
		}
		catch (SocketException se)
		{
			se.printStackTrace();
		}
	}

	public void run()
	{
		while (running)
		{
			try
			{
				// Set up packet
				byte[] buf = new byte[256];

				// Set up what the packet will send
				String send = new String("YAY!");
				buf = send.getBytes();

				// Send the packet
				InetAddress group = InetAddress.getByName("228.5.6.7");
				DatagramPacket packet = new DatagramPacket(buf, buf.length,
						group, CLIENT_PORT);
				socket.send(packet);

				// Wait
				try
				{
					sleep(1000);
				}
				catch (InterruptedException ie)
				{
					ie.printStackTrace();
				}
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
				running = false;
			}
		}
		socket.close();
	}
}

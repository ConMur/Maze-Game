package DatagramChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

public class Server
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		int port = 30001;

		int id = 0x8CFD65B8;

		ByteBuffer in = ByteBuffer.allocate(1024);
		ByteBuffer out = ByteBuffer.allocate(1024);
		out.order(ByteOrder.BIG_ENDIAN);

		SocketAddress serverAddress = new InetSocketAddress(port);
		DatagramChannel channel = DatagramChannel.open();
		DatagramSocket socket = channel.socket();
		socket.bind(serverAddress);

		channel.configureBlocking(false);
		//Thread.sleep(10000);
		while (true)
		{
			try
			{
				in.clear();
				SocketAddress client = channel.receive(in);
				String clientMessage = new String(in.array()).trim();
				if (clientMessage.length() <= 0 || client == null)
				{
					continue;
				}
				if (in.getInt(0) != id)
				{
					System.err.println("received an invalid packet");
					System.err.println("Packet: " + new String(in.array()));
					continue;
				}
				else
				{
					System.out.println("Client Message: " + clientMessage.substring(4));

					// System.err.println(client);
					String message = "'avin' a giggle m80?";
					out.clear();
					out.put(message.getBytes());
					out.flip();

					channel.send(out, client);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}

package DatagramChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

public class Client
{
	static DatagramChannel channel;
	static DatagramSocket socket;
	static InetSocketAddress serverAddress;

	public static void main(String[] args) throws IOException
	{
		try
		{
			channel = DatagramChannel.open();
			channel.configureBlocking(false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		socket = channel.socket();
		socket.bind(new InetSocketAddress(0));

		serverAddress = new InetSocketAddress("192.168.2.16", 30001);
		ByteBuffer buf = ByteBuffer.allocate(1024);
		buf.order(ByteOrder.BIG_ENDIAN);

		byte[] msgData = "hahaha".getBytes();
		ByteBuffer messageData = ByteBuffer.wrap(msgData);

		ByteBuffer temp = ByteBuffer.allocate(1024);
		temp.putInt(0, 0x8CFD65B8);
		temp.position(4);
		temp.put(messageData);
		
		System.out.println(new String(temp.array()).trim().substring(4));
		temp.flip();

		channel.send(temp, serverAddress);

		buf.clear();
		temp.clear();
		
		String message = "";
		ByteBuffer in = ByteBuffer.allocate(1024);
		while (message.length() <= 0)
		{
			in.clear();
			channel.receive(in);
			in.flip();
			message = new String(in.array()).trim();
		}
		System.out.println("YO: " + message);
		channel.close();
	}
}

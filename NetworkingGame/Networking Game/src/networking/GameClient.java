package networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

import states.GameStateManager;

public class GameClient
{
	private final static int DEFAULT_SERVER_PORT = 9898;
	private final static int DEFAULT_PROTOCOL_ID = 0x8CFD65B8;
	private final static int NO_HEADER_PROTOCOL_ID = 0x512578;
	private int protocolId;

	private ByteBuffer in;
	private static DatagramChannel channel;
	private static InetSocketAddress serverAddress;
	private String serverIpAddress;
	private int port;
	private boolean connected;

	private final float TIMEOUT = 10000.0f;
	// After this time the client stops looking for the server
	private final float RECONNECT_TIMEOUT = 5000.0f;
	private long firstReconnectTime;
	private float timeoutAccumulator;

	private ReliabilitySystem reliabilitySystem;
	private int packetLossMask;

	final int headerSize = 12;

	public GameClient(String serverIpAddress)
	{
		this(serverIpAddress, DEFAULT_PROTOCOL_ID, DEFAULT_SERVER_PORT);
	}

	public GameClient(String serverIpAddress, int protocolId)
	{
		this(serverIpAddress, protocolId, DEFAULT_SERVER_PORT);
	}

	/**
	 * Creates a GameClient to send data to the server specified and to receive data from that server
	 * @param serverIpAddress the IP Address of the server the client should connect to.
	 * @param protocolId the unique id that is used to validate the incoming packets.
	 * @param port the port of the server that the client should connect to.
	 */
	public GameClient(String serverIpAddress, int protocolId, int port)
	{
		this.protocolId = protocolId;
		this.serverIpAddress = serverIpAddress;
		this.port = port;

		connected = false;

		timeoutAccumulator = 0.0f;

		packetLossMask = 0;
		reliabilitySystem = new ReliabilitySystem(250, Integer.MAX_VALUE);

		in = ByteBuffer.allocate(1024);
		in.order(ByteOrder.BIG_ENDIAN);

		serverAddress = new InetSocketAddress(serverIpAddress, port);
		try
		{
			channel = DatagramChannel.open();
			channel.configureBlocking(false);
			// Bind to any available port
			channel.socket().bind(new InetSocketAddress(0));
			connected = true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Used to see if the connection is still alive and must be called every game update.
	 * @param deltaTime the time between each frame
	 * @return false if the connection is timed out and true if the connection is good.
	 */
	public void update(float deltaTime)
	{
		timeoutAccumulator += deltaTime;
		if (timeoutAccumulator > TIMEOUT && connected)
		{
			System.out.println("Connection with server timed out!(" + timeoutAccumulator + "ms)");
			firstReconnectTime = System.currentTimeMillis();
			connected = false;
		}
		reliabilitySystem.update(deltaTime);

		if (!connected)
		{
			attemptToReconnect();
		}
	}

	/**
	 * Receives data from the server in a non blocking way and processes the data before hand to determine if it is from a valid source using the given protocol
	 * id. It then checks the packet number to validate that it is a more up to date packet.
	 * @return null if there is no data received or the protocolId does not match the given one. Returns a String of data if data was received and it is a more
	 *         recent packet. The string returned does not contain the reliability information.
	 */
	public String receiveData()
	{
		// Zero the buffer
		in.clear();
		in.put(new byte[1024]);
		in.clear();

		SocketAddress server = null;
		try
		{
			server = channel.receive(in);
		}
		catch (IOException ioe)
		{
			System.err.println("There was an error receiving the data the server");
			ioe.printStackTrace();
		}
		// Null server means that the channel.receive() did not receive a packet
		if (server == null)
		{
			return null;
		}
		// All that is received is the header
		if (in.remaining() < headerSize)
		{
			return null;
		}

		// Check to make sure the header matches the protocolId and return the string without the id if the Id is correct
		int id = in.getInt(0);
		if (id == protocolId)
		{
			timeoutAccumulator = 0.0f;
			int packetSequence = in.getInt(4);
			int packetAck = in.getInt(8);
			int packetAckBits = in.getInt(12);
			in.position(16);

			reliabilitySystem.packetReceived(packetSequence, in.remaining());
			reliabilitySystem.processAck(packetAck, packetAckBits);

			String data = new String(in.array()).trim().substring(16);

			// Discard heart beat packets
			if (data.equals("hb"))
			{
				return null;
			}

			// Received data when previously not connected, means we are connected once again
			if (!connected)
			{
				connected = true;
			}

			return data;
		}
		else if (id == NO_HEADER_PROTOCOL_ID)
		{
			timeoutAccumulator = 0.0f;
			String data = new String(in.array()).trim().substring(3);
			return data;
		}
		return null;
	}

	/**
	 * Sends the byte array of data to the client specified and prepends the protocolId to it
	 * @param data the data to be sent
	 * @return if the data was send successfully from the local machine
	 */
	public boolean sendData(byte[] data)
	{
		return this.sendData(ByteBuffer.wrap(data));
	}

	/**
	 * Sends the ByteBuffer of data to the client specified and prepends the protocolId to it
	 * @param data the data to be sent
	 * @return if the data was send successfully from the local machine
	 */
	public boolean sendData(ByteBuffer data)
	{
		int packetSize = data.remaining();

		// 1 means true
		if ((reliabilitySystem.getLocalSequence() & packetLossMask) == 1)
		{
			// TODO: remove when not debugging
			reliabilitySystem.packetSent(packetSize);
		}

		// Add the protocolId header
		ByteBuffer temp = ByteBuffer.allocate(1024);
		// Protocol Id
		temp.putInt(0, protocolId);
		temp.position(4);
		// Sequence Number
		temp.putInt(reliabilitySystem.getLocalSequence());
		temp.position(8);
		// Ack
		temp.putInt(reliabilitySystem.getRemoteSequence());
		temp.position(12);
		// Ack bits
		temp.putInt(reliabilitySystem.generateAckBits());
		temp.position(16);
		temp.put(data);

		temp.flip();
		try
		{
			int bytesSent = channel.send(temp, serverAddress);
			// Ensure all bytes are send from the local machine
			if (bytesSent < packetSize)
			{
				return false;
			}
		}
		catch (IOException ioe)
		{
			System.err.println("There was an error sending the data to: " + serverAddress.getAddress() + " on port: " + serverAddress.getPort());
			ioe.printStackTrace();
		}
		return true;
	}

	public void setPacketLossMask(int mask)
	{
		packetLossMask = mask;
	}

	/**
	 * Closes the DatagramChannel associated with this GameClient. Also cleans up the resources associated with this GameClient.
	 */
	public void close()
	{
		try
		{
			channel.close();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		in.clear();
		in = null;
		serverAddress = null;
		reliabilitySystem = null;
	}

	private void attemptToReconnect()
	{
		if (System.currentTimeMillis() - firstReconnectTime < RECONNECT_TIMEOUT)
		{
			serverAddress = new InetSocketAddress(serverIpAddress, port);
		}
		else
		{
			GameStateManager.setState(GameStateManager.MENU_STATE);
		}
	}

}

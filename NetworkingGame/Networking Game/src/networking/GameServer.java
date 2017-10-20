package networking;

import helperClasses.VaraibleStorage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameServer
{
	private final static int DEFAULT_PORT = 9898;
	private final static int DEFAULT_PROTOCOL_ID = 0x8CFD65B8;
	private int protocolId;

	private ByteBuffer in;
	private DatagramChannel channel;

	private final int TIMEOUT = 10000; // 10 seconds
	private ArrayList<Float> timeoutAccumulators;

	private Map<Integer, ClientData> clients;
	private int nextClientNo;

	private ReliabilitySystem reliabilitySystem;
	private int packetLossMask;

	private final int headerSize = 12;

	private boolean allowNewConnections;

	public GameServer()
	{
		this(DEFAULT_PROTOCOL_ID, DEFAULT_PORT);
	}

	public GameServer(int protocolId)
	{
		this(protocolId, DEFAULT_PORT);
	}

	/**
	 * Creates a game server to send data to the connected clients specified and to receive data from connected clients.
	 * @param protocolId the unique id that is used to validate the incoming packets.
	 * @param port the port that this server is hosted on.
	 */
	public GameServer(int protocolId, int port)
	{
		this.protocolId = protocolId;

		allowNewConnections = true;

		timeoutAccumulators = new ArrayList<Float>(4);

		packetLossMask = 0;
		reliabilitySystem = new ReliabilitySystem(250, Integer.MAX_VALUE);

		clients = new LinkedHashMap<Integer, ClientData>(4);
		//1 bc the server is 0 
		nextClientNo = 1;

		in = ByteBuffer.allocate(1024);
		in.order(ByteOrder.BIG_ENDIAN);

		SocketAddress serverAddress = new InetSocketAddress(port);
		try
		{
			channel = DatagramChannel.open();
			channel.configureBlocking(false);
			channel.socket().bind(serverAddress);
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
		int timeoutAccumulatorsSize = timeoutAccumulators.size();
		if (timeoutAccumulatorsSize <= 1)
			return;
		
		//Skip the server as the server cannot timeout from its self
		for (int client = 0; client < timeoutAccumulators.size(); client++)
		{
			float timeout = timeoutAccumulators.get(client);
			timeout += deltaTime;
			timeoutAccumulators.set(client, timeout);
			if (timeout > TIMEOUT)
			{
				System.out.println("SERVER - Client number: " + client + " timed out!(" + timeout + "ms)");
				processDisconnect(client);
			}
		}
		reliabilitySystem.update(deltaTime);
	}

	/**
	 * Receives data from the clients in a non blocking way and processes the data before hand to determine if it is from a valid source using the given
	 * protocol id
	 * @return null if there is no data received or the protocolId does not match the given one. Returns a String of data if data was received. The string
	 *         returned does not contain the reliability information.
	 */
	public DataPacket receiveData()
	{
		// Zero the buffer
		in.clear();
		in.put(new byte[1024]);
		in.clear();

		SocketAddress client = null;
		try
		{
			client = channel.receive(in);
		}
		catch (IOException ioe)
		{
			System.err.println("There was an error receiving the data from a client");
			ioe.printStackTrace();
		}
		// Null client means that the channel.receive() did not receive a packet
		if (client == null)
		{
			return null;
		}

		// All that is received is the header
		if (in.remaining() < headerSize)
		{
			return null;
		}

		// Only allow new connections when wanted
		if (allowNewConnections)
			checkIfNewConnection(client);

		int id = in.getInt(0);
		// Check to make sure the header matches the protocolId and return the string without the id if the Id is correct
		if (id == protocolId)
		{
			InetSocketAddress addr = (InetSocketAddress) client;
			int pos = getPosOfValue(addr);
			timeoutAccumulators.set(pos, 0.0f);

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
			if (data.contains("EXITING"))
			{
				processDisconnect(Integer.parseInt(data.substring(7, data.length())));
			}

			InetSocketAddress address = (InetSocketAddress) client;
			DataPacket packet = new DataPacket(data, address.getAddress(), address.getPort());
			return packet;
		}
		return null;
	}

	/**
	 * Sends the byte array of data to the client specified and prepends the protocolId to it
	 * @param data the data to be sent
	 * @param ipAddress the ipAddress of the client
	 * @param port the port of the client
	 * @return if the data was send successfully from the local machine
	 */
	public boolean sendData(byte[] data, InetAddress ipAddress, int port)
	{
		return this.sendData(ByteBuffer.wrap(data), ipAddress, port);
	}

	/**
	 * Sends the given data to all the clients that have made a connection to this server
	 * @param data the data to send to all the clients
	 * @return if the data was successfully sent to all the connected clients
	 */
	public boolean sendDataToAllClients(byte[] data)
	{
		boolean sucess = true;
		for (Map.Entry<Integer, ClientData> client : clients.entrySet())
		{
			if (client.getKey() != 0)
			{
				ClientData clientData = client.getValue();
				if (!this.sendData(ByteBuffer.wrap(data), clientData.ip, clientData.port))
				{
					sucess = false;
				}
			}
		}

		return sucess;
	}

	/**
	 * Sends the given data to all the clients that have made a connection to this server
	 * @param data the data to send to all the clients
	 * @return if the data was successfully sent to all the connected clients
	 */
	public boolean sendDataToAllClients(ByteBuffer data)
	{
		boolean sucess = true;
		for (Map.Entry<Integer, ClientData> client : clients.entrySet())
		{
			if (client.getKey() != 0)
			{
				ClientData clientData = client.getValue();
				if (!this.sendData(data, clientData.ip, clientData.port))
				{
					sucess = false;
				}
			}
		}

		return sucess;
	}

	/**
	 * Sends the ByteBuffer of data to the client specified and prepends the protocolId to it
	 * @param data the data to be sent
	 * @param ipAddress the ipAddress of the client
	 * @param port the port of the client
	 * @return if the data was send successfully from the local machine
	 */
	public boolean sendData(ByteBuffer data, InetAddress ipAddress, int port)
	{
		int packetSize = data.remaining();

		ByteBuffer temp = ByteBuffer.allocate(1024);

		// 1 means true
		if ((reliabilitySystem.getLocalSequence() & packetLossMask) == 1)
		{
			// TODO: remove when not debugging
			reliabilitySystem.packetSent(packetSize);
		}
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
			int bytesSent = channel.send(temp, new InetSocketAddress(ipAddress, port));
			// Ensure all bytes are send from the local machine
			if (bytesSent < packetSize)
			{
				return false;
			}
		}
		catch (IOException ioe)
		{
			System.err.println("There was an error sending the data to: " + ipAddress + " on port: " + port);
			ioe.printStackTrace();
		}
		return true;
	}

	/**
	 * Checks the map of clients to see if the data received is from a new client. If it is, the client's information is stored.
	 * @param address the client's connection information
	 */
	private void checkIfNewConnection(SocketAddress address)
	{
		if (address instanceof InetSocketAddress)
		{
			InetSocketAddress addr = (InetSocketAddress) address;
			int port = addr.getPort();
			InetAddress ip = addr.getAddress();

			boolean newClient = false;
			for (ClientData data : clients.values())
			{
				if (data.port == port)
				{
					newClient = true;
					break;
				}
			}
			if (!newClient)
			{
				// TODO: remove when not debugging
				System.out.println("SERVER - NEW CONNECTION DETECTED WITH IP: " + ip + " ON PORT: " + port + ", NOTIFYING CLIENT");
				notifyClient(addr);
				clients.put(nextClientNo++, new ClientData(ip, port));
				timeoutAccumulators.add(0.0f);
			}
		}
		else
		{
			throw new IllegalArgumentException("The given address is not valid");
		}
	}

	private void notifyClient(InetSocketAddress addr)
	{
		sendData(("CLIENTNO" + Integer.toString(nextClientNo)).getBytes(), addr.getAddress(), addr.getPort());
		sendData(("SEED" + Long.toString(VaraibleStorage.getSeed())).getBytes(), addr.getAddress(), addr.getPort());
	}

	/**
	 * Returns the position of the requested ip address in the clients array
	 * @param addr the ip address of the requested client
	 * @return the position of the requested ip address in the clients array
	 */
	private int getPosOfValue(InetSocketAddress addr)
	{
		int pos = 0;
		int port = addr.getPort();
		Iterator<ClientData> it = clients.values().iterator();
		while (it.hasNext())
		{
			if (it.next().port == port)
			{
				return pos;
			}
			pos++;
		}
		return pos;
	}

	public void setPacketLossMask(int mask)
	{
		packetLossMask = mask;
	}

	/**
	 * Sends the new client numbers to the still connected clients and removes the connected client from the list of connected clients.
	 * @param removedClient the number of the removed client.
	 */
	private void processDisconnect(int removedClient)
	{
		if (clients.containsKey(removedClient))
		{
			clients.remove(removedClient);
			timeoutAccumulators.remove(removedClient);
			int totalClients = clients.size();

			for (int theClient = removedClient; theClient < totalClients; theClient++)
			{
				ClientData clientData = clients.get(theClient);
				this.sendData(("NEWNUM" + theClient).getBytes(), clientData.ip, clientData.port);
			}
		}
		else
		{
			throw new IllegalArgumentException("The given client: " + removedClient + " does not exist");
		}
	}

	/**
	 * Closes the DatagramChannel associated with this GameServer. Also cleans up the resources associated with this GameClient.
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
		reliabilitySystem = null;
		timeoutAccumulators.clear();
		timeoutAccumulators = null;
		clients.clear();
		clients = null;

	}
	
	public void accecptNewConnections(boolean accecpt)
	{
		allowNewConnections = accecpt;
	}

	public Map<Integer, ClientData> getClients()
	{
		return clients;
	}
}

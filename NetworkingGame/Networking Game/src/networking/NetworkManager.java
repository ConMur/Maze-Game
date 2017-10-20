package networking;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Map;

import main.GamePanel;
import states.GameStateManager;

public final class NetworkManager
{
	private final static int DEFAULT_PORT = 9898;

	private static GameClient client = null;
	private static GameServer server = null;

	private static boolean host = false;
	private static boolean allowsNewConnections = true;

	private static long lastBeat = 0;
	private static final long HEART_BEAT_TIMER = 1000;

	// From server sending to clients
	private static ArrayList<GuaranteedPacket> guarenteedServerPackets = new ArrayList<GuaranteedPacket>();
	// From client sending to server
	private static ArrayList<GuaranteedPacket> guarenteedClientPackets = new ArrayList<GuaranteedPacket>();

	private NetworkManager()
	{
	}

	public static void createClient(String ip)
	{
		if (client == null)
			client = new GameClient(ip);
	}

	public static void createClient(InetAddress ip)
	{
		NetworkManager.createClient(ip.toString());
	}

	public static void createServer()
	{
		if (server == null)
			server = new GameServer();
	}

	public static boolean isHost()
	{
		return host;
	}

	public static void setIsHost(boolean isHost)
	{
		host = isHost;
	}

	public static void sendMessageToServer(String data)
	{
		NetworkManager.sendMessageToServer(data.getBytes());
	}

	public static void sendMessageToServer(byte[] data)
	{
		// TODO: remove when not debugging
		if (client == null)
		{
			throw new NullPointerException("The client was not created!\nMake sure the NetworkManager.createClient() method was called.");
		}

		boolean ok = client.sendData(data);
		if (!ok)
		{
			System.err.println("The data was not sent to the server sucessfully");
			System.exit(-5);
		}
	}

	public static void sendMessageToAllClients(byte[] data)
	{
		// TODO: remove when not debugging
		if (server == null)
		{
			throw new NullPointerException("The server was not created!\nMake sure the NetworkManager.createServer() method was called.");
		}
		boolean ok = server.sendDataToAllClients(data);
		if (!ok)
		{
			System.err.println("The data was not sent to the clients sucessfully");
			System.exit(-5);
		}
	}

	public static void sendMessageToClient(String data, InetAddress ipAddress, int port)
	{
		boolean ok = server.sendData(data.getBytes(), ipAddress, port);
		if (!ok)
		{
			System.err.println("The data was not sent to the specified client sucessfully");
			System.exit(-5);
		}
	}

	public static void sendMessageToClient(byte[] data, InetAddress ipAddress, int port)
	{
		// TODO: remove when not debugging
		if (server == null)
		{
			throw new NullPointerException("The server was not created!\nMake sure the NetworkManager.createServer() method was called.");
		}
		boolean ok = server.sendData(data, ipAddress, port);
		if (!ok)
		{
			System.err.println("The data was not sent to the specified client sucessfully");
			System.exit(-5);
		}
	}

	public static void sendGuaranteedMessageToClient(String data, InetAddress ipAddress, int port, int clientNo, int type)
	{
		guarenteedServerPackets.add(new GuaranteedPacket(data, ipAddress, port, clientNo, type));
	}

	public static void sendGuaranteedMessageToClient(byte[] data, InetAddress ipAddress, int port, int clientNo, int type)
	{
		guarenteedServerPackets.add(new GuaranteedPacket(data, ipAddress, port, clientNo, type));
	}

	public static void sendGuaranteedMessageToAllClients(String data, int type)
	{
		NetworkManager.sendGuaranteedMessageToAllClients(data.getBytes(), type);
	}

	public static void sendGuaranteedMessageToAllClients(byte[] data, int type)
	{
		Map<Integer, ClientData> clients = server.getClients();
		for (Map.Entry<Integer, ClientData> client : clients.entrySet())
		{
			int clientNo = client.getKey();
			if (clientNo != 0)
			{
				ClientData clientData = client.getValue();
				guarenteedServerPackets.add(new GuaranteedPacket(data, clientData.ip, clientData.port, clientNo, type));
			}
		}
	}

	public static void sendGuaranteedMessageToServer(String data, int clientNo, int type)
	{
		guarenteedClientPackets.add(new GuaranteedPacket(data, clientNo, type));
	}

	public static void sendGuaranteedMessageToServer(byte[] data, int clientNo, int type)
	{
		guarenteedClientPackets.add(new GuaranteedPacket(data, clientNo, type));
	}

	public static void update()
	{
		if (host)
		{
			updateServer();
		}
		else
		{
			updateClient();
		}

		sendHeartBeat();

		resendPendingGuaranteedPackets();
	}

	private static void updateServer()
	{
		boolean processPacket = true;
		DataPacket packet = server.receiveData();
		if (packet != null)
		{
			String message = packet.data;
			// TODO: remove when not debugging
			if (!message.startsWith("CPOS"))
				System.out.println("server received: " + message);

			int messageLength = message.length();
			if (message.contains("RCVNUM"))
			{
				processPacket = false;
				int clientNo = Integer.parseInt(message.substring(6, messageLength));
				removeServerPacket(clientNo, PacketTypes.TYPE_NUM);
			}
			else if (message.contains("RCVNEWNUM"))
			{
				processPacket = false;
				int clientNo = Integer.parseInt(message.substring(9, messageLength));
				removeServerPacket(clientNo, PacketTypes.TYPE_NEW_NUM);
			}
			else if (message.contains("RCVTOT"))
			{
				processPacket = false;
				int clientNo = Integer.parseInt(message.substring(6, messageLength));
				removeServerPacket(clientNo, PacketTypes.TYPE_TOT);
			}
			else if (message.contains("RCVSEED"))
			{
				processPacket = false;
				int clientNo = Integer.parseInt(message.substring(7, messageLength));
				removeServerPacket(clientNo, PacketTypes.TYPE_SEED);
			}
			else if (message.contains("RCVPLAY"))
			{
				processPacket = false;
				int clientNo = Integer.parseInt(message.substring(7, messageLength));
				removeServerPacket(clientNo, PacketTypes.TYPE_PLAY);
			}
			if (message.contains("RCVGOAL"))
			{
				processPacket = false;
				int clientNo = Integer.parseInt(message.substring(7, messageLength));
				removeServerPacket(clientNo, PacketTypes.TYPE_GOAL);
			}
			else if (message.contains("RCVNEWNUM"))
			{
				processPacket = false;
				int clientNo = Integer.parseInt(message.substring(9, messageLength));
				removeServerPacket(clientNo, PacketTypes.TYPE_NEW_NUM);
			}
			else if(message.contains("RCVDIFF"))
			{
				processPacket = false;
				int clientNo = Integer.parseInt(message.substring(7, messageLength));
				removeServerPacket(clientNo, PacketTypes.TYPE_DIFF);
			}

			if (processPacket)
				GameStateManager.getCurrentState().processServerMessage(packet);
		}
		server.update(GamePanel.delta);
	}

	private static void updateClient()
	{
		boolean processPacket = true;
		String message = client.receiveData();
		if (message != null)
		{
			if (!message.startsWith("SPOS"))
				System.out.println("client received: " + message);

			if (message.contains("RCVNRDY"))
			{
				processPacket = false;
				removeClientPacket(PacketTypes.TYPE_NRDY);
			}
			else if (message.contains("RCVRDY"))
			{
				processPacket = false;
				removeClientPacket(PacketTypes.TYPE_RDY);
			}
			if (message.contains("RCVGOAL"))
			{
				processPacket = false;
				removeClientPacket(PacketTypes.TYPE_GOAL);
			}

			if (processPacket)
				GameStateManager.getCurrentState().processClientMessage(message);
		}
		client.update(GamePanel.delta);
	}

	private static void resendPendingGuaranteedPackets()
	{
		// TODO: re-send packets every 2nd? update until they have been confirmed to be sent
		for (GuaranteedPacket packet : guarenteedServerPackets)
		{
			NetworkManager.sendMessageToClient(packet.data, packet.ip, packet.port);
		}

		for (GuaranteedPacket packet : guarenteedClientPackets)
		{
			NetworkManager.sendMessageToServer(packet.data);
		}
	}

	private static void sendHeartBeat()
	{
		// Send heartbeat packets every sec
		if (System.currentTimeMillis() - lastBeat > HEART_BEAT_TIMER)
		{
			// hb for heart beat
			if (host)
			{
				server.sendDataToAllClients("hb".getBytes());
			}
			else
			{
				client.sendData("hb".getBytes());
			}
			lastBeat = System.currentTimeMillis();
		}
	}

	private static void removeServerPacket(int clientNo, int type)
	{
		int size = guarenteedServerPackets.size();
		for (int packet = 0; packet < size; packet++)
		{
			GuaranteedPacket thePacket = guarenteedServerPackets.get(packet);
			if (thePacket.type == type && thePacket.clientNo == clientNo)
			{
				guarenteedServerPackets.remove(packet);
				break;
			}
		}
	}

	private static void removeClientPacket(int type)
	{
		int size = guarenteedClientPackets.size();
		for (int packet = 0; packet < size; packet++)
		{
			GuaranteedPacket thePacket = guarenteedClientPackets.get(packet);
			if (thePacket.type == type)
			{
				guarenteedClientPackets.remove(packet);
				break;
			}
		}
	}

	public static void close()
	{
		if (client != null)
			client.close();
		if (server != null)
			server.close();
	}

	public static void reset()
	{
		close();
		client = null;
		server = null;
	}

	public static int defaultServerPort()
	{
		return DEFAULT_PORT;
	}

	public static void accecptNewConnections(boolean accecpt)
	{
		allowsNewConnections = accecpt;
		server.accecptNewConnections(accecpt);
	}

	public static boolean accecptsNewConnections()
	{
		return allowsNewConnections;
	}
}

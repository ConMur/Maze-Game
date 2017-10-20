package networking;

import java.net.InetAddress;

public class GuaranteedPacket
{
	public String data;
	public InetAddress ip;
	public int port;
	public int type;
	public int clientNo;

	public GuaranteedPacket(byte[] data, InetAddress ipAddress, int port, int clientNo, int type)
	{
		this(new String(data), ipAddress, port, clientNo, type);
	}

	public GuaranteedPacket(String data, InetAddress ip, int port, int clientNo, int type)
	{
		this.data = data;
		this.ip = ip;
		this.port = port;
		this.type = type;
		this.clientNo = clientNo;
	}

	public GuaranteedPacket(byte[] data, int clientNo, int type)
	{
		this(new String(data), clientNo, type);
	}

	public GuaranteedPacket(String data, int clientNo, int type)
	{
		this.data = data;
		this.clientNo = clientNo;
		this.type = type;
	}
}

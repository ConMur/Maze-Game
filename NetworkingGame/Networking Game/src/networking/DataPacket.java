package networking;

import java.net.InetAddress;

public class DataPacket
{
	public String data;
	public InetAddress ip;
	public int port;
	
	public DataPacket(String data, InetAddress ip, int port)
	{
		this.data = data;
		this.ip = ip;
		this.port = port;
	}
}

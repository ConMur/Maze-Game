package networking;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientData
{
	public InetAddress ip;
	public int port;
	
	public ClientData(InetAddress ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}
	
	public ClientData(String ip, int port)
	{
		try
		{
			this.ip = InetAddress.getByName(ip);
		}
		catch (UnknownHostException uhe)
		{
			uhe.printStackTrace();
		}
		this.port = port;
	}
}

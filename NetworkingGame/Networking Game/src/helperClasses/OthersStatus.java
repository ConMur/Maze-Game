package helperClasses;

public class OthersStatus
{
	public class XandY
	{
		public int x, y;

		public XandY(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		public void setXandY(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
	}

	private XandY[] clientsPos;
	private int totalClients;

	// TODO: maybe add an image type so they can look different
	public OthersStatus(int totalClients)
	{
		clientsPos = new XandY[totalClients];
		this.totalClients = totalClients;
	}
	
	public void setXAndY(int x, int y, int clientNo)
	{
		if(clientNo < 0 || clientNo > totalClients)
			throw new IllegalArgumentException("Invalid client number");
		
		clientsPos[clientNo].setXandY(x, y);
	}

	/**
	 * Returns a string with client number then x position then a '`' character then y position then a '|' character to mark the end of that section
	 */
	public String toString()
	{
		String message = "";
		
		for(int client = 0; client < totalClients; client++)
		{
			message += "ID" + client + "POSX" + clientsPos[client].x + "POSY" + clientsPos[client].y;
		}
		
		return message;
	}

}

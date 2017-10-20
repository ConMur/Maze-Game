package helperClasses;

public final class VaraibleStorage
{
	private static boolean isClosing;

	private static boolean isHost = false;
	private static int clientNo;
	private static String ip;
	private static int totalClients;
	private static long seed;
	private static int difficulty;

	private VaraibleStorage()
	{
	}

	public static void clear()
	{
		isClosing = false;
		isHost = false;
		clientNo = 0;
		ip = null;
		totalClients = 0;
		seed = 0;
	}

	public static void setIsHost(boolean host)
	{
		isHost = host;
	}

	public static boolean isHost()
	{
		return isHost;
	}

	public static void setIp(String address)
	{
		ip = address;
	}

	public static String getIp()
	{
		return ip;
	}

	public static void setClientNo(int clientNum)
	{
		clientNo = clientNum;
	}

	public static void setTotalClients(int total)
	{
		totalClients = total;
	}

	public static int getTotalClients()
	{
		return totalClients;
	}

	public static int getClientNo()
	{
		return clientNo;
	}

	public static void setClosing(boolean closing)
	{
		isClosing = closing;
	}

	public static boolean isClosing()
	{
		return isClosing;
	}

	public static long getSeed()
	{
		return seed;
	}

	public static void setSeed(long seed)
	{
		VaraibleStorage.seed = seed;
	}

	public static int getDifficulty()
	{
		return difficulty;
	}

	public static void setDifficulty(int difficulty)
	{
		VaraibleStorage.difficulty = difficulty;
	}
}

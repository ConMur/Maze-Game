package states;

import helperClasses.MessageBox;
import helperClasses.VaraibleStorage;
import input.Button;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import main.GamePanel;
import networking.DataPacket;
import networking.NetworkManager;
import networking.PacketTypes;

public class LobbyState implements GameState
{
	private boolean isHost;
	private String ipAddress;
	private long seed;

	private boolean isReady;
	private boolean recievedClientNo;

	private Image bg;
	private Image readyImage;
	private int readyX, readyY;
	private boolean sentOnce;

	private Button[] difficultyButtons;
	private final int NUM_DIFFICULTIES = 4;
	private int difficulty;
	private boolean isSelectingDifficulty;

	private ArrayList<Boolean> clientsReady;
	private int nextClientNo;
	private int clientNo;
	private int totalClients;

	private boolean enterPlayState;
	private boolean sentClosingOnce;

	private Font font;
	private int ipX, ipY;

	public LobbyState()
	{
	}

	public void init()
	{
		bg = new Image(getClass().getResourceAsStream("/Backgrounds/LobbyBackground.png"));
		readyImage = new Image(getClass().getResourceAsStream("/Popups/ReadyToStart.png"));

		ipX = (int) (GamePanel.WIDTH * 0.35);
		ipY = (int) (GamePanel.HEIGHT * 0.9);

		isReady = false;

		readyX = (int) ((GamePanel.WIDTH / 2) - (readyImage.getWidth() / 2));
		readyY = (int) ((GamePanel.HEIGHT / 2) - (readyImage.getHeight() / 2));

		int baseButtonX = 0;
		int baseButtonY = 0;
		difficultyButtons = new Button[NUM_DIFFICULTIES];
		for (int button = 0; button < difficultyButtons.length; button++)
		{
			Image buttonImage = new Image(getClass().getResourceAsStream("/Buttons/Difficulty" + (button + 1) + ".png"));
			int buttonWidth = (int) buttonImage.getWidth();
			int buttonHeight = (int) buttonImage.getHeight();

			// Set these values the first time
			if (button == 0)
			{
				baseButtonX = (int) (GamePanel.WIDTH / NUM_DIFFICULTIES - buttonWidth / 2);
				baseButtonY = (int) (GamePanel.HEIGHT / NUM_DIFFICULTIES - buttonHeight / 2);
			}
			difficultyButtons[button] = new Button(buttonImage, baseButtonX + (button * buttonWidth), baseButtonY);
		}
		difficulty = -1;

		recievedClientNo = false;
		clientNo = 0;
		nextClientNo = 1;
		clientsReady = new ArrayList<Boolean>();
		totalClients = 1;

		sentOnce = false;

		enterPlayState = false;
		sentClosingOnce = false;

		clientsReady.add(0, false);

		isHost = VaraibleStorage.isHost();

		if (isHost)
		{
			try
			{
				System.setOut(new PrintStream("HostDebug.log"));
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}

			try
			{
				ipAddress = InetAddress.getLocalHost().getHostAddress();
			}
			catch (UnknownHostException uhe)
			{
				uhe.printStackTrace();
			}
			// MessageBox.showMessageDialog("The IP of the server is: " + ipAddress, "IP info");
			System.out.println("The IP of the server is: " + ipAddress + " on port: " + NetworkManager.defaultServerPort());

			font = Font.loadFont(getClass().getResourceAsStream("/Fonts/AYearWithoutRain.ttf"), 36);
			seed = System.currentTimeMillis();
			VaraibleStorage.setSeed(seed);
			isSelectingDifficulty = true;

			NetworkManager.createServer();
			NetworkManager.setIsHost(true);
		}
		else
		{
			try
			{
				System.setOut(new PrintStream("ClientDebug.log"));
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			// TODO: change back to ""
			String ip = "";
			while (ip.length() == 0)
			{
				ip = MessageBox.showInputDialog("Enter IP: ", "Enter the ip of the server", "Enter IP");
				if(!isValidIP(ip))
				{
					ip = "";
				}
			}
			isSelectingDifficulty = false;
			NetworkManager.setIsHost(false);
			NetworkManager.createClient(ip);
			NetworkManager.sendMessageToServer("CONNECTED".getBytes());
		}
	}

	public void update()
	{
		if (isHost)
		{
			if (totalClients == 4 && NetworkManager.accecptsNewConnections())
			{
				NetworkManager.accecptNewConnections(false);
			}
			else if (totalClients < 4 && !NetworkManager.accecptsNewConnections())
			{
				NetworkManager.accecptNewConnections(true);
			}
		}

		NetworkManager.update();

		if (!isHost)
		{
			if (isReady && !sentOnce)
			{
				NetworkManager.sendGuaranteedMessageToServer(("READY" + clientNo).getBytes(), clientNo, PacketTypes.TYPE_RDY);
				sentOnce = true;
			}
			else if (!isReady && !sentOnce && recievedClientNo)
			{
				NetworkManager.sendGuaranteedMessageToServer(("NOTREADY" + clientNo).getBytes(), clientNo, PacketTypes.TYPE_NRDY);
				sentOnce = true;
			}
		}

		if (isHost)
		{
			if (isSelectingDifficulty)
			{
				for (int button = 0; button < difficultyButtons.length; button++)
				{
					if (difficultyButtons[button].isClicked())
					{
						difficulty = button;
						isSelectingDifficulty = false;
						VaraibleStorage.setDifficulty(difficulty);
						break;
					}
				}
			}
			// Only check if all are ready when there are more clients than the host
			if (totalClients > 1)
			{
				boolean allReady = true;
				// Check the server
				if (!isReady)
				{
					clientsReady.set(0, false);
				}
				else
				{
					clientsReady.set(0, true);
				}
				// Check the clients
				for (int client = 0; client < totalClients; client++)
				{
					if (!clientsReady.get(client))
					{
						allReady = false;
						break;
					}
				}

				if (allReady)
				{
					// Message clients that the game is to be set to play state now
					NetworkManager.sendGuaranteedMessageToAllClients(("TOTALCLIENTS" + totalClients).getBytes(), PacketTypes.TYPE_TOT);
					NetworkManager.sendGuaranteedMessageToAllClients("DIFF" + difficulty, PacketTypes.TYPE_DIFF);
					NetworkManager.sendGuaranteedMessageToAllClients("PLAY".getBytes(), PacketTypes.TYPE_PLAY);
					enterPlayState = true;
				}
			}
		}

		// When the window is closing, notify the server that the client is
		// disconnecting
		if (VaraibleStorage.isClosing() && !sentClosingOnce)
		{
			NetworkManager.sendMessageToServer(("EXITING" + clientNo).getBytes());
			sentClosingOnce = true;
		}

		// Check if should enter play state
		if (enterPlayState)
		{
			VaraibleStorage.setClientNo(clientNo);
			VaraibleStorage.setIp(ipAddress);
			VaraibleStorage.setTotalClients(totalClients);
			GameStateManager.setState(GameStateManager.PLAY_STATE);
		}
	}

	public void draw(GraphicsContext g)
	{
		g.drawImage(bg, 0, 0);
		if (isSelectingDifficulty && isHost)
		{
			for (int button = 0; button < difficultyButtons.length; button++)
			{
				difficultyButtons[button].draw(g);
			}
		}
		else
		{
			if (isReady)
			{
				g.drawImage(readyImage, readyX, readyY);
			}
		}
		if (isHost)
		{
			g.setFont(font);
			g.fillText(ipAddress, ipX, ipY);
		}
	}

	public void cleanUp()
	{
		ipAddress = null;
		bg = null;
		readyImage = null;
		clientsReady = null;
		font = null;
	}

	public void mousePressed(MouseEvent e)
	{
		if (isSelectingDifficulty)
		{
			for (int button = 0; button < difficultyButtons.length; button++)
			{
				difficultyButtons[button].mosuePressed(e);
			}
		}
		else
		{
			isReady = !isReady;
			sentOnce = false;
		}
	}

	public void mouseReleased(MouseEvent e)
	{
	}
	
	private boolean isValidIP (String ip) {
	    try {
	        if ( ip == null || ip.isEmpty() ) {
	            return false;
	        }

	        String[] parts = ip.split( "\\." );
	        if ( parts.length != 4 ) {
	            return false;
	        }

	        for ( String s : parts ) {
	            int i = Integer.parseInt( s );
	            if ( (i < 0) || (i > 255) ) {
	                return false;
	            }
	        }
	        if ( ip.endsWith(".") ) {
	            return false;
	        }

	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}

	@Override
	public void processServerMessage(DataPacket packet)
	{
		String data = packet.data;
		if (data.equals("CONNECTED"))
		{
			clientsReady.add(nextClientNo, false);
			++totalClients;
			++nextClientNo;
			System.out.println("SERVER - NEW CONNECTON, TOTALCLIENTS NOW: " + totalClients + " NEXTCLIENTNO NOW: " + nextClientNo);
		}
		else if (data.contains("NOTREADY"))
		{
			int clientNum = Integer.parseInt(data.substring(8, data.length()));
			clientsReady.set(clientNum, false);
			System.out.println("SERVER - SET CLIENT NUMBER: " + clientNum + " TO NOT READY");

			// Send confirmation
			NetworkManager.sendMessageToClient("RCVNRDY", packet.ip, packet.port);
		}
		else if (data.contains("READY"))
		{
			int clientNum = Integer.parseInt(data.substring(5, data.length()));
			clientsReady.set(clientNum, true);
			System.out.println("SERVER - SET CLIENT NUMBER: " + clientNum + " TO READY");

			// Send confirmation
			NetworkManager.sendMessageToClient("RCVRDY", packet.ip, packet.port);
		}
		else if (data.contains("EXITING"))
		{
			int removedClient = Integer.parseInt(data.substring(7));
			clientsReady.remove(removedClient);
			totalClients--;
			// Can do this because the still connected clients get new numbers that are less than nextClientNo - 1.
			nextClientNo--;
			System.out.println("SERVER - CLIENT NUMBER: " + removedClient + " REMOVED. TOTALCLIENTS NOW: " + totalClients);
		}
		// TODO: REMOVE WHEN NOT DEBUGGING
		else
		{
			System.out.println("Invalid packet received: " + data);
		}
	}

	@Override
	public void processClientMessage(String data)
	{
		if (data.contains("CLIENTNO"))
		{
			try
			{
				clientNo = Integer.parseInt(data.substring(8, data.length()));
				recievedClientNo = true;
				System.out.println("CLIENT - SET CLIENTNO TO: " + clientNo);
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("Error receiving client no");
				nfe.printStackTrace();
			}

			// Send confirmation
			NetworkManager.sendMessageToServer("RCVNUM" + clientNo);
		}
		else if (data.contains("NEWNUM"))
		{
			try
			{
				clientNo = Integer.parseInt(data.substring(6, data.length()));
				System.out.println("CLIENT - SET NEW CLIENTNO TO: " + clientNo);
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("Error receiving new client no");
				nfe.printStackTrace();
			}

			// Send confirmation
			NetworkManager.sendMessageToServer("RCVNEWNUM" + clientNo);
		}
		else if (data.equals("PLAY"))
		{
			enterPlayState = true;

			// Send confirmation
			NetworkManager.sendMessageToServer("RCVPLAY" + clientNo);
		}
		else if (data.contains("TOTALCLIENTS"))
		{
			try
			{
				totalClients = Integer.parseInt(data.substring(12, data.length()));
				VaraibleStorage.setTotalClients(totalClients);
				System.out.println("CLIENT - SET TOTALCLIENTS TO: " + totalClients);
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("Error parsing total clients");
				nfe.printStackTrace();
			}

			// Send confirmation
			NetworkManager.sendMessageToServer("RCVTOT" + clientNo);
		}
		else if (data.contains("SEED"))
		{
			long seed = Long.parseLong(data.substring(4));
			VaraibleStorage.setSeed(seed);
			System.out.println("CLIENT - SET SEED TO: " + seed);

			// Send confirmation
			NetworkManager.sendMessageToServer("RCVSEED" + clientNo);
		}
		else if(data.contains("DIFF"))
		{
			int diff = Integer.parseInt(data.substring(4));
			VaraibleStorage.setDifficulty(diff);
			System.out.println("CLIENT - SET DIFFICULTY TO: " + diff);
			
			//Send confirmation
			NetworkManager.sendMessageToServer("RCVDIFF" + clientNo);
		}
		// TODO: REMOVE WHEN NOT DEBUGGING
		else
		{
			System.out.println("Invalid packet received: " + data);
		}
	}
}
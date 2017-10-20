package states;

//TODO: make the server not implement the client
//TODO: make sure the RCV... packets are dealt with
import helperClasses.VaraibleStorage;
import input.Input;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import main.GamePanel;
import networking.DataPacket;
import networking.NetworkManager;
import networking.PacketTypes;
import player.OtherPlayer;
import player.Player;
import tileMap.TileMap;

public class PlayState implements GameState
{
	private final String EASY_MAP = "/Maps/Easy.map";
	private final String MEDIUM_MAP = "/Maps/Medium.map";
	private final String HARD_MAP = "/Maps/Hard.map";
	private final String EXTREME_MAP = "/Maps/Extreme.map";
	
	private final int EASY = 0;
	private final int MEDIUM = 1;
	private final int HARD = 2;
	private final int EXTREME = 3;

	private TileMap tileMap;

	private Image bg, gameOverPopup;
	private int gameOverX, gameOverY;

	private int clientNo;
	private boolean isHost;
	private int totalClients;
	private boolean send;

	private Player player;
	private int posX, posY;
	private int tileSize;

	private boolean showMap;

	private long seed;

	private OtherPlayer[] serverOtherPlayers;
	private OtherPlayer[] clientOtherPlayers;
	private Image otherPlayerMove, otherPlayerIdle;

	private boolean sentClosingOnce;
	private boolean sentGameOverOnce;

	private boolean gameOver;

	public PlayState()
	{
	}

	public void init()
	{
		bg = new Image(getClass().getResourceAsStream("/Backgrounds/PlayBackground.png"));
		gameOverPopup = new Image(getClass().getResourceAsStream("/Popups/GameOver.png"));

		tileMap = new TileMap(80);
		tileMap.setPosition(0, 0);

		showMap = false;

		gameOverX = GamePanel.WIDTH / 4;
		gameOverY = GamePanel.HEIGHT / 4;

		sentClosingOnce = false;
		sentGameOverOnce = false;
		send = false;

		totalClients = VaraibleStorage.getTotalClients();

		seed = VaraibleStorage.getSeed();
		int difficulty = VaraibleStorage.getDifficulty();
		String mapPath = "";
		if(difficulty == EASY)
		{
			mapPath = EASY_MAP;
		}
		else if(difficulty == MEDIUM)
		{
			mapPath = MEDIUM_MAP;
		}
		else if(difficulty == HARD)
		{
			mapPath = HARD_MAP;
		}
		else if(difficulty == EXTREME)
		{
			mapPath = EXTREME_MAP;
		}
		tileMap.loadMap(mapPath, seed, false);

		tileSize = tileMap.getTileSize();

		isHost = VaraibleStorage.isHost();

		clientNo = VaraibleStorage.getClientNo();

		// Set starting positions
		if (clientNo == 0)
		{
			posX = tileSize;
			posY = tileSize;
		}
		else if (clientNo == 1)
		{
			posX = (tileMap.getNumRows() - 2) * tileSize;
			posY = (tileMap.getNumCols() - 2) * tileSize;
		}
		else if (clientNo == 2)
		{
			posX = tileSize;
			posY = (tileMap.getNumRows() - 2) * tileSize;
		}
		else if (clientNo == 3)
		{
			posX = (tileMap.getNumCols() - 2) * tileSize;
			posY = tileSize;
		}

		player = new Player(new Image(getClass().getResourceAsStream("/Players/maleWalk.png")),
				new Image(getClass().getResourceAsStream("/Players/maleIdle.png")), 44, 68, tileMap);
		player.setStartPos(posX, posY);

		createOtherPlayers();

		gameOver = false;
	}

	public void update()
	{
		NetworkManager.update();

		if (VaraibleStorage.isClosing() && !sentClosingOnce)
		{
			NetworkManager.sendMessageToServer(("EXITING" + clientNo).getBytes());
			sentClosingOnce = true;
		}

		if (!gameOver)
		{
			player.move();
			posX = player.getX();
			posY = player.getY();

			updateMap();

			checkIfReachedGoal();

			sendUpdates();
		}
	}

	private void updateMap()
	{
		if (Input.isKeyPressed(KeyCode.M))
		{
			showMap = true;
		}
		else
		{
			showMap = false;
		}
	}

	private void sendUpdates()
	{
		// So that every second frame it will send the new positions
		send = !send;
		if (send)
		{
			if (isHost)
			{
				sendUpdatedPositions();
			}
			else
			{
				sendNewPosToServer();
			}
		}
	}

	private void sendNewPosToServer()
	{
		boolean moving = player.isMoving();
		String move = moving ? "M" : "N"; // M for moving N for not moving
		NetworkManager.sendMessageToServer(("CPOS" + clientNo + "X" + posX + "Y" + posY + move).getBytes());
	}

	private void sendUpdatedPositions()
	{
		String message = "SPOS";

		for (int thePlayer = 0; thePlayer < totalClients; thePlayer++)
		{
			// The host
			if (thePlayer != clientNo)
			{
				message += "I" + thePlayer + serverOtherPlayers[thePlayer];
			}
			else
			{
				boolean moving = player.isMoving();
				String move = moving ? "M" : "N"; // M for moving N for not moving
				message += "I" + clientNo + "X" + player.getX() + "Y" + player.getY() + move;
			}
		}

		NetworkManager.sendMessageToAllClients(message.getBytes());
	}

	public void draw(GraphicsContext g)
	{
		g.drawImage(bg, 0, 0);

		tileMap.draw(g);

		drawOtherPlayers(g);

		player.draw(g);

		if (gameOver)
		{
			g.drawImage(gameOverPopup, gameOverX, gameOverY);
		}

		if (showMap)
		{
			tileMap.drawMiniMap(g, posX, posY);
		}
	}

	public void cleanUp()
	{
		bg = null;
		gameOverPopup = null;
		tileMap = null;
		player = null;
		clientOtherPlayers = null;
		serverOtherPlayers = null;
	}

	@Override
	/**
	 * Processes messages directed at the server
	 */
	public void processServerMessage(DataPacket packet)
	{
		String data = packet.data;
		// CNEWPOS is new position of the client
		if (data.contains("CPOS"))
		{
			int clientNum = Integer.parseInt(data.substring(4, 5));

			int indexOfX = data.indexOf('X');
			int indexOfY = data.indexOf('Y');
			int clientX = Integer.parseInt(data.substring(indexOfX + 1, indexOfY));
			int clientY = Integer.parseInt(data.substring(indexOfY + 1, data.length() - 1));
			char moving = data.charAt(data.length() - 1);
			boolean move = moving == 'M' ? true : false;
			serverOtherPlayers[clientNum].setXandY(clientX, clientY);
			serverOtherPlayers[clientNum].setMoving(move);
			clientOtherPlayers[clientNum].setXandY(clientX, clientY);
			clientOtherPlayers[clientNum].setMoving(move);
		}
		else if (data.contains("GOAL"))
		{
			int clientNum = Integer.parseInt(data.substring(4));
			NetworkManager.sendMessageToAllClients(("GOAL" + clientNum).getBytes());
			System.out.println("SERVER - CLIENT NUMBER: " + clientNum + " REACHED THE GOAL; NOTIFIED OTHER CLIENTS");
			processGameOver(clientNo);

			// Send confirmation
			NetworkManager.sendMessageToClient("RCVGOAL", packet.ip, packet.port);
		}
		else if (data.contains("EXITING"))
		{
			totalClients--;
			System.out.println("SERVER - CLIENT NUMBER: " + Integer.parseInt(data.substring(7)) + " DISCONNECTED.");
		}
	}

	@Override
	public void processClientMessage(String data)
	{
		// SNEWPOS is the new positions from the server
		if (data.contains("SPOS"))
		{
			int lastId = 0;
			int lastIndexOfX = 0;
			int lastIndexOfY = 0;
			for (int theClient = 0; theClient < totalClients; theClient++)
			{
				int indexOfId = data.indexOf("I", lastId);
				int clientNum = Integer.parseInt(data.substring(indexOfId + 1, indexOfId + 2));

				int indexOfX = data.indexOf("X", lastIndexOfX);
				int indexOfY = data.indexOf("Y", lastIndexOfY);
				int clientX = Integer.parseInt(data.substring(indexOfX + 1, indexOfY));
				int nextId = data.indexOf("I", indexOfY);

				int clientY = 0;
				boolean moving;
				if (nextId != -1)
				{
					clientY = Integer.parseInt(data.substring(indexOfY + 1, nextId - 1));
					char move = data.charAt(nextId - 1);
					moving = move == 'M' ? true : false;
				}
				else
				{
					clientY = Integer.parseInt(data.substring(indexOfY + 1, data.length() - 1));
					char move = data.charAt(data.length() - 1);
					moving = move == 'M' ? true : false;
				}

				clientOtherPlayers[clientNum].setXandY(clientX, clientY);
				clientOtherPlayers[clientNum].setMoving(moving);

				lastId = indexOfId + 1;
				lastIndexOfX = indexOfX + 1;
				lastIndexOfY = indexOfY + 1;
			}
		}
		else if (data.contains("GOAL"))
		{
			int winningClient = Integer.parseInt(data.substring(4));
			processGameOver(winningClient);
			System.out.println("CLIENT - CLIENT NUMBER: " + winningClient + " REACHED THE GOAL.");

			// Send confirmation
			NetworkManager.sendMessageToServer("RCVGOAL" + clientNo);
		}
		else if (data.contains("NEWNUM"))
		{
			try
			{
				clientNo = Integer.parseInt(data.substring(6, data.length()));
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("Error receiving new client no");
				nfe.printStackTrace();
			}

			// Send confirmation
			NetworkManager.sendMessageToServer("RCVNEWNUM" + clientNo);
		}
		// TODO: REMOVE WHEN NOT DEBUGGING
		else
		{
			/* Send confirmation from things from last state */
			if (data.contains("PLAY"))
			{
				// Send confirmation
				NetworkManager.sendMessageToServer("RCVPLAY" + clientNo);
			}
			else if (data.contains("TOTALCLIENTS"))
			{
				// Send confirmation
				NetworkManager.sendMessageToServer("RCVTOT" + clientNo);
			}
			else if (data.contains("DIFF"))
			{
				// Send confirmation
				NetworkManager.sendMessageToServer("RCVDIFF" + clientNo);
			}
			else
			{
				System.err.println("Invalid packet received: " + data);
			}
		}

	}

	private void processGameOver(int winningClient)
	{
		gameOver = true;
		player.lockMovement();
	}

	private void createOtherPlayers()
	{
		serverOtherPlayers = new OtherPlayer[totalClients];

		otherPlayerMove = new Image(getClass().getResourceAsStream("/Players/builderWalk.png"));
		otherPlayerIdle = new Image(getClass().getResourceAsStream("/Players/builderIdle.png"));

		// Whoever runs the server's position
		serverOtherPlayers[0] = new OtherPlayer(tileSize, tileSize, otherPlayerMove,
				otherPlayerIdle, 44, 68, false, tileMap);
		if (totalClients > 1)
		{
			serverOtherPlayers[1] = new OtherPlayer((tileMap.getNumCols() - 1) * tileSize, (tileMap.getNumRows() - 1) * tileSize, otherPlayerMove,
					otherPlayerIdle, 44, 68, false, tileMap);
		}
		if (totalClients > 2)
		{
			serverOtherPlayers[2] = new OtherPlayer(tileSize, (tileMap.getNumRows() - 1) * tileSize, otherPlayerMove,
					otherPlayerIdle, 44, 68, false, tileMap);
		}
		if (totalClients > 3)
		{
			serverOtherPlayers[3] = new OtherPlayer((tileMap.getNumCols() - 1) * tileSize, tileSize, otherPlayerMove,
					otherPlayerIdle, 44, 68, false, tileMap);
		}

		clientOtherPlayers = new OtherPlayer[totalClients];

		// Whoever runs the server's position
		clientOtherPlayers[0] = new OtherPlayer(tileSize, tileSize, otherPlayerMove,
				otherPlayerIdle, 44, 68, true, tileMap);
		if (totalClients > 1)
		{
			clientOtherPlayers[1] = new OtherPlayer((tileMap.getNumCols() - 1) * tileSize, (tileMap.getNumRows() - 1) * tileSize, otherPlayerMove,
					otherPlayerIdle, 44, 68, true, tileMap);
		}
		if (totalClients > 2)
		{
			clientOtherPlayers[2] = new OtherPlayer(tileSize, (tileMap.getNumRows() - 1) * tileSize, otherPlayerMove,
					otherPlayerIdle, 44, 68, true, tileMap);
		}
		if (totalClients > 3)
		{
			clientOtherPlayers[3] = new OtherPlayer((tileMap.getNumCols() - 1) * tileSize, tileSize, otherPlayerMove,
					otherPlayerIdle, 44, 68, true, tileMap);
		}

	}

	private void drawOtherPlayers(GraphicsContext g)
	{
		for (int otherPlayer = 0; otherPlayer < totalClients; otherPlayer++)
		{
			if (otherPlayer != clientNo)
			{
				clientOtherPlayers[otherPlayer].draw(g);
			}
		}
	}

	private void checkIfReachedGoal()
	{
		if (player.reachedGoal() && !sentGameOverOnce)
		{
			if (isHost)
			{
				NetworkManager.sendGuaranteedMessageToAllClients(("GOAL" + clientNo).getBytes(), PacketTypes.TYPE_GOAL);
				System.out.println("SERVER PLAYER REACHED GOAL; NOTIFIED CLIENTS.");
				processGameOver(clientNo);
			}
			else
			{
				NetworkManager.sendGuaranteedMessageToServer(("GOAL" + clientNo).getBytes(), clientNo, PacketTypes.TYPE_GOAL);
				System.out.println("CLIENT NUMBER: " + clientNo + " REACHED GOAL; NOTIFIED SERVER");
			}
			sentGameOverOnce = true;
		}
	}

	public void mousePressed(MouseEvent e)
	{
		@SuppressWarnings("unused")
		int x = (int) e.getX();
		int y = (int) e.getY();
		if (gameOver)
		{
			if (y < 200)
			{
				GameStateManager.setState(GameStateManager.MENU_STATE);
			}
		}
	}

	public void mouseReleased(MouseEvent e)
	{

	}

}

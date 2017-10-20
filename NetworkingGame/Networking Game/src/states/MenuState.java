package states;

import helperClasses.VaraibleStorage;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import networking.DataPacket;
import networking.NetworkManager;

public class MenuState implements GameState
{
	private Image bg;

	public MenuState()
	{
	}

	public void init()
	{
		bg = new Image(getClass().getResourceAsStream("/Backgrounds/MenuBackground.png"));
		//Everytime the user is at the main menu, the game is reset
		VaraibleStorage.clear();
		NetworkManager.reset();
	}

	public void update()
	{
	}

	public void draw(GraphicsContext g)
	{
		g.drawImage(bg, 0, 0);
	}

	public void cleanUp()
	{
		bg = null;
	}

	public void mousePressed(MouseEvent e)
	{
		int x = (int) e.getX();
		int y = (int) e.getY();

		// Quit button
		if (x < 100 && y > 350)
		{
			System.exit(0);
		}
		// Host button
		else if (x < 165 && y > 260 && y < 300)
		{
			VaraibleStorage.setIsHost(true);
			GameStateManager.setState(GameStateManager.LOBBY_STATE);
		}
		// Join button
		else if (x > 410 && y > 260 && y < 300)
		{
			VaraibleStorage.setIsHost(false);
			GameStateManager.setState(GameStateManager.LOBBY_STATE);
		}
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void processServerMessage(DataPacket data)
	{
	}

	public void processClientMessage(String data)
	{
	}
}

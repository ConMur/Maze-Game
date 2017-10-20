package states;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

public class GameStateManager
{
	public static final int MENU_STATE = 0;
	public static final int LOBBY_STATE = 1;
	public static final int PLAY_STATE = 2;
	public static final int GAME_OVER_STATE = 3;

	private static int currentState;
	private static final int MAX_STATES = 4;
	private static ArrayList<GameState> states;

	private static Image loadingImage;

	private static boolean doneLoading;

	public GameStateManager()
	{
		currentState = 0;
		states = new ArrayList<GameState>();
		states.add(new MenuState());
		states.add(new LobbyState());
		states.add(new PlayState());

		doneLoading = false;

		loadingImage = new Image(getClass().getResourceAsStream("/Backgrounds/LoadingBackground.png"));
	}

	public static void setState(int state)
	{
		doneLoading = false;
		if (state < 0 || state > MAX_STATES)
			throw new IllegalArgumentException(state + " is an invalid state");
		states.get(currentState).cleanUp();
		currentState = state;
		states.get(currentState).init();
		doneLoading = true;
	}

	public static GameState getCurrentState()
	{
		return states.get(currentState);
	}

	public static int getCurrentStateValue()
	{
		return currentState;
	}

	public static void update()
	{
		if (doneLoading)
			states.get(currentState).update();
	}

	public static void draw(GraphicsContext g)
	{
		if (doneLoading)
			states.get(currentState).draw(g);
		else
		{
			g.drawImage(loadingImage, 0, 0);
		}
	}

	public static void mousePressed(MouseEvent e)
	{
		states.get(currentState).mousePressed(e);
	}

	public static void mouseReleased(MouseEvent e)
	{
		states.get(currentState).mouseReleased(e);
	}

}
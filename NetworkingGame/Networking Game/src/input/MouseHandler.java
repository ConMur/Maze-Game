package input;

import states.GameStateManager;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

public class MouseHandler
{

	public static void createMousePressedHandler(Scene scene, GameStateManager gsm)
	{
		final EventHandler<MouseEvent> handler = new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				if(e.getEventType() == MouseEvent.MOUSE_PRESSED)
				{
					GameStateManager.getCurrentState().mousePressed(e);
				}
				else if(e.getEventType() == MouseEvent.MOUSE_RELEASED)
				{
					GameStateManager.getCurrentState().mouseReleased(e);
				}
					
			}

		};
		
		scene.setOnMousePressed(handler);
	}
}

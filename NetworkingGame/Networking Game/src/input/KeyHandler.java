package input;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyHandler
{
	public static void createKeyHandler(Scene scene)
	{
		final EventHandler<KeyEvent> handler = new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent e)
			{
				boolean pressed = e.getEventType() == KeyEvent.KEY_PRESSED;
				KeyCode key = e.getCode();
				if (key == KeyCode.W || key == KeyCode.UP)
					Input.setUp(pressed);
				if (key == KeyCode.S || key == KeyCode.DOWN)
					Input.setDown(pressed);
				if (key == KeyCode.A || key == KeyCode.LEFT)
					Input.setLeft(pressed);
				if (key == KeyCode.D || key == KeyCode.RIGHT)
					Input.setRight(pressed);
				if(pressed)
				{
					Input.setKeyPressed(key);
				}
				else
				{
					Input.setKeyReleased(key);
				}
			}

		};
		
		scene.setOnKeyPressed(handler);
		scene.setOnKeyReleased(handler);
	}
}

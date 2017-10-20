package input;

import java.util.ArrayList;

import javafx.scene.input.KeyCode;

public class Input
{
	private static boolean isUp = false;
	private static boolean isDown = false;
	private static boolean isLeft = false;
	private static boolean isRight = false;

	private static int mouseX = 0;
	private static int mouseY = 0;

	private static ArrayList<KeyCode> pressedKeys = new ArrayList<KeyCode>();

	public static void setUp(boolean up)
	{
		isUp = up;
	}

	public static boolean isUp()
	{
		return isUp;
	}

	public static void setDown(boolean down)
	{
		isDown = down;
	}

	public static boolean isDown()
	{
		return isDown;
	}

	public static void setLeft(boolean left)
	{
		isLeft = left;
	}

	public static boolean isLeft()
	{
		return isLeft;
	}

	public static void setRight(boolean right)
	{
		isRight = right;
	}

	public static boolean isRight()
	{
		return isRight;
	}

	public static int getMouseX()
	{
		return mouseX;
	}

	public static void setMouseX(int mouseX)
	{
		Input.mouseX = mouseX;
	}

	public static int getMouseY()
	{
		return mouseY;
	}

	public static void setMouseY(int mouseY)
	{
		Input.mouseY = mouseY;
	}

	public static void setKeyPressed(KeyCode code)
	{
		if (!pressedKeys.contains(code))
			pressedKeys.add(code);
	}

	public static void setKeyReleased(KeyCode code)
	{
		pressedKeys.remove(code);
	}

	public static boolean isKeyPressed(KeyCode code)
	{
		return pressedKeys.contains(code);
	}

}

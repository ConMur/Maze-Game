package main;

import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import helperClasses.VaraibleStorage;
import input.Input;
import input.KeyHandler;
import input.MouseHandler;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import states.GameStateManager;

public class GamePanel extends Application
{
	public final static int WIDTH = 560;
	public final static int HEIGHT = 400;

	public static final float MS_PER_FRAME = 1000.0f / 60.0f;

	private GameStateManager gsm = new GameStateManager();
	private GraphicsContext gc;

	long lastTime = System.nanoTime();
	final double ticks = 60D;
	double ns = 1000000000 / ticks;
	public static float delta = 0;

	public static void main(String[] args)
	{
//		try
//		{
//			System.setErr(new PrintStream("Crash.txt"));
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
		launch(args);
	}

	@Override
	public void start(Stage primaryStage)
	{
		primaryStage.setTitle("Game");
		Group root = new Group();
		Scene scene = new Scene(root, WIDTH, HEIGHT);
		KeyHandler.createKeyHandler(scene);
		MouseHandler.createMousePressedHandler(scene, gsm);
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		gc = canvas.getGraphicsContext2D();
		GameStateManager.setState(GameStateManager.MENU_STATE);

		new AnimationTimer()
		{
			@Override
			public void handle(long now)
			{
				gc.clearRect(0, 0, WIDTH, HEIGHT);

				delta += (now - lastTime) / ns;
				lastTime = now;
				if (delta >= 1)
				{
					GameStateManager.update();
					GameStateManager.draw(gc);
					delta--;
				}
			}
		}.start();

		root.getChildren().add(canvas);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/Icons/GameIcon.png")));
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(e -> handleClosing());
		primaryStage.setResizable(false);
		primaryStage.centerOnScreen();
		primaryStage.requestFocus();
		primaryStage.show();
	}

	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
			Input.setUp(true);
		if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
			Input.setDown(true);
		if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
			Input.setLeft(true);
		if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
			Input.setRight(true);
	}

	public void keyReleased(KeyEvent e)
	{
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
			Input.setUp(false);
		if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
			Input.setDown(false);
		if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
			Input.setLeft(false);
		if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
			Input.setRight(false);
	}

	private void handleClosing()
	{
		VaraibleStorage.setClosing(true);
		System.exit(0);
	}

}

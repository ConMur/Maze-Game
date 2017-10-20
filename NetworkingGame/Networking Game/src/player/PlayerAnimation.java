package player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class PlayerAnimation
{
	// Amimation
	private int currentMoveImage;
	private int numMoveImages;
	private Image[] moveImages;
	private long lastMoveAnimationTime;
	private final int ANIMATION_RATE = 100; // miliseconds between each animation

	private int currentIdleImage;
	private int numIdleImages;
	private Image[] idleImages;
	private long lastIdleAnimationTime;
	private final int IDLE_RATE = 200;

	private int drawX, drawY;

	private final int IMAGE_X_OFFSET = 18;
	private final int IMAGE_Y_OFFSET = 8;

	private boolean moving;

	public PlayerAnimation(Image moveSequence, Image idleSequence, int width, int height)
	{
		// Animation
		moving = false;

		drawX = 0;
		drawY = 0;

		lastMoveAnimationTime = 0;

		numMoveImages = (int) (moveSequence.getWidth() / width);

		moveImages = new Image[numMoveImages];

		PixelReader reader = moveSequence.getPixelReader();

		for (int frame = 0; frame < numMoveImages; frame++)
		{
			moveImages[frame] = new WritableImage(reader, frame * width, 0, width, height);
		}

		lastIdleAnimationTime = 0;

		numIdleImages = (int) (idleSequence.getWidth() / width);

		idleImages = new Image[numIdleImages];

		reader = idleSequence.getPixelReader();

		for (int frame = 0; frame < numIdleImages; frame++)
		{
			idleImages[frame] = new WritableImage(reader, frame * width, 0, width, height);
		}
	}

	public void update(int x, int y)
	{
		drawX = x + IMAGE_X_OFFSET;
		drawY = y + IMAGE_Y_OFFSET;

		// Check if the animation should change
		long currentMillis = System.currentTimeMillis();
		if (currentMillis - lastMoveAnimationTime > ANIMATION_RATE)
		{
			currentMoveImage++;
			if (currentMoveImage >= numMoveImages)
			{
				currentMoveImage = 0;
			}

			lastMoveAnimationTime = currentMillis;
		}
		if (currentMillis - lastIdleAnimationTime > IDLE_RATE)
		{
			currentIdleImage++;
			if (currentIdleImage >= numIdleImages)
			{
				currentIdleImage = 0;
			}

			lastIdleAnimationTime = currentMillis;
		}
	}
	
	public void setMoving(boolean move)
	{
		moving = move;
	}

	public void animate(GraphicsContext g)
	{
		if (moving)
			g.drawImage(moveImages[currentMoveImage], drawX, drawY);
		else
			g.drawImage(idleImages[currentIdleImage], drawX, drawY);
	}

}

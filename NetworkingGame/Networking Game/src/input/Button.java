package input;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class Button
{
	private int x, y;
	private int width, height;
	private Image image;

	private boolean isClicked;

	public Button(Image image, int x, int y)
	{
		this.image = image;
		width = (int) image.getWidth();
		height = (int) image.getHeight();
		this.x = x;
		this.y = y;

		isClicked = false;
	}

	public void mosuePressed(MouseEvent e)
	{
		double mouseX = e.getX();
		double mouseY = e.getY();

		Rectangle r = new Rectangle(x, y, width, height);
		if (r.contains(mouseX, mouseY))
		{
			isClicked = true;
		}
	}

	public boolean isClicked()
	{
		return isClicked;
	}

	public void draw(GraphicsContext g)
	{
		g.drawImage(image, x, y);
	}
}

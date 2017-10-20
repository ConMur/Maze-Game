package tileMap;

import javafx.scene.CacheHint;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Tile
{
	private Image image;
	private Image walkedOnImage;
	private Type type;
	private boolean walkedOn;

	public enum Type {
		WALL, PATH, GOAL
	}

	public Tile(Image image, Type type)
	{
		this.image = image;
		
		//TODO: find out how to make this change the image colour
		ColorAdjust ca = new ColorAdjust();
		ca.setHue(359);

		ImageView iv = new ImageView();
		iv.setImage(image);
		iv.setEffect(ca);
		iv.setCache(true);
		iv.setCacheHint(CacheHint.SPEED);

		walkedOnImage = iv.getImage();
		this.type = type;

		walkedOn = false;
	}

	public Image getImage()
	{
		if (walkedOn)
			return walkedOnImage;
		else
			return image;
	}

	public Type getType()
	{
		return type;
	}

	public void walkedOn()
	{
		walkedOn = true;
	}
}

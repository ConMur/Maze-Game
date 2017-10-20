package player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import tileMap.TileMap;

public class OtherPlayer
{
	private int x, y;

	private TileMap tileMap;

	private PlayerAnimation animation;

	private boolean moving;
	private boolean adjustForTilemap;

	public OtherPlayer(int x, int y, Image moveSequence, Image idleSequence, int width, int height, boolean adjustForTilemap, TileMap tileMap)
	{
		animation = new PlayerAnimation(moveSequence, idleSequence, width, height);
		moving = false;
		animation.setMoving(moving);

		this.x = x;
		this.y = y;
		this.adjustForTilemap = adjustForTilemap;
		this.tileMap = tileMap;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public void setMoving(boolean move)
	{
		moving = move;
		animation.setMoving(moving);
	}

	public void setXandY(int x, int y)
	{
		this.x = x;
		this.y = y;
		if (adjustForTilemap)
			adjustForTileMapPos();
		animation.update(this.x, this.y);
	}

	private void adjustForTileMapPos()
	{
		int tileMapX = tileMap.getX();
		int tileMapY = tileMap.getY();

		this.x = tileMapX + x;
		this.y = tileMapY + y;
	}

	public String toString()
	{
		String move = moving ? "M" : "N"; // M for moving N for not moving
		return "X" + x + "Y" + y + move;
	}

	public void draw(GraphicsContext g)
	{
		animation.animate(g);
	}
}

package tileMap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import helperClasses.MazeGenerator;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import main.GamePanel;
import tileMap.Tile.Type;

public class TileMap
{
	// Position
	private int x, y;

	// Bounds
	private int xMin, xMax, yMin, yMax;

	// Map
	private byte[][] maze;
	private Tile[][] mapOfTiles;
	private int tileSize;

	private int numRows;
	private int numCols;
	private int width, height;

	// Tileset
	private int numTilesAccross;
	private Image[] tileImages;
	private Tile[] minimapTiles;

	// Drawing
	private int rowOffset, colOffset;
	private int numRowsToDraw, numColsToDraw;

	private static final int WALL = 0;
	private static final int PATH = 1;
	private static final int GOAL = 2;

	private final int MINIMAP_TILE_SIZE = 5;
	private int minimapOffsetX, minimapOffsetY;

	public TileMap(int tileSize)
	{
		this.tileSize = tileSize;
		numRowsToDraw = GamePanel.HEIGHT / tileSize + 2;
		numColsToDraw = GamePanel.WIDTH / tileSize + 2;
		x = 0;
		y = 0;

		try
		{
			numTilesAccross = 3;
			tileImages = new Image[numTilesAccross];

			// path image
			tileImages[0] = new Image(getClass().getResourceAsStream("/Tiles/PathTile.png"));
			// wall image
			tileImages[1] = new Image(getClass().getResourceAsStream("/Tiles/BlockedTile.png"));
			// Goal image
			tileImages[2] = new Image(getClass().getResourceAsStream("/Tiles/GoalTile.png"));

			minimapTiles = new Tile[numTilesAccross];

			// path image
			minimapTiles[0] = new Tile(new Image(getClass().getResourceAsStream("/Tiles/PathTile.png"), MINIMAP_TILE_SIZE, MINIMAP_TILE_SIZE, false, false),
					Type.PATH);
			// wall image
			minimapTiles[1] = new Tile(new Image(getClass().getResourceAsStream("/Tiles/BlockedTile.png"), MINIMAP_TILE_SIZE, MINIMAP_TILE_SIZE, false, false),
					Type.WALL);
			// Goal image
			minimapTiles[2] = new Tile(new Image(getClass().getResourceAsStream("/Tiles/GoalTile.png"), MINIMAP_TILE_SIZE, MINIMAP_TILE_SIZE, false, false),
					Type.WALL);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		colOffset = 2;
		rowOffset = 2;

		minimapOffsetX = 0;
		minimapOffsetY = 0;
	}

	public void loadMap(String path, long seed, boolean test)
	{
		try
		{
			InputStream in = TileMap.class.getResourceAsStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			numCols = Integer.parseInt(br.readLine());
			numRows = Integer.parseInt(br.readLine());
			MazeGenerator.setSeed(seed);
			maze = MazeGenerator.generateMaze(numRows, numCols, test);
			width = numCols * tileSize;
			height = numRows * tileSize;
			xMax = 0;
			xMin = GamePanel.WIDTH - width;
			yMax = 0;
			yMin = GamePanel.HEIGHT - height;

			minimapOffsetX = GamePanel.WIDTH / 2 - ((MINIMAP_TILE_SIZE * numCols) / 2);
			minimapOffsetY = GamePanel.HEIGHT / 2 - ((MINIMAP_TILE_SIZE * numRows) / 2);
			
			mapOfTiles = new Tile[maze[0].length][maze.length];
			for (int row = 0; row < maze[0].length; row++)
			{
				for (int col = 0; col < maze.length; col++)
				{
					byte tile = maze[row][col];
					if (tile == PATH)
						mapOfTiles[row][col] = new Tile(tileImages[0], Type.PATH);
					else if (tile == WALL)
						mapOfTiles[row][col] = new Tile(tileImages[1], Type.WALL);
					else if(tile == GOAL)
						mapOfTiles[row][col] = new Tile(tileImages[2], Type.GOAL);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int getTileSize()
	{
		return tileSize;
	}

	public int getX()
	{
		return (int) x;
	}

	public int getY()
	{
		return (int) y;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public int getNumRows()
	{
		return numRows;
	}

	public int getNumCols()
	{
		return numCols;
	}

	public byte[][] getMaze()
	{
		return maze;
	}

	public Type getType(int row, int col)
	{
		// If reqested row or col is out of bounds, return that it is a wall
		if (row < 0 || col < 0 || row > numRows || col > numCols)
			return Type.WALL;

		byte nextTile = maze[row][col];
		if (nextTile == PATH)
			return Type.PATH;
		else if (nextTile == WALL)
			return Type.WALL;
		else if (nextTile == GOAL)
			return Type.GOAL;
		else
			return null;
	}

	public void setTileWalkedOn(int row, int col)
	{
		mapOfTiles[row][col].walkedOn();
	}

	public void setPosition(int x, int y)
	{
		this.x = x;
		this.y = y;

		fixBounds();

		colOffset = (int) -(this.x / tileSize);
		rowOffset = (int) -(this.y / tileSize);
	}

	private void fixBounds()
	{
		if (x < xMin)
			x = xMin;
		if (y < yMin)
			y = yMin;
		if (x > xMax)
			x = xMax;
		if (y > yMax)
			y = yMax;
	}

	public void draw(GraphicsContext g)
	{
		for (int row = rowOffset; row < rowOffset + numRowsToDraw; row++)
		{
			if (row >= numRows)
				break;
			for (int col = colOffset; col < colOffset + numColsToDraw; col++)
			{
				if (col >= numRows)
					break;
				
				g.drawImage(mapOfTiles[row][col].getImage(), x + col * tileSize, y + row * tileSize);
			}
		}
	}

	public void drawMiniMap(GraphicsContext g, int posX, int posY)
	{
		int drawOnMapX = (posX / tileSize) * MINIMAP_TILE_SIZE + minimapOffsetX;
		int drawOnMapY = (posY / tileSize) * MINIMAP_TILE_SIZE + minimapOffsetY;

		// Draw map
		for (int row = 0; row < maze[0].length; row++)
		{
			for (int col = 0; col < maze.length; col++)
			{
				byte tile = maze[row][col];
				if (tile == PATH)
				{
					g.drawImage(minimapTiles[0].getImage(), col * MINIMAP_TILE_SIZE + minimapOffsetX, row * MINIMAP_TILE_SIZE + minimapOffsetY);
				}
				else if (tile == WALL)
				{
					g.drawImage(minimapTiles[1].getImage(), col * MINIMAP_TILE_SIZE + minimapOffsetX, row * MINIMAP_TILE_SIZE + minimapOffsetY);
				}
				else if (tile == GOAL)
				{
					g.drawImage(minimapTiles[0].getImage(), col * MINIMAP_TILE_SIZE + minimapOffsetX, row * MINIMAP_TILE_SIZE + minimapOffsetY);
					g.drawImage(minimapTiles[2].getImage(), col * MINIMAP_TILE_SIZE + minimapOffsetX, row * MINIMAP_TILE_SIZE + minimapOffsetY);
				}
			}
		}

		// Draw where player is
		g.setFill(Paint.valueOf("BLUE"));
		g.fillRect(drawOnMapX, drawOnMapY, MINIMAP_TILE_SIZE, MINIMAP_TILE_SIZE);
	}
}

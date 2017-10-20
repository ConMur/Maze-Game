package player;

import input.Input;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import main.GamePanel;
import tileMap.Tile.Type;
import tileMap.TileMap;

public class Player
{
	private int x, y;
	private int drawX, drawY;
	private int currentCol, currentRow;

	private TileMap tileMap;
	private int tileSize;

	private boolean moving;
	private boolean moveUp, moveDown, moveLeft, moveRight;
	private boolean reachedGoal;
	private int distanceMoved;
	private int speed;

	private byte[][] maze;

	private boolean up, down, left, right;

	private boolean canMove;

	private final int MAZE_WIDTH, MAZE_HEIGHT;
	private final int STOP_MOVE_X_MIN, STOP_MOVE_Y_MIN;
	private final int STOP_MOVE_X_MAX, STOP_MOVE_Y_MAX;
	private final int CENTRE_TILE_X, CENTRE_TILE_Y;
	private final int HALF_SCREEN_WIDTH, HALF_SCREEN_HEIGHT;
	private final int HALF_TILE;

	PlayerAnimation animation;

	public Player(Image moveSequence, Image idleSequence, int width, int height, TileMap tileMap)
	{
		x = 0;
		y = 0;

		animation = new PlayerAnimation(moveSequence, idleSequence, width, height);

		moving = false;
		moveUp = false;
		moveDown = false;
		moveLeft = false;
		moveRight = false;
		distanceMoved = 0;
		speed = 5;
		canMove = true;

		this.tileMap = tileMap;
		tileSize = tileMap.getTileSize();
		this.maze = tileMap.getMaze();
		MAZE_WIDTH = maze.length * tileSize;
		MAZE_HEIGHT = maze[0].length * tileSize;
		HALF_SCREEN_WIDTH = GamePanel.WIDTH / 2;
		HALF_SCREEN_HEIGHT = GamePanel.HEIGHT / 2;
		HALF_TILE = tileSize / 2;
		CENTRE_TILE_X = HALF_SCREEN_WIDTH - HALF_TILE;
		CENTRE_TILE_Y = HALF_SCREEN_HEIGHT - HALF_TILE;
		STOP_MOVE_X_MIN = CENTRE_TILE_X;
		STOP_MOVE_Y_MIN = CENTRE_TILE_Y;
		STOP_MOVE_X_MAX = MAZE_WIDTH - CENTRE_TILE_X - tileSize;
		STOP_MOVE_Y_MAX = MAZE_HEIGHT - CENTRE_TILE_Y - tileSize; // Add somehting like above
		drawX = CENTRE_TILE_X;
		drawY = CENTRE_TILE_Y;

		up = false;
		down = false;
		left = false;
		right = false;

		reachedGoal = false;
	}

	public void setStartPos(int startX, int startY)
	{
		x = startX;
		y = startY;
		currentCol = x / tileSize;
		currentRow = y / tileSize;
	}

	public void setSpeed(int theSpeed)
	{
		speed = theSpeed;
	}

	/**
	 * Moves the player in the specified direction if it can
	 */
	public void move()
	{
		findWhereToMove();
		tryToMove();
		updatePosition();
		findWhereToDraw();

		// Update Tilemap
		tileMap.setPosition(HALF_SCREEN_WIDTH - HALF_TILE - x, HALF_SCREEN_HEIGHT - HALF_TILE - y);

		animation.setMoving(moving);
		animation.update(drawX, drawY);
	}

	private void findWhereToMove()
	{
		if (!moving)
		{
			up = false;
			down = false;
			left = false;
			right = false;

			if (Input.isUp() && !Input.isDown())
			{
				up = true;
			}
			if (Input.isDown() && !Input.isUp())
			{
				down = true;
			}
			if (Input.isLeft() && !Input.isRight())
			{
				left = true;
			}
			if (Input.isRight() && !Input.isLeft())
			{
				right = true;
			}
		}
	}

	private void tryToMove()
	{
		if (!moving && canMove)
		{
			Type type = null;
			if (up)
			{
				type = tileMap.getType(currentRow - 1, currentCol);
				if (type == Type.PATH || type == Type.GOAL)
				{
					moving = true;
					moveUp = true;
					if (type == Type.GOAL)
					{
						reachedGoal = true;
					}
					tileMap.setTileWalkedOn(currentRow - 1, currentCol);
				}
			}
			else if (down)
			{
				type = tileMap.getType(currentRow + 1, currentCol);
				if (type == Type.PATH || type == Type.GOAL)
				{
					moving = true;
					moveDown = true;
					if (type == Type.GOAL)
					{
						reachedGoal = true;
					}
					tileMap.setTileWalkedOn(currentRow + 1, currentCol);
				}
			}
			else if (left)
			{
				type = tileMap.getType(currentRow, currentCol - 1);
				if (type == Type.PATH || type == Type.GOAL)
				{
					moving = true;
					moveLeft = true;
					if (type == Type.GOAL)
					{
						reachedGoal = true;
					}
					tileMap.setTileWalkedOn(currentRow, currentCol - 1);
				}
			}
			else if (right)
			{
				type = tileMap.getType(currentRow, currentCol + 1);
				if (type == Type.PATH || type == Type.GOAL)
				{
					moving = true;
					moveRight = true;
					if (type == Type.GOAL)
					{
						reachedGoal = true;
					}
					tileMap.setTileWalkedOn(currentRow, currentCol + 1);
				}
			}
		}
	}

	private void updatePosition()
	{
		if (moving && distanceMoved < tileSize)
		{
			if (moveUp)
			{
				y -= speed;
			}
			else if (moveDown)
			{
				y += speed;
			}
			else if (moveLeft)
			{
				x -= speed;
			}
			else if (moveRight)
			{
				x += speed;
			}
			distanceMoved += speed;
		}
		else if (distanceMoved >= tileSize)
		{
			moving = false;
			distanceMoved = 0;

			// Update row or column
			if (moveUp)
			{
				currentRow--;
				moveUp = false;
			}
			else if (moveDown)
			{
				currentRow++;
				moveDown = false;
			}
			else if (moveLeft)
			{
				currentCol--;
				moveLeft = false;
			}
			else if (moveRight)
			{
				currentCol++;
				moveRight = false;
			}
		}
	}

	private void findWhereToDraw()
	{
		if (x < STOP_MOVE_X_MIN && y < STOP_MOVE_Y_MIN)
		{
			drawX = x;
			drawY = y;
		}
		else if (x >= STOP_MOVE_X_MAX && y >= STOP_MOVE_Y_MAX)
		{
			drawX = GamePanel.WIDTH - (MAZE_WIDTH - x);
			drawY = GamePanel.HEIGHT - (MAZE_HEIGHT - y);
		}
		else if (x < STOP_MOVE_X_MIN && y >= STOP_MOVE_Y_MAX)
		{
			drawX = x;
			drawY = GamePanel.HEIGHT - (MAZE_HEIGHT - y);
		}
		else if (y < STOP_MOVE_Y_MIN && x >= STOP_MOVE_X_MAX)
		{
			drawX = GamePanel.WIDTH - (MAZE_WIDTH - x);
			drawY = y;
		}
		else if (y < STOP_MOVE_Y_MIN)
		{
			drawX = CENTRE_TILE_X;
			drawY = y;
		}
		else if (x < STOP_MOVE_X_MIN)
		{
			drawX = x;
			drawY = CENTRE_TILE_Y;
		}
		else if (x >= STOP_MOVE_X_MAX && !(y < STOP_MOVE_Y_MIN))
		{
			drawX = GamePanel.WIDTH - (MAZE_WIDTH - x);
			drawY = CENTRE_TILE_Y;
		}
		else if (y >= STOP_MOVE_Y_MAX && !(x < STOP_MOVE_X_MIN))
		{
			drawX = CENTRE_TILE_X;
			drawY = GamePanel.HEIGHT - (MAZE_HEIGHT - y);
		}
		else
		{
			drawX = CENTRE_TILE_X;
			drawY = CENTRE_TILE_Y;
		}
	}

	public void draw(GraphicsContext g)
	{
		animation.animate(g);
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public boolean reachedGoal()
	{
		return reachedGoal;
	}

	public void lockMovement()
	{
		canMove = false;
	}

	public void unlockMovement()
	{
		canMove = true;
	}

	public boolean isMoving()
	{
		return moving;
	}
}
package helperClasses;

import java.util.ArrayList;
import java.util.Random;

public final class MazeGenerator
{
	private static byte[][] maze;
	private static int height, width;
	private static long seed = 0;

	private static Random dirRandomizer;

	@SuppressWarnings("unused")
	private static final int WALL = 0;
	private static final int PATH = 1;
	private static final int GOAL = 2;

	private static ArrayList<Dir> dirs;

	/**
	 * Dont let anyone instantiate this object
	 */
	private MazeGenerator()
	{
	}

	private enum Dir {
		UP, RIGHT, DOWN, LEFT
	}

	/**
	 * False is wall true is path
	 * @return
	 */
	public static byte[][] generateMaze(int theHeight, int theWidth, boolean test)
	{
		if(test)
		{
			maze = new byte[theHeight][theWidth];
			for(int row = 0; row < maze[0].length; row++)
			{
				for(int col = 0; col < maze.length; col++)
				{
					maze[row][col] = PATH;
				}
			}
			return maze;
		}
		dirs = new ArrayList<Dir>(4);
		
		height = theHeight;
		width = theWidth;
		if (height % 2 == 0 || width % 2 == 0)
			throw new IllegalArgumentException("Paramaters must both be odd");

		maze = new byte[height][width];

		Random rand = null;
		if (seed != 0)
		{
			rand = new Random(seed);
			dirRandomizer = new Random(seed - 1234);
		}
		else
		{
			throw new IllegalStateException("The random number generator's seed has not been set!");
		}

		// r for row c for column
		// Generate random r
		int r = rand.nextInt(height);
		while (r % 2 == 0)
		{
			r = rand.nextInt(height);
		}
		// Generate random c
		int c = rand.nextInt(width);
		while (c % 2 == 0)
		{
			c = rand.nextInt(width);
		}
		// Starting cell
		maze[r][c] = PATH;

		// Allocate the maze with recursive method
		recursion(r, c);

		// Put the goal in the middle
		maze[height / 2][width / 2] = GOAL;

		return maze;
	}

	private static void recursion(int r, int c)
	{
		// 4 random directions
		Dir[] dirs = generateRandomDirections();
		// Examine each direction
		for (int i = 0; i < dirs.length; i++)
		{
			switch (dirs[i])
			{
			case UP: // Up
				// Whether 2 cells up is out or not
				if (r - 2 <= 0)
					continue;
				if (maze[r - 2][c] != PATH)
				{
					maze[r - 2][c] = PATH;
					maze[r - 1][c] = PATH;
					recursion(r - 2, c);
				}
				break;
			case RIGHT: // Right
				// Whether 2 cells to the right is out or not
				if (c + 2 >= width - 1)
					continue;
				if (maze[r][c + 2] != PATH)
				{
					maze[r][c + 2] = PATH;
					maze[r][c + 1] = PATH;
					recursion(r, c + 2);
				}
				break;
			case DOWN: // Down
				// Whether 2 cells down is out or not
				if (r + 2 >= height - 1)
					continue;
				if (maze[r + 2][c] != PATH)
				{
					maze[r + 2][c] = PATH;
					maze[r + 1][c] = PATH;
					recursion(r + 2, c);
				}
				break;
			case LEFT: // Left
				// Whether 2 cells to the left is out or not
				if (c - 2 <= 0)
					continue;
				if (maze[r][c - 2] != PATH)
				{
					maze[r][c - 2] = PATH;
					maze[r][c - 1] = PATH;
					recursion(r, c - 2);
				}
				break;
			}
		}

	}

	/**
	 * Generate an array with random directions 1-4
	 * 
	 * @return Array containing 4 directions in random order
	 */
	private static Dir[] generateRandomDirections()
	{
		Dir[] directions = new Dir[4];
		dirs.add(Dir.UP);
		dirs.add(Dir.DOWN);
		dirs.add(Dir.LEFT);
		dirs.add(Dir.RIGHT);

		for(int direction = 0; direction < directions.length; direction++)
		{
			int nextDirPos = dirRandomizer.nextInt(dirs.size());
			Dir nextDir = dirs.get(nextDirPos);
			directions[direction] = nextDir;
			dirs.remove(nextDirPos);
		}
		return directions;
	}

	public static void setSeed(long value)
	{
		seed = value;
	}

	public static long getSeed()
	{
		return seed;
	}
}

package helperClasses;

public class DelayClosing implements Runnable
{
	private long delay;
	private long startTime;
	
	/**
	 * 
	 * @param delay the delay in seconds until exiting the application
	 */
	public DelayClosing(long delay)
	{
		this.delay = delay;
		startTime = System.currentTimeMillis();
	}
	
	public void run()
	{
		while((System.currentTimeMillis() - startTime) / 1000 != delay)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
	
}
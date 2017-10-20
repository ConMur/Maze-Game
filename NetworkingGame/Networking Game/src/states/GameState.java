package states;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import networking.DataPacket;

public interface GameState
{
	public void init();
	
	public void update();
	
	public void draw(GraphicsContext g);
	
	public void cleanUp();
	
	public void mousePressed(MouseEvent e);
	
	public void mouseReleased(MouseEvent e);
	
	/**
	 * Processes data send to the server
	 * @param data the string of data sent to the server
	 */
	public void processServerMessage(DataPacket data);
	
	/**
	 * Processes data send to the client
	 * @param data the string of data sent to the client
	 */
	public void processClientMessage(String data);
}

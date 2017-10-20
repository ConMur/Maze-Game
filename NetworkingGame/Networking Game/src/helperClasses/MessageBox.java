package helperClasses;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
public class MessageBox
{
	private static TextField field;
	private static String inputMessage = "";
	private static Stage stage;

	public static void showMessageDialog(String message, String title)
	{
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle(title);
		stage.setMinWidth(250);
		stage.setResizable(false);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> stage.close());

		Label label = new Label();
		label.setText(message);

		Button buttonOK = new Button();
		buttonOK.setText("OK");
		buttonOK.setOnAction(e -> stage.close());

		VBox pane = new VBox(20);
		pane.getChildren().addAll(label, buttonOK);
		pane.setPadding(new Insets(10));
		pane.setAlignment(Pos.CENTER);

		Scene scene = new Scene(pane);
		stage.setScene(scene);
		stage.showAndWait();
	}

	public static String showInputDialog(String labelText, String promptText, String title)
	{
		stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle(title);
		stage.setMinWidth(250);
		stage.setResizable(false);
		
		Label inputLabel = new Label(labelText);
		inputLabel.setAlignment(Pos.BOTTOM_RIGHT);

		field = new TextField();
		field.setMinWidth(200);
		field.setMaxWidth(200);
		field.setPromptText(promptText);
		field.addEventHandler(KeyEvent.KEY_PRESSED, e -> processEnterKeyPress(e));

		Button buttonOK = new Button();
		buttonOK.setText("OK");
		buttonOK.setOnAction(e -> processDoneEnteringInfoIntent());
		
		HBox inputPane = new HBox(20, inputLabel, field);
		inputPane.setPadding(new Insets(10));
		
		HBox buttonPane = new HBox(20, buttonOK);
		buttonPane.setPadding(new Insets(10));
		buttonPane.setAlignment(Pos.BOTTOM_CENTER);
		
		VBox pane = new VBox(10, inputPane, buttonPane);
		
		Scene scene = new Scene(pane);
		stage.setScene(scene);
		stage.showAndWait();
		
		//reset the input message
		String temp = inputMessage;
		inputMessage = "";
		return temp;
	}

	private static void processEnterKeyPress(KeyEvent e)
	{
		if(e.getCode() == KeyCode.ENTER)
		{
			processDoneEnteringInfoIntent();
		}
	}

	private static void processDoneEnteringInfoIntent()
	{
		String errorMessage = "";
		
		String fieldText = field.getText();
		
		if(fieldText.length() == 0)
		{
			errorMessage += "\nNothing was entered";
		}
		
		//No error
		if(errorMessage.length() == 0)
		{
			inputMessage = fieldText;
		}
		else
		{
			MessageBox.showMessageDialog(errorMessage, "Missing Data!");
		}
		stage.close();
	}
}

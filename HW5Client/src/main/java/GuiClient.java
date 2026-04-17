
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{

	
	TextField c1;
	Button b1;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;
	ListView<String> listItems2;

	ListView<String> userList;
	
	String userUsername = null;
	TextField inputUsername;
	Label usernameError;
	Stage primaryStage;
	TextField inputServerIP;
	TextField inputPort;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		clientConnection = new Client(message-> Platform.runLater(() -> handleMessage((Message) message)));

							
		clientConnection.start();


		listItems2 = new ListView<String>();
		c1 = new TextField();
		b1 = new Button("Send");
		b1.setOnAction(e -> sendMessage());
		c1.setOnAction(e -> sendMessage());
		
		sceneMap = new HashMap<String, Scene>();

		sceneMap.put("client",  createClientGui());
		sceneMap.put("username", createUsernameGui());
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });


		primaryStage.setScene(sceneMap.get("username"));
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}

	void handleMessage(Message data){
		if(data.type.equals(Message.usernameTaken)){
			usernameError.setText(data.sentMessage);
		}
		else if (data.type.equals(Message.usernameGood)){
			primaryStage.setScene(sceneMap.get("client"));
		}
		else if (data.type.equals(Message.userList)) {
			userList.getItems().setAll(data.users);
		}
		else if (data.type.equals(Message.message)){
			listItems2.getItems().add(data.user + ": " + data.sentMessage);
		}
	}

	void sendUsername(){
		String name = inputUsername.getText();
		if(name.isEmpty()){
			return;
		}
		else {
			userUsername = name;
			clientConnection.send(new Message(Message.userID, null, name));
		}
	}

	public Scene createUsernameGui(){
		inputUsername = new TextField();
		usernameError = new Label("");
		Button jB = new Button("Join");
		inputServerIP = new TextField();
		inputPort = new TextField();

		jB.setOnAction(e -> sendUsername());

		VBox box = new VBox(10, new Label("Enter Server IP:"), inputServerIP,
				new Label("Enter Port:"), inputPort,
				new Label("Enter username: "), inputUsername, usernameError, jB);
		box.setPadding(new Insets(20));
		return new Scene(box, 500, 500);
	}

	
	public Scene createClientGui() {
		userList = new ListView<String>();
		Label ifOnline = new Label("Online: ");
		VBox onlineUsers = new VBox(10, ifOnline, userList);
		onlineUsers.setPrefWidth(120);

		HBox chatInput = new HBox(10, c1, b1);
		HBox.setHgrow(c1, Priority.ALWAYS);



		clientBox = new VBox(30,listItems2, chatInput);
		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");

		BorderPane root = new BorderPane();
		root.setCenter(clientBox);
		root.setRight(onlineUsers);

		return new Scene(root, 400, 300);


	}

	public void sendMessage(){
		String input = c1.getText();
		if(input.isEmpty()){
			return;
		}
		else {
			clientConnection.send(new Message(Message.message, userUsername, input));
		}
	}

}


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
		else if (data.type.equals("color_assigned")) { //what color you are

			if (data.user.equals(userUsername)) {
				listItems2.getItems().add("You are " + data.sentMessage);
			}
		}
		else if (data.type.equals("color_broadcast")) { //tells eveyone you chose a color
			listItems2.getItems().add(data.sentMessage);
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
		VBox colorBox = new VBox(15);
		colorBox.setPadding(new Insets(10));

		Label colorTitle = new Label("Choose a Color");
		colorTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		//Buttons
		Button redBtn = new Button("Red");
		Button blackBtn = new Button("Black");
		//Button nxt = new Button("Continue"); // heading to scene3. Will also add the checker piece to the board,
		redBtn.setPrefSize(120, 80);
		blackBtn.setPrefSize(120, 80);
		nxt.setPrefSize(80,80);
		redBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
		blackBtn.setStyle("-fx-background-color: black; -fx-text-fill: white;");
		nxt.setStyle("-fx-background-color: pink; -fx-text-fill: white;");

		//Buttons are Clickable!
		redBtn.setOnAction(e -> {
			clientConnection.send(new Message("color_request", userUsername, "red"));
			listItems2.getItems().add("TEAM RED");
			//disables button
			redBtn.setDisable(true);
			blackBtn.setDisable(true);
		});

		blackBtn.setOnAction(e -> {
			clientConnection.send(new Message("color_request", userUsername, "black"));
			listItems2.getItems().add("TEAM BLACK");
			//disables button
			redBtn.setDisable(true);
			blackBtn.setDisable(true);
		});

//		nxt.setOnAction(e -> {
//			primaryStage.setScene(sceneMap.get("scene3"));
//		})

		colorBox.getChildren().addAll(colorTitle, redBtn, blackBtn);

		//Your code has not been messsed with.
		userList = new ListView<String>();
		Label ifOnline = new Label("Online: ");
		VBox onlineUsers = new VBox(10, ifOnline, userList);
		onlineUsers.setPrefWidth(120);

		HBox chatInput = new HBox(10, c1, b1);
		HBox.setHgrow(c1, Priority.ALWAYS);



		clientBox = new VBox(20, colorBox,listItems2, chatInput); //added colorBox
		clientBox.setStyle("-fx-background-color: blue;"+"-fx-font-family: 'serif';");

		BorderPane root = new BorderPane();
		root.setLeft(colorBox); //added this and changed the centering
		root.setCenter(clientBox);
		root.setRight(onlineUsers);

		return new Scene(root, 600, 400); //changed from 400 300


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


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
import javafx.scene.text.Font;

public class GuiClient extends Application{

	
	TextField c1Client;
	TextField c1Checkers;
	Button b1Client;
	Button b1Checkers;
	HashMap<String, Scene> sceneMap;
	VBox clientBox;
	Client clientConnection;
	ListView<String> listItems2Client;
	ListView<String> listItems2Checkers;

	ListView<String> userListClient;
	ListView<String> userListCheckers;
	
	String userUsername = null;
	TextField inputUsername;
	Label usernameError;
	Stage primaryStage;
	TextField inputServerIP;
	TextField inputPort;
	int[][] boardLogic = new int[8][8];
	Button[][] buttonBoard = new Button[8][8];
	boolean firstMove = true;
	int startRow;
	int startCol;
	boolean turn = false;
	String color = "";
	int playerPieceType = 0;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		//clientConnection = new Client(message-> Platform.runLater(() -> handleMessage((Message) message)));

							
		//clientConnection.start();


		listItems2Client = new ListView<String>();
		listItems2Checkers = new ListView<String>();

		c1Client = new TextField();
		b1Client = new Button("Send");
		b1Client.setFont(new Font("Serif", 12));
		b1Client.setOnAction(e -> sendMessage(c1Client));
		c1Client.setOnAction(e -> sendMessage(c1Client));

		c1Checkers = new TextField();
		b1Checkers = new Button("Send");
		b1Checkers.setFont(new Font("Serif", 12));
		b1Checkers.setOnAction(e -> sendMessage(c1Checkers));
		c1Checkers.setOnAction(e -> sendMessage(c1Checkers));
		
		sceneMap = new HashMap<String, Scene>();

		sceneMap.put("client",  createClientGui());
		sceneMap.put("username", createUsernameGui());
		sceneMap.put("checkers", createCheckersGui());
		
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
			userListClient.getItems().setAll(data.users);
			userListCheckers.getItems().setAll(data.users);
		}
		else if (data.type.equals(Message.message)){
			listItems2Client.getItems().add(data.user + ": " + data.sentMessage);
			listItems2Checkers.getItems().add(data.user + ": " + data.sentMessage);

		}
		else if(data.type.equals(Message.updateBoard)){
			boardLogic = data.board;
			updateBoard();
			if(data.sentMessage != null && !data.sentMessage.isEmpty()){
				int turnInt = Integer.parseInt(data.sentMessage);
				turn = (playerPieceType == turnInt);
				//turn = (playerPieceType == Integer.parseInt(data.sentMessage));
				//System.out.println("turn updated. piece type: " + playerPieceType + ", current turn: " + turnInt);
			}

		}
		else if (data.type.equals(Message.gameOver)){
			turn = false;
			listItems2Client.getItems().add("Game over" + data.sentMessage);
			listItems2Checkers.getItems().add("Game over" + data.sentMessage);

			playAgain();
		}
		else if(data.type.equals(Message.startGame)){
			playerPieceType = Integer.parseInt(data.sentMessage);
			turn = (playerPieceType == 1);
			firstMove = true;
			this.boardLogic = data.board;
			updateBoard();
			clientBox.getChildren().removeIf(node -> node instanceof Button && ((Button) node).getText().equals("Play Again!"));
		}
		else if (data.type.equals("color_assigned")) { //what color you are

			if (data.user.equals(userUsername)) {
				listItems2Client.getItems().add("You are " + data.sentMessage);
				listItems2Checkers.getItems().add("You are " + data.sentMessage);
			}
		}
		else if (data.type.equals("color_broadcast")) { //tells eveyone you chose a color
			listItems2Client.getItems().add(data.sentMessage);
			listItems2Checkers.getItems().add(data.sentMessage);

		}
		else if (data.type.equals(Message.move)) {
			if ("invalid".equals(data.sentMessage)) {
				this.turn = true;
				this.firstMove = true;
				//System.out.println("Move was invalid");
			}//maybe fix
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

		jB.setOnAction(e -> {
			String ip = inputServerIP.getText();
			int port = Integer.parseInt(inputPort.getText());
			userUsername = inputUsername.getText();

			clientConnection = new Client(ip, port, message->
					Platform.runLater(() -> handleMessage((Message) message)), () -> sendUsername() );
			clientConnection.start();

			//sendUsername();
		});

		VBox box = new VBox(10, new Label("Enter Server IP:"), inputServerIP,
				new Label("Enter Port:"), inputPort,
				new Label("Enter username: "), inputUsername, usernameError, jB);
		box.setPadding(new Insets(20));
		return new Scene(box, 500, 500);
	}

	public Scene createClientGui() {
		VBox colorBox = new VBox(15);
		colorBox.setPadding(new Insets(10));
		colorBox.setStyle("-fx-background-color: #ffccff;" + "-fx-background-radius: 10;");

		Label colorTitle = new Label("Choose a Color");
		colorTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		//Buttons
		Button redBtn = new Button("Red");
		Button blackBtn = new Button("Black");
		Button nxt = new Button("Continue"); // heading to scene3. Will also add the checker piece to the board,
		redBtn.setPrefSize(120, 80);
		blackBtn.setPrefSize(120, 80);
		nxt.setPrefSize(120,50);
		redBtn.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 15px;");
		blackBtn.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 15px;");
		nxt.setStyle("-fx-background-color: #CCCCFF ; -fx-text-fill: white; -fx-font-size: 15px;");

		//Buttons are Clickable!
		redBtn.setOnAction(e -> {
			clientConnection.send(new Message("color_request", userUsername, "red"));
			listItems2Client.getItems().add("TEAM RED");
			//disables button
			redBtn.setDisable(true);
			blackBtn.setDisable(true);
			nxt.setDisable(false);
		});

		blackBtn.setOnAction(e -> {
			clientConnection.send(new Message("color_request", userUsername, "black"));
			listItems2Client.getItems().add("TEAM BLACK");
			//disables button
			redBtn.setDisable(true);
			blackBtn.setDisable(true);
			nxt.setDisable(false);
		});

	     nxt.setOnAction(e -> { // this will take the clients to the checkerboard
	        primaryStage.setScene(sceneMap.get("checkers"));
	        nxt.setDisable(false);

	     });

		colorBox.getChildren().addAll(colorTitle, redBtn, blackBtn, nxt);

		//Your code will be commented out just in case.
	//     userList = new ListView<String>();
	//     Label ifOnline = new Label("Online: ");
	//     VBox onlineUsers = new VBox(10, ifOnline, userList);
	//     onlineUsers.setPrefWidth(120);
	//     onlineUsers.setStyle("-fx-background-color: #ffccff;" + "-fx-background-radius: 10;"); //added

		//rigth side style START
		userListClient = new ListView<>();
		userListClient.setPrefWidth(100);

	// style the list itself
		userListClient.setStyle("-fx-background-color: #cce5ff;" + "-fx-control-inner-background: #cce5ff;" + "-fx-border-color: transparent;" + "-fx-text-fill: white;");
	// title label
		Label ifOnline = new Label("ONLINE");
		ifOnline.setStyle("-fx-text-fill: #b9bbbe;" + "-fx-font-size: 12px;" + "-fx-font-weight: bold;");
	// container
		VBox onlineUsers = new VBox(10, ifOnline, userListClient);
		onlineUsers.setPadding(new Insets(15));
	// sidebar background
		onlineUsers.setStyle("-fx-background-color: #ffccff; -fx-background-radius: 10;" );
		//right side style END


		//HBox chatInput = new HBox(10, c1, b1);      //original
		//HBox.setHgrow(c1, Priority.ALWAYS);        //original

		// Chat box stlye update!!!
		listItems2Client = new ListView<>();
		listItems2Client.setPrefHeight(300);
		listItems2Client.setStyle("-fx-background-color: #cce5ff;" + "-fx-control-inner-background: #cce5ff;" + "-fx-text-fill: black;");

		c1Client = new TextField(); //where the client types
		c1Client.setPromptText("Message <3");
		c1Client.setPrefWidth(230);
		c1Client.setStyle("-fx-background-radius: 8;" + "-fx-padding: 8;");

		b1Client = new Button("Send");
		b1Client.setStyle("-fx-background-color: #4a90e2;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;" + "-fx-background-radius: 8;");

		b1Client.setOnAction(e -> sendMessage(c1Client));
		c1Client.setOnAction(e -> sendMessage(c1Client));

		HBox chatInput = new HBox(10, c1Client, b1Client);
		chatInput.setPadding(new Insets(10));

		clientBox = new VBox(15, listItems2Client, chatInput);
		clientBox.setPadding(new Insets(10));
		clientBox.setStyle("-fx-background-color: #CCCCFF;" + "-fx-background-radius: 10;");
		//Chat box style update END!!!


		//clientBox = new VBox(20, colorBox,listItems2, chatInput); //added colorBox
		//clientBox.setStyle("-fx-background-color: #e0b0ff;"+"-fx-font-family: 'serif';"); // added

		BorderPane root = new BorderPane();
		root.setLeft(colorBox); //added this and changed the centering
		root.setCenter(clientBox);
		root.setRight(onlineUsers);

		return new Scene(root, 600, 400); //changed from 400 300


	}


	//create checkersGui()
	public Scene createCheckersGui() {
		//userList = new ListView<String>();
		userListCheckers = new ListView<>();
		listItems2Checkers = new ListView<>();
		c1Checkers = new TextField();
		b1Checkers = new Button("Send");

		b1Checkers.setOnAction(e -> sendMessage(c1Checkers));
		c1Checkers.setOnAction(e -> sendMessage(c1Checkers));
		Label ifOnline = new Label("Online: ");
		VBox onlineUsers = new VBox(10, ifOnline, userListCheckers);
		onlineUsers.setPrefWidth(150);
		onlineUsers.setPadding(new Insets(10));


//style for online (RIGHT SIDE)
		listItems2Checkers.setStyle("-fx-background-color: #cce5ff;" + "-fx-control-inner-background: #cce5ff;" + "-fx-text-fill: black;");
		userListCheckers.setStyle("-fx-background-color: #cce5ff;" + "-fx-control-inner-background: #cce5ff;" + "-fx-border-color: transparent;" + "-fx-text-fill: white;");
		ifOnline.setStyle("-fx-text-fill: #b9bbbe;" + "-fx-font-size: 12px;" + "-fx-font-weight: bold;");
		onlineUsers.setStyle("-fx-background-color: #ffccff; -fx-background-radius: 10;");
		c1Checkers.setPromptText("Message <3");
		c1Checkers.setStyle("-fx-background-radius: 8;" + "-fx-padding: 8;");
		b1Checkers.setStyle("-fx-background-color: #4a90e2;" + "-fx-text-fill: white;" + "-fx-font-weight: bold;" + "-fx-background-radius: 8;");


		HBox chatInput = new HBox(10, c1Checkers, b1Checkers);
		HBox.setHgrow(c1Checkers, Priority.ALWAYS);

		GridPane board = new GridPane();
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){
				final int row = i;
				final int col = j;
				Button cell = new Button();
				cell.setPrefSize(50,50);
				if((i + j) % 2 == 0) {
					cell.setStyle("-fx-background-color: #FCBBCD;");
				}
				else {
					cell.setStyle("-fx-background-color: #B99A74;");
				}
				buttonBoard[i][j] = cell;
				board.add(cell, j, i);
				cell.setOnAction(e -> {
					handleMove(row, col);
				});
			}
		}
		HBox guiLayout = new HBox(20);
		guiLayout.setPadding(new Insets(10));
		clientBox = new VBox(30,listItems2Checkers, chatInput);
		clientBox.setStyle("-fx-background-color: #FA8FA6;"+"-fx-font-family: 'serif';");
		HBox.setHgrow(clientBox, Priority.ALWAYS);
		guiLayout.getChildren().addAll(board, clientBox);
		BorderPane root = new BorderPane();
		root.setCenter(guiLayout);
		root.setRight(onlineUsers);

		return new Scene(root, 1000, 420);

	}

	int pieceTypeCheck(){
		return playerPieceType;
	}


	void handleMove(int r, int c){

		if(turn == false){
			//System.out.println("input ignored not correct turn");
			return;
		}

		boolean pieceCheckForAllTypes = (playerPieceType == 1 && (boardLogic[r][c] == 1 || boardLogic[r][c] == 3)) ||
				(playerPieceType == 2 && (boardLogic[r][c] == 2 || boardLogic[r][c] == 4));

		if(firstMove == true && pieceCheckForAllTypes){
			startRow = r;
			startCol = c;
			firstMove = false;
			//System.out.println("First move: Selected piece at [" + r + ", " + c + "]");
		} else if (firstMove == false){
			//System.out.println("Second move: Attempting to move from [" + startRow + ", " + startCol + "] to [" + r + ", " + c + "]");

			Message m = new Message(Message.move, userUsername, null);
			m.fromRow = startRow;
			m.fromCol = startCol;
			m.toRow = r;
			m.toCol = c;
			//maybe change??
			clientConnection.send(m);
			firstMove = true;
			//turn = false;
		} else {
			//System.out.println("clicked wrong piece type");
		}
	}

	void updateBoard(){
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){
				int piece = boardLogic[i][j];
				String bgColor = ((i + j) % 2 == 0) ? "#FCBBCD" : "#B99A74";
				if(piece == 1){
					buttonBoard[i][j].setText("●");
					buttonBoard[i][j].setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: red; -fx-font-size: 20px;");
				}
				else if (piece == 2){
					buttonBoard[i][j].setText("●");
					buttonBoard[i][j].setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: black; -fx-font-size: 20px;");
				}
				else if(piece == 3){
					buttonBoard[i][j].setText("●");
					buttonBoard[i][j].setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: #780000; -fx-font-size: 20px;");
				}
				else if (piece == 4){
					buttonBoard[i][j].setText("●");
					buttonBoard[i][j].setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: #696969; -fx-font-size: 20px;");
				}
				else {
					buttonBoard[i][j].setText("");
					buttonBoard[i][j].setStyle("-fx-background-color: " + bgColor + ";");
				}
//				if(piece == 1){
//					buttonBoard[i][j].setText("●");
//					buttonBoard[i][j].setStyle("-fx-background-color: #FCBBCD; -fx-text-fill: red; -fx-font-weight: bold;");
//				}
//				else if (piece == 2){
//					buttonBoard[i][j].setText("●");
//					buttonBoard[i][j].setStyle("-fx-background-color: #FCBBCD; -fx-text-fill: black; -fx-font-weight: bold;");
//				}
//				else if(piece == 3){
//					buttonBoard[i][j].setText("●");
//					buttonBoard[i][j].setStyle("-fx-background-color: #FCBBCD; -fx-text-fill: #9C2007; -fx-font-weight: bold;");
//				}
//				else if (piece == 4){
//					buttonBoard[i][j].setText("●");
//					buttonBoard[i][j].setStyle("-fx-background-color: #FCBBCD; -fx-text-fill: #696969; -fx-font-weight: bold;");
//				}
//				else {
//					buttonBoard[i][j].setText("");
//					buttonBoard[i][j].setStyle("-fx-background-color: #FCBBCD;");
//				}
			}
		}
	}

	public void playAgain(){
		Button playAgain = new Button("Play Again!");

		playAgain.setOnAction(e -> {
			clientConnection.send(new Message(Message.playAgain, userUsername, null));
			clientBox.getChildren().remove(playAgain);
		});
		clientBox.getChildren().add(playAgain);
	}


	public void sendMessage(TextField textField){
		String input = textField.getText();
		if(input.isEmpty()){
			return;
		}
		else {
			clientConnection.send(new Message(Message.message, userUsername, input));
			textField.clear();
		}
	}

}


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
		clientConnection = new Client(message-> Platform.runLater(() -> handleMessage((Message) message)));

							
		clientConnection.start();


		listItems2 = new ListView<String>();
		c1 = new TextField();
		b1 = new Button("Send");
		b1.setFont(new Font("Serif", 12));
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
		else if(data.type.equals(Message.updateBoard)){
			boardLogic = data.board;
			updateBoard();
		}
		else if (data.type.equals(Message.gameOver)){
			turn = false;
			listItems2.getItems().add("Game over" + data.sentMessage);
			playAgain();
		}
		else if(data.type.equals(Message.startGame)){
			playerPieceType = Integer.parseInt(data.sentMessage);
			turn = (playerPieceType == 1);
			this.boardLogic = data.board;
			updateBoard();
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
		onlineUsers.setPrefWidth(150);
		onlineUsers.setPadding(new Insets(10));

		HBox chatInput = new HBox(10, c1, b1);
		HBox.setHgrow(c1, Priority.ALWAYS);




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


		clientBox = new VBox(30,listItems2, chatInput);
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
			return;
		}
		if(firstMove == true && boardLogic[r][c] == pieceTypeCheck()){
			startRow = r;
			startCol = c;
			firstMove = false;
		} else {
			firstMove = true;
			Message m = new Message(Message.move, userUsername, null);
			m.fromRow = startRow;
			m.fromCol = startCol;
			m.toRow = r;
			m.toCol = c;
			//maybe change??
			clientConnection.send(m);
			turn = false;
		}
	}

	void updateBoard(){
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){
				int piece = boardLogic[i][j];
				if(piece == 1){
					buttonBoard[i][j].setText("R");
					//buttonBoard[i][j].setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
				}
				else if (piece == 2){
					buttonBoard[i][j].setText("B");
					//buttonBoard[i][j].setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
				}
				else {
					buttonBoard[i][j].setText("");
				}
			}
		}
	}

//	void initializeBoard(){
//		for(int i = 0; i < 3; i++){
//			for(int j = 0; j < 8; j++){
//				if((i+j) % 2 == 1){
//					boardLogic[i][j] = 2;
//				}
//			}
//		}
//
//		for(int i = 5; i < 8; i++){
//			for(int j = 0; j < 8; j++){
//				if((i+j) % 2 == 1){
//					boardLogic[i][j] = 1;
//				}
//			}
//		}
//	}

	public void playAgain(){
		Button playAgain = new Button("Play Again!");

		playAgain.setOnAction(e -> {
			clientConnection.send(new Message(Message.playAgain, userUsername, null));
			clientBox.getChildren().remove(playAgain);
		});
		clientBox.getChildren().add(playAgain);
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

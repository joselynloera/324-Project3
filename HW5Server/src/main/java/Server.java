import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.scene.control.ListView;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

//why is there an iclicker question here lmao???

public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	HashMap<String, ClientThread> usernameMap = new HashMap<>();
	int[][] boardLogic = new int[8][8];
	boolean redTurn = true;

	boolean redTaken = false; //added
	boolean blackTaken = false; //added
	
	Server(Consumer<Serializable> call){
	
		callback = call;
		server = new TheServer();
		server.start();
	}
	ClientThread getClientUserID(String username){
		for(ClientThread thread : clients){
			if(username.equals(thread.username)){
				return thread;
			}
		}
		return null;
	}

	boolean usernameCheck(String username){
		if(getClientUserID(username) != null){
			return true;
		}
		return false;

	}

	void showUserList(){
		ArrayList<String> usernames = new ArrayList<>();
		for(ClientThread thread : clients){
			if(thread.username != null){
				usernames.add(thread.username);
			}
		}
		for(ClientThread thread : clients){
			try{
				Message m = new Message(Message.userList, null, null);
				m.users = usernames;
				thread.out.writeObject(m);
			} catch(Exception e) {
                throw new RuntimeException(e);
            };
		}
	}
	
	public class TheServer extends Thread{
		
		public void run() {
		
			try(ServerSocket mysocket = new ServerSocket(5555);){
		    System.out.println("Server is waiting for a client!");
		  
			
		    while(true) {
		
				ClientThread c = new ClientThread(mysocket.accept(), count);
				callback.accept("client has connected to server: " + "client #" + count);
				clients.add(c);
				c.start();
				
				count++;
				
			    }
			}//end of try
				catch(Exception e) {
					callback.accept("Server socket did not launch");
				}
			}//end of while
		}
	

		class ClientThread extends Thread{

			String color = null;
			Socket connection;
			int count;
			ObjectInputStream in;
			ObjectOutputStream out;
			String username = null;
			
			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;	
			}
			
			public void updateClients(Message message) {
				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
					 t.out.writeObject(message);
					 t.out.reset();
					}
					catch(Exception e) {}
				}
			}
			void goToMessage(Message input){
				try {
					out.writeObject(input);
					out.reset();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			public void run(){
					
				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);	
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}
				
				//updateClients("new client on server: client #"+count);
					
				 while(true) {
					    try {
					    	Message data = (Message)in.readObject();
					    	callback.accept("client: " + count + " sent: " + data);
					    	//updateClients("client #"+count+" said: "+data);

							if(data.type.equals(Message.userID)){
								setUsername(data);
							}
							else if (data.type.equals(Message.message)){
								handleMessage(data);
							}
							else if(data.type.equals(Message.move)){
								handleMove(data, this);
							}
							else if (data.type.equals("color_request")) { //added
								handleColorRequest(data);
							}
					    	
					    	}
					    catch(Exception e) {
					    	//callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
					    	//updateClients("Client #"+count+" has left the server!");
							if ("red".equals(this.color)) { //added
								redTaken = false;
							}
							if ("black".equals(this.color)) {
								blackTaken = false;
							}
							callback.accept(username + " disconnected");
					    	clients.remove(this);
							showUserList();
					    	break;
					    }
					}
				}//end of run

			void handleColorRequest(Message data) {
				String requested = data.sentMessage; //color the client asked for
				String assigned = null; //assigning the color

				synchronized (Server.this) { //prevents two clients from getting the same colorf
					if (requested.equals("red")) {
						if (!redTaken) {
							redTaken = true;
							color = "red";
							assigned = "red";
						}
						else if (!blackTaken) { //when red is taken
							blackTaken = true;
							color = "black";
							assigned = "black";
						}
					}
					else if (requested.equals("black")) { //client requests black
						if (!blackTaken) {
							blackTaken = true;
							color = "black";
							assigned = "black";
						}
						else if (!redTaken) { //black is taken give red instead
							redTaken = true;
							color = "red";
							assigned = "red";
						}
					}
					if (assigned == null) { //both colors are taken
						assigned = "none";
					}
				}
				try { // Send the result only to the client who made the request
					out.writeObject(new Message("color_assigned", username, assigned));
					out.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (ClientThread c : clients) { // Send a message to all of the clients saying who got which color. THIS WORKS YAY
					try {
						c.out.writeObject(new Message("color_broadcast", username,
								username + " is " + assigned));
						c.out.reset();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			void setUsername(Message data){
				if(usernameCheck(data.sentMessage) == true){
					goToMessage(new Message(Message.usernameTaken, null, "Username is taken, try another one"));
				}
				else{
					this.username = data.sentMessage;
					goToMessage(new Message(Message.usernameGood, null, "Username good"));
					showUserList();
					callback.accept(username + " connected");
					if(clients.size() == 2){
						startGame(); //change so that when one color is chosen start game
					}
				}
			}

			void handleMessage(Message data){
				data.user = this.username;
				for(ClientThread thread: clients){
					try {
						thread.out.writeObject(data);
						thread.out.reset();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			void initializeBoard(){
				for(int i = 0; i < 3; i++){
					for(int j = 0; j < 8; j++){
						if((i+j) % 2 == 1){
							boardLogic[i][j] = 2;
						}
					}
				}

				for(int i = 5; i < 8; i++){
					for(int j = 0; j < 8; j++){
						if((i+j) % 2 == 1){
							boardLogic[i][j] = 1;
						}
					}
				}
			}
			//change this
			void startGame(){
				initializeBoard();
				redTurn = true;

				ClientThread player1 = clients.get(0);
				ClientThread player2 = clients.get(1);

				Message m1 = new Message(Message.startGame, null, "1");
				m1.board = boardLogic;
				player1.goToMessage(m1);

				Message m2 = new Message(Message.startGame, null, "2");
				m2.board = boardLogic;
				player2.goToMessage(m2);
			}

			void handleMove(Message data, ClientThread user){
				int fromR = data.fromRow;
				int fromC = data.fromCol;
				int toR = data.toRow;
				int toC = data.toCol;

				int piece = boardLogic[fromR][fromC];
				boardLogic[toR][toC] = piece;
				boardLogic[fromR][fromC] = 0;

				Message update = new Message(Message.updateBoard, null, null);
				update.board = boardLogic;

				for(ClientThread client: clients){
					client.goToMessage(update);
				}
//				if(validMove(fromR, fromC, toR, toC, currentPlayer) == false){
//					user.goToMessage(new Message(Message.move, null, "invalid"));
//					return;
//				}
//				userMoves(fromR, fromC, toR, toC);
//				message m = new Message(Message.updateBoard, null, null);
//				m.board = board;
//				player1.goToMessage(m);
//				player2.goToMessage(m);
			}

			
			
		}//end of client thread
}


	
	

	

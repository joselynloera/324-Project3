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

public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	HashMap<String, ClientThread> usernameMap = new HashMap<>();
	
	
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
				}
				catch(Exception e){

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
					    	
					    	}
					    catch(Exception e) {
					    	//callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
					    	//updateClients("Client #"+count+" has left the server!");
							callback.accept(username + " disconnected");
					    	clients.remove(this);
							showUserList();
					    	break;
					    }
					}
				}//end of run
			void setUsername(Message data){
				if(usernameCheck(data.sentMessage) == true){
					goToMessage(new Message(Message.usernameTaken, null, "Username is taken, try another one"));
				}
				else{
					this.username = data.sentMessage;
					goToMessage(new Message(Message.usernameGood, null, "Username good"));
					showUserList();
					callback.accept(username + " connected");
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

			
			
		}//end of client thread
}


	
	

	

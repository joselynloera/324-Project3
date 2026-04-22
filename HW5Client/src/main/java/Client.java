import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;



public class Client extends Thread{

	
	Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private Consumer<Serializable> callback;
	private Runnable onConnected;
	
	Client(Consumer<Serializable> call){
		this.ip = "127.0.0.1";
		this.port = 5555;
		callback = call;
	}

	private String ip;
	private int port;

	public Client(String ip, int port, Consumer<Serializable> callback, Runnable onConnected){
		this.ip = ip;
		this.port = port;
		this.callback = callback;
		this.onConnected = onConnected;
	}
	
	public void run() {
		
		try {
		//socketClient= new Socket("127.0.0.1",5555);
			socketClient = new Socket(ip, port);
	    this.out = new ObjectOutputStream(socketClient.getOutputStream());
	    this.in = new ObjectInputStream(socketClient.getInputStream());
	    socketClient.setTcpNoDelay(true);
		if(onConnected != null){
			onConnected.run();
		}
		}
		catch(Exception e) {
			callback.accept(new Message("error", null, "couldnt connect to ip"));
			return;
		}
		
		while(true) {
			 
			try {
			Message message = (Message) in.readObject();
			callback.accept(message);
			}
			catch(Exception e) {
				break;
			}
		}
	
    }
	
	public void send(Message data) {
		
		try {
			out.writeObject(data);
			out.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    public static final String userID = "setUserID";
    public static final String message = "message";
    public static final  String userList = "userList";
    public static final String serverAnnounce = "serverAnnouncement";
    public static final String usernameGood = "usernameGood";
    public static final String usernameTaken = "usernameTaken";
    public static final String colorPick = "colorPick";
    public ArrayList<String> users;
    public String type;
    public String user;
    public String sentMessage;

    public Message(String type, String user,
                   String sentMessage ){
        this.type = type;
        this.user = user;
        this.sentMessage = sentMessage;
    }

    @Override
    public String toString(){
        if(this.type.equals(userID)){
            return "Set username to: " + sentMessage;
        }
        else if (this.type.equals(usernameTaken)){
            return "Username taken";
        }
        else if (this.type.equals(userList)){
            String list;
            if(this.users != null && !this.users.isEmpty()){
                list = String.join(", ", this.users);
            }
            else {
                list = "empty";
            }
            return "User list:" + list;
        }
        else {
            String name;
            if(this.user != null){
                name = this.user;
            }
            else {
                name = "server";
            }
            return type + " " + name + " " + message;
        }

    }
}

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    protected ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    protected ArrayList<MessageListener> messageListeners = new ArrayList<>();
    protected ArrayList<ChatListener> chatListeners = new ArrayList<>();
    private String IPAddress = null;
    private int Port;

    /**
     * Constructed for ChatClient class
     * @param serverName name of the server.
     * @param serverPort port number of server.
     */
    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    /**
     * Connects client to server and starts two threads which allow client to read output messages from server
     * and in turn sends messages to the severs such as commands.
     * @return if true prints out to client that it has successfully connected to sever. else if false prints out
     * connection failed.
     */
    private boolean connect() {
        //Try catch statements to handle errors.
        try {
            this.socket = new Socket(serverName,serverPort);
            //outputs what the client port is
            System.out.println("Client port is " + socket.getLocalPort());
            //Starts thread that reads output messages from server.
            new ReadThread(socket,this).start();
            //Starts thread that reads input from client to send to server.
            new WriteThread(socket,this).start();
            return true;
        } catch (IOException ex) {
            if(ex.getMessage().equalsIgnoreCase("Connection refused: connect")) {
                System.err.println("Server is not online");
                System.exit(0);
            }
            else if(ex.getMessage().equalsIgnoreCase(IPAddress)) {
                System.err.println("That server is not online!");
                System.exit(0);
            }
            else {
                System.err.println("the error: " + ex.getMessage());
                ex.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Main method of ChatClient, this adds clients to listener interfaces and connects client to server.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ChatClient client = null;
        //if person calling ChatClient want to connect to a different server or port.
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("-cca")) {
                client = new ChatClient(args[1], 14002);
                client.IPAddress = args[1];
            }
            else if(args[0].equalsIgnoreCase("-ccp")){
                client = new ChatClient("localhost",Integer.parseInt(args[1]));
                client.Port = Integer.parseInt(args[1]);
            }
        }
        else if (args.length == 4) {
            if(args[0].equalsIgnoreCase("-cca")) {
                client = new ChatClient(args[1], Integer.parseInt(args[3]));
                client.IPAddress = args[1];
                client.Port = Integer.parseInt(args[3]);
            }
            else if(args[0].equalsIgnoreCase("-ccp")){
                client = new ChatClient(args[3], Integer.parseInt(args[1]));
                client.IPAddress = args[3];
                client.Port = Integer.parseInt(args[1]);
            }
        }
        else {
            client = new ChatClient("localhost", 14002);
        }
        //register client on user status list so that they get messages from the server from someone logs off or on.
        client.addUserListener(new UserStatusListener() {
            /**
             * prints to client that a client has joined the server.
             * @param userName The user name of the client who joined.
             */
            @Override
            public void online(String userName) {
                System.out.println(userName + " joined");
            }

            /**
             * Prints to client that a client has left the server.
             * @param userName The user name of the client who joined.
             */
            @Override
            public void offline(String userName) {
                System.out.println(userName + " left");
            }
        });
        //Registers client on a message listener interface so they are able to receive personal messages
        //from other clients.
        client.addMessageListener(new MessageListener() {
            /**
             * Prints to client a personal message from another client.
             * @param fromUser  user who originally sent the message.
             * @param msgBody The actual message it self.
             */
            @Override
            public void onMessage(String fromUser, String msgBody) {
                System.out.println("From [" + fromUser + "]: " + msgBody);
            }
        });
        //Registers client on a general chat listener interface so they receive public messages from
        //all clients connected
        client.addChatListener(new ChatListener() {
            /**
             * Prints to client public messages from other clients.
             * @param fromUser who the public message is from.
             * @param msgBody the public message.
             */
            @Override
            public void onChat(String fromUser, String msgBody) {
                System.out.println("[" + fromUser + "]: " + msgBody);
            }
        });
        //if the client connection to the server fails.
        if (!client.connect()) {
            System.err.println("Connection failed");
        }
        //if the client connection to the server is successful.
        else {
            System.out.println("Connection Successful");
            System.out.println("Welcome To The Tactical Driller server! To get started set your user name using /setUser \"name\".");
        }
    }

    /**
     * adds client to listener.
     * @param listener client
     */
    public void addUserListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    /**
     * removes client from listener.
     * @param listener client.
     */
    public void removeUserListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    /**
     * adds client to listener.
     * @param listener client
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /**
     * removes client from listener
     * @param listener client
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    /**
     * Adds client to listener.
     * @param listener client
     */
    public void addChatListener(ChatListener listener) {
        chatListeners.add(listener);
    }

    /**
     * Removes client from listener.
     * @param listener client.
     */
    public void removeChatListener(ChatListener listener) {
        chatListeners.remove(listener);
    }

    public static boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

}

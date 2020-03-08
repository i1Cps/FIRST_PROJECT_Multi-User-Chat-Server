import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class BotClient {

    //server port number
    private final int serverPort;
    //server name
    private final String serverName;
    private String IPAddress = null;
    private int Port;

    /**
     * Constructor for Bot client
     *
     * @param serverName server name
     * @param serverPort server port number
     */
    public BotClient(String serverName, int serverPort) {
        //IP address of server trying to connect to
        this.serverName = serverName;
        //port of server trying to connect to
        this.serverPort = serverPort;

    }

    private boolean connect() throws IOException {
        try {
            //creates an instance of the socket that connects bot to server
            //socket that connects to server
            Socket socket = new Socket(serverName, serverPort);
            //prints out local port
            System.out.println("Client port is " + socket.getLocalPort());
            //starts the thread that reads server output for bot client.
            new ReadThreadBot(socket,this).start();
            //starts the thread that reads bot input to send to server.
            new WriteThreadBot(socket,this).start();
            //OutputStream output = socket.getOutputStream();
            //user name of bot
            String botName = "bot";
            String line = "/setuser " + botName + "\n";
            //creates output stream so bot can login to server.
            socket.getOutputStream().write(line.getBytes());

            return true;
        } catch (IOException ex) {
            if(ex.getMessage().equalsIgnoreCase("Connection refused: connect")) {
                //if try catch statements catches an error that suggest bot cant connect to server because server
                //is not online it prints
                System.err.println("Server is not online");
                System.exit(0);
            }
            System.err.println("the error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        BotClient client = null;

        //if person calling ChatClient want to connect to a different server or port.
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("-cca")) {
                client = new BotClient(args[1], 14002);
                client.IPAddress = args[1];
            }
            else if(args[0].equalsIgnoreCase("-ccp")){
                client = new BotClient("localhost",Integer.parseInt(args[1]));
                client.Port = Integer.parseInt(args[1]);
            }
        }
        else if (args.length == 4) {
            if(args[0].equalsIgnoreCase("-cca")) {
                client = new BotClient(args[1], Integer.parseInt(args[3]));
                client.IPAddress = args[1];
                client.Port = Integer.parseInt(args[3]);
            }
            else if(args[0].equalsIgnoreCase("-ccp")){
                client = new BotClient(args[3], Integer.parseInt(args[1]));
                client.IPAddress = args[3];
                client.Port = Integer.parseInt(args[1]);
            }
        }
        else {
            client = new BotClient("localhost", 14002);

            //if the connection fails
            if (!client.connect()) {
                System.err.println("Connection failed");
            }
            //if the client connection to the server is successful.
            else {
                System.out.println("Connection Successful");
            }
        }
    }
}


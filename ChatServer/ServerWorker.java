import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * worker class to handle client communication, this handles helps handle multiple connections.
 */
public class ServerWorker extends Thread {
    //socket which the client is connected to.
    private final Socket clientSocket;
    //server attribute
    private final Server server;
    //user name of client
    private String userName = null;
    // output stream which is used to send messages from server to clients
    private OutputStream outputStream;


    /**
     * Constructed for the class Server worker.
     * @param server server
     * @param clientSocket client socket
     */
    public ServerWorker(Server server, Socket clientSocket) {
        //creates server instance
        this.server = server;
        //creates an instance
        this.clientSocket = clientSocket;
    }

    /**
     * Get user name of client.
     * @return userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Runs the thread.
     */
    @Override
    public void run() {
        //Surrounds method call with try catch statement.
        try {
            //calls method.
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method handles the clients connection to the server, reading their output streams
     * and responds.
     * @throws IOException
     */
    private void handleClientSocket() throws IOException {
        //Gets input from client
        InputStream inputStream = clientSocket.getInputStream();
        //creates output stream for to send to the client connected to this server worker.
        this.outputStream = clientSocket.getOutputStream();
        //reader that readers the input from the client.
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        try {
            //If server worker picks up client input through the clients output stream, the loop is true.
            while ((line = reader.readLine()) != null) {
                //gets lists of all current clients connected
                List<ServerWorker> workerList = server.getWorkerList();
                //gets lists of all user names connected
                List<String> userNameList = server.getUserNameList();
                //splits user input into tokens/words so that code can read them individually
                String[] token = line.split("\\s");
                //if user input starts with "/" code treats it as command
                if (line.startsWith("/")) {
                    //calls method which handles commands.
                    if (command(line) == -1) {
                        /* If this method returns with "-1" then the client has outputted that they want to logoff.
                           Therefore we need to break out of the while loop so the code stop checking for input from
                           the client that has logged off.
                         */
                        break;
                    }
                } else {
                    //If the user has set their user name.
                    if(!(userName == null)) {
                        //Output what user typed
                        String onlineMsg = "GMsg " + userName + " " + line + "\n";
                        //Gets lists of all current clients connected and sends the clients message to all of them.
                        for (ServerWorker worker : workerList) {
                            worker.send(onlineMsg);
                        }
                    }
                }
            }
        } catch ( Exception ex) {
            /* If try catch method catches a "connection reset" as the exception message. it calls a method which logs
               off the client */
            if(ex.getMessage().equalsIgnoreCase("Connection reset")) {
                //Calls method which logs of clients.
                handleLogoff();
            }
            else {
                /*
                If the try catch statement outputs an exception message other than "Connection reset" it prints the
                exception message and prints the stack trace.
                */
                System.err.println("Error reading from client: " + ex.getMessage() + ".");
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method deals with client output streams which contain commands
     * @param line client output.
     * @return value which dictates whether client output was a logoff command or not.
     * @throws IOException
     */
    private int command(String line) throws IOException {
        //gets lists of all current clients connected
        List<ServerWorker> workerList = server.getWorkerList();
        //gets lists of all user names connected
        List<String> userNameList = server.getUserNameList();
        //splits user input into tokens/words so that code can read them individually
        String[] token = line.split("\\s");
        //command word setUser allows clients to pick a username
        if ("/setUser".equalsIgnoreCase(token[0])) {

            //Checks if username has no spaces, if it did have any spaces there would be more than 2 tokens
            if (token.length == 2) {
                //Sets clients user name to what ever the second token is.
                this.userName = token[1];
                /*
                If the user name the client picks is already currently being used, the server outputs,
                string responding that the username is taken.
                */
                if(userNameList.contains(userName.toLowerCase())) {
                    String response = "This user name is taken please choose another! \n";
                    outputStream.write(response.getBytes());
                    //Resets the clients user name to null.
                    this.userName = null;
                }
                else {
                    //Adds clients user name to a user name list of all clients currently connected
                    userNameList.add((userName).toLowerCase());
                    //Prints out to server owner client has logged in successfully.
                    System.out.println("User " + userName + " has logged in successfully");
                    //Lets client know their log in was successful.
                    String msg = "You logged in as " + userName + "\n";
                    outputStream.write(msg.getBytes());
                    //Outputs to all clients connected that this client has joined.
                    String onlineMsg = "Joined " + userName + "\n";
                    for (ServerWorker worker : workerList) {
                        if (!userName.equals(worker.getUserName())) {
                            worker.send(onlineMsg);
                        }
                    }
                }
            }
            //If client tries to input username with spaces server responds saying this is not allowed.
            else if ("/setUser".equalsIgnoreCase(token[0]) && token.length > 2) {
                String msg = "Your user name cannot have any spaces!\n";
                outputStream.write(msg.getBytes());
                System.err.println("unluckers");
            }
        }
        //If client is trying to send a personal message to another client connected.
        else if("/msg".equalsIgnoreCase(token[0])) {
            //As long client has set a user name.
            if (!(userName == null)) {
                //Splits client input into tokens split by the space(" ").
                String[] tokensMsg = line.split("\\s", 3);
                //Attempts to send message to specified user from client.
                if (handleMessage(tokensMsg, userName)) {
                    String output = "msg " + tokensMsg[1] + " " + tokensMsg[2] + "\n";
                    outputStream.write(output.getBytes());
                } else {
                    //If user is not found prints out:
                    String response = "Error User not found \n";
                    outputStream.write(response.getBytes());
                }
            }
        }
        //If the user types "/bot" calls handleBotMessages
        else if ("/bot".equalsIgnoreCase(token[0])) {
            List<String> serverUserNameList = server.getUserNameList();
            if(userNameList.contains("bot")) {
                //handles messages that the client bot will handle
                handleBotMessages(line, userName);
            }
            else {
                String response = "Bot is not connected yet! \n";
                outputStream.write(response.getBytes());
            }
        }
        else if("/help".equalsIgnoreCase(token[0])) {
            //handles /help command
            commandHelp();
        }
        //if the user types "/quit" or ".logoff" break out of while loop.
        else if ("/logoff".equalsIgnoreCase(token[0]) || "/quit".equalsIgnoreCase(token[0])) {
            //closes connection of server
            handleLogoff();
            return -1;
        }
        //If command is not recognised, return "unknown command".
        else {
            String msg = "Unknown command, use /help for a list of available commands! \n";
            outputStream.write(msg.getBytes());
        }
    return 0;
    }

    private void commandHelp() throws IOException {
        String help = "Commands:\n/setuser - This sets your user name.\n/msg - This is used to message someone.\n" +
                "/bot - This is to communicate with the sever bot.\n/logoff - This is to quit.\n";
        outputStream.write(help.getBytes());
    }

    /**
     * Method that handles /bot commands
     * @param line server input
     * @param userName user name of sender
     * @throws IOException
     */
    private void handleBotMessages(String line, String userName) throws IOException {
        //splits user input into tokens/words so that code can read them individually
        String[] token = line.split("\\s");
        //gets worker list.
        List<ServerWorker> workerList = server.getWorkerList();
        String botMsg = "msgFrom " + userName + " " + token[1] + "\n";
        //scans through workers in worker list
        for (ServerWorker worker : workerList) {
            //if they find the bots user name they send it to that user.
            if ("bot".equalsIgnoreCase(worker.getUserName())) {
                worker.send(botMsg);
            }
        }
    }

    /**
     * Handles personal messages send from one client to another.
     * @param token Output message from client which has been split into tokens by the spaces in the output message.
     * @param sender The user who the client is trying to message.
     * @return Returns true if user the client is trying to message exists, else returns false.
     * @throws IOException
     */
    private boolean handleMessage(String[] token, String sender) throws IOException {
        //First token is the receiver.
        String receiver = token[1];
        //Second token is the message.
        String message = token[2];
        //Gets a list of all the clients connected.
        List<ServerWorker> workerList = server.getWorkerList();
        //Sends message to the user the client is trying to reach.
        for (ServerWorker worker : workerList) {
            if (receiver.equalsIgnoreCase(worker.getUserName())) {
                String personalMsg = "msgFrom " + sender + " " + message + "\n";
                worker.send(personalMsg);
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the output message that is sent to all connected clients when a client logs off
     * @throws IOException
     */
    private void handleLogoff() throws IOException {
        try {
            //gets list of clients connected
            List<ServerWorker> workerList = server.getWorkerList();
            //Removes clients server worker from the list of server workers.
            server.removeWorker(this);
            //Gets list of user names of clients connected.
            List<String> userNames = server.getUserNameList();
            //Removes logging off clients user name from the list.
            server.removeUserNameList(userName);
            String onlineMsg = "left " + userName + "\n";
            //sends connected clients message when someone logs off.
            for (ServerWorker worker : workerList) {
                if (!userName.equals(worker.getUserName())) {
                    worker.send(onlineMsg);
                }
            }
            //Outputs to server owner that a clients has logged off successfully.
            System.out.println("User " + userName + " has logged off successfully");
            //closes clients socket.
            clientSocket.close();
        } catch (IOException ex) {
            System.err.println("Error caused: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Sends message to given client/clients.
     * @param Msg Message to be send to client/clients.
     * @throws IOException
     */
    private void send(String Msg) throws IOException {
        //So long as client user name is not null.
        if(userName != null)
        outputStream.write(Msg.getBytes());
    }
}


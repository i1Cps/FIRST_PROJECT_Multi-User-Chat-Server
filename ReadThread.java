import java.io.*;
import java.net.Socket;


/**
 * This thread is responsible for reading the servers output
 * and printing it to the client
 */
public class ReadThread extends Thread {
    private BufferedReader BufferedIn;
    private Socket socket;
    private ChatClient client;

    /**
     * Constructor for ReadThread class.
     * @param socket client socket
     * @param client client
     */
    public ReadThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            //gets output from server
            InputStream input = socket.getInputStream();
            //converts server output into buffered reader
            BufferedIn = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            //prints out exception message
            System.out.println("Error getting input Stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    //runs thread
    public void run() {
        while(true) {
            String line;
            try {
                //while loop so that it is continuously looking for server output streams.
                while ((line = BufferedIn.readLine()) != null) {
                    //splits user input into tokens/words so that code can read them individually
                    String[] token = line.split("\\s");
                    if(token.length > 0) {
                        //determines server message by checking the first word in the server output stream.
                        String serverOutput = token[0];
                        //if first word from server output stream is "joined" code calls handleOnline method.
                        if("Joined".equalsIgnoreCase(serverOutput)) {
                            handleOnline(token);
                        }
                        //If first word from server output stream is "left" code calls handleOffline method.
                        else if("left".equalsIgnoreCase(serverOutput)) {
                            handleOffline(token);
                        }
                        //If first word from server output stream is "msg" code calls handleMsgOut method.
                        else if("msg".equalsIgnoreCase(token[0])) {
                            handleMsgOut(line);
                        }
                        //If first word from server output stream is "msgFrom" code calls handleMsgIn method.
                        else if("msgFrom".equalsIgnoreCase(token[0])) {
                            handleMsgIn(line);
                        }
                        //If first word from server output stream is "GMsg" code calls handleGeneralMsg mehod.
                        else if("GMsg".equalsIgnoreCase(token[0])) {
                            handleGeneralMsg(line);
                        }
                        //If first word from server output stream is "Error" it prints out the error with red font.
                        else if("Error".equalsIgnoreCase(token[0])) {
                            System.err.println(line);
                        }
                        //Else just prints the server output stream
                        else {
                            System.out.println(line);
                        }
                    }
                }
            } catch (Exception ex) {
                //if try catch statement catches an error caused because "socket closed"  code breaks loop to avoid
                //client code crashing.
                if(ex.getMessage().equalsIgnoreCase("Socket closed")) {
                    break;
                }
                else if(ex.getMessage().equalsIgnoreCase("Connection reset")) {
                    //If try catch statement catches an error caused by "connection reset" this mean the server
                    //has shutdown so it just ends the code as there is no server to connect to.
                    System.err.println("Error reading from server: " + ex.getMessage() + ".");
                    System.exit(0);
                }
                else {
                    //Else code prints the general error with a stack trace.
                    System.err.println("Error reading from server: " + ex.getMessage() + ".");
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Prints to client someone has joined
     * @param token server output stream but it is split into tokens
     */
    void handleOnline(String[] token) {
        String userName = token[1];
        for(UserStatusListener listener : client.userStatusListeners) {
            listener.online(userName);
        }
    }

    /**
     * Prints to client someone has left
     * @param token sever output stream but it is split into tokens.
     */
    void handleOffline(String[] token) {
        String userName = token[1];
        for(UserStatusListener listener : client.userStatusListeners) {
            listener.offline(userName);
        }
    }

    /**
     * Prints to client the message the client has just sent.
     * @param line the server output stream.
     */
    void handleMsgOut(String line) {
        String[] token = line.split("\\s",3);
        String userName = token[1];
        String body = token[2];
        String output = "To [" + userName + "]: " + body + "\n";
        System.out.println(output);
    }

    /**
     * Prints to client a message that another client has sent to them
     * @param line the server output stream.
     */
    void handleMsgIn(String line) {
        String[] token = line.split("\\s", 3);
        String userName = token[1];
        String body = token[2];
        for(MessageListener listener : client.messageListeners) {
            listener.onMessage(userName,body);
        }
    }

    /**
     * Prints to client a public message sent from another client
     * @param line the sever output stream
     */
    void handleGeneralMsg(String line) {
        String[] token = line.split("\\s", 3);
        String userName = token[1];
        String body = token[2];
        for(ChatListener listener : client.chatListeners) {
            listener.onChat(userName, body);
        }
    }
}

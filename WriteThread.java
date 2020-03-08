import java.io.*;
import java.net.Socket;

/**
 * This thread is responsible for reading client input
 * then sends it to server
 */
public class WriteThread extends Thread{
    private Socket socket;
    private ChatClient client;
    private BufferedReader userIn;
    private PrintWriter writer;

    /**
     * Constructor for WriteThread Thread
     * @param socket socket of client
     * @param client client
     */
    public WriteThread(Socket socket,ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            //creates output stream to send message to server.
            OutputStream output = socket.getOutputStream();
            userIn = new BufferedReader(new InputStreamReader(System.in));
            writer = new PrintWriter(output,true);
        } catch (IOException ex) {
            System.out.println("Error getting output stream " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * runs the thread
     */
    public void run() {
        String line;
        try {
            do {
                line = userIn.readLine();
                //splits user input into tokens using the spaces in the line.
                String[] token = line.split("\\s");
                //if the first token is = to /quit or /logoff.
                if (token[0].equalsIgnoreCase("/quit") || token[0].equalsIgnoreCase("/logoff")) {
                    //send first token to server and prints to client "logging off".
                    writer.println(token[0]);
                    System.out.println("Logging off");
                    //try catch statement to catch errors
                    try {
                        //attempts to close socket connection between client and the server.
                        socket.close();
                        //if successful prints to client that they have logged off.
                        System.out.println("Logged off");
                    } catch (IOException ex) {
                        //catches errors and prints "Error reading from server" with the exception message.
                        System.out.println("Error reading from server: " + ex.getMessage());
                        //prints stack trace.
                        ex.printStackTrace();
                    }
                    break;
                } else {
                    //else just send client input to the server which will handle the output.
                    writer.println(line);
                }
            }
            //continuously looking for client input to output to server.
            while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

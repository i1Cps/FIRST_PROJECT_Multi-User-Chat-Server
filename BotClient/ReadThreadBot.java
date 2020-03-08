import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * This thread is responsible for reading the output from server directed to the bot
 */
public class ReadThreadBot extends Thread {
    private BufferedReader BufferedIn;
    private Socket botSocket;
    private BotClient client;
    private String senderUserName;


    /**
     * Constructor for ReadThread class.
     * @param socket client socket
     * @param client client
     */
    public ReadThreadBot(Socket socket, BotClient client) throws IOException {
        this.botSocket = socket;
        this.client = client;

        try {
            //gets output from server
            InputStream input =  socket.getInputStream();
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
            //this allows bot client to send messages to server

            String line;
            try {
                //while loop so that it is continuously looking for server output streams.
                while ((line = BufferedIn.readLine()) != null) {
                    //splits user input into tokens/words so that code can read them individually
                    String[] token = line.split("\\s");
                    if(token.length > 0) {
                        //If first word from server output stream is "msgFrom" code calls handleMsgIn method.
                        if("msgFrom".equalsIgnoreCase(token[0])) {
                            handleMsgIn(line);
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
     * Handles messages sent to bot.
     * @param line message
     */
    private void handleMsgIn(String line) {
        //creates an instance of the WriteThreadBot class which handles output messages from bot client to server.
        WriteThreadBot writeBot = new WriteThreadBot(botSocket, client);
        //splits incoming message into tokens
        String[] token = line.split("\\s",3);
        //second token is stored as senders user name
        senderUserName = token[1];
        //if message contains hello
        if (token[2].toLowerCase().contains("hello") || token[2].toLowerCase().contains("yo")) {
            String output = "/msg " + senderUserName + " Yo, what you saying G!";
            //sends to String output server
            writeBot.sendToServer(output);
        }
        //if message contains ping
        else if(token[2].toLowerCase().contains("ping")) {
            String output = "/msg " + senderUserName + " pong";
            //sends to String output server
            writeBot.sendToServer(output);
        }
        //if message if some sort of parting message
        else if(token[2].toLowerCase().contains("bye") || token[2].toLowerCase().contains("goodbye") || token[2].toLowerCase().contains("cya")) {
            String output = "/msg " + senderUserName + " Adios";
            //sends to String output server
            writeBot.sendToServer(output);
        }
        //if message relates to anime in any way
        else if(token[2].toLowerCase().contains("dead") || token[2].toLowerCase().contains("japanese") || token[2].toLowerCase().contains("anime")) {
            String output = "/msg " + senderUserName + " OMAE WA MOU SHINDEIRU!!";
            //sends to String output server
            writeBot.sendToServer(output);
        }
        //if message relates to time or date
        else if(token[2].toLowerCase().contains("time") || token[2].toLowerCase().contains("date")) {
            //String timestamp gets the time in a presentable format.
            String timeStamp = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss").format(Calendar.getInstance(Locale.ENGLISH).getTime());
            String output = "/msg " + senderUserName + " " + timeStamp;
            //sends to String output server
            writeBot.sendToServer(output);
        }

        //if incoming message has no direct output message from bot
        else {
            String output = "/msg " + senderUserName + " NANI??";
            //sends to String output server
            writeBot.sendToServer(output);
        }
    }
}


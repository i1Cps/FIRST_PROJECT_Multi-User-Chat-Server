import java.io.IOException;

/**
 * This is the main class for the server, it should be called to active the server!
 */
public class ChatServer {

    public static void main(String[] args) throws IOException {
        Server server = null;
        if (args.length == 2 && args[0].equalsIgnoreCase("-csp")) {
            //port number for server socket.
            int port = Integer.parseInt(args[1]);
            server = new Server(port);
            //starts the server.
            server.start();
            //creates an instance of a thread which checks for input
            CheckForExit checker = new CheckForExit(port);
            //starts a thread which checks for server owner input in case they want to stop the server using "EXIT".
            checker.start();
        }
        else {
                //port number for server socket.
                int port = 14002;
                server = new Server(port);
                //starts the server.
                server.start();
                //creates an instance of a thread which checks for input
                CheckForExit checker = new CheckForExit(port);
                //starts a thread which checks for server owner input in case they want to stop the server using "EXIT".
                checker.start();
            }
    }


}

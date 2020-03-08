import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This thread checks for the server owners input, If owner types "exit" the server stops.
 */
public class CheckForExit extends Thread {
    //reads server owner input.
    private BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Constructor for class CheckForExit.
     * @param port port number for server
     */
    public CheckForExit(int port) {
    }

    /**
     * This method runs the thread.
     */
    public void run() {
        String line;
        //try catch statement to catch errors while reading input from server owning.
        try {
            //constantly be checking for input while thread is running.
            while (true) {
                //turns buffered reader line into a string
                line = userIn.readLine();
                //if string "line" contains "exit" (ignoring case) server shuts down (code ends)
                if (line.equalsIgnoreCase("exit")) {
                    System.exit(0);
                }
            }
        } catch (IOException ex) {
            /*
            if this try catch statement catches an errors, It prints to the server owner that it was
            caused while checking fo exit command and also gets the exception error message to.
             */
            System.out.println("Error checking for exit command: " + ex.getMessage());
            //prints stack trace so owner can track back to the line the error came from.
            ex.printStackTrace();
        }
    }
}

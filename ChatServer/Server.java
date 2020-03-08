import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread that runs the server
 */
public class Server extends Thread{
    //server port number
    private final int serverPort;
    //list of all server workers which will enable messages to be sent to other clients
    private ArrayList<ServerWorker> workerList = new ArrayList<>();
    //list of all client user names
    private ArrayList<String> userNames = new ArrayList<>();


    /**
     * Constructor for server class.
     * @param serverPort port number for server
     */
    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * gets a list of user names connected to server.
     * @return list of user names.
     */
    public List<String> getUserNameList() {
        return userNames;
    }

    /**
     * adds user name to list of user names.
     * @param userName username of client
     */
    public void addUserNameList (String userName) {
        userNames.add(userName);
    }

    /**
     * remove user name to list of user names.
     * @param userName user name of client
     */
    public void removeUserNameList (String userName) {
        userNames.remove(userName);
    }

    /**
     * returns the list of workers(workers are the threads that handle the connection client to server.
     * @return list of workers
     */
    public List<ServerWorker> getWorkerList() {
        return workerList;
    }

    //this runs the thread allowing multiple clients to connect and use the server.
    @Override
    public void run() {
        //try catch statement
        try {
            //creates an instance of server
            ServerSocket serverSocket = new ServerSocket(serverPort);
            //constantly looking for new clients that connect through a server worker.
            while(true) {
                System.out.println("About to accept client connection...");
                //accepts connection from clients
                Socket clientSocket = serverSocket.accept();
                //prints out when server has accepted a connection
                System.out.println("Accepted connection from" + clientSocket);
                //creates instance of serverWorker that will handle clientSocket (an accepted client).
                ServerWorker worker = new ServerWorker(this, clientSocket);
                //adds worker which is handling the client connection to a worker list.
                workerList.add(worker);
                //starts the thread that handles the connections from new clients.
                worker.start();
            }
        } catch (IOException e) {
            //if error is caught this prints stack trace.
            e.printStackTrace();
        }
    }

    /**
     * removes a worker from the worker list this prevents Output streams being sent to clients that have disconnected.
     * @param serverWorker code that handles output streams for individual client
     */
    public void removeWorker(ServerWorker serverWorker) {
        //removes server worker from list.
        workerList.remove(serverWorker);
    }
}

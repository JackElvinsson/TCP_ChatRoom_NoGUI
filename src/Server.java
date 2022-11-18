import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private final ArrayList<ConnectionHandler> connectionsList;
    private ServerSocket serverSocket;
    private boolean done;
    private ExecutorService pool;

    public Server() {

        connectionsList = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {


        try {

            serverSocket = new ServerSocket(44444);
            pool = Executors.newCachedThreadPool();

            while (!done) {

                Socket clientSocket = serverSocket.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(clientSocket);
                connectionsList.add(connectionHandler);
                pool.execute(connectionHandler);


            }
        } catch (IOException e) {
            shutDown();
        }
    }

    public void broadcast(String message) {

        for (ConnectionHandler ch : connectionsList) {

            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutDown() {

        try {

            done = true;
            pool.shutdown();

            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ConnectionHandler ch : connectionsList) {
                ch.shutDown();
            }

        } catch (IOException ignored) {
        }
    }

    class ConnectionHandler implements Runnable {

        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }


        @Override
        public void run() {

            try {

                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname: ");
                nickname = in.readLine().trim();
                System.out.println(nickname + " CONNECTED");
                broadcast(nickname + " Joined the chat");
                String message;

                while ((message = in.readLine()) != null) {

                    //---------------------------
                    // Change nickname operation
                    //---------------------------
                    if (message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " changed their nickname to: " + messageSplit[1]);
                            System.out.println(nickname + " changed their nickname to: " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to: " + nickname);

                        } else {
                            out.println("No nickname was provided");
                        }

                        //----------------------------
                        // Quit operation
                        //----------------------------
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " Left the chat");
                        System.out.println(nickname + " DISCONNECTED");
                        shutDown();

                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }


            } catch (IOException e) {
                shutDown();
            }
        }

        public void sendMessage(String message) {

            out.println(message);
        }

        public void shutDown() {

            try {
                in.close();
                out.close();

                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException ignored) {
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}

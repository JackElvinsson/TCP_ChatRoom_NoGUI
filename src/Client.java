import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {

        try  {

            clientSocket = new Socket("127.0.0.1", 44444);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }

        } catch (IOException e) {
            shutDown();
        }
    }

    public void shutDown() {

        done = true;

        try {

            in.close();
            out.close();

            if(!clientSocket.isClosed()) {
                clientSocket.close();
            }

        } catch (IOException ignored) {
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {

            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit")) {
                        out.println(message);
                        in.close();
                        shutDown();


                    } else {
                        out.println(message);
                    }
                }

            } catch (IOException e) {
                shutDown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}

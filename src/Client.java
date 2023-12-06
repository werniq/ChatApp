import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    //

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    @Override
    public void run()
    {
        try {
            client = new Socket("127.0.0.1", 8080);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);

            t.start();

            String inMessage;

            while ((inMessage = in.readLine()) != null)
            {
                System.out.println(inMessage);
            }

        } catch (IOException e) {
            // TODO: handle
            shutdown();
        }
    }

    public void shutdown()
    {
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed())
            {
                client.close();
            }
        } catch (IOException e) {
            // ignore it
        }
    }


    class InputHandler implements Runnable {


        @Override
        public void run()
        {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

                while (!done)
                {
                    String message = inputReader.readLine();
                    if (message == "/quit")
                    {
                        inputReader.close();
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                // handle it
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}

import javax.naming.NoInitialContextException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// this class can now be passed to thread, and be executed concurrently
public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connectionList;
    private ServerSocket server;
    private boolean done;
    private ExecutorService threadpool;

    public Server()
    {
        connectionList = new ArrayList<>();
        done = false;
    }

    @Override
    public void run()
    {
        // create a thread and instead of passing a thread
        try {
            server = new ServerSocket(8080 );
            threadpool = Executors.newCachedThreadPool();

            while (!done)
            {
                Socket client = server.accept();

                ConnectionHandler handler = new ConnectionHandler(client);
                connectionList.add(handler);
                threadpool.execute(handler);
                //
            }
        } catch (Exception e)
        {
            shutdown();
        }

    }

    public void broadcast(String message)
    {
        for (ConnectionHandler ch : connectionList)
        {
            if (ch != null)
            {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown()
    {
        try {
            done = true;
            if (!server.isClosed())
            {
                server.close();
            }

            for (ConnectionHandler ch : connectionList)
            {
                ch.shutdown();
            }
        } catch (IOException e) {
            // ignored it (can not handle)
        }
    }

    class ConnectionHandler implements Runnable
    {

        private Socket client;

        // get the stream from the sockets
        private BufferedReader in;

        // write something to the client
        private PrintWriter out;

        private String nickname;
        public ConnectionHandler(Socket client)
        {
            this.client = client;
        }

        @Override
        public void run()
        {
            try {
                // initialize reader and writer
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Please, enter the nickname: ");
                 nickname = in.readLine();
                 System.out.println(nickname + "connected!");
                 broadcast(nickname + "joined the chat");

                 String message;
                 while ((message = in.readLine()) != null)
                 {
                     if (message.startsWith("/nick "))
                     {
                         // TODO: handle nickname
                         String[] messageSplit = message.split(" ", 2);
                         if (messageSplit.length == 2)
                         {
                             broadcast(nickname + "changed a nickname to :" + messageSplit[1]);
                             System.out.println("nickname changed");

                             nickname = messageSplit[1];
                             out.println(nickname + "changed nickname successfully");
                         }
                         else
                         {
                             out.println("No nickname provided");
                         }
                     } else if (message.startsWith("/quit"))
                     {
                         broadcast(nickname + " has left the chat");
                        shutdown();
                     } else
                     {
                        broadcast(nickname + ": " + message);
                     }
                 }
            } catch (IOException e) {
                shutdown();
            }

        }

        public void sendMessage(String message)
        {
            out.println(message);
        }

        public void shutdown()
        {
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
    }


    public static void main(String[] args)
    {
        Server server = new Server();
        server.run();
    }
}

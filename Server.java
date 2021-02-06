import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private List<Socket> clients;
    UUID uuid;
    private int id = 0;

    public Server() {
        int port = 3691;
        try {
            // server is listening on port 3691
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started!");
            // 讀的時侯複製出一份相同的資料內容
            clients = new CopyOnWriteArrayList<>();

            // running infinite loop for getting client request
            while (true) {
                // socket accepted from client
                Socket s = serverSocket.accept();
                uuid = UUID.randomUUID();
                // add to the list of all clients
                clients.add(s);
                // Process received messages from clients
                ClientThread c = new ClientThread(s, uuid);
                c.start();
                // id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }


    // class that handles client
    class ClientThread extends Thread {
        private String clientName;
        private Socket client;
        // dont make it global since this may be changed when multi-thread is used
        // private String message;
        private PrintWriter printWriter;
        private UUID id;
        private BufferedReader bufferedReader;

        public ClientThread(Socket socket, UUID id) {
            this.client = socket;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                // prompt user to enter a name
                promptClientName();
                // receive messages from client
                sendWelcomeMessage();
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String str;
                while ((str = bufferedReader.readLine()) != null) {
                    // to handle the current quitting client
                    if (str.equals("QUIT")) {
                        clients.remove(client);
                        sendQuittingMessage();
                        client.close();
                        break;
                    }
                    sendToEachClient(str);
                }
            } catch (IOException e) {
                System.out.println("BufferedReader.");
                e.printStackTrace();
            }
        }

        private void promptClientName() throws IOException {
            printWriter = new PrintWriter(client.getOutputStream(), true);
            printWriter.println("What is your name?");
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            if ((clientName = bufferedReader.readLine()).equals("")) {
                promptClientName();
            }
        }


        // method to send messages to every single client
        public void sendToEachClient(String message) {
            // to be changed
            for (Socket socket : clients) {
                try {
                    // sending messages with title to every clients
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println(getClientName() + " : " + message);
                } catch (IOException e) {
                    System.out.println("Could not get socket output stream");
                    e.printStackTrace();
                }
            }
        }


        // --------- message methods ---------

        public String getClientCount() {
            StringBuilder builder = new StringBuilder();
            builder.append("There are currently ");
            builder.append(clients.size());
            builder.append(" clients on this server.");

            return builder.toString();
        }

        public String getClientName() {
            return clientName;
        }

        public void sendWelcomeMessage() {
            sendToEachClient(getClientName() +  " has joined! " + getClientCount());
        }

        public void sendQuittingMessage() {
            sendToEachClient(getClientName() +  " quited. " + getClientCount());
        }
    }
}

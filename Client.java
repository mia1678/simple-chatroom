import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

public class Client {
    private Socket s;

    public Client() {
        int port = 3691;
        try {
            s = new Socket("localhost", port);
            Scanner scanner = new Scanner(System.in);

            // initialize PrintWriter to send messages
            PrintWriter printWriter = new PrintWriter(s.getOutputStream(), true);
            System.out.println("Client connected to server...");

            // initializing a new thread to listen to server
            ClientReceive receive = new ClientReceive();
            receive.start();

            // to inform current client how to quit in the current console
            System.out.println("Type /q to quit.\n");
            // send messages(user input in console) to server
            String str;
            while ((str = scanner.nextLine()) != null) {
                // quit program if user input '/q'
                if (str.equals("/q")) {
                    printWriter.println("QUIT");
                    System.exit(0);
                }
                printWriter.println(str);
            }

        } catch (ConnectException ce) {
            System.out.println("Could not find server with port number " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client();
    }

    class ClientReceive extends Thread {
        public void run() {
            try {
                // receive from server
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String str;
                // print to the current client console
                while ((str = bufferedReader.readLine()) != null) {
                    System.out.println(str);
                }
            } catch (IOException e) {
                System.out.println("Failed initializing BufferedReader.");
                e.printStackTrace();
            }
        }
    }
}

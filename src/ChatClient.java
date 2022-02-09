import java.io.*;
import java.net.Socket;

public class ChatClient {
    private String hostName;
    private int port;
    private String userName;

    public ChatClient(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostName, port);
            System.out.println("Connected to chat server!");

            new ReadThread(socket, this).start();
            new WriteThread(socket, this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
        System.out.println("[" + userName + "]: ");

    }

    public String getUserName() {
        return userName;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Syntax: java ChatClient <port-number>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        ChatClient chatClient = new ChatClient("localhost", port);
        chatClient.execute();
    }
}

class ReadThread extends Thread {
    private Socket socket;
    private ChatClient chatClient;
    private BufferedReader bufferedReader;

    public ReadThread(Socket socket, ChatClient chatClient) {
        this.socket = socket;
        this.chatClient = chatClient;

        try {
            InputStream inputStream = socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                String message = bufferedReader.readLine();
                System.out.println("\n" + message);

                if (chatClient.getUserName() != null) {
                    System.out.println("[" + chatClient.getUserName() + "]: ");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class WriteThread extends Thread {

    private Socket socket;
    private ChatClient chatClient;
    private PrintWriter printWriter;

    public WriteThread(Socket socket, ChatClient chatClient) {
        this.socket = socket;
        this.chatClient = chatClient;

        try {
            OutputStream outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Console console = System.console();
        String userName = console.readLine("\nEnter your name: ");
        chatClient.setUserName(userName);

        printWriter.println(userName);

        String text;
        do {
            text = console.readLine();
            printWriter.println(text);
            System.out.println("[" + userName + "]: ");
        } while (!text.equalsIgnoreCase("bye"));

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

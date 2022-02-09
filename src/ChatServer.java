import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.exit;

public class ChatServer {
    private int port;
    private Set<String> userNames = new HashSet<>();
    private Set<UserThread> userThreads = new HashSet<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void execute() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening at port: " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Syntax: java ChatServer <port-number>");
            exit(0);
        }

        int port = Integer.parseInt(args[0]);
        ChatServer chatServer = new ChatServer(port);
        chatServer.execute();
    }

    public void addUserName(String userName) {
        userNames.add(userName);
    }

    public void broadcast(String serverMessage, UserThread excludeUserThread) {
        for (UserThread userThread : userThreads) {
            if (userThread != excludeUserThread) {
                userThread.sendMessage(serverMessage);
            }
        }
    }

    public void removeUser(String userName, UserThread userThread) {
        boolean removed = userNames.remove(userName);
        if (removed) {
            userThreads.remove(userThread);
            System.out.println("The user " + userName + " quited!");
        }
    }

    public boolean hasUsers() {
        return !userNames.isEmpty();
    }

    public Set<String> getUserNames() {
        return userNames;
    }
}

class UserThread extends Thread {
    private Socket socket;
    private ChatServer chatServer;
    private PrintWriter printWriter;

    public UserThread(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            OutputStream outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream, true);

            printUsers();

            String userName = bufferedReader.readLine();
            chatServer.addUserName(userName);

            String serverMessage = "New user connected with name: " + userName;
            chatServer.broadcast(serverMessage, this);

            String clientMessage;

            do {
                clientMessage = bufferedReader.readLine();
                serverMessage = "[" + userName + "]: " + clientMessage;
                chatServer.broadcast(serverMessage, this);
            } while (!clientMessage.equalsIgnoreCase("bye"));

            chatServer.removeUser(userName, this);
            socket.close();

            serverMessage = userName + " has quited!";
            chatServer.broadcast(serverMessage, this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printUsers() {
        if (chatServer.hasUsers()) {
            printWriter.println("Connected users: " + chatServer.getUserNames());
        } else {
            printWriter.println("No user connected!");
        }
    }

    public void sendMessage(String serverMessage) {
        printWriter.println(serverMessage);
    }
}


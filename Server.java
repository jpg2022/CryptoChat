import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private static Socket socket;
    private static ServerSocket serverSocket;

    private static ArrayList<ClientHandler> clients;

    private static final int port = 1234;
    private static final int maxClients = 3;
    private static String message;

    private static int[] publicKey;

    public int[] getPublicKey() {
        return publicKey;
    }

    public static void closeServer() throws IOException {
        socket.close();
        serverSocket.close();
    }

    public static void main(String[] args) {
        clients = new ArrayList<>(maxClients);
        Server server = new Server();

        publicKey = RSA.publicKey();
        message = String.valueOf(publicKey[0]) + "," + String.valueOf(publicKey[1]);

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Waiting for connection...");

            while (true) {

                socket = serverSocket.accept();

                if (clients.size() < maxClients) {
                    ClientHandler handler = server.new ClientHandler(socket);
                    clients.add(handler);
                    Thread thread = new Thread(handler);
                    thread.start();
                } else {
                    System.out.println("Too many clients...");
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    closeServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClientHandler implements Runnable {

        final Socket clientSocket;

        OutputStreamWriter outputStreamWriter;
        InputStreamReader inputStreamReader;
        BufferedWriter bufferedWriter;
        BufferedReader bufferedReader;

        String clientName;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
        }

        public void receive() {
            String msg;
            try {
                while (true) {
                    msg = bufferedReader.readLine();

                    if (msg.equals("\\disconnect")) {
                        System.out.println(clientName + " has disconnected...");
                        broadcastMessage(RSA.encrypt(clientName + " has disconnected...", publicKey[0], publicKey[1]),
                                clientName);
                        clients.remove(this);
                        break;
                    }

                    System.out
                            .println(clientName + " (" + clientSocket.getInetAddress().getHostAddress() + ", "
                                    + clientSocket.getPort() + "): " + msg);

                    broadcastMessage(clientName + ": " + msg, clientName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    closeAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void broadcastMessage(String m, String name) throws IOException {
            for (ClientHandler client : clients) {
                if (!client.clientName.equals(name)) {
                    client.bufferedWriter.write(m);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }
        }

        public void sendMsg() throws IOException {
            String msg = message;
            bufferedWriter.write(msg);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            receiveUsername();
            if (clientName.equals("Eve")) return;
            broadcastMessage(clientName + " " + RSA.encrypt("connected...", publicKey[0], publicKey[1]), clientName);
        }

        public void closeAll() throws IOException {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (inputStreamReader != null)
                inputStreamReader.close();
            if (outputStreamWriter != null)
                outputStreamWriter.close();
        }

        public void receiveUsername() throws IOException {
            this.clientName = bufferedReader.readLine();
            if (this.clientName.equals("Eve")) return;
            System.out.println(clientName + " connected...");
        }

        @Override
        public void run() {
            try {
                outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream());
                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                bufferedWriter = new BufferedWriter(outputStreamWriter);
                bufferedReader = new BufferedReader(inputStreamReader);

                sendMsg();
                receive();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    closeAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
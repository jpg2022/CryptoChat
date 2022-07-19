import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Eve {
    
    private static Socket socket;

    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;
    private static InputStreamReader inputStreamReader;
    private static OutputStreamWriter outputStreamWriter;

    private static final String ip = "localhost";
    private static final int port = 1234;

    private static int[] publicKey;

    public static void writeMsg(String msg) throws IOException {
        bufferedWriter.write(msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public static void closeAll() throws IOException {
        socket.close();
        bufferedReader.close();
        bufferedWriter.close();
        inputStreamReader.close();
        outputStreamWriter.close();
    }

    public static int factorial(int n) {
        if (n <= 2) {
            return n;
        }
        return n * factorial(n - 1);
    }

    public static int[] crackRSA(int n) {
        int k = 2;
        int counter = 1;
        int gcd;

        while (true) {
            int fact = factorial(counter);
            counter++;

            int test = RSA.powerMod(k, fact, n);
            gcd = RSA.gcd(test - 1, n);

            if (gcd > 1) {
                break;
            }
            if (counter > 10) {
                k++;
                counter = 1;
            }
        }

        int[] res = {gcd, n / gcd};
        
        return res;
    }

    public static void main(String[] args) throws IOException {
        socket = new Socket(ip, port);
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

        writeMsg("Eve");

        String msg = bufferedReader.readLine();
        String[] keyStringArr = msg.split(",", 2);
        int[] keyArr = { Integer.parseInt(keyStringArr[0]), Integer.parseInt(keyStringArr[1]) };
        publicKey = keyArr;

        System.out.println("Received public key from server...");
        System.out.println("e = " + publicKey[0] + " | N = " + publicKey[1]);
        

        int[] nFactored = crackRSA(publicKey[1]);
        int totient = (nFactored[0] - 1) * (nFactored[1] - 1);
        int d = RSA.modInverse(publicKey[0], totient);

        System.out.println("Solving discrete log algorithm...");
        System.out.println("d = " + d);

        String fullMsg;
        String clientName;

        while (socket.isConnected()) {
            try {
                fullMsg = bufferedReader.readLine();

                if (fullMsg == null) {
                    try {
                        closeAll();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                clientName = fullMsg.substring(0, fullMsg.indexOf(" ") + 1);
                msg = fullMsg.substring(fullMsg.indexOf(" ") + 1, fullMsg.length());
                if (msg.length() > 0) {
                    msg = RSA.decrypt(msg, d, publicKey[1]);
                }               

                System.out.println("\b\b\b>> " + clientName + msg);
                System.out.print(">> ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

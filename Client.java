import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class Client {

    private static Socket socket;

    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;
    private static InputStreamReader inputStreamReader;
    private static OutputStreamWriter outputStreamWriter;

    private static final String ip = "localhost";
    private static final int port = 1234;

    private static int[] publicKey;
    private static int[] privateKey;

    public Client() throws IOException {
        String msg = bufferedReader.readLine();
        String[] keyStringArr = msg.split(",", 2);
        int[] keyArr = { Integer.parseInt(keyStringArr[0]), Integer.parseInt(keyStringArr[1]) };
        publicKey = keyArr;
        privateKey = RSA.privateKey(publicKey[0]);

        System.out.println(publicKey[0]);
        System.out.println(privateKey[0]);
    }

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

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        socket = new Socket(ip, port);
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

        Client client = new Client();

        ListenerHandler ListenerHandler = client.new ListenerHandler();
        Thread thread = new Thread(ListenerHandler);
        thread.start();

        thread.join();
        closeAll();
    }

    private class ListenerHandler implements Runnable {

        @Override
        public void run() {
            JFrame frame = new JFrame();

            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent evt) {
                    try {
                        writeMsg("\\disconnect");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
            });
            frame.setLayout(new BorderLayout());
            frame.setTitle("Cryptochat");
            frame.setPreferredSize(new Dimension(750, 1000));
            frame.setVisible(true);
            frame.pack();

            JPanel titleWrapper = new JPanel();
            JLabel titleLabel = new JLabel();

            titleWrapper.setLayout(new BorderLayout());
            titleWrapper.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight() / 15));
            titleWrapper.setBackground(new Color(0x212121));
            titleLabel.setText("CryptoChat");
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            titleLabel.setVerticalTextPosition(JLabel.CENTER);
            titleLabel.setHorizontalTextPosition(JLabel.CENTER);
            titleLabel.setForeground(Color.WHITE);
            titleWrapper.add(titleLabel, BorderLayout.CENTER);

            JPanel msgWrapper = new JPanel();
            JPanel textWrapper = new JPanel();
            JPanel msgPanel = new JPanel();
            JTextField text = new JTextField();

            msgWrapper.setLayout(new BorderLayout());
            msgWrapper.setBackground(new Color(0x323232));

            textWrapper.setLayout(new BorderLayout());
            textWrapper.setBackground(new Color(0x212121));
            textWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

            msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
            msgPanel.setPreferredSize(msgWrapper.getSize());
            msgPanel.setBackground(msgWrapper.getBackground());

            LineBorder lineBorder = new LineBorder(Color.white, 8, true);
            text.setBorder(lineBorder);
            text.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String msg = text.getText();

                    JLabel msgLabel = new JLabel("<html><span color=\"#ff0000\">You:  </span>" + msg + "</html>");
                    msgLabel.setForeground(Color.white);

                    if (msg.equals("")) {
                        return;
                    }

                    msgPanel.add(msgLabel);
                    msgPanel.revalidate();
                    msgPanel.repaint();
                    text.setText("");

                    try {
                        writeMsg(RSA.encrypt(msg, publicKey[0], publicKey[1]));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    System.out.println(msg);
                }
            });

            textWrapper.add(text);
            msgWrapper.add(textWrapper, BorderLayout.SOUTH);
            msgWrapper.add(msgPanel);

            JPanel backgroundPanel = new JPanel();

            backgroundPanel.setLayout(new BorderLayout());
            backgroundPanel.setBackground(new Color(0x323232));
            backgroundPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            backgroundPanel.add(msgWrapper, BorderLayout.CENTER);
            backgroundPanel.add(titleWrapper, BorderLayout.NORTH);

            JPanel namePanel = new JPanel();
            JPanel nameWrapper = new JPanel();
            JTextField nameInput = new JTextField();
            JLabel namePrompt = new JLabel();

            namePanel.setLayout(new BorderLayout());
            namePanel.setBackground(new Color(0x323232));
            namePanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            namePanel.add(titleWrapper, BorderLayout.PAGE_START);

            nameWrapper.setLayout(new GridLayout(10, 1));
            nameWrapper.setBackground(namePanel.getBackground());
            nameWrapper.setBorder(new EmptyBorder(300, 200, 300, 200));

            nameInput.setBorder(lineBorder);
            nameInput.setPreferredSize(new Dimension(100, 30));
            nameInput.setHorizontalAlignment(JTextField.CENTER);

            namePrompt.setPreferredSize(new Dimension(50, 50));
            namePrompt.setForeground(Color.WHITE);
            namePrompt.setText("Enter username: ");
            namePrompt.setHorizontalAlignment(JLabel.CENTER);
            namePrompt.setLabelFor(nameInput);

            nameInput.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String msg = nameInput.getText();
                    System.out.println(msg);

                    try {
                        writeMsg(msg);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    frame.remove(namePanel);
                    frame.add(backgroundPanel);
                    frame.add(titleWrapper, BorderLayout.NORTH);
                    frame.revalidate();
                    frame.repaint();
                }
            });

            nameWrapper.add(namePrompt);
            nameWrapper.add(nameInput);

            namePanel.add(nameWrapper);

            frame.add(namePanel);

            String fullMsg;
            String clientName;
            String msg;

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
                    if (msg.length() > 0)
                        msg = RSA.decrypt(msg, privateKey[0], privateKey[1]);

                    JLabel msgLabel = new JLabel(
                            "<html><span color=\"#46E2E6\">" + clientName + "  </span>" + msg + "</html>");
                    msgLabel.setForeground(Color.white);

                    msgPanel.add(msgLabel);
                    msgPanel.revalidate();
                    msgPanel.repaint();

                    System.out.println("\b\b\b>> " + clientName + msg);
                    System.out.print(">> ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

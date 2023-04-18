package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
public class client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8003)) {
            // Input and output streams for the socket
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Send message to server
            output.println("Hello from client.");

            // Receive response from server
            String response = input.readLine();
            System.out.println("Received from server: " + response);
        } catch (IOException e) {
            System.out.println("Unknown host: " + e.getMessage());
        }
        GridPane menu = new GridPane();
        Screen primaryScreen = Screen.getPrimary();

    }
}
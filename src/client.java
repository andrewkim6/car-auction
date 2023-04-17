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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class client extends Application{
    String user = "akim6";
    int pass = 1234;
    public static void main(String[] args) {
        //connect();
        
        launch(args);
        
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Auction Client Login");

        // create login form elements
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Login");

        // create layout for login form
        VBox loginLayout = new VBox(10);
        loginLayout.getChildren().addAll(userLabel, userField, passLabel, passField, loginButton);
        loginLayout.setStyle("-fx-padding: 10px");

        // create scene for login window
        Scene loginScene = new Scene(loginLayout, 300, 200);

        // show login window
        primaryStage.setScene(loginScene);
        primaryStage.show();

        loginButton.setOnAction(e-> {
            if(userField.getText().equals(user) && Integer.parseInt(passField.getText()) == pass){
                //AuctionWindow auctionWindow = new AuctionWindow();
              //  auctionWindow.show();
                primaryStage.hide();
            }
        });
    } 

    public static void connect(){

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
    }

    
}




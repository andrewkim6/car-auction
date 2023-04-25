import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class client extends Application{
    
    public static void main(String[] args) {
        //connect();

        launch(args);
         String URI = "mongodb+srv://akim678910:2812368663a@cluster0.iku4q9b.mongodb.net/?retryWrites=true&w=majority";
        String DB = "auction";
        String COLLECTION = "users";
        MongoDatabase database = MongoClients.create(URI).getDatabase(DB);
        MongoCollection<Document> users = database.getCollection(COLLECTION);
        

    }
    Socket socket;
        OutputStream os;
        ObjectOutputStream oos;
        InputStream is;
        ObjectInputStream ois;
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Auction Client Login");

        
        try {
            socket = new Socket("localhost", 8003);
            os = socket.getOutputStream(); //clientsocket's output stream
            oos = new ObjectOutputStream(os);
            is = socket.getInputStream();
            ois = new ObjectInputStream(is);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // create login form elements
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Login");
        Label error = new Label();
        Label create = new Label("Create New Account");
        Label newUserLabel = new Label("Username:");
        Label newPassLabel = new Label("Password:");
        TextField newUser = new TextField();
        TextField newPass = new TextField();
        Button createButton = new Button();
        createButton.setText("Create");
        Label createError = new Label();
        Label guestTitle = new Label("Login as Guest");
        Label guestUser = new Label("Username:");
        TextField guestUserField = new TextField();
        Label guestError = new Label();
        Button guestLoginButton = new Button("Login");
        // create layout for login form

        HBox guestLine = new HBox();
        guestLine.setSpacing(10);
        guestLine.setAlignment(Pos.CENTER_LEFT);
        guestLine.getChildren().addAll(guestUser, guestUserField);

        HBox guestLoginLine = new HBox();
        guestLoginLine.setSpacing(10);
        guestLoginLine.setAlignment(Pos.CENTER_LEFT);
        guestLoginLine.getChildren().addAll(guestLoginButton, guestError);

        HBox user = new HBox();
        user.setSpacing(10);
        user.setAlignment(Pos.CENTER_LEFT);
        user.getChildren().addAll(newUserLabel, newUser);

        HBox pass = new HBox();
        pass.setSpacing(10);
        pass.setAlignment(Pos.CENTER_LEFT);
        pass.getChildren().addAll(newPassLabel, newPass);

        HBox loginLine = new HBox();
        loginLine.setSpacing(10);
        loginLine.setAlignment(Pos.CENTER_LEFT);
        loginLine.getChildren().addAll(loginButton, error);
        
        HBox createLine = new HBox();
        createLine.setSpacing(10);
        createLine.setAlignment(Pos.CENTER_LEFT);
        createLine.getChildren().addAll(createButton, createError);

        

        VBox loginLayout = new VBox(10);
        loginLayout.getChildren().addAll(userLabel, userField, passLabel, passField, loginLine, create, user, pass, createLine, 
        guestTitle, guestLine, guestLoginLine);
        loginLayout.setStyle("-fx-padding: 10px");

        // create scene for login window
        Scene loginScene = new Scene(loginLayout, 500, 500);

        // show login window
        primaryStage.setScene(loginScene);
        primaryStage.show();

        createButton.setOnAction(e->{
            if(!newUser.getText().equals("") && !newUser.getText().equals("")){
                String newUsername = newUser.getText();
                String newPassword = newPass.getText();
                User myUser = new User(newUsername, newPassword);
                try {
                    oos.reset();
                    oos.writeUnshared(myUser); //adds to output stream
                    oos.flush();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                createError.setText("User successfully created!");
            }
            else{
                createError.setText("Error: field(s) left blank!");
            }
        });
        
        guestLoginButton.setOnAction(e->{
            String guest = guestUserField.getText();
            System.out.println(guest);
            if(!guest.equals("")){
                guestError.setText("Connecting...");
                AuctionWindow auctionWindow = new AuctionWindow(guest, socket, os, oos, ois);
                primaryStage.hide();
                auctionWindow.show();
            }
            else{
                guestError.setText("Error: Field left blank!");
            }
        });
        loginButton.setOnAction(e-> {
            try{
            if(!userField.getText().equals("") && !passField.getText().equals("")){
                Document doc = validUser(userField.getText(), passField.getText());
                if(doc!=null){ //if the user exists and the password is correct
                    error.setText("connecting...");
                    AuctionWindow auctionWindow = new AuctionWindow(userField.getText(), socket, os, oos, ois);
                    primaryStage.hide();
                    auctionWindow.show();
                }
                else{
                    error.setText("Error: Incorrect password or username!");
                }
            }
            else{
                error.setText("Error: Field(s) left blank!");
            }
        } catch(NumberFormatException e2){
            error.setText("Error: Field(s) left blank!");
        }
        });
    } 
    

    public static Document validUser(String user, String pass) {
        String URI = "mongodb+srv://akim678910:2812368663a@cluster0.iku4q9b.mongodb.net/?retryWrites=true&w=majority";
        String DB = "auction";
        String COLLECTION = "users";
        MongoDatabase database = MongoClients.create(URI).getDatabase(DB);
        MongoCollection<Document> users = database.getCollection(COLLECTION);
        FindIterable<Document> documents = users.find();
        for (Document doc : documents) {
            String username = doc.getString("Username");
            String password = doc.getString("Password");
            if(user.equals(username) && pass.equals(password)){
                return doc;
            }
        }
        return null;

    }

    


}


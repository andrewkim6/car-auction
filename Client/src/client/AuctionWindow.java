package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import client.Bid;
import client.AuctionWindow;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.HashMap;

public class AuctionWindow extends Stage {
    private TextArea bidHistoryTextArea = new TextArea();
    private Label highestBidLabel = new Label();
    private TextField bidTextField = new TextField();
    private Button bidButton = new Button("Bid");
    private ComboBox<String> itemComboBox = new ComboBox<>();
    private final static String URI = "mongodb+srv://akim678910:2812368663a@cluster0.iku4q9b.mongodb.net/?retryWrites=true&w=majority";
    private static final String DB = "auction";
    private static final String COLLECTION = "items";
    private Label brand = new Label();
    private Label type = new Label();
    private Label currentBid = new Label();
    private Button exit = new Button();
    private Label error = new Label();
    private String user;
    private Label bidAmount = new Label();
    private HashMap<String, Bid> bidMap = new HashMap<>();

        // Input and output streams for the socket
        
        
        
    
    private Socket socket;
    private OutputStream os;
    private ObjectOutputStream oos;

    public AuctionWindow(String user , Socket socket, OutputStream os, ObjectOutputStream oos) {
        this.socket = socket;
        this.os = os;
        this.oos = oos;
        
        exit.setText("Logout");
        this.user = user;
        setTitle("Auction Window");
        brand.setText("Brand");
        currentBid.setText("Current Bid:");
        type.setText("");
        bidAmount.setText("Bid Amount ($):");

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(itemComboBox, brand, type, currentBid);

        HBox bidBox = new HBox();
        bidBox.setSpacing(10);
        bidBox.setAlignment(Pos.CENTER_LEFT);
        bidBox.getChildren().addAll(bidButton, error);

        HBox bidLine = new HBox();
        bidLine.setSpacing(10);
        bidLine.setAlignment(Pos.CENTER_LEFT);
        bidLine.getChildren().addAll(bidAmount, bidTextField);

        // Create GUI components and add them to layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(hbox, bidHistoryTextArea, highestBidLabel, bidLine, bidBox, exit);

        // Set layout and size of window
        setScene(new Scene(layout));
        setWidth(800);
        setHeight(600);
        setResizable(false);

        MongoDatabase database = MongoClients.create(URI).getDatabase(DB);
        MongoCollection<Document> items = database.getCollection(COLLECTION);
        MongoCursor<Document> cursor = items.find().iterator();
        // Retrieve data from MongoDB and add to a List
        List<String> listings = new ArrayList<String>();
        FindIterable<Document> documents = items.find();
        for (Document doc : documents) {
            String itemName = doc.getString("Model");
            listings.add(itemName);
        }

        // Convert List to ObservableList and set as items of ComboBox
        ObservableList<String> observableItems = FXCollections.observableArrayList(listings);
        itemComboBox.setItems(observableItems);
        itemComboBox.getItems().addAll(listings);   
        itemComboBox.setVisibleRowCount(listings.size());

        // Calculate height of ComboBox
        double rowHeight = 10; // replace with actual row height
        double comboBoxHeight = itemComboBox.getVisibleRowCount() * rowHeight;
        itemComboBox.setPrefHeight(comboBoxHeight);

        itemComboBox.setOnAction(e-> {
            
            // create a query document to find the document by name field
            Document query = new Document("Model", itemComboBox.getValue());
            Document result = items.find(query).first();
            System.out.println(result.getString("Brand"));
            // execute the query and get the document that matches the query
            if (result.getString("Brand") != null && result.getString("Type") != null) {
                // set the brand label text to the brand value from the document
                brand.setText(result.getString("Brand"));
                type.setText(result.getString("Type"));
                Double maxBid = result.getDouble("Max");
                if (maxBid != null) {
                    currentBid.setText("Current Bid: " + result.getDouble("Max"));
                } else {
                    currentBid.setText("No bids yet");
                }               
                 // TODO: You can add more code here to update other UI elements based on the selected item
            }


        });

        bidButton.setOnAction(e->{
            try{
                Document query = new Document("Model", itemComboBox.getValue());
                Document result = items.find(query).first();
                double bidValue = Double.parseDouble(bidTextField.getText());
                if(bidValue >0 && bidValue > result.getDouble("Max")){
                    currentBid.setText("Current Bid: "+result.getDouble("Max"));
                    Bid newBid = new Bid(bidValue, user, result.getString("Model"), result.getString("Brand"), result.getString("Type"));
                    try {
                        oos.reset();
                        oos.writeUnshared(newBid); //adds to output stream
                        oos.flush();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    /*Document update = new Document("$set", new Document("Max", bidValue));
                    items.updateOne(result, update);
                    error.setText("Bid succesfully made"); */
                }
                else{
                    error.setText("Error: Bid too low");
                }
            } catch(NumberFormatException e1){
                error.setText("Error: Incorrect Format");
            }
        });

        exit.setOnAction(e->{
            try {
                oos.close();
                os.close();
                socket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.exit(0);
        });
        // TODO: Add event handler for bid button

        
    }


    private FindIterable<Document> items() {
        return null;
    }

    // TODO: Implement methods for updating bid history, highest bid, and disabling bidding
    
}

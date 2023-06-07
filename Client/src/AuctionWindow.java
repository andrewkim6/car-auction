// Copy-paste this file at the top of every file you turn in.
/*
* EE422C Final Project submission by
* Replace <...> with your actual data.
* Andrew Kim
* AK46428
* 17150
* Spring 2023
*/


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.HashMap;
import java.time.LocalDateTime;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TableView;
import javafx.util.Duration;


public class AuctionWindow extends Stage {
    Image gtrs = new Image("911.jpg");
    Image mustang = new Image("mustang.jpg");
    Image s500 = new Image("s500.jpg");
    Image model3 = new Image("model3.jpeg");
    Image m5 = new Image("m5.jpg");
    ImageView imageView = new ImageView();
    private Label highestBidLabel = new Label();
    private TextField bidTextField = new TextField();
    private Button bidButton = new Button("Bid");
    private ComboBox<String> itemComboBox = new ComboBox<>();
    private final static String URI = "mongodb+srv://user:pass@cluster0.iku4q9b.mongodb.net/?retryWrites=true&w=majority";
    private static final String DB = "auction";
    private static final String COLLECTION = "items";
    private Label brand = new Label();
    private Label type = new Label();
    private Label currentBid = new Label();
    private Button exit = new Button();
    private Label error = new Label();
    private Label MSRP = new Label("MSRP");
    private Label minimum = new Label("Minimum Bid: ");
    private Label description = new Label("Description: ");
    private String user;
    private Label bidAmount = new Label();
    private HashMap<String, Bid> bidMap = new HashMap<>();
    private Socket socket;
    private OutputStream os;
    private ObjectInput ois;
    private ObjectOutputStream oos;
    private TextArea textArea = new TextArea();
    private ObjectInputStream serverInput;
    private boolean isListening = false;
    private boolean isChecking = true;
    private boolean isUpdating = true;
    private Thread checkingThread;
    private Thread listenerThread;
    private Thread updatingThread;
    private int gtrsTime;
    private int mustangTime;
    private int model3Time;
    private int s500Time;
    private int m5Time;
    private Label label = new Label();
    private Timeline timeline;
    private boolean itemUpdating;
    private Thread itemThread;
    private TextArea itemHistory = new TextArea();
    private Thread startMusic;
    MongoDatabase database = MongoClients.create(URI).getDatabase(DB);
    MongoCollection<Document> items = database.getCollection(COLLECTION);
    MongoCursor<Document> cursor = items.find().iterator();
    MongoCollection<Document> histories = database.getCollection("history");
    public void startChecking(){
        isChecking = true;
        checkingThread = new Thread(() -> {
            while(isChecking){
            List<String> listofItems = new ArrayList<String>();
            FindIterable<Document> docs = items.find();
            for (Document doc : docs) {
                LocalDateTime expirationTime = null;
                Date date = doc.getDate("Time");
                if (date != null) {
                    expirationTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                }
                if (expirationTime != null && expirationTime.atZone(ZoneId.systemDefault()).isAfter(LocalDateTime.now().atZone(ZoneId.systemDefault())) && doc.getBoolean("Status")) {
                    String itemName = doc.getString("Model");
                    listofItems.add(itemName);
                }
            }

            Platform.runLater(() -> {
                ObservableList<String> observable = FXCollections.observableArrayList(listofItems);
                itemComboBox.setItems(observable);
                itemComboBox.setVisibleRowCount(listofItems.size());

                // Calculate height of ComboBox
                double row = 10; // replace with actual row height
                double height = itemComboBox.getVisibleRowCount() * row;
                itemComboBox.setPrefHeight(height);
            });
            try {
                Thread.sleep(1000); // replace with appropriate interval between updates
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        });
        checkingThread.start();
    }
    Object updateLock = new Object();
    Object listenLock = new Object();
    public void setTime(){
        for(Document doc : items.find()){
            if(doc.getString("Model").equals("911")){
                Date date = doc.getDate("Time");
                LocalDateTime expirationTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                LocalDateTime currentDate = LocalDateTime.now(); // example current date, using current time
                int totalSeconds1 = expirationTime.getHour() * 3600 + expirationTime.getMinute() * 60 + expirationTime.getSecond();
                int totalSeconds2 = currentDate.getHour() * 3600 + currentDate.getMinute() * 60 + currentDate.getSecond();
                gtrsTime = (totalSeconds1 - totalSeconds2);
            }
            else if(doc.getString("Model").equals("Mustang")){
                Date date = doc.getDate("Time");
                LocalDateTime expirationTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                LocalDateTime currentDate = LocalDateTime.now(); // example current date, using current time
                int totalSeconds1 = expirationTime.getHour() * 3600 + expirationTime.getMinute() * 60 + expirationTime.getSecond();
                int totalSeconds2 = currentDate.getHour() * 3600 + currentDate.getMinute() * 60 + currentDate.getSecond();
                mustangTime = (totalSeconds1 - totalSeconds2);
            }
            else if(doc.getString("Model").equals("S500")){
                Date date = doc.getDate("Time");
                LocalDateTime expirationTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                LocalDateTime currentDate = LocalDateTime.now(); // example current date, using current time
                int totalSeconds1 = expirationTime.getHour() * 3600 + expirationTime.getMinute() * 60 + expirationTime.getSecond();
                int totalSeconds2 = currentDate.getHour() * 3600 + currentDate.getMinute() * 60 + currentDate.getSecond();
                s500Time = (totalSeconds1 - totalSeconds2);
            }
            else if(doc.getString("Model").equals("Model 3")){
                Date date = doc.getDate("Time");
                LocalDateTime expirationTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                LocalDateTime currentDate = LocalDateTime.now(); // example current date, using current time
                int totalSeconds1 = expirationTime.getHour() * 3600 + expirationTime.getMinute() * 60 + expirationTime.getSecond();
                int totalSeconds2 = currentDate.getHour() * 3600 + currentDate.getMinute() * 60 + currentDate.getSecond();
                model3Time = (totalSeconds1 - totalSeconds2);
            }
            else if(doc.getString("Model").equals("M5")){
                Date date = doc.getDate("Time");
                LocalDateTime expirationTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                LocalDateTime currentDate = LocalDateTime.now(); // example current date, using current time
                int totalSeconds1 = expirationTime.getHour() * 3600 + expirationTime.getMinute() * 60 + expirationTime.getSecond();
                int totalSeconds2 = currentDate.getHour() * 3600 + currentDate.getMinute() * 60 + currentDate.getSecond();
                m5Time = (totalSeconds1 - totalSeconds2);
            }
        }
    }
    public void reset(){
        imageView.setImage(null);
        brand.setText("Brand:");
        type.setText("Type:");
        MSRP.setText("MSRP:");
        minimum.setText("Minimum Bid:");
        label.setText("Timer:");
        currentBid.setText("Current Bid:");
        description.setText("Description:");
    }
    
    public void itemUpdating(){
        itemUpdating = true;
        itemThread = new Thread(() ->{
            try{
                while(itemUpdating){
                    if(itemComboBox.getValue()!= null){
                        Document query = new Document("Model", itemComboBox.getValue()).append("Status", true);
                        Document result = items.find(query).first();
                        List<String> itemhis = new ArrayList<>((List<String>) result.get("History"));
                        //itemHistory.setText("");
                        Platform.runLater(() -> {
                            itemHistory.clear();
                            for(String line : itemhis){    
                                itemHistory.appendText(line);
                            }
                        });
                          Thread.sleep(5000);
                        }
                }
            }catch (Exception e7){
                e7.printStackTrace();
            }
        }); itemThread.start();
    }
    int value = 0;
    public synchronized void startUpdating(){
        isUpdating = true;
        updatingThread = new Thread(() -> {
            try{
                while(isUpdating) {  
                    value= 0;  
                    if(histories.find().first() != null) {
                    Document queryHistory = histories.find().first();
                    List<String> all = (List<String>) queryHistory.get("History");
                   // System.out.println(all.size());
                    Platform.runLater(() -> {
                        textArea.clear();
                        for(String line : all){    
                            textArea.appendText(line);
                        }
                    });
                    if(itemComboBox.getValue()!= null){
                        Document query = new Document("Model", itemComboBox.getValue());
                        Document result = items.find(query).first();
                        List<String> itemhis = new ArrayList<>((List<String>) result.get("History"));
                        //itemHistory.setText("");
                        Platform.runLater(() -> {
                            itemHistory.clear();
                            for(String line : itemhis){    
                                itemHistory.appendText(line);
                            }
                        });
                    }
                }
                    value = 1;
                    Thread.sleep(3000);
            }
            }catch(Exception e8 ){
                e8.printStackTrace();
            }
        }); 
        updatingThread.start();
    }

    public void startListening() {
        isListening = true;
        listenerThread = new Thread(() -> {
            try {
                while (isListening && socket.isConnected() && !socket.isClosed()) {
                        // read from the socket
                        
                        Object receivedObject = ois.readObject();
                        while(value == 0){

                        }
                        if (receivedObject instanceof Bid) {
                            // Handle the bid object
                            Document result;
                            Document query;
                            Document queryHistory;
                            Bid bid = (Bid) receivedObject;
                            synchronized(this){
                             query = new Document("Model", bid.getModel());
                             result = items.find(query).first();
                             queryHistory = histories.find().first(); 
                            }
                                
                            if(bid.getHighbidder() == false){
                                if(result.getBoolean("Status")){
                                    String entry = (bid.getBidder()+" has bid: $" + bid.getAmount() + " on " + bid.getBrand() + " " +bid.getModel()+"\n");
                                    Document updateHistory = new Document("$push", new Document("History", entry));
                                    histories.updateOne(queryHistory, updateHistory);
                                    System.out.println(queryHistory.getList("History",String.class).size());
            
                                    Document update = new Document("$push", new Document("History", entry));
                                    items.updateOne(result, update);
                                    System.out.println(result.getList("History", String.class).size());
                                }
                                else{
                                    String entry = (bid.getBidder()+" has purchased "+ bid.getBrand() + " " + bid.getModel() + " for $" + bid.getAmount() +"\n");
                                    Document updateHistory = new Document("$push",new Document("History", entry));
                                    histories.updateOne(queryHistory, updateHistory);

                                    Document update = new Document("$push", new Document("History", entry));
                                    items.updateOne(result, update);
                                }
                            }
                            else{
                                if(!bid.getBidder().equals("")){
                                    String entry = (bid.getBidder()+" has won "+ bid.getBrand() + " " + bid.getModel() + " for $" + bid.getAmount() + " by time" +"\n");
                                    Document updateHistory = new Document("$push", new Document("History", entry));
                                    histories.updateOne(queryHistory, updateHistory);

                                    Document update = new Document("$push", new Document("History", entry));
                                    items.updateOne(result, update);
                                }
                                else{
                                    String entry = ("Time for " + bid.getBrand() + " " + bid.getModel() + " has been expired" + "\n");
                                    Document updateHistory = new Document("$push" , new Document("History", entry));
                                    histories.updateOne(queryHistory, updateHistory); 
                                    
                                    Document update = new Document("$push", new Document("History", entry));
                                    items.updateOne(result, update);
                                }
                            }
                        }
                        
                    
                
                }
            } catch (IOException | ClassNotFoundException e) {
                return;
            }
        });
        listenerThread.start();
    }
    public void music(){
        startMusic = new Thread(() -> {
            Media media = new Media(new File("src/tokyodrift.mp3").toURI().toString());
        // Create a MediaPlayer object to play the music
        AudioClip mediaPlayer = new AudioClip(media.getSource());
        // Start playing the music
        mediaPlayer.setVolume(.2);
        mediaPlayer.play();

        }); startMusic.start();
    }

    public AuctionWindow(String user , Socket socket, OutputStream os, ObjectOutputStream oos, ObjectInputStream ois) {
        this.socket = socket;
        this.os = os;
        this.oos = oos;
        this.ois = ois;
        
        setTime();
        music();
        
        
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                gtrsTime--;
                mustangTime--;
                model3Time--;
                s500Time--;
                m5Time--;
                if(itemComboBox.getValue() != null){
                    String model = itemComboBox.getValue();
                    if(model.equals("911")){
                        if (gtrsTime >= 0) {
                            label.setText(String.format("Timer: "+"%02d:%02d", gtrsTime / 60, gtrsTime % 60));
                        } else {
                            timeline.stop();
                            label.setText("Time's up!");
                        }
                    }
                    else if(model.equals("Mustang")){
                        if (mustangTime >= 0) {
                            label.setText(String.format("Timer: "+"%02d:%02d", mustangTime / 60, mustangTime % 60));
                        } else {
                            timeline.stop();
                            label.setText("Time's up!");
                        }
                    }
                    else if(model.equals("S500")){
                        if (s500Time >= 0) {
                            label.setText(String.format("Timer: "+"%02d:%02d", s500Time / 60, s500Time % 60));
                        } else {
                            timeline.stop();
                            label.setText("Time's up!");
                        }
                    } 
                    else if(model.equals("Model 3")){
                        if (model3Time >= 0) {
                            label.setText(String.format("Timer: "+ "%02d:%02d", model3Time / 60, model3Time % 60));
                        } else {
                            timeline.stop();
                            label.setText("Time's up!");
                        }
                    } 
                    else if(model.equals("M5")){
                        if (m5Time >= 0) {
                            label.setText(String.format("Timer: "+"%02d:%02d", m5Time / 60, m5Time % 60));
                        } else {
                            timeline.stop();
                            label.setText("Time's up!");
                        }
                    } 
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        
        imageView.setFitWidth(300);
        imageView.setFitHeight(150);
        imageView.setLayoutX(50);
        imageView.setLayoutY(50);
        

        startListening();
        startChecking();
        startUpdating();
        //itemUpdating();


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
        hbox.getChildren().addAll(itemComboBox, brand, type, currentBid, MSRP, minimum, label);

        HBox bidBox = new HBox();
        bidBox.setSpacing(10);
        bidBox.setAlignment(Pos.CENTER_LEFT);
        bidBox.getChildren().addAll(bidButton, error);

        HBox bidLine = new HBox();
        bidLine.setSpacing(10);
        bidLine.setAlignment(Pos.CENTER_LEFT);
        bidTextField.setStyle("-fx-background-color: #f5e6cd; -fx-text-fill: #000000;");
        bidLine.getChildren().addAll(bidAmount, bidTextField);

        BorderPane borderPane = new BorderPane();
        Label itemHistoryLabel = new Label("Item History");
        itemHistory.setStyle("-fx-background-color: #f5e6cd; -fx-text-fill: #000000;");
        borderPane.setCenter(itemHistory);
        borderPane.setTop(itemHistoryLabel);

        BorderPane historyPane = new BorderPane();
        Label totalHistoryLabel = new Label("All History");
        textArea.setStyle("-fx-background-color: #f5e6cd; -fx-text-fill: #000000;");
        historyPane.setCenter(textArea);
        historyPane.setTop(totalHistoryLabel);


        HBox imageLine = new HBox();
        imageLine.setSpacing(10);
        imageLine.setAlignment(Pos.CENTER_LEFT);
        imageLine.getChildren().addAll(imageView, borderPane);

        // Create GUI components and add them to layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(hbox, description, imageLine, highestBidLabel, bidLine, bidBox, historyPane, exit);
        layout.setStyle("-fx-background-color: #d9d1c4;");

        // Set layout and size of window
        setScene(new Scene(layout));
        setWidth(800);
        setHeight(600);
        setResizable(false);

        
        

    

        // Calculate height of ComboBox
        double rowHeight = 10; // replace with actual row height
        double comboBoxHeight = itemComboBox.getVisibleRowCount() * rowHeight;
        itemComboBox.setPrefHeight(comboBoxHeight);
        itemComboBox.setOnAction(e-> {
            // create a query document to find the document by name field
            Document query = new Document("Model", itemComboBox.getValue()).append("Status", true);
            Document result = items.find(query).first();
            //System.out.println(result.getString("Brand"));
            // execute the query and get the document that matches the query
            if(itemComboBox.getValue() != null && result != null){
                if (result.getString("Brand") != null && result.getString("Type") != null) {
                    // set the brand label text to the brand value from the document
                    if(result.getString("Model").equals("911")){
                        imageView.setImage(gtrs);
                    }
                    else if(result.getString("Model").equals("Mustang")){
                        imageView.setImage(mustang);
                    }
                    else if(result.getString("Model").equals("S500")){
                        imageView.setImage(s500);
                    }
                    else if(result.getString("Model").equals("Model 3")){
                        imageView.setImage(model3);
                    }
                    else if(result.getString("Model").equals("M5")){
                        imageView.setImage(m5);
                    }
                    brand.setText(result.getString("Brand"));
                    type.setText(result.getString("Type"));
                    Double maxBid = result.getDouble("Max");
                    if (maxBid != null) {
                        currentBid.setText("Current Bid: $" + result.getDouble("Max"));
                        MSRP.setText("MSRP: $" + result.getDouble("Price").toString());
                        minimum.setText("Minimum Bid: $" + result.getDouble("Min"));
                        description.setText("Description: " + result.getString("Info"));
                    } else {
                        currentBid.setText("No bids yet");
                    }                
                    // TODO: You can add more code here to update other UI elements based on the selected item
                }
            }else{
                reset();
            }


        });
        bidButton.setStyle("-fx-background-color: #3f3e43; -fx-text-fill: #ffffff;");
        bidButton.setOnAction(e->{
            try{
                Document query = new Document("Model", itemComboBox.getValue());
                Document result = items.find(query).first();
                double bidValue = Double.parseDouble(bidTextField.getText());
                Date date = result.getDate("Time");
                LocalDateTime expirationTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                if(result.getBoolean("Status") == true && expirationTime.atZone(ZoneId.systemDefault()).isAfter(LocalDateTime.now().atZone(ZoneId.systemDefault()))){
                    if(bidValue >0 && bidValue > result.getDouble("Max") && bidValue > result.getDouble("Min")){
                        currentBid.setText("Current Bid: "+result.getDouble("Max"));
                        Bid newBid = new Bid(bidValue, user, result.getString("Model"), result.getString("Brand"), result.getString("Type"), false);
                        try {
                            oos.reset();
                            oos.writeUnshared(newBid); //adds to output stream
                            oos.flush();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        if(bidValue > result.getDouble("Price")){
                            error.setText("Item sold!");
                            reset();
                        }
                        else{
                            error.setText("Bid succesfully made");
                        }
                    }
                    else{
                        error.setText("Error: Bid too low");
                    }
                }
                else{ //if the item is not longer active
                    error.setText("Error: Item is no longer available");
                }
            } catch(NumberFormatException | NullPointerException e1){
                error.setText("Error: Incorrect Format");
            }
        });

        exit.setStyle("-fx-background-color: #3f3e43; -fx-text-fill: #ffffff;");
        exit.setOnAction(e->{
            try {
                oos.flush(); // flush the output stream before closing
                oos.close();
                os.close();
                socket.close();
                stopListening();
                stopChecking();
                
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.exit(0);
        });
        // TODO: Add event handler for bid button

        
    }

    public void stopListening() {
        isListening = false;
        try {
            listenerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopChecking(){
        isChecking = false;
        try{
            checkingThread.join();
        } catch (InterruptedException e1){
            e1.printStackTrace();
        }
    }

    private FindIterable<Document> items() {
        return null;
    }

    // TODO: Implement methods for updating bid history, highest bid, and disabling bidding
    
}

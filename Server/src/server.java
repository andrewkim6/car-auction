import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;
import java.util.Set;

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
public class server implements AuctionServerInterface {
    // Implementation of AuctionServerInterface methods
    private static MongoClient mongo;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private final static String URI = "mongodb+srv://akim678910:2812368663a@cluster0.iku4q9b.mongodb.net/?retryWrites=true&w=majority";
    private static final String DB = "auction";
    private static final String COLLECTION = "items";
    private static final String COLLECTION2 = "users";
    public static void main(String[] args) throws IOException {
        mongo = MongoClients.create(URI);
        database = mongo.getDatabase(DB);
        collection = database.getCollection(COLLECTION);
        

        
        AuctionServerInterface server = new server();
        server.loadItemsFromFile("cars.txt");
        List<Socket> connectedClients = new ArrayList<>();
        List<ObjectOutputStream> clientOutputStreams = new ArrayList<>();
        
        ServerSocket ss = new ServerSocket(8003);
        

        Set<Bid> sentItemIds = new HashSet<>();
       // mongo.close();
       Thread thread = new Thread(new Runnable(){
            
        @Override
        public void run(){
            while (true) {
                // Query the collection for all items
                try{
                Bid bid = null;
                FindIterable<Document> docs = collection.find();
                for (Document item : docs) {
                    LocalDateTime expirationTime = null;
                    Date date = item.getDate("Time");
                    if (date != null) {
                        expirationTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                    }
                    
                    if (expirationTime != null && !expirationTime.isAfter(LocalDateTime.now()) && item.getBoolean("Status")) {
                        bid = new Bid(item.getDouble("Max"), item.getString("Bidder"), item.getString("Model"), item.getString("Brand"), item.getString("Type"), true);
                        Document update = new Document("$set", new Document("Status", false));
                        collection.updateOne(item, update);
                        for (ObjectOutputStream clientStream : clientOutputStreams) {
                                clientStream.reset();
                                clientStream.writeObject(bid);
                                clientStream.flush();
                        }
                    
                    }
                    
                        
                
                    
                
                }
            }catch(Exception e5){
                e5.printStackTrace();
            }
        }
        }
    });
    thread.start();
        while (true) {
            try{
            Socket s = ss.accept();
            connectedClients.add(s);
            OutputStream os = s.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            InputStream is =s.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            clientOutputStreams.add(oos);
            
            Thread t = new Thread(new Runnable(){
                @Override
                public void run() {
                    while(true){
                        Object receivedObject = null;
                        try  {
                
                            receivedObject = ois.readObject();
                            if (receivedObject instanceof Bid) {
                                Bid bid = (Bid) receivedObject;
                                MongoDatabase database = MongoClients.create(URI).getDatabase(DB);
                                MongoCollection<Document> items = database.getCollection(COLLECTION);
                                Document query = new Document("Model", bid.getModel());
                                Document result = items.find(query).first();
                                if(bid.getAmount() >= result.getDouble("Price")){
                                    Document sold = new Document("$set", new Document("Status", false).append("Max", bid.getAmount()).append("Bidder", bid.getBidder()));
                                    items.updateOne(result, sold);
                                }
                                else{
                                    Document update = new Document("$set", new Document("Max", bid.getAmount()).append("Bidder", bid.getBidder()));
                                    items.updateOne(result, update);
                                }
                                for (ObjectOutputStream clientStream : clientOutputStreams) {
                                    clientStream.reset();
                                    clientStream.writeObject(bid);
                                    clientStream.flush();
                                }
                            } else if (receivedObject instanceof User) {
                                User user = (User) receivedObject;
                                String URI = "mongodb+srv://akim678910:2812368663a@cluster0.iku4q9b.mongodb.net/?retryWrites=true&w=majority";
                                String DB = "auction";
                                String COLLECTION2 = "users";
                                MongoDatabase database = MongoClients.create(URI).getDatabase(DB);
                                MongoCollection<Document> users = database.getCollection(COLLECTION2);
                                Document doc = new Document("Username", user.getUser())
                                .append("Password", user.getPass());   
                                users.insertOne(doc);
                                // Handle the user object
                            }  
                        }
                        catch (Exception e) {
                            System.out.println("A client has disconnected.");
                            int index = connectedClients.indexOf(e);
                            if (index != -1) {
                                connectedClients.remove(index);
                                clientOutputStreams.remove(index);
                            }
                            return;
                        }
                            
                        }
                    }
            });
            t.start();
        } catch (SocketException e){
        }
        }
        }

                
   // }

    public static void ping() {
        try {
        Bson command = new BsonDocument("ping", new BsonInt64(1));
        Document commandResult = database.runCommand(command);
        System.out.println("Connected successfully to server.");
        } catch (MongoException me) {
        System.err.println("An error occurred while attempting to run a command: " + me);
        }
    }

    public void loadItemsFromFile(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            // Create MongoClient instance
            MongoCollection<Document> collections = database.getCollection(COLLECTION);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] info = line.split("  ");               
                Document doc = new Document("Brand", info[0])
                .append("Model", info[1])
                .append("Type", info[2])
                .append("Max", 0.0).append("Price", Double.parseDouble(info[3])).append("Status", true)
                .append("Time", Date.from(LocalDateTime.now().plusSeconds(Integer.parseInt(info[4])).atZone(ZoneId.systemDefault()).toInstant())).append("Bidder", "")
                .append("Min", Double.parseDouble(info[5])).append("Info", info[6]);
                collections.insertOne(doc);
                
            }

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        }
    }





    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

                AuctionServerInterface server = new server();
                while (true) {
                    // Read method name and arguments from input stream
                    String methodName = (String) in.readObject();
                    Object[] args = (Object[]) in.readObject();

                    // Invoke method on server
                    Method method = server.getClass().getMethod(methodName, getArgumentTypes(args));
                    Object result = method.invoke(server, args);

                    // Send result back to client
                    out.writeObject(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Class<?>[] getArgumentTypes(Object[] args) {
            Class<?>[] types = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass();
            }
            return types;
        }
    }
    }
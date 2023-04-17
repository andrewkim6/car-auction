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
import java.util.Arrays;
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
    public static void main(String[] args) throws IOException {
        mongo = MongoClients.create(URI);
        database = mongo.getDatabase(DB);
        collection = database.getCollection(COLLECTION);
        ping();

        AuctionServerInterface server = new server();
       // server.loadItemsFromFile("cars.txt");
//
        ServerSocket serverSocket = new ServerSocket(8003);
        System.out.println("Server started on port 8001");

        
        Socket clientSocket = serverSocket.accept();
        System.out.println("Accepted connection from " + clientSocket.getInetAddress());
        mongo.close();
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Client connected: " + socket.getInetAddress().getHostName());

                // Input and output streams for the socket
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                String line;
                while ((line = input.readLine()) != null) {
                    System.out.println("Received from client: " + line);

                    // Send response back to client
                    output.println("Server received: " + line);
                }

                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            }
    
        }        
    }

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
                String[] info = line.split(" ");
                Document doc = new Document("Brand", info[0])
                .append("Model", info[1])
                .append("Type", info[2]);
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

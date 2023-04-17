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
<<<<<<< HEAD

import client.client;
public class mongoDB extends client{
=======
public class mongoDB {
>>>>>>> 0a3e8cf (server files)
    private static MongoClient mongo;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private final static String URI = "mongodb+srv://akim678910:2812368663a@cluster0.iku4q9b.mongodb.net/?retryWrites=true&w=majority";
    private static final String DB = "auction";
    private static final String COLLECTION = "items";

    public static void main(String[] args) {
        mongo = MongoClients.create(URI);
        database = mongo.getDatabase(DB);
        collection = database.getCollection(COLLECTION);
        ping();
        mongo.close();
        }
        public static void create() {
            Document doc = new Document();
            doc.put("name", "Nike Air Min 270");
            doc.put("description", "Super bad sneaker");
            doc.put("winner", "");
            doc.put("finalPrice", 0.0);
            collection.insertOne(doc);
            }
            public static void findAndRead() {
            MongoCursor<Document> cursor = collection.find(
            Filters.eq("name", "Nike Air Max 270")
            ).cursor();
            while(cursor.hasNext()) {
            System.out.println(cursor.next().toJson());
            }
            }
            public static void readAll() {
            MongoCursor<Document> cursor = collection.find(
            Filters.empty()
            ).cursor();
            while(cursor.hasNext()) {
            System.out.println(cursor.next().toJson());
            }
            }
            public static void update() {
            collection.updateOne(
            Filters.eq("name", "Nike Air Max 270"),
            Updates.combine(
            Updates.set("winner", "Rifat"),
            Updates.set("finalPrice", 100.0)
            )
            );
            }
            public static void delete() {
            collection.deleteOne(
            Filters.eq("name", "Nike Air Max 270")
            );
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
    }
        


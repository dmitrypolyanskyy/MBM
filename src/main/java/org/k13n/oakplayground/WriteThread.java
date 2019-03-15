package org.k13n.oakplayground;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.apache.jackrabbit.oak.InitialContent;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.api.ContentSession;
import org.apache.jackrabbit.oak.api.Root;
import org.apache.jackrabbit.oak.api.Tree;
import org.apache.jackrabbit.oak.plugins.commit.ConflictValidatorProvider;
import org.apache.jackrabbit.oak.plugins.commit.JcrConflictHandler;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexEditorProvider;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexProvider;
import org.apache.jackrabbit.oak.spi.security.OpenSecurityProvider;

import javax.jcr.Credentials;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.SimpleCredentials;
import javax.security.auth.login.LoginException;
import java.util.List;

public class WriteThread extends Thread {

    private static final String DB_NAME = "test";
    private static final Credentials CREDENTIALS = new SimpleCredentials("admin", "admin".toCharArray());

    private MongoClient client;
    private DocumentNodeStore nodeStore;
    private ContentRepository repository;

    public void run() {
        connect();
        for (int i=0;i<100;i++){
            insertTransaction();
            deleteTransaction();
        }
        //searchTransaction();
        disconnect();
    }
    //PP in
    // ACID properties review for understanding Transaction (ISOLATION for concurrency control)
    public void createIndex() {
        dropDatabase(DB_NAME);
        connect();
        try (ContentSession session = newSession()) {
            Root root = session.getLatestRoot();
            Tree rootTree = root.getTree("/");
            Tree index = rootTree.addChild("index");
            root.commit(); // call to update
        } catch (Exception e) {
            e.printStackTrace();
        }
        disconnect();
    }

    public void insertTransaction() {
        try (ContentSession session = newSession()) {
            Root root = session.getLatestRoot();
            Tree index  = root.getTree("/index");
            PPIndex ppIndex = new PPIndex(index, "pub");
            ppIndex.insert("now","/content/dow/tech/msft");
            ppIndex.insert("now","/content/dow/tech/msft/qqq");
            ppIndex.insert("now","/content/dow/tech/msft/qqq/qowo");
            ppIndex.insert("now","/cont/dow/tech/msft");
            root.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteTransaction() {
        try (ContentSession session = newSession()) {
            Root root = session.getLatestRoot();
            Tree index  = root.getTree("/index");
            PPIndex ppIndex = new PPIndex(index, "pub");
            ppIndex.delete("now","/content/dow/tech/msft");
            ppIndex.delete("now","/content/dow/tech/msft/qqq");
            ppIndex.delete("now","/content/dow/tech/msft/qqq/qowo");
            ppIndex.delete("now","/cont/dow/tech/msft");
            root.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchTransaction() {
        try (ContentSession session = newSession()) {
            Root root = session.getLatestRoot();
            Tree index  = root.getTree("/index");
            PPIndex ppIndex = new PPIndex(index, "pub");
            List<String> results = ppIndex.search("now","/content/dow");
            for (String result:results){
                String[] re = result.split("/content/dow");
                //System.out.println(result);
                System.out.println(result.split(re[0])[1]);
            }
            root.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void connect() {
        client = getMongoDb();
        DB db = client.getDB(DB_NAME);

        DocumentMK.Builder builder = new DocumentMK.Builder().setMongoDB(db);
        nodeStore = builder.getNodeStore();

        Oak oak = new Oak(nodeStore)
                .with(new InitialContent())
                .with(new OpenSecurityProvider())
                .with(JcrConflictHandler.createJcrConflictHandler())
                .with(new ConflictValidatorProvider())
                .with(new PropertyIndexEditorProvider())
                .with(new PropertyIndexProvider());

        repository = oak.createContentRepository();
    }


    public void disconnect() {
        nodeStore.dispose();
        client.close();
        repository = null;
        nodeStore = null;
        client = null;
    }


    public synchronized ContentSession newSession() throws NoSuchWorkspaceException, LoginException {
        if (repository == null) {
            throw new IllegalStateException("not connected");
        }
        return repository.login(CREDENTIALS, "default");
    }


    public static void dropDatabase(String dbName) {
        MongoClient client = getMongoDb();
        client.getDB(dbName).dropDatabase();
        client.close();
    }

    private static MongoClient getMongoDb() {
        ServerAddress address = new ServerAddress("127.0.0.1", 27017);
        return new MongoClient(address);
    }

}

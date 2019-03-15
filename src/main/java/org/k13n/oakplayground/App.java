package org.k13n.oakplayground;

import com.mongodb.*;
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

public class App {


    public void execute () throws InterruptedException {
        WriteThread makeIndex = new WriteThread();
        makeIndex.createIndex();
        WriteThread thread1 = new WriteThread();
        WriteThread thread2 = new WriteThread();
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
    }

    /**
     *
      *
     */

    public static void main(String[] args) throws InterruptedException {
        new App().execute();
    }


}

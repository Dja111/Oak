package Oak.repositories;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Repository;
import java.net.UnknownHostException;

public class OakRepositoryBuilder {

    public static Logger logger = LoggerFactory.getLogger(OakRepositoryBuilder.class);

    public static Repository getRepo(String host, final int port) throws UnknownHostException {
        String uri = "mongodb://" + host + ":" + port;
        //logger.info(uri);
        System.setProperty("oak.documentMK.disableLeaseCheck", "true");
        DocumentNodeStore ns = new DocumentMK.Builder().setMongoDB(uri, "oak_demo", 16).getNodeStore();
        Repository repo = new Jcr(new Oak(ns)).createRepository();
        logger.info("oak.documentMK.disableLeaseCheck=" + System.getProperty("oak.documentMK.disableLeaseCheck"));
        return repo;
    }
}

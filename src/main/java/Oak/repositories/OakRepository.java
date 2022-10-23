package Oak.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import javax.jcr.*;
import java.net.UnknownHostException;

import static javax.jcr.nodetype.NodeType.NT_FOLDER;

@Service
@Scope("singleton")
public class OakRepository {

    private Logger logger = LoggerFactory.getLogger(OakRepository.class);
    private Repository repository;

    public OakRepository() throws UnknownHostException {
        repository = OakRepositoryBuilder.getRepo("localhost", 27017);
    }

    //CRUD Operation on folder

    public Node createNode(String nodeName) throws RepositoryException {
        Session session = getSession(repository);
        Node rootNode = session.getRootNode();
        if(rootNode.hasNode(nodeName)){
            logger.warn("This folder already exists!");
            return rootNode.getNode(nodeName);
        } else {
            String[] nodeNames = (null != nodeName) ? nodeName.split("/") : null;
            rootNode = createNodes(session, nodeNames);
            session.save();
            sessionLogout(session);
            return rootNode;
        }
    }

    private Node createNodes(Session session, String [] nodeNames) throws RepositoryException {
        Node parentNode = session.getRootNode();
        for (String childNode : nodeNames) {
            if (StringUtils.isNotBlank(childNode)) {
                addChild(parentNode, childNode);
                parentNode = parentNode.getNode(childNode);
                parentNode.setProperty("jcr:nodeType", NT_FOLDER); // set the node type
            }
        }
        return parentNode;
    }

    private boolean addChild(Node parentNode, String childNode) throws RepositoryException {
        boolean nodeAdded = false;
        if (!parentNode.isNode()) {
            throw new RepositoryException("The parentNode does not exist..");
        }
        if (!parentNode.hasNode(childNode)) {
            parentNode.addNode(childNode);
            nodeAdded = true;
        }
        return nodeAdded;
    }

    public Node getNodeByName(String absPath) throws RepositoryException {
        Session session = getSession(repository);
        Node rootNode = session.getRootNode();
        if(rootNode.hasNode(absPath)) {
            return rootNode.getNode(absPath);
        }else {
            throw new RepositoryException("there is not a node with the Name  " + absPath);
        }
    }

    public boolean updateNodeByAbsPathAndNodeName(String absPath, String newNodeName)
            throws RepositoryException {
        Session session = getSession(repository);
        Node root = session.getRootNode();
        if(root.hasNode(absPath)){
            Node node = root.getNode(absPath);
            //Check who is the parent
            if(node.getParent().getPath().equals("/")) {
                session.move(node.getPath(), node.getParent().getPath() + newNodeName);
            } else {
                session.move(node.getPath(), node.getParent().getPath() + "/" + newNodeName);
            }
            session.save();
            return true;
        } else {
            System.out.println("this node doesn't exist");
            return false;
        }
    }

    public boolean deleteNode(String nodeName) throws RepositoryException {
        Session session = getSession(repository);
        Node rootNode = session.getRootNode();
        if(!rootNode.hasNode(nodeName)) {
            logger.info("there is not Node with this name " + nodeName);
            return false;
        } else {
            rootNode.getNode(nodeName).remove();
            session.save();
            sessionLogout(session);
            return true;
        }
    }

    //CRUD Operation on document

    // GetSession and sessionLogout
    private Session getSession(Repository repository) throws RepositoryException {
        Session session = repository.login(new SimpleCredentials("admin","admin".toCharArray()));
        return session;
    }

    private void  sessionLogout(Session session){
        session.logout();
    }
}

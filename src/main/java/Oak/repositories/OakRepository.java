package Oak.repositories;

import Oak.entities.FileResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.*;
import javax.jcr.version.VersionManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import static javax.jcr.nodetype.NodeType.NT_FILE;
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

    public FileResponse getNodeByName(String absPath) throws RepositoryException {
        Session session = getSession(repository);
        Node rootNode = session.getRootNode();
        if(rootNode.hasNode(absPath)) {
            Node node = rootNode.getNode(absPath);
            FileResponse fileResponse = new FileResponse();
            fileResponse.setNodeId(node.getIdentifier());
            fileResponse.setNodeName(node.getName());
            fileResponse.setNodePath(node.getPath());
            sessionLogout(session);
            return fileResponse;
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

    public void createDocument(String basePath, MultipartFile file) throws RepositoryException, IOException {
        Session session = getSession(repository);
        Node rootNode = session.getRootNode();
        if(!rootNode.hasNode(basePath)) {
            logger.info("there is not a node with this name " + basePath);
            return;
        }
        else {
            Node basePathNode = rootNode.getNode(basePath);
            //create node for the File
            Node fileNode = basePathNode.addNode(file.getOriginalFilename(), NT_FILE);
            Node content = fileNode.addNode("jcr:content", "nt:resource");
            // FileInputStream to Binary
            ValueFactory vf = session.getValueFactory();
            Binary binary = vf.createBinary(file.getInputStream());
            // insert Binary into the content Node
            content.setProperty("jcr:data", binary);
            session.save();
            binary.dispose();
            sessionLogout(session);
            logger.info("File Saved...");
        }
    }

    public byte[] getFileNodeContent(String basePath, String fileName)
            throws RepositoryException, IOException {

        Session session = getSession(repository);
        Node rootNode = session.getRootNode();
        Node basePathNode = rootNode.getNode(basePath);

        //fileNode
        Node fileNode = basePathNode.getNode(fileName);
        //fileContent
        Node fileContent = fileNode.getNode("jcr:content");
        //binary
        Binary binary = fileContent.getProperty("jcr:data").getBinary();
        //transform binary
        InputStream stream = binary.getStream();
        //transform InputStream to byte
        byte[] bytes = IOUtils.toByteArray(stream);
        binary.dispose();
        stream.close();
        //logout of the seesion
        sessionLogout(session);
        return bytes;
    }

    public String updateFileNodeContent(String absPath, MultipartFile file, String userName)
            throws RepositoryException, IOException {

        Session session = getSession(repository);

        if (session.nodeExists(absPath) && session.getNode(absPath).hasNode(file.getOriginalFilename())) {

            Node node = session.getNode(absPath);
            Node fileHolder = node.getNode(file.getOriginalFilename());

//      fileHolder.setProperty("jcr:createdBy", userName);
//      fileHolder.setProperty("size", file.getSize());

//      Node fileNode = fileHolder.getNode(file.getOriginalFilename());

            Node content = fileHolder.getNode("jcr:content");

//      FileInputStream is = new FileInputStream(file);
            Binary binary = session.getValueFactory().createBinary(file.getInputStream());

            content.setProperty("jcr:data", binary);
            session.save();
            // FIXME close resources with try-with-resource
            //vm.checkin(fileHolder.getPath());
            //is.close();
            return "this data was edited";
        } else {
            // FIXME created custom exceptions
            throw new RepositoryException("The path:" + absPath + "does not exist...");
        }
    }

    public boolean deleteFileNodeContent(String basePath, String fileName) throws RepositoryException {
        Session session = getSession(repository);
        if(session.nodeExists(basePath) && session.getNode(basePath).hasNode(fileName)) {
            session.getNode(basePath).getNode(fileName).remove();
            session.save();
            sessionLogout(session);
            return true;
        } else {
            logger.warn("Nothing was deleted.........");
            return false;
        }
    }

    // GetSession and sessionLogout
    private Session getSession(Repository repository) throws RepositoryException {
        Session session = repository.login(new SimpleCredentials("admin","admin".toCharArray()));
        return session;
    }

    private void  sessionLogout(Session session){
        session.logout();
    }
}

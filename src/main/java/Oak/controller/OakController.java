package Oak.controller;

import Oak.entities.FileResponse;
import Oak.repositories.OakRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping(path = OakController.ENTRY_POINT)
public class OakController {
    protected static final String ENTRY_POINT = "/oaks";

    private OakRepository oakRepository;

    public OakController(OakRepository oakRepository) {
        this.oakRepository = oakRepository;
    }

    //CRUD for Node/Folder
    @PostMapping(path = "/folders", produces = "application/json")
    ResponseEntity createNode (String absPath) throws RepositoryException {
        oakRepository.createNode(absPath);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(path = "/folders")
    @ResponseBody
    FileResponse getNode (String absPath) throws RepositoryException {
        FileResponse fileResponse = oakRepository.getNodeByName(absPath);
        return fileResponse;
    }

    @DeleteMapping(path = "/folders")
    @ResponseBody
    boolean deleteNode (String absPath) throws RepositoryException {
        return oakRepository.deleteNode(absPath);
    }

    @PutMapping(path = "/nodes")
    @ResponseBody
    boolean updateNodeByAbsPathUndName(String absPath, String newNodeName)
            throws RepositoryException {
        return oakRepository.updateNodeByAbsPathAndNodeName(absPath, newNodeName);
    }

    //CRUD for document/fileNode
    @PostMapping(path = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity createFileNode (@RequestParam String basePath, @RequestPart("file") MultipartFile file, @RequestParam String username)
            throws RepositoryException, IOException {
        oakRepository.createDocument(basePath, file);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(path = "/documents/Content")
    ResponseEntity<String> getFileNodeContent (String basePath, String fileName)
            throws RepositoryException, IOException {
        return new ResponseEntity<>(new String(oakRepository.getFileNodeContent(basePath, fileName),
                StandardCharsets.UTF_8), HttpStatus.OK);
    }

    @PutMapping(path = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity editFileNode(@RequestParam String basePath, @RequestPart("file") MultipartFile file, @RequestParam String username)
            throws RepositoryException, IOException {
        oakRepository.updateFileNodeContent(basePath, file, username);
        return new ResponseEntity(HttpStatus.OK);
    }


    @DeleteMapping(path = "/documents")
    ResponseEntity deleteFileNode (String basePath, String fileName) throws RepositoryException {
        oakRepository.deleteFileNodeContent(basePath, fileName);
        return new ResponseEntity(HttpStatus.OK);
    }
}

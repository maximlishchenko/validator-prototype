package uk.max.validator;

import org.apache.coyote.Response;
import org.apache.jena.ext.xerces.util.URI;
import org.apache.jena.irix.IRIException;
import org.apache.jena.riot.RiotNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@RestController
public class Controller {

    @Autowired
    Service service;

    @PostMapping("/validate-cardinality")
    public ResponseEntity<String> validateCardinality(@RequestBody String provenanceTraceName) {
        try {
            String result = service.validateCardinality(provenanceTraceName);
            return ResponseEntity.ok(result);
        } catch (RiotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        } catch (IRIException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        }
    }

    @PostMapping("/validate-type")
    public ResponseEntity<String> validateType(@RequestBody String provenanceTraceName) {
        try {
            String result = service.validateType(provenanceTraceName);
            return ResponseEntity.ok(result);
        } catch (RiotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        } catch (IRIException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        }
    }

    @PostMapping("/validate-sparql")
    public ResponseEntity<String> validateSparql(@RequestBody String provenanceTraceName) {
        try {
            String result = service.validateSparql(provenanceTraceName);
            return ResponseEntity.ok(result);
        } catch (RiotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        } catch (IRIException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        }
    }

    @GetMapping("/get-file-names")
    public ResponseEntity<List<String>> getFileNames() {
        List<String> fileNames = service.getFileNames();
        return ResponseEntity.ok(fileNames);
    }
}

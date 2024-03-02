package uk.max.validator;

import org.apache.coyote.Response;
import org.apache.jena.ext.xerces.util.URI;
import org.apache.jena.riot.RiotNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }

    @PostMapping("/validate-type")
    public ResponseEntity<String> validateType(@RequestBody String provenanceTraceName) {
        try {
            String result = service.validateType(provenanceTraceName);
            return ResponseEntity.ok(result);
        } catch (RiotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }

    @PostMapping("/validate-sparql")
    public ResponseEntity<String> validateSparql(@RequestBody String provenanceTraceName) {
        try {
            String result = service.validateSparql(provenanceTraceName);
            return ResponseEntity.ok(result);
        } catch (RiotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }
}

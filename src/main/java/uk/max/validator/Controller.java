package uk.max.validator;

import com.google.gson.JsonArray;
import org.apache.jena.graph.Graph;
import org.apache.jena.irix.IRIException;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.shacl.ValidationReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.max.validator.Model.ValidationResult;
import uk.max.validator.utils.Utils;

import java.io.*;
import java.util.List;


@org.springframework.stereotype.Controller
@RestController
public class Controller {

    // automatic dependency injection
    @Autowired
    Service service;

    // endpoint for applying cardinality constraints
    @PostMapping("/validate-cardinality")
    public ResponseEntity<String> validateCardinality(@RequestBody String provenanceTraceName) {
        try {
            // call service method
            String result = service.validateCardinality(provenanceTraceName);
            return ResponseEntity.ok(result); // send OK status code
        // handle exceptions ...
        } catch (RiotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        } catch (IRIException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        }
    }

    // endpoint for applying type constraints
    @PostMapping("/validate-type")
    public ResponseEntity<String> validateType(@RequestBody String provenanceTraceName) {
        try {
            // call service method
            String result = service.validateType(provenanceTraceName);
            return ResponseEntity.ok(result);
        // handle exceptions...
        } catch (RiotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        } catch (IRIException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        }
    }

    // endpoint for applying sparql constraints
    @PostMapping("/validate-sparql")
    public ResponseEntity<String> validateSparql(@RequestBody String provenanceTraceName) {
        try {
            // call service method
            String result = service.validateSparql(provenanceTraceName);
            return ResponseEntity.ok(result);
        // handle exceptions
        } catch (RiotNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        } catch (IRIException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"File not found\"}");
        }
    }

    // endpoint for fetching all present provenance trace names
    @GetMapping("/get-file-names")
    public ResponseEntity<List<String>> getFileNames() {
        List<String> fileNames = service.getFileNames();
        return ResponseEntity.ok(fileNames);
    }

    // endpoint for uploading and validating provenance trace
    @PostMapping("upload-and-validate")
    public ResponseEntity<String> uploadAndValidate(@RequestParam("file") MultipartFile file) {
        // check if file is empty
        if (file.isEmpty()) {
            // send not found status code and error message in response
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Uploaded file is empty\"}");
        }
        // get the file name of uploaded file
        String filename = file.getOriginalFilename();
        // anything other than json-ld (json syntax) is not parseable in our case
        if (!filename.toLowerCase().endsWith(".json")) {
            // send bad request status code and error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Only .json files are accepted\"}");
        }
        try {
            // load file content in bytes
            byte[] bytes = file.getBytes();
            String fileContent = new String(bytes); // convert to string
            Graph dataGraph = Utils.parseJSONLD(fileContent); // create knowledge graph given json-ld content
            // apply the rules
            Graph rules = RDFDataMgr.loadGraph("./rules/all-constraints.ttl", Lang.TTL);
            // generate report
            ValidationReport report = Utils.generateReport(rules, dataGraph);
            // case if no violations present
            if (report.conforms()) {
                String res = "[]";
                return ResponseEntity.ok(res); // send 200 status code
            }
            // call utility functions
            JsonArray validationResults = Utils.generateValidationResults(report);
            List<ValidationResult> responses = Utils.generateValidationResultObjects(validationResults);
            return ResponseEntity.ok(ValidationResult.buildJsonResponse(responses));
        // handle IO exceptions..
        } catch (IOException e) {
            throw new RuntimeException("Error reading uploaded file", e);
        } catch (RiotException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Error parsing the file\"}");
        }
    }
}

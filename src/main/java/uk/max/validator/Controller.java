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

    @PostMapping("upload-and-validate")
    public ResponseEntity<String> uploadAndValidate(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Uploaded file is empty\"}");
        }
        String filename = file.getOriginalFilename();
        if (!filename.toLowerCase().endsWith(".json")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Only .json files are accepted\"}");
        }
        try {
            byte[] bytes = file.getBytes();
            String fileContent = new String(bytes);
            Graph dataGraph = Utils.parseJSONLD(fileContent);
            Graph rules = RDFDataMgr.loadGraph("./rules/all-constraints.ttl", Lang.TTL);
            ValidationReport report = Utils.generateReport(rules, dataGraph);
            if (report.conforms()) {
                String res = "[]";
                return ResponseEntity.ok(res);
            }
            JsonArray validationResults = Utils.generateValidationResults(report);
            List<ValidationResult> responses = Utils.generateValidationResultObjects(validationResults);
            return ResponseEntity.ok(ValidationResult.buildJsonResponse(responses));
        } catch (IOException e) {
            throw new RuntimeException("Error reading uploaded file", e);
        } catch (RiotException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Error parsing the file\"}");
        }
    }
}

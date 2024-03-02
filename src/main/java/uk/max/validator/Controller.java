package uk.max.validator;

import org.apache.jena.ext.xerces.util.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    Service service;

    @PostMapping("/validate-cardinality")
    public String validateCardinality(@RequestBody String provenanceTraceName) {
        return service.validateCardinality(provenanceTraceName);
    }

    @PostMapping("/validate-type")
    public String validateType(@RequestBody String provenanceTraceName) {
        return service.validateType(provenanceTraceName);
    }

    @PostMapping("/validate-sparql")
    public String validateSparql(@RequestBody String provenanceTraceName) {
        return service.validateSparql(provenanceTraceName);
    }
}

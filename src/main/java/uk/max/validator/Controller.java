package uk.max.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    Service service;

    @GetMapping("/validate-cardinality")
    public void validateCardinality() {
        service.validateCardinality();
    }

    @GetMapping("/validate-type")
    public void validateType() {
        service.validateType();
    }

    @GetMapping("/validate-sparql")
    public void validateSparql() {
        service.validateSparql();
    }
}

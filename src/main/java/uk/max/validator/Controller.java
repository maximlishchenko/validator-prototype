package uk.max.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    Service service;

    @GetMapping("/validate-sosa")
    public void validateSosa() {
        service.validateSosa();
    }

    @GetMapping("/validate-peco")
    public void validatePeco() {
        service.validatePeco();
    }

    @GetMapping("/validate-ecfo")
    public void validateEcfo() {
        service.validateEcfo();
    }
}

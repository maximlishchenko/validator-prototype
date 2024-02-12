package uk.max.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    TestService testService;

    @GetMapping("/validate")
    public void test() {
        testService.validate();
    }
}

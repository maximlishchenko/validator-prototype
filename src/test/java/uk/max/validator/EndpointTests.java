package uk.max.validator;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest
class EndpointTests {

    @InjectMocks
    private Controller controller;

    @Test
    public void testControllerValidateCardinalitySuccess() {
        // assume status OK for a correct existing trace name
        ResponseEntity<String> responseEntity = controller.validateCardinality("smli_trace1_valid");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testControllerValidateCardinalityFileNotFound() {
        // provide a non-existent trace to the controller
        ResponseEntity<String> responseEntity = controller.validateCardinality("arbitrary_trace123");
        // expect the not found status code and appropriate error message
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("{\"message\": \"File not found\"}", responseEntity.getBody());
    }

    @Test
    public void testControllerValidateTypeSuccess() {
        // assume status OK for a correct existing trace name
        ResponseEntity<String> responseEntity = controller.validateType("smli_trace1_valid");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testControllerValidateTypeFileNotFound() {
        // provide a non-existent trace to the controller
        ResponseEntity<String> responseEntity = controller.validateType("arbitrary_trace123");
        // expect the not found status code
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("{\"message\": \"File not found\"}", responseEntity.getBody());
    }

    @Test
    public void testControllerValidateSparqlSuccess() {
        // assume status OK for a correct existing trace name
        ResponseEntity<String> responseEntity = controller.validateSparql("smli_trace1_valid");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testControllerValidateSparqlFileNotFound() {
        // provide a non-existent trace to the controller
        ResponseEntity<String> responseEntity = controller.validateSparql("arbitrary_trace123");
        // expect the not found status code
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("{\"message\": \"File not found\"}", responseEntity.getBody());
    }

    @Test
    public void testUploadAndValidateEmptyFile() {
        // test that empty file case handled by controller
        // create mock empty file
        MockMultipartFile file = new MockMultipartFile("file", "test.json", "application/json", new byte[0]);

        // call controller method with empty file
        ResponseEntity<String> responseEntity = controller.uploadAndValidate(file);

        // expect not found status code and appropriate error message
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("{\"message\": \"Uploaded file is empty\"}", responseEntity.getBody());
    }

    @Test
    public void testUploadAndValidateIncorrectFileFormat() {
        // test that file with incorrect format handled by controller
        // create non-empty mock file with incorrect extension
        MockMultipartFile file = new MockMultipartFile("file", "test.xls", "application/xls", new byte[10]);

        // call controller method
        ResponseEntity<String> responseEntity = controller.uploadAndValidate(file);

        // expect bad request status code and appropriate error message
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("{\"message\": \"Only .json files are accepted\"}", responseEntity.getBody());
    }

    @Test
    public void testUploadAndValidateIncorrectJsonSyntax() {
        // test that file with incorrect json syntax is handled
        // define json file contents
        String jsonContent = "{\"key\": 123;!@#$,%^,&\"invalid syntax\":}";
        // create the mock file
        MockMultipartFile file = new MockMultipartFile("file", "test.json", "application/json", jsonContent.getBytes());

        // call controller method
        ResponseEntity<String> responseEntity = controller.uploadAndValidate(file);

        // expect bad request status code and appropriate error message
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("{\"message\": \"Error parsing the file\"}", responseEntity.getBody());
    }
}
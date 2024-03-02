package uk.max.validator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ValidatorApplicationTests {
	@Test
	void testValidateCardinality() {
		Service service = new Service();
		String result = service.validateCardinality("pesticides_trace_valid");
		assertEquals("No violations", result);
	}
}

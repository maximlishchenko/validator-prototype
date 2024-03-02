package uk.max.validator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class ValidatorApplicationTests {

	@Test
	public void testValidate_SMLI_Trace1() {
		assertEquals("No violations", Service.validateCardinality("smli_trace1_valid"));
		assertEquals("No violations", Service.validateType("smli_trace1_valid"));
		assertEquals("No violations", Service.validateSparql("smli_trace1_valid"));
	}

	@Test
	public void testValidate_SMLI_Trace2() {
		assertEquals("No violations", Service.validateCardinality("smli_trace2_valid"));
		assertEquals("No violations", Service.validateType("smli_trace2_valid"));
		String res = Service.validateSparql("smli_trace2_valid");
		JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
		assertEquals(1, jsonArray.size());
		JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
		assertEquals("https://w3id.org/ecfkg/i/UK/BEIS/2022/CF_2127", jsonObject.get("focusNode").getAsString());
		assertEquals("ecfo:hasApplicablePeriod", jsonObject.get("resultPath").getAsString());
		assertEquals("sh:Warning", jsonObject.get("resultSeverity").getAsString());
	}
}

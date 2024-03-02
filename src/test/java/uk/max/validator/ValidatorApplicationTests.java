package uk.max.validator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class ValidatorApplicationTests {

    @Test
    public void testValidateSMLITrace1() {
        assertEquals("No violations", Service.validateCardinality("smli_trace1_valid"));
        assertEquals("No violations", Service.validateType("smli_trace1_valid"));
        assertEquals("No violations", Service.validateSparql("smli_trace1_valid"));
    }

    @Test
    public void testValidateSMLITrace2() {
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

    @Test
    public void testValidateWaterTrace() {
        assertEquals("No violations", Service.validateCardinality("water_trace_valid"));
        assertEquals("No violations", Service.validateType("water_trace_valid"));
        assertEquals("No violations", Service.validateSparql("water_trace_valid"));
    }

    @Test
    public void testValidatePesticidesTrace() {
        assertEquals("No violations", Service.validateCardinality("pesticides_trace_valid"));
        assertEquals("No violations", Service.validateType("pesticides_trace_valid"));
        assertEquals("No violations", Service.validateSparql("pesticides_trace_valid"));
    }

    @Test
    public void testValidateCalciumChlorideTrace() {
        assertEquals("No violations", Service.validateCardinality("calcium_chloride_trace_valid"));
        assertEquals("No violations", Service.validateType("calcium_chloride_trace_valid"));
        String res = Service.validateSparql("calcium_chloride_trace_valid");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        assertEquals(1, jsonArray.size());
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        assertEquals("https://cacl2.com/provenance/EmissionGenerationActivity/ega1", jsonObject.get("focusNode").getAsString());
        assertEquals("prov:atLocation", jsonObject.get("resultPath").getAsString());
        assertEquals("sh:Warning", jsonObject.get("resultSeverity").getAsString());
    }

    @Test
    // catch cardinality constraints related to ECFO entities
    public void testValidateECFOCardinalitySMLITrace3() {
        String res = Service.validateCardinality("smli_trace3_invalid");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        assertEquals(4, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            // check that each validation result focuses the CF
            assertEquals("https://w3id.org/ecfkg/i/mlco2/aws/cn-north-1/cf", focusNode);

            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
        }

        // check that each resultPath occurs once
        assertEquals(1, pathCount.get("ecfo:hasSourceUnit"));
        assertEquals(1, pathCount.get("ecfo:hasTargetUnit"));
        assertEquals(1, pathCount.get("ecfo:hasApplicableLocation"));
        assertEquals(1, pathCount.get("ecfo:hasEmissionTarget"));
    }
}

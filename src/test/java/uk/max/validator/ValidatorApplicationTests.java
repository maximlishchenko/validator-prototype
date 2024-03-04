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
        String res = Service.validateCardinality("smli_trace3_invalid_ecfo_cardinality");
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

    @Test
    // catch cardinality constraints related to ECFO entities
    public void testValidatePECOCardinalitySMLITrace3() {
        String res = Service.validateCardinality("smli_trace3_invalid_peco_cardinality");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        assertEquals(3, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
        }

        // check count of resultPath occurrences
        assertEquals(2, pathCount.get("prov:used"));
        assertEquals(1, pathCount.get("peco:hasEmissionScore"));
    }

    @Test
    // catch cardinality constraints related to ECFO entities
    public void testValidateSOSACardinalitySMLITrace3() {
        String res = Service.validateCardinality("smli_trace3_invalid_sosa_cardinality");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        assertEquals(4, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            // check that each validation result focuses the observation
            assertEquals("https://github.com/mlco2/impact/provenance/i/Observation/8ca5f2a3-4179-4507-adcc-a40aee2cf5b9", focusNode);

            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
        }

        // check that each resultPath occurs once
        assertEquals(1, pathCount.get("sosa:hasFeatureOfInterest"));
        assertEquals(1, pathCount.get("sosa:hasResult"));
        assertEquals(1, pathCount.get("sosa:madeBySensor"));
        assertEquals(1, pathCount.get("peco:inEmissionActivityContext"));
    }

    @Test
    // catch type constraints related to ECFO entities
    public void testValidateECFOTypeSMLITrace4() {
        String res = Service.validateType("smli_trace4_invalid_ecfo_type");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        assertEquals(4, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            // check that each validation result focuses the conversion factor
            assertEquals("https://w3id.org/ecfkg/i/mlco2/gcp/australia-southeast1/cf", focusNode);

            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
        }

        // check that each resultPath occurs once
        assertEquals(1, pathCount.get("ecfo:hasSourceUnit"));
        assertEquals(1, pathCount.get("ecfo:hasTargetUnit"));
        assertEquals(1, pathCount.get("peco:scope"));
        assertEquals(1, pathCount.get("ecfo:hasEmissionTarget"));
    }

    @Test
    // catch type constraints related to PECO and QUDT entities
    public void testValidatePECOQUDTTypeSMLITrace4() {
        String res = Service.validateType("smli_trace4_invalid_peco_qudt_type");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        // 1 error was introduced in EmissionScore
        // In 4 Quantities, 2 errors were introduced in each
        assertEquals(9, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();

            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
        }

        assertEquals(4, pathCount.get("qudt:unit"));
        assertEquals(4, pathCount.get("qudt:hasQuantityKind"));
        assertEquals(1, pathCount.get("peco:hasEmissionScore"));
    }

    @Test
    // catch type constraints related to SOSA entities
    public void testValidateSOSATypeSMLITrace4() {
        String res = Service.validateType("smli_trace4_invalid_sosa_type");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        // 3 errors were introduced related to sosa:Observation
        assertEquals(3, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            // check that each validation result focuses sosa:Observation
            assertEquals("https://github.com/mlco2/impact/provenance/i/Observation/9db05d79-d4c0-41b6-afd2-976c2587623d", focusNode);

            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
        }

        // check that each resultPath occurs once
        assertEquals(1, pathCount.get("sosa:hasResult"));
        assertEquals(1, pathCount.get("sosa:hasFeatureOfInterest"));
        assertEquals(1, pathCount.get("peco:inEmissionActivityContext"));
    }
}

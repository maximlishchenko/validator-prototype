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
        // expect the trace to be valid
        assertEquals("[]", Service.validateCardinality("smli_trace1_valid"));
        assertEquals("[]", Service.validateType("smli_trace1_valid"));
        assertEquals("[]", Service.validateSparql("smli_trace1_valid"));
    }

    @Test
    public void testValidateSMLITrace2() {
        // expect no violations related to type and cardinality constraints
        assertEquals("[]", Service.validateCardinality("smli_trace2_valid"));
        assertEquals("[]", Service.validateType("smli_trace2_valid"));
        String res = Service.validateSparql("smli_trace2_valid");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        // expect the array to contain 1 violation
        assertEquals(1, jsonArray.size());
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        // expect the CF to be out of date since the applicable period is 2022
        assertEquals("https://w3id.org/ecfkg/i/UK/BEIS/2022/CF_2127", jsonObject.get("focusNode").getAsString());
        assertEquals("ecfo:hasApplicablePeriod", jsonObject.get("resultPath").getAsString());
        assertEquals("sh:Warning", jsonObject.get("resultSeverity").getAsString());
    }

    @Test
    public void testValidateWaterTrace() {
        // expect the trace to be valid
        assertEquals("[]", Service.validateCardinality("water_trace_valid"));
        assertEquals("[]", Service.validateType("water_trace_valid"));
        assertEquals("[]", Service.validateSparql("water_trace_valid"));
    }

    @Test
    public void testValidatePesticidesTrace() {
        // expect the trace to be valid
        assertEquals("[]", Service.validateCardinality("pesticides_trace_valid"));
        assertEquals("[]", Service.validateType("pesticides_trace_valid"));
        assertEquals("[]", Service.validateSparql("pesticides_trace_valid"));
    }

    @Test
    public void testValidateCalciumChlorideTrace() {
        // expect the type and cardinality to be correct
        assertEquals("[]", Service.validateCardinality("calcium_chloride_trace_valid"));
        assertEquals("[]", Service.validateType("calcium_chloride_trace_valid"));
        String res = Service.validateSparql("calcium_chloride_trace_valid");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        assertEquals(1, jsonArray.size());
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        // expect an error because CF is applicable to Canada, but generation activity happened in the UK
        assertEquals("https://cacl2.com/provenance/EmissionGenerationActivity/ega1", jsonObject.get("focusNode").getAsString());
        assertEquals("prov:atLocation", jsonObject.get("resultPath").getAsString());
        assertEquals("sh:Warning", jsonObject.get("resultSeverity").getAsString());
    }

    @Test
    // catch cardinality constraints related to ECFO entities
    public void testValidateECFOCardinalitySMLITrace3() {
        // introduce 4 errors in CF
        // 2 source units, 2 target units, 2 emission targets, 2 values
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
        assertEquals(1, pathCount.get("rdf:value"));
        assertEquals(1, pathCount.get("ecfo:hasEmissionTarget"));
    }

    @Test
    // catch cardinality constraints related to PECO entities
    public void testValidatePECOCardinalitySMLITrace3() {
        // introduce an error so that an emission generation activity has 2 emission scores, 1 of which is non-existent
        // introduce an error in 2 emission generation activities:
        // 1) one activity uses another activity + an applicable period entity
        // 2) one activity uses a CF + an applicable period entity
        String res = Service.validateCardinality("smli_trace3_invalid_peco_cardinality");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        // expect the array to contain 3 violations
        assertEquals(3, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();
        Map<String, Integer> focusNodeCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
            // keep track of focus nodes
            String focusNode = jsonObject.get("focusNode").getAsString();
            focusNodeCount.put(focusNode, focusNodeCount.getOrDefault(focusNode, 0) + 1);
        }

        // check count of resultPath occurrences
        assertEquals(2, pathCount.get("prov:used"));
        assertEquals(1, pathCount.get("peco:hasEmissionScore"));
        // assert the expected focus nodes the errors were introduced in
        assertEquals(1, focusNodeCount.get("https://github.com/mlco2/impact/provenance/i/43e88788-92a6-414f-8a7c-5728b5c8282c"));
        assertEquals(1, focusNodeCount.get("https://github.com/mlco2/impact/provenance/i/EmissionGenerationActivity/dfea7cf3-3850-4a2d-bc26-720e191bb2ed"));
        assertEquals(1, focusNodeCount.get("https://github.com/mlco2/impact/provenance/i/905ef1a9-bdf4-41c7-8f75-cd683fb62823"));
    }

    @Test
    // catch cardinality constraints related to SOSA entities
    public void testValidateSOSACardinalitySMLITrace3() {
        // introduce 4 errors in sosa:Observation
        // 1) 2 features of interest
        // 2) made by 2 sensors
        // 3) linked to 2 emission generation activities one of which is non-existent
        // 4) has no results
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

        // check that the expected resultPath occurs exactly once
        assertEquals(1, pathCount.get("sosa:hasFeatureOfInterest"));
        assertEquals(1, pathCount.get("sosa:hasResult"));
        assertEquals(1, pathCount.get("sosa:madeBySensor"));
        assertEquals(1, pathCount.get("peco:inEmissionActivityContext"));
    }

    @Test
    // catch type constraints related to ECFO entities
    public void testValidateECFOTypeSMLITrace4() {
        // introduce 4 errors in the conversion factor
        // 1) invalid source units
        // 2) invalid target units
        // 3) invalid emission target
        // 4) invalid scope
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

        // expect that each resultPath occurs exactly once
        assertEquals(1, pathCount.get("ecfo:hasSourceUnit"));
        assertEquals(1, pathCount.get("ecfo:hasTargetUnit"));
        assertEquals(1, pathCount.get("peco:scope"));
        assertEquals(1, pathCount.get("ecfo:hasEmissionTarget"));
    }

    @Test
    // catch type constraints related to PECO and QUDT entities
    public void testValidatePECOQUDTTypeSMLITrace4() {
        // introduce 9 errors
        // remove the emission score from the @type array of the emission score
        // make the units and quantity kind invalid in all the 4 quantities present in the trace
        String res = Service.validateType("smli_trace4_invalid_peco_qudt_type");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        assertEquals(9, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();

            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
        }

        // expect to see invalid units and quantity kinds 4 times
        assertEquals(4, pathCount.get("qudt:unit"));
        assertEquals(4, pathCount.get("qudt:hasQuantityKind"));
        // expect to see path related to emission score once
        assertEquals(1, pathCount.get("peco:hasEmissionScore"));
    }

    @Test
    // catch type constraints related to SOSA entities
    public void testValidateSOSATypeSMLITrace4() {
        // introduce 3 errors related to sosa:Observation
        // 1) remove the sosa:Result type from the observation result's @type array
        // 2) remove the sosa:featureOfInterest type from the FOI's @type array
        // 3) the observation is linked to a non-existent emission generation activity
        String res = Service.validateType("smli_trace4_invalid_sosa_type");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
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

        // expect that each resultPath occurs once
        assertEquals(1, pathCount.get("sosa:hasResult"));
        assertEquals(1, pathCount.get("sosa:hasFeatureOfInterest"));
        assertEquals(1, pathCount.get("peco:inEmissionActivityContext"));
    }

    @Test
    public void validateInvalidObsResultWaterTrace() {
        // Introduced an error in observation result's units

        // expect to have no cardinality violations
        String cardinalityRes = Service.validateCardinality("water_trace_invalid_obsresult");
        assertEquals("[]", cardinalityRes);

        String sparqlRes = Service.validateSparql("water_trace_invalid_obsresult");
        JsonArray sparqlViolationsArray = new Gson().fromJson(sparqlRes, JsonArray.class);
        assertEquals(1, sparqlViolationsArray.size());
        JsonObject jsonObject = sparqlViolationsArray.get(0).getAsJsonObject();
        // catch errors focusing the conversion factor
        assertEquals("https://water.com/provenance/cf", jsonObject.get("focusNode").getAsString());
        assertEquals("An emission calculation activity that involved a conversion factor used an entity with units different from that CF units", jsonObject.get("resultMessage").getAsString());

        String typeRes = Service.validateType("water_trace_invalid_obsresult");
        JsonArray typeViolationsArray = new Gson().fromJson(typeRes, JsonArray.class);
        assertEquals(1, typeViolationsArray.size());
        jsonObject = typeViolationsArray.get(0).getAsJsonObject();
        // expect to see an error in observation result's units
        assertEquals("https://water.com/provenance/CalculationEntity/ObservationResult/obsresult1", jsonObject.get("focusNode").getAsString());
        assertEquals("Quantity's units are not of type qudt:Unit", jsonObject.get("resultMessage").getAsString());
        assertEquals("qudt:unit", jsonObject.get("resultPath").getAsString());
    }

    @Test
    public void validateInvalidEmissionScoreWaterTrace() {
        // Introduced an error in an Emission score's quantity kind
        // expect to have no cardinality violations
        String cardinalityRes = Service.validateCardinality("water_trace_invalid_emissionscore");
        assertEquals("[]", cardinalityRes);


        String sparqlRes = Service.validateSparql("water_trace_invalid_emissionscore");
        JsonArray sparqlViolationsArray = new Gson().fromJson(sparqlRes, JsonArray.class);
        assertEquals(1, sparqlViolationsArray.size());
        JsonObject jsonObject = sparqlViolationsArray.get(0).getAsJsonObject();
        // catch errors related to incompatibility of CF and emission score
        assertEquals("https://water.com/provenance/CalculationEntity/emission-score", jsonObject.get("focusNode").getAsString());
        assertEquals("The quantity kind of the emission score is not compatible with the conversion factor's target chemical compound", jsonObject.get("resultMessage").getAsString());

        String typeRes = Service.validateType("water_trace_invalid_emissionscore");
        JsonArray typeViolationsArray = new Gson().fromJson(typeRes, JsonArray.class);
        assertEquals(1, typeViolationsArray.size());
        jsonObject = typeViolationsArray.get(0).getAsJsonObject();
        // catch error that quantity kind is of inappropriate type
        assertEquals("https://water.com/provenance/CalculationEntity/emission-score", jsonObject.get("focusNode").getAsString());
        assertEquals("Quantity's quantity kind should is not of type qudt:QuantityKind", jsonObject.get("resultMessage").getAsString());
        assertEquals("qudt:hasQuantityKind", jsonObject.get("resultPath").getAsString());
    }

    @Test
    public void validateInvalidCFSMLITrace3() {
        // added a second source unit to the conversion factor
        // the conversion factor is out of date

        String cardinalityRes = Service.validateCardinality("smli_trace3_invalid_cf");
        JsonArray cardinalityViolationsArray = new Gson().fromJson(cardinalityRes, JsonArray.class);
        assertEquals(1, cardinalityViolationsArray.size());
        JsonObject jsonObject = cardinalityViolationsArray.get(0).getAsJsonObject();
        // expect the violation to target the conversion factor
        assertEquals("https://w3id.org/ecfkg/i/mlco2/aws/cn-north-1/cf", jsonObject.get("focusNode").getAsString());
        // expect the path to be ecfo:hasSourceUnit
        assertEquals("ecfo:hasSourceUnit", jsonObject.get("resultPath").getAsString());
        assertEquals("An emission conversion factor has more than one source unit", jsonObject.get("resultMessage").getAsString());

        String typeRes = Service.validateType("smli_trace3_invalid_cf");
        JsonArray typeViolationsArray = new Gson().fromJson(typeRes, JsonArray.class);
        assertEquals(1, typeViolationsArray.size());
        jsonObject = typeViolationsArray.get(0).getAsJsonObject();
        // expect the violation to target the conversion factor
        assertEquals("https://w3id.org/ecfkg/i/mlco2/aws/cn-north-1/cf", jsonObject.get("focusNode").getAsString());
        // expect the path to be ecfo:hasSourceUnit
        assertEquals("ecfo:hasSourceUnit", jsonObject.get("resultPath").getAsString());
        // expect to see a violation since the type of conversion factor's second source unit is incorrect
        assertEquals("An emission conversion factor's source units are not of type qudt:Unit", jsonObject.get("resultMessage").getAsString());

        String sparqlRes = Service.validateSparql("smli_trace3_invalid_cf");
        JsonArray jsonArray = new Gson().fromJson(sparqlRes, JsonArray.class);
        // expect to see 2 violations: CF out of date
        // and that activity used an entity with units that do not match CF units
        assertEquals(2, jsonArray.size());

        // keep track of error messages
        Map<String, Integer> messageCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            // expect that each validation result focuses the CF
            assertEquals("https://w3id.org/ecfkg/i/mlco2/aws/cn-north-1/cf", focusNode);
            // keep track of messages
            String resultMessage = jsonObject.get("resultMessage").getAsString();
            messageCount.put(resultMessage, messageCount.getOrDefault(resultMessage, 0) + 1);
        }

        // expect appropriate messages to appear exactly once
        assertEquals(1, messageCount.get("An emission calculation activity that involved a conversion factor used an entity with units different from that CF units"));
        assertEquals(1, messageCount.get("An emission conversion factor used in the calculation is out of date"));
    }

    @Test
    public void validateInvalidCFInvalidEmissionScoreCalciumChlorideTrace() {
        // changed the emission generation activity's location to match CF location, so this error is not raised
        // made CF value negative
        // changed the units of the emission score so they do not match CF target units
        String sparqlRes = Service.validateSparql("calcium_chloride_trace_invalid_cf_invalid_score");
        JsonArray jsonArray = new Gson().fromJson(sparqlRes, JsonArray.class);
        // expect to see 2 violations: CF value negative
        // and that emission score's units do not match CF target units
        assertEquals(2, jsonArray.size());

        // keep track of focused nodes
        Map<String, Integer> focusNodeCount = new HashMap<>();
        // keep track of error messages
        Map<String, Integer> messageCount = new HashMap<>();
        // keep track of result paths
        Map<String, Integer> resultPathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            focusNodeCount.put(focusNode, focusNodeCount.getOrDefault(focusNode, 0) + 1);
            String resultMessage = jsonObject.get("resultMessage").getAsString();
            messageCount.put(resultMessage, messageCount.getOrDefault(resultMessage, 0) + 1);
            String resultPath = jsonObject.get("resultPath").getAsString();
            resultPathCount.put(resultPath, resultPathCount.getOrDefault(resultPath, 0) + 1);
        }

        // expect that emission score and cf were targeted once
        assertEquals(1, focusNodeCount.get("https://cacl2.com/provenance/cf"));
        assertEquals(1, focusNodeCount.get("https://cacl2.com/provenance/CalculationEntity/emission-score"));
        // expect the appropriate error messages
        assertEquals(1, messageCount.get("An emission conversion factor's value is negative"));
        assertEquals(1, messageCount.get("The units of the emission score are not compatible with the conversion factor's target units"));
        // expect the result paths to be rdf:value for CF, qudt:unit for emission score
        assertEquals(1, resultPathCount.get("rdf:value"));
        assertEquals(1, resultPathCount.get("qudt:unit"));
    }

    @Test
    public void validateRigFertiliserTrace() {
        // expect the trace to be valid
        assertEquals("[]", Service.validateCardinality("rig_fertiliser_trace"));
        assertEquals("[]", Service.validateType("rig_fertiliser_trace"));
        assertEquals("[]", Service.validateSparql("rig_fertiliser_trace"));
    }

    @Test
    public void validateRigFertiliserInvalidEntityTrace() {
        // made 2 emission calculation entities have a negative value
        // expect no type and cardinality violations
        assertEquals("[]", Service.validateCardinality("rig_fertiliser_trace_invalid_entity"));
        assertEquals("[]", Service.validateType("rig_fertiliser_trace_invalid_entity"));
        String sparqlRes = Service.validateSparql("rig_fertiliser_trace_invalid_entity");
        JsonArray jsonArray = new Gson().fromJson(sparqlRes, JsonArray.class);
        // expect to see 2 violations
        assertEquals(2, jsonArray.size());

        // keep track of error messages
        Map<String, Integer> messageCount = new HashMap<>();
        // keep track of result paths
        Map<String, Integer> resultPathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String resultMessage = jsonObject.get("resultMessage").getAsString();
            messageCount.put(resultMessage, messageCount.getOrDefault(resultMessage, 0) + 1);
            String resultPath = jsonObject.get("resultPath").getAsString();
            resultPathCount.put(resultPath, resultPathCount.getOrDefault(resultPath, 0) + 1);
        }

        // expect to see 2 same messages
        assertEquals(2, messageCount.get("An emission calculation entity has a negative value"));
        // expect to see 2 same paths
        assertEquals(2, resultPathCount.get("qudt:value"));
    }
}

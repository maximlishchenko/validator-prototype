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
        // empty array means no violations are present
        assertEquals("[]", Service.validateCardinality("smli_trace1_valid"));
        assertEquals("[]", Service.validateType("smli_trace1_valid"));
        assertEquals("[]", Service.validateSparql("smli_trace1_valid"));
    }

    @Test
    public void testValidateSMLITrace2() {
        // expect violations in: Constraint 3.2

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
        // expect violations in: Constraint 3.4

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
        // expect violations in: Constraints 1.1, 1.2, 1.4, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.5, 3.6

        // introduce 4 errors in CF
        // 2 source units, 2 target units, 2 emission targets, 2 values

        // test cardinality constraints
        String cardinalityRes = Service.validateCardinality("smli_trace3_invalid_ecfo_cardinality");
        JsonArray cardinalityJsonArray = new Gson().fromJson(cardinalityRes, JsonArray.class);
        assertEquals(4, cardinalityJsonArray.size());

        Map<String, Integer> CardinalityPathCount = new HashMap<>();

        for (JsonElement element : cardinalityJsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            // check that each validation result focuses the CF since this is where errors were introduced
            assertEquals("https://w3id.org/ecfkg/i/mlco2/aws/cn-north-1/cf", focusNode);

            // keep track of present result paths
            String resultPath = jsonObject.get("resultPath").getAsString();
            CardinalityPathCount.put(resultPath, CardinalityPathCount.getOrDefault(resultPath, 0) + 1);
        }

        // check that each resultPath occurs once
        assertEquals(1, CardinalityPathCount.get("ecfo:hasSourceUnit")); // (2.1)
        assertEquals(1, CardinalityPathCount.get("ecfo:hasTargetUnit")); // (2.2)
        assertEquals(1, CardinalityPathCount.get("rdf:value")); // (2.4)
        assertEquals(1, CardinalityPathCount.get("ecfo:hasEmissionTarget")); // (2.3)

        // test type constraints
        String typeRes = Service.validateType("smli_trace3_invalid_ecfo_cardinality");
        JsonArray typeJsonArray = new Gson().fromJson(typeRes, JsonArray.class);
        assertEquals(3, typeJsonArray.size());

        Map<String, Integer> typePathCount = new HashMap<>();

        for (JsonElement element : typeJsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            // check that each validation result focuses the CF since this is where errors were introduced
            assertEquals("https://w3id.org/ecfkg/i/mlco2/aws/cn-north-1/cf", focusNode);

            // keep track of present result paths for type constraints
            String resultPath = jsonObject.get("resultPath").getAsString();
            typePathCount.put(resultPath, typePathCount.getOrDefault(resultPath, 0) + 1);
        }

        // check that required resultPath occurs once
        assertEquals(1, typePathCount.get("ecfo:hasSourceUnit")); // (1.1)
        assertEquals(1, typePathCount.get("ecfo:hasTargetUnit")); // (1.2)
        assertEquals(1, typePathCount.get("ecfo:hasEmissionTarget")); // (1.4)

        // test sparql constraints
        String sparqlRes = Service.validateSparql("smli_trace3_invalid_ecfo_cardinality");
        JsonArray sparqlViolationsArray = new Gson().fromJson(sparqlRes, JsonArray.class);
        assertEquals(4, sparqlViolationsArray.size());
        JsonObject jsonObject = sparqlViolationsArray.get(0).getAsJsonObject();

        Map<String, Integer> sparqlResultMessageCount = new HashMap<>();
        Map<String, Integer> sparqlFocusNodeCount = new HashMap<>();

        // iterate through array of violations for sparql constraints
        for (JsonElement element : sparqlViolationsArray) {
            jsonObject = element.getAsJsonObject();

            // keep track of present sparql result messages
            String resultMessage = jsonObject.get("resultMessage").getAsString();
            sparqlResultMessageCount.put(resultMessage, sparqlResultMessageCount.getOrDefault(resultMessage, 0) + 1);
            // keep track of present sparql focus nodes
            String focusNode = jsonObject.get("focusNode").getAsString();
            sparqlFocusNodeCount.put(focusNode, sparqlFocusNodeCount.getOrDefault(focusNode, 0) + 1);
        }

        // expect the violations to target the emission score 2 times:
        // 1) incompatibility of chemical compound with emission score (3.6)
        // 2) incompatibility of target units with emission score (3.5)
        assertEquals(2, sparqlFocusNodeCount.get("https://github.com/mlco2/impact/provenance/i/CalculationEntity/7c3aee80-7718-4d78-ba6b-485af4d67347"));
        // expect to see ECF 2 times:
        // 1) out of date violation (3.2)
        // 2) activity used ECF with an entity where non-matching pair of units was found (3.1)
        assertEquals(2, sparqlFocusNodeCount.get("https://w3id.org/ecfkg/i/mlco2/aws/cn-north-1/cf"));
        // expect to see appropriate error messages once for each violation
        assertEquals(1, sparqlResultMessageCount.get("The quantity kind of the emission score is not compatible with the conversion factor's target chemical compound"));
        assertEquals(1, sparqlResultMessageCount.get("The units of the emission score are not compatible with the conversion factor's target units"));
        assertEquals(1, sparqlResultMessageCount.get("An emission conversion factor used in the calculation is out of date"));
        assertEquals(1, sparqlResultMessageCount.get("An emission calculation activity that involved a conversion factor used an entity with units different from that CF units"));
    }

    @Test
    // catch cardinality constraints related to PECO entities
    public void testValidatePECOCardinalitySMLITrace3() {
        // expect violations in: Constraints 1.5, 2.5, 2.6, 3.2

        // introduce an error so that an emission generation activity has 2 emission scores, 1 of which is non-existent
        // introduce an error in 2 emission generation activities:
        // 1) one activity uses another activity + an applicable period entity
        // 2) one activity uses a CF + an applicable period entity

        // test cardinality constraints
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
        // expect violations in: Constraints 1.7, 1.8, 2.7, 2.8, 2.9, 2.10, 3.2

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
        // expect violations in: 1.1, 1.2, 1.3, 1.4, 3.1, 3.2, 3.5, 3.6

        // introduce 4 errors in the conversion factor
        // 1) invalid source units
        // 2) invalid target units
        // 3) invalid emission target
        // 4) invalid scope

        // expect no cardinality violations
        assertEquals("[]", Service.validateCardinality("smli_trace4_invalid_ecfo_type"));

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

        // test sparql constraints
        String sparqlRes = Service.validateSparql("smli_trace4_invalid_ecfo_type");
        JsonArray sparqlViolationsArray = new Gson().fromJson(sparqlRes, JsonArray.class);
        assertEquals(4, sparqlViolationsArray.size());
        JsonObject jsonObject = sparqlViolationsArray.get(0).getAsJsonObject();

        Map<String, Integer> sparqlResultMessageCount = new HashMap<>();
        Map<String, Integer> sparqlFocusNodeCount = new HashMap<>();

        // iterate through array of violations for sparql constraints
        for (JsonElement element : sparqlViolationsArray) {
            jsonObject = element.getAsJsonObject();

            // keep track of present sparql result messages
            String resultMessage = jsonObject.get("resultMessage").getAsString();
            sparqlResultMessageCount.put(resultMessage, sparqlResultMessageCount.getOrDefault(resultMessage, 0) + 1);
            // keep track of present sparql focus nodes
            String focusNode = jsonObject.get("focusNode").getAsString();
            sparqlFocusNodeCount.put(focusNode, sparqlFocusNodeCount.getOrDefault(focusNode, 0) + 1);
        }

        // expect the violations to target the emission score 2 times:
        // 1) incompatibility of chemical compound with emission score (3.6)
        // 2) incompatibility of target units with emission score (3.5)
        assertEquals(2, sparqlFocusNodeCount.get("https://github.com/mlco2/impact/provenance/i/CalculationEntity/f16b7e64-6b7f-4e4d-918d-39367b7b4d50"));
        // expect to see ECF 2 times:
        // 1) out of date violation (3.2)
        // 2) activity used ECF with an entity where non-matching pair of units was found (3.1)
        assertEquals(2, sparqlFocusNodeCount.get("https://w3id.org/ecfkg/i/mlco2/gcp/australia-southeast1/cf"));
        // expect to see appropriate error messages once for each violation
        assertEquals(1, sparqlResultMessageCount.get("The quantity kind of the emission score is not compatible with the conversion factor's target chemical compound"));
        assertEquals(1, sparqlResultMessageCount.get("The units of the emission score are not compatible with the conversion factor's target units"));
        assertEquals(1, sparqlResultMessageCount.get("An emission conversion factor used in the calculation is out of date"));
        assertEquals(1, sparqlResultMessageCount.get("An emission calculation activity that involved a conversion factor used an entity with units different from that CF units"));

    }

    @Test
    // catch type constraints related to PECO and QUDT entities
    public void testValidatePECOQUDTTypeSMLITrace4() {
        // expect violations in: Constraints 1.5, 1.9, 1.10, 3.1, 3.2

        // introduce 9 errors
        // remove the emission score from the @type array of the emission score
        // make the units and quantity kind invalid in all the 4 quantities present in the trace

        // expect no cardinality violations
        assertEquals("[]", Service.validateCardinality("smli_trace4_invalid_peco_qudt_type"));

        String res = Service.validateType("smli_trace4_invalid_peco_qudt_type");
        JsonArray jsonArray = new Gson().fromJson(res, JsonArray.class);
        assertEquals(9, jsonArray.size());

        Map<String, Integer> pathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();

            // keep track of present result paths (for type constraints)
            String resultPath = jsonObject.get("resultPath").getAsString();
            pathCount.put(resultPath, pathCount.getOrDefault(resultPath, 0) + 1);
        }

        // expect to see invalid units and quantity kinds 4 times
        assertEquals(4, pathCount.get("qudt:unit"));
        assertEquals(4, pathCount.get("qudt:hasQuantityKind"));
        // expect to see path related to emission score once
        assertEquals(1, pathCount.get("peco:hasEmissionScore"));

        // test sparql constraints
        String sparqlRes = Service.validateSparql("smli_trace4_invalid_peco_qudt_type");
        JsonArray sparqlViolationsArray = new Gson().fromJson(sparqlRes, JsonArray.class);
        assertEquals(2, sparqlViolationsArray.size());
        JsonObject jsonObject = sparqlViolationsArray.get(0).getAsJsonObject();

        Map<String, Integer> sparqlResultMessageCount = new HashMap<>();
        Map<String, Integer> sparqlFocusNodeCount = new HashMap<>();

        // iterate through array of violations for sparql constraints
        for (JsonElement element : sparqlViolationsArray) {
            jsonObject = element.getAsJsonObject();

            // keep track of present sparql result messages
            String resultMessage = jsonObject.get("resultMessage").getAsString();
            sparqlResultMessageCount.put(resultMessage, sparqlResultMessageCount.getOrDefault(resultMessage, 0) + 1);
            // keep track of present sparql focus nodes
            String focusNode = jsonObject.get("focusNode").getAsString();
            sparqlFocusNodeCount.put(focusNode, sparqlFocusNodeCount.getOrDefault(focusNode, 0) + 1);
        }

        // expect the violation to target the conversion factor twice
        assertEquals(2, sparqlFocusNodeCount.get("https://w3id.org/ecfkg/i/mlco2/gcp/australia-southeast1/cf"));
        // expect to see appropriate error messages once for each violation
        assertEquals(1, sparqlResultMessageCount.get("An emission calculation activity that involved a conversion factor used an entity with units different from that CF units"));
        assertEquals(1, sparqlResultMessageCount.get("An emission conversion factor used in the calculation is out of date"));
    }

    @Test
    // catch type constraints related to SOSA entities
    public void testValidateSOSATypeSMLITrace4() {
        // expect violations in: Constraints 1.6, 1.7, 1.8, 3.2

        // introduce 3 errors related to sosa:Observation
        // 1) remove the sosa:Result type from the observation result's @type array
        // 2) remove the sosa:featureOfInterest type from the FOI's @type array
        // 3) the observation is linked to a non-existent emission generation activity

        // expect no cardinality violations
        assertEquals("[]", Service.validateCardinality("smli_trace4_invalid_sosa_type"));

        String typeRes = Service.validateType("smli_trace4_invalid_sosa_type");
        JsonArray jsonArray = new Gson().fromJson(typeRes, JsonArray.class);
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
        // expect violations in: Constraints 1.9, 3.1

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
        // expect violations in: Constraints 1.10, 3.5

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
        assertEquals("Quantity's quantity kind is not of type qudt:QuantityKind", jsonObject.get("resultMessage").getAsString());
        assertEquals("qudt:hasQuantityKind", jsonObject.get("resultPath").getAsString());
    }

    @Test
    public void validateInvalidCFSMLITrace3() {
        // expect violations in: Constraints 1.1, 2.1, 3.1, 3.2

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
        // expect violations in: Constraints 3.3, 3.6

        // changed the emission generation activity's location to match CF location, so this error is not raised
        // made CF value negative
        // changed the units of the emission score so they do not match CF target units

        // expect no type and cardinality violations
        assertEquals("[]", Service.validateType("calcium_chloride_trace_invalid_cf_invalid_score"));
        assertEquals("[]", Service.validateCardinality("calcium_chloride_trace_invalid_cf_invalid_score"));
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
    public void validateFleetVehiclesTraceSensor() {
        // expect the trace to be valid
        assertEquals("[]", Service.validateCardinality("fleet_vehicles_trace_sensor"));
        assertEquals("[]", Service.validateType("fleet_vehicles_trace_sensor"));
        assertEquals("[]", Service.validateSparql("fleet_vehicles_trace_sensor"));
    }

    @Test
    public void validateFleetVehiclesTraceFuelPrice() {
        // expect the trace to be valid
        assertEquals("[]", Service.validateCardinality("fleet_vehicles_trace_fuel_price"));
        assertEquals("[]", Service.validateType("fleet_vehicles_trace_fuel_price"));
        assertEquals("[]", Service.validateSparql("fleet_vehicles_trace_fuel_price"));
    }

    @Test
    public void validateRigFertiliserInvalidEntityTrace() {
        // expect violations in: Constraint 3.7

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

    @Test
    public void validateFleetVehiclesTraceFuelPriceInvalidType() {
        // expect violations in: Constraints 1.1, 1.2, 1.6, 1.7, 1.8, 1.9
        // errors introduced:
        // 1) sosa:hasFeatureOfInterest links observation to EGA instead of FOI
        // 2) make EGA not be of class peco:EmissionGenerationActivity
        // 3) make observation result not be of class sosa:Result
        // 4) remove mapping between kilograms (11570) and qudt:Unit
        // 5) remove mapping between litres (11582) and qudt:Unit

        // expect no cardinality and sparql violations
        assertEquals("[]", Service.validateCardinality("fleet_vehicles_trace_fuel_price_invalid_type"));
        assertEquals("[]", Service.validateSparql("fleet_vehicles_trace_fuel_price_invalid_type"));
        String typeRes = Service.validateType("fleet_vehicles_trace_fuel_price_invalid_type");

        JsonArray jsonArray = new Gson().fromJson(typeRes, JsonArray.class);
        // expect to see 7 violations, 2 for constraint 1.9, and 1 for each other constraint
        assertEquals(7, jsonArray.size());

        // keep track of focused nodes
        Map<String, Integer> focusNodeCount = new HashMap<>();
        // keep track of result paths
        Map<String, Integer> resultPathCount = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String focusNode = jsonObject.get("focusNode").getAsString();
            focusNodeCount.put(focusNode, focusNodeCount.getOrDefault(focusNode, 0) + 1);
            String resultPath = jsonObject.get("resultPath").getAsString();
            resultPathCount.put(resultPath, resultPathCount.getOrDefault(resultPath, 0) + 1);
        }

        // expect that observation was targeted 3 times, as three errors were introduced related to it
        assertEquals(3, focusNodeCount.get("https://fleetvehicles.com/provenance/Observation/cost-observation"));
        // expect that ECF targeted 2 times, as it has litres as source units, kg as target units
        assertEquals(2, focusNodeCount.get("https://fleetvehicles.com/provenance/cf"));
        // expect emission score to be targeted once, as it has litres as units
        assertEquals(1, focusNodeCount.get("https://fleetvehicles.com/provenance/CalculationEntity/emission-score"));
        // expect the below entity to be targeted once, as it has litres as units
        assertEquals(1, focusNodeCount.get("https://fleetvehicles.com/provenance/CalculationEntity/entity2"));
        // expect qudt:unit appear twice, as 2 errors were introduced related to it
        assertEquals(2, resultPathCount.get("qudt:unit"));
        // expect sosa:hasFeatureOfInterest to appear once, as there is 1 observation and 1 error related to it
        assertEquals(1, resultPathCount.get("sosa:hasFeatureOfInterest"));
        // expect ecfo:hasSourceUnit to appear once, as there is 1 ECF and 1 error related to ECF's source units (litres)
        assertEquals(1, resultPathCount.get("ecfo:hasSourceUnit"));
        // expect ecfo:hasTargetunit to appear once, as there is 1 ECF and 1 error related to ECF's target units (kg)
        assertEquals(1, resultPathCount.get("ecfo:hasTargetUnit"));
        // expect sosa:hasResult to appear once, as there is 1 observation and 1 error related to obs result
        assertEquals(1, resultPathCount.get("sosa:hasResult"));
        // expect peco:inEmissionActivityContext to appear once, as there is 1 obs, and 1 error related to the linked EGA
        assertEquals(1, resultPathCount.get("peco:inEmissionActivityContext"));
    }
}

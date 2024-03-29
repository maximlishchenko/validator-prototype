package uk.max.validator.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.sparql.graph.GraphFactory;
import uk.max.validator.Model.ValidationResult;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    // generate a SHACL validation report from provenance trace by applying rules
    public static ValidationReport generateReport(Graph rules, Graph trace) {
        Shapes shapes = Shapes.parse(rules);
        ValidationReport report = ShaclValidator.get().validate(shapes, trace);
        return report;
    }

    public static JsonArray generateValidationResults(ValidationReport report) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, report.getModel(), Lang.JSONLD);
        String reportData = stringWriter.toString();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(reportData, JsonObject.class);
        JsonArray graph = jsonObject.getAsJsonArray("@graph"); // access the graph array
        JsonArray validationResults = new JsonArray();

        // iterate through the graph array
        for (JsonElement element : graph) {
            JsonObject graphObject = element.getAsJsonObject();
            if (graphObject.has("@type")) {
                String type = graphObject.get("@type").getAsString();
                if (type.equals("sh:ValidationResult")) {
                    validationResults.add(graphObject);
                }
            }
        }
        return validationResults;
    }

    public static List<ValidationResult> generateValidationResultObjects(JsonArray validationResults) {
        List<ValidationResult> responses = new ArrayList<>();

        // build the ValidationResult objects
        for (JsonElement element : validationResults) {
            JsonObject json = element.getAsJsonObject();
            ValidationResult result = new ValidationResult(
                    json.get("@id").toString(),
                    json.get("focusNode").toString(),
                    json.get("resultMessage").toString(),
                    (json.has("resultPath") && !json.get("resultPath").isJsonNull()) ? json.get("resultPath").toString() : "\"\"",
                    json.get("resultSeverity").toString()
            );
            responses.add(result);
        }
        return responses;
    }

    public static String validate(String provenanceTraceName, Graph rules) {
        String provenanceTracePath = "data/" + provenanceTraceName + ".json";
        Graph dataGraph = RDFDataMgr.loadGraph(provenanceTracePath, Lang.JSONLD);
        ValidationReport report = generateReport(rules, dataGraph);

        if (report.conforms()) {
            // empty JSON array means no validation errors are present
            String res = "[]";
            return res;
        }
        // else ...
        JsonArray validationResults = generateValidationResults(report);
        List<ValidationResult> responses = generateValidationResultObjects(validationResults);
        return ValidationResult.buildJsonResponse(responses);
    }

    public static Graph parseJSONLD(String jsonLDContent) {
        InputStream inputStream = new ByteArrayInputStream(jsonLDContent.getBytes(StandardCharsets.UTF_8));

        RDFParser parser = RDFParser.create()
                .source(inputStream)
                .lang(RDFFormat.JSONLD.getLang())
                .base("http://example.org/base/")
                .build();

        Graph graph = GraphFactory.createDefaultGraph();
        parser.parse(graph);

        return graph;
    }
}

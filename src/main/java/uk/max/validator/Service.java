package uk.max.validator;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.http.ResponseEntity;
import uk.max.validator.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@org.springframework.stereotype.Service
public class Service {

    public static String validateCardinality(String provenanceTraceName) {
        Graph cardinalityRules = RDFDataMgr.loadGraph("./rules/cardinality-constraints.ttl", Lang.TTL);
        return Utils.validate(provenanceTraceName, cardinalityRules);
    }

    public static String validateType(String provenanceTraceName) {
        Graph typeRules = RDFDataMgr.loadGraph("./rules/type-constraints.ttl", Lang.TTL);
        return Utils.validate(provenanceTraceName, typeRules);
    }

    public static String validateSparql(String provenanceTraceName) {
        Graph sparqlRules = RDFDataMgr.loadGraph("./rules/sparql-constraints.ttl", Lang.TTL);
        return Utils.validate(provenanceTraceName, sparqlRules);
    }

    public static List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>();
        File dir = new File("src/main/resources/data/");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".json")) {
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    }
                    fileNames.add(fileName);
                }
            }
        }
        return fileNames;
    }
}

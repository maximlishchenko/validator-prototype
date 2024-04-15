package uk.max.validator;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import uk.max.validator.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@org.springframework.stereotype.Service
public class Service {

    public static String validateCardinality(String provenanceTraceName) {
        Graph cardinalityRules = RDFDataMgr.loadGraph("./rules/cardinality-constraints.ttl", Lang.TTL);
        // call main Utils function
        return Utils.validate(provenanceTraceName, cardinalityRules);
    }

    public static String validateType(String provenanceTraceName) {
        Graph typeRules = RDFDataMgr.loadGraph("./rules/type-constraints.ttl", Lang.TTL);
        // call main Utils function
        return Utils.validate(provenanceTraceName, typeRules);
    }

    public static String validateSparql(String provenanceTraceName) {
        Graph sparqlRules = RDFDataMgr.loadGraph("./rules/sparql-constraints.ttl", Lang.TTL);
        // call main Utils function
        return Utils.validate(provenanceTraceName, sparqlRules);
    }

    // function to retrieve all provenance trace names
    public static List<String> getFileNames() {
        List<String> fileNames = new ArrayList<>(); // this will store all the file names
        File dir = new File("src/main/resources/data/"); // constant data directory
        File[] files = dir.listFiles();
        if (files != null) {
            // loop through files and ensure correct file format present (json)
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(".json")) {
                        // remove '.json' from actual file name sent to user
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    }
                    fileNames.add(fileName);
                }
            }
        }
        return fileNames;
    }
}

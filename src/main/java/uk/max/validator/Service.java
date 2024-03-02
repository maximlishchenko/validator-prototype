package uk.max.validator;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import uk.max.validator.utils.Utils;


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
}

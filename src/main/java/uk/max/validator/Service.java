package uk.max.validator;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;


@org.springframework.stereotype.Service
public class Service {

    public static void validateCardinality() {
        Graph cardinalityRules = RDFDataMgr.loadGraph("./rules/cardinality-constraints.ttl", Lang.TTL);
        Graph dataGraph = RDFDataMgr.loadGraph("./data/provenance_trace2.json", Lang.JSONLD);
        Shapes shapes = Shapes.parse(cardinalityRules);
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        boolean conforms = report.conforms();

        if (conforms) {
            System.out.println("No constraints violated when checking cardinality constraints");
        } else {
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
        }
    }

    public static void validateType() {
        Graph typeRules = RDFDataMgr.loadGraph("./rules/type-constraints.ttl", Lang.TTL);
        Graph dataGraph = RDFDataMgr.loadGraph("./data/provenance_trace2.json", Lang.JSONLD);
        Shapes shapes = Shapes.parse(typeRules);
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        boolean conforms = report.conforms();

        if (conforms) {
            System.out.println("No constraints violated when checking type constraints");
        } else {
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
        }
    }

    public static void validateSparql() {
        Graph sparqlRules = RDFDataMgr.loadGraph("./rules/sparql-constraints.ttl", Lang.TTL);
        Graph dataGraph = RDFDataMgr.loadGraph("./data/provenance_trace2.json", Lang.JSONLD);
        Shapes shapes = Shapes.parse(sparqlRules);
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        boolean conforms = report.conforms();

        if (conforms) {
            System.out.println("No constraints violated when checking SPARQL constraints");
        } else {
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
        }
    }
}

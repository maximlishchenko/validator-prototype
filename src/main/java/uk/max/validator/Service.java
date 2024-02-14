package uk.max.validator;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;


@org.springframework.stereotype.Service
public class Service {

    public static void validateSosa() {
        Graph sosaRules = RDFDataMgr.loadGraph("./rules/rules-sosa.ttl", Lang.TTL);
        Graph dataGraph = RDFDataMgr.loadGraph("./data/provenance_trace.json", Lang.JSONLD);
        Shapes shapes = Shapes.parse(sosaRules);
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        boolean conforms = report.conforms();

        if (conforms) {
            System.out.println("No constraints violated for SOSA entities");
        } else {
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
        }
    }

    public static void validatePeco() {
        Graph pecoRules = RDFDataMgr.loadGraph("./rules/rules-peco.ttl", Lang.TTL);
        Graph dataGraph = RDFDataMgr.loadGraph("./data/provenance_trace.json", Lang.JSONLD);
        Shapes shapes = Shapes.parse(pecoRules);
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        boolean conforms = report.conforms();

        if (conforms) {
            System.out.println("No constraints violated for PECO entities");
        } else {
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
        }
    }

    public static void validateEcfo() {
        Graph ecfoRules = RDFDataMgr.loadGraph("./rules/rules-ecfo.ttl", Lang.TTL);
        Graph dataGraph = RDFDataMgr.loadGraph("./data/provenance_trace.json", Lang.JSONLD);
        Shapes shapes = Shapes.parse(ecfoRules);
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        boolean conforms = report.conforms();

        if (conforms) {
            System.out.println("No constraints violated for ECFO entities");
        } else {
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
        }
    }
}

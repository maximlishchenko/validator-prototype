package uk.max.validator;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    public static void test() {

        Graph rules = RDFDataMgr.loadGraph("./rules/rules.ttl", Lang.TTL);

        Graph dataGraph = RDFDataMgr.loadGraph("./data/provenance_trace.json", Lang.JSONLD);


        Shapes shapes = Shapes.parse(rules);

        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        boolean conforms = report.conforms();
        if (conforms) {
            System.out.println("No constraints violated");
        } else {
            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);
        }
    }
}

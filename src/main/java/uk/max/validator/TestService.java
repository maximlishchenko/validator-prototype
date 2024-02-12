package uk.max.validator;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.springframework.stereotype.Service;


@Service
public class TestService {
    public static void validate() {
        Graph rules = RDFDataMgr.loadGraph("./rules/rules.ttl", Lang.TTL);
        Graph dataGraph = RDFDataMgr.loadGraph("./data/provenance_trace.json", Lang.JSONLD);
        Shapes shapes = Shapes.parse(rules);
        ValidationReport report = ShaclValidator.get().validate(shapes, dataGraph);

        boolean conforms = report.conforms();

        if (conforms) {
            System.out.println("No constraints violated");
        } else {
//            RDFDataMgr.write(System.out, report.getModel(), Lang.TTL);

            Model model = report.getModel();
            Resource validationReport = model.listResourcesWithProperty(model.createProperty("http://www.w3.org/ns/shacl#conforms"), model.createTypedLiteral(false)).next();
            StmtIterator results = validationReport.listProperties(model.createProperty("http://www.w3.org/ns/shacl#result"));

            while (results.hasNext()) {
                Resource result = results.next().getResource();
                Resource focusNode = result.getPropertyResourceValue(model.createProperty("http://www.w3.org/ns/shacl#focusNode"));
                System.out.println("Focus Node: " + focusNode.toString());
                RDFNode resultMessageNode = result.getProperty(model.createProperty("http://www.w3.org/ns/shacl#resultMessage")).getObject();
                String resultMessage = resultMessageNode.asLiteral().getString();
                System.out.println("Result Message: " + resultMessage);
                System.out.println("------------------------");
            }
        }
    }
}

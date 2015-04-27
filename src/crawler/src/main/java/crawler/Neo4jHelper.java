package crawler;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.ConstraintType;

import java.util.Arrays;

public class Neo4jHelper {
    private Neo4jHelper() {
    }

    public static GraphDatabaseService openGrapDb(String dbPath) {
        final GraphDatabaseService graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(dbPath)
                .newGraphDatabase();

        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });

        return graphDb;
    }

    public static void createSchema(GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            // Add a constraint for uniqueness of "uri" on each label
            for (String labelName : Arrays.asList("Player", "Team", "Tournament", "Trainer")) {
                Label label = DynamicLabel.label(labelName);
                boolean uniqueConstraintOnUri = false;

                for (ConstraintDefinition definition : graphDb.schema().getConstraints(label)) {
                    if (definition.isConstraintType(ConstraintType.UNIQUENESS)) {
                        if (definition.getPropertyKeys().spliterator().getExactSizeIfKnown() == 1
                                && "uri".equals(definition.getPropertyKeys().iterator().next())) {
                            uniqueConstraintOnUri = true;
                            break;
                        }
                    }
                }

                if (!uniqueConstraintOnUri) {
                    graphDb.schema()
                            .constraintFor(label)
                            .assertPropertyIsUnique("uri").create();
                }
            }

            tx.success();
        }
    }
}

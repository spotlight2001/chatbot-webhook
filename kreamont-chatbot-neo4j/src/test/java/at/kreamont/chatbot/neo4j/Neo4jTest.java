package at.kreamont.chatbot.neo4j;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

public class Neo4jTest {

	private GraphDatabaseService graphDb;

	@Before
	public void prepareTestDatabase() {
		graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
	}

	@After
	public void destroyTestDatabase() {
		graphDb.shutdown();
	}

	@Test
	public void test() {
		Node n = null;
		try (Transaction tx = graphDb.beginTx()) {
			n = graphDb.createNode();
			n.setProperty("name", "Nancy");
			tx.success();
		}

		// The node should have a valid id
		assertThat(n.getId(), is(greaterThan(-1L)));

		// Retrieve a node by using the id of the created node. The id's and
		// property should match.
		try (Transaction tx = graphDb.beginTx()) {
			Node foundNode = graphDb.getNodeById(n.getId());
			assertThat(foundNode.getId(), is(n.getId()));
			assertThat((String) foundNode.getProperty("name"), is("Nancy"));

			Result result = graphDb.execute("MATCH(p) WHERE p.name CONTAINS 'aNcy' RETURN p, p.name");
			while (result.hasNext()) {
				Map<String, Object> next = result.next();
				Node node = (Node) next.get("p");
				assertThat((String) node.getProperty("name"), is("Nancy"));
				assertThat((String) next.get("p.name"), is("Nancy"));
			}
		}
	}

}

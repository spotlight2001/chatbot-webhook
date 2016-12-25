package at.kreamont.chatbot.neo4j;


import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.core.RelationshipTypeToken;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelefonlisteNeo4j {

	private static final Logger LOGGER = LoggerFactory.getLogger(TelefonlisteNeo4j.class.getName());

	private GraphDatabaseService graphDb;

	public TelefonlisteNeo4j() {
		this.graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
	}

	public void index(String csv) {

		try (Transaction tx = graphDb.beginTx()) {

			int relId = 0;

			String[] rows = csv.split("\r?\n");
			LOGGER.info(String.format("csv has '%s' rows", rows.length));
			for (int i = 2; i < rows.length; i++) { // skip first line
				String row = rows[i];
				String[] fields = row.split(";");

				LOGGER.debug(String.format("csv row has '%s' values", fields.length));
				if (fields.length < 21) {
					continue;
				}

				Node kid = graphDb.createNode();
				kid.setProperty("firstname", fields[1]);
				kid.setProperty("lastname", fields[0]);

				Node mother = graphDb.createNode();
				mother.setProperty("firstname", fields[4]);
				mother.setProperty("lastname", fields[5]);
				mother.setProperty("mobile", fields[6]);

				Node father = graphDb.createNode();
				father.setProperty("firstname", fields[7]);
				father.setProperty("lastname", fields[8]);
				father.setProperty("mobile", fields[9]);

				kid.createRelationshipTo(mother, new RelationshipTypeToken("kid", relId++));
				kid.createRelationshipTo(father, new RelationshipTypeToken("kid", relId++));
				mother.createRelationshipTo(kid, new RelationshipTypeToken("mother", relId++));
				father.createRelationshipTo(kid, new RelationshipTypeToken("father", relId++));
			}
			tx.success();
		}
	}

	public String getTelephoneNumberAnswer(String filter) {
		try (Transaction tx = graphDb.beginTx()) {
			Map<String, Object> params = new HashMap<>();
			params.put("filter", "(?i).*" + filter + ".*");
			Result result = graphDb.execute(
					"MATCH(p) WHERE (p.firstname =~ {filter} OR p.lastname =~ {filter}) RETURN p.firstname, p.lastname, p.mobile",
					params);
			while (result.hasNext()) {
				Map<String, Object> next = result.next();
				StringBuilder out = new StringBuilder();
				out.append("Ich glaub Du meinst ").append(next.get("p.firstname")).append(" ")
						.append(next.get("p.lastname")).append(". Handy: ").append(next.get("p.mobile"));
				return out.toString();
			}
		}
		return null;
	}
}
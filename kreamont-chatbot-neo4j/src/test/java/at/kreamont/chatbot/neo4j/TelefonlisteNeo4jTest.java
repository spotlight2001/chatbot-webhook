package at.kreamont.chatbot.neo4j;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.Scanner;

import org.junit.Test;

public class TelefonlisteNeo4jTest {

	private TelefonlisteNeo4j testMe = new TelefonlisteNeo4j();

	private String csv = new Scanner(TelefonlisteNeo4j.class.getResourceAsStream("/telefonliste.csv"), "UTF-8")
			.useDelimiter("\\A").next();

	@Test
	public void indexShouldSucceed() {
		testMe.index(csv);
	}

	@Test
	public void getTelephoneNumberAnswerShouldSucceed() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("alTer");
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("Walter"));
	}
}
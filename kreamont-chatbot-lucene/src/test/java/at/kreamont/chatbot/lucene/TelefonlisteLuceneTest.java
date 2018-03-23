package at.kreamont.chatbot.lucene;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.Scanner;

import org.junit.Test;

public class TelefonlisteLuceneTest {

	private TelefonlisteLucene testMe = new TelefonlisteLucene();

	private String csv = new Scanner(TelefonlisteLucene.class.getResourceAsStream("/telefonliste.csv"), "UTF-8")
			.useDelimiter("\\A").next();

	@Test
	public void indexShouldSucceed() {
		testMe.index(csv);
	}

	@Test
	public void firstnameAndLastnameShouldBeFound() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("alter", "Murit");
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("alter"));
	}
	
	@Test
	public void firstnameShouldBeFound() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("alter", "murit");
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("alter"));
	}
	
	@Test
	public void firstnameCaseInsensitiveShouldBeFound() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("aNna", "murIt");
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("anna"));
	}
	
	@Test
	public void fuzzySearchWithFirstAndLastnameShouldWork() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("alte", "Muritz");
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("alter"));
	}
}
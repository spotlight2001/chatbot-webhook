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
		String answer = testMe.getTelephoneNumberAnswer("Walter", "Mauritz");
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("Walter"));
	}
	
	@Test
	public void firstnameShouldBeFound() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("Walter", null);
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("Walter"));
	}
	
	@Test
	public void firstnameCaseInsensitiveShouldBeFound() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("walTer", null);
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("Walter"));
	}
	
	@Test
	public void fuzzySearchShouldWork() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("walTe", null);
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("Walter"));
	}
	
	@Test
	public void fuzzySearchWithFirstAndLastnameShouldWork() {
		testMe.index(csv);
		String answer = testMe.getTelephoneNumberAnswer("walTe", "Muritz");
		System.out.println("answer: " + answer);
		assertThat(answer, containsString("Walter"));
	}
}
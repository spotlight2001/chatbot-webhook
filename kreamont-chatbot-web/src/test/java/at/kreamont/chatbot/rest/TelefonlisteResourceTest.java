package at.kreamont.chatbot.rest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Test;

import at.kreamont.chatbot.model.FulfillmentResponse;

public class TelefonlisteResourceTest {

	private TelefonlisteResource testMe = new TelefonlisteResource();

	private String fulfillmentRequestJson = new Scanner(
			TelefonlisteResource.class.getResourceAsStream("/fulfillment-request.json"), "UTF-8").useDelimiter("\\A")
					.next();

	private String csv = new Scanner(TelefonlisteResource.class.getResourceAsStream("/telefonliste.csv"), "UTF-8")
			.useDelimiter("\\A").next();

	@Test
	public void getFulfillmentShouldSucceed() {
		testMe.setTelefonlisteCsv(csv);
		testMe.feedDataToDatabase();
		FulfillmentResponse fulfillment = testMe.getFulfillment(fulfillmentRequestJson);
		assertThat(fulfillment.getSpeech(), containsString("Walter"));
	}
}

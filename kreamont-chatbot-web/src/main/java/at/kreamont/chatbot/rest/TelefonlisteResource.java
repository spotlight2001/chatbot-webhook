package at.kreamont.chatbot.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import at.kreamont.chatbot.lucene.TelefonlisteLucene;
import at.kreamont.chatbot.model.FulfillmentResponse;
import at.kreamont.chatbot.service.GoogleCalendarService;
import at.kreamont.chatbot.service.WordpressNewsService;

@RestController
@RequestMapping("telefonliste")
public class TelefonlisteResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(TelefonlisteResource.class);

	// Familienname;Kindername;;Festnetz;Mutter;;;Vater;;;Mutter;;;Vater;;;Adresse;;;;
	private String telefonlisteCsv;

	private TelefonlisteLucene db;

	private final Gson gson = GsonFactory.getDefaultFactory().getGson();

	@Autowired
	private Environment env;

	@Autowired
	private GoogleCalendarService googleCalendarService;

	@Autowired
	private WordpressNewsService wordpressNewsService;

	public void setTelefonlisteCsv(String telefonlisteCsv) {
		this.telefonlisteCsv = telefonlisteCsv;
	}

	@PostConstruct
	void setupAllSteps() throws URISyntaxException {
		getTelefonlisteCsvFromRemote();
		feedDataToDatabase();
	}

	void getTelefonlisteCsvFromRemote() throws URISyntaxException {
		String url = "http://www.kreamont.at/telefonliste/telefonliste.csv";

		String plainCreds = env.getRequiredProperty("KREAMONT_USER") + ":"
				+ env.getRequiredProperty("KREAMONT_PASSWORD");
		String base64Creds = DatatypeConverter.printBase64Binary(plainCreds.getBytes(StandardCharsets.UTF_8));

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		RequestEntity<Void> requestEntity = new RequestEntity<Void>(headers, HttpMethod.GET, new URI(url));

		RestTemplate client = new RestTemplate();
		// csv is actually UTF-8 but http response doesnt tell - so our code thinks its
		// latin1 which is wrong
		client.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		ResponseEntity<String> exchange = client.exchange(requestEntity, String.class);
		String csv = exchange.getBody();
		this.telefonlisteCsv = csv;

		LOGGER.info(telefonlisteCsv);
	}

	void feedDataToDatabase() {
		// feed neo4j with csv
		this.db = new TelefonlisteLucene();
		db.index(this.telefonlisteCsv);
	}

	// {"id":"84cf326a-529b-44bc-877b-e6ab6b69c0f6","timestamp":"2016-12-24T00:19:04.035Z","result":{"source":"agent","resolvedQuery":"wie
	// ist die Nummer von
	// Walter","speech":"","action":"GetPersonDataFromTelephoneList","actionIncomplete":false,"parameters":{"personName":"Walter"},"contexts":[],"metadata":{"intentId":"a29900c3-e37c-4ac8-9503-04190a89b8c1","webhookUsed":"true","webhookForSlotFillingUsed":"false","intentName":"GetPersonData"},"fulfillment":{"speech":"","messages":[{"type":0,"speech":""}]},"score":1.0},"status":{"code":200,"errorType":"success"},"sessionId":"1487e981-f005-468d-934b-54f37ea89124","originalRequest":null}
	@RequestMapping(method = RequestMethod.POST, path = "fulfillment", consumes = MediaType.APPLICATION_JSON_VALUE)
	public FulfillmentResponse getFulfillment(@RequestBody String requestStr) {
		LOGGER.debug("getFulfillment gets called");
		LOGGER.info(requestStr);
		final AIResponse request = gson.fromJson(requestStr, AIResponse.class);

		String action = request.getResult().getAction();
		if (StringUtils.containsIgnoreCase(action, "termin")) {
			return googleCalendarService.get();
		} else if (StringUtils.containsIgnoreCase(action, "news")) {
			return wordpressNewsService.get();
		}

		String firstname = getParameter(request, "firstname");
		String lastname = getParameter(request, "lastname");

		FulfillmentResponse out = new FulfillmentResponse();
		LOGGER.info("use personName: " + firstname + " " + lastname);
		out.setSpeech(db.getTelephoneNumberAnswer(firstname, lastname));
		return out;
	}

	private String getParameter(AIResponse request, String key) {
		JsonElement jsonElement = request.getResult().getParameters().get(key);
		if (jsonElement != null) {
			return jsonElement.getAsString();
		} else {
			return null;
		}
	}
}
package at.kreamont.chatbot.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import at.kreamont.chatbot.lucene.TelefonlisteLucene;
import at.kreamont.chatbot.model.FulfillmentResponse;

@Service
public class TelefonlisteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TelefonlisteService.class);

	private TelefonlisteLucene db;

	@Autowired
	private Environment env;

	// Familienname;Kindername;;Festnetz;Mutter;;;Vater;;;Mutter;;;Vater;;;Adresse;;;;
	private String telefonlisteCsv;

	@PostConstruct
	void setupAllSteps() throws URISyntaxException {
		getTelefonlisteCsvFromRemote();
		feedDataToDatabase();
	}

	public void setTelefonlisteCsv(String telefonlisteCsv) {
		this.telefonlisteCsv = telefonlisteCsv;
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

	public FulfillmentResponse getFulfillment(String firstname, String lastname) {
		FulfillmentResponse out = new FulfillmentResponse();		
		out.setSpeech(db.getTelephoneNumberAnswer(firstname, lastname));
		return out;
	}
}
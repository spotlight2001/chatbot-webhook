package at.kreamont.chatbot.service;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.kreamont.chatbot.model.FulfillmentResponse;

@Service
public class WordpressNewsService {
	public FulfillmentResponse get() {
		RestTemplate client = new RestTemplate();
		String url = "http://www.kreamont.at/wp-json/wp/v2/posts?per_page=1";
		ResponseEntity<String> response = client.getForEntity(url, String.class);
		String jsonStr = response.getBody();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json;
		try {
			json = mapper.readTree(jsonStr);
		} catch (IOException e) {
			throw new RuntimeException("failed to read wordpress feed json: " + jsonStr + "; " + e.getMessage(), e);
		}
		StringBuilder buf = new StringBuilder();
		buf.append("News von der Homepage: \n");
		json.elements().forEachRemaining(item -> {
			String title = item.get("title").get("rendered").asText();
			buf.append(title).append("\n");
		});
		FulfillmentResponse result = new FulfillmentResponse();
		result.setSpeech(buf.toString());
		return result;
	}
}
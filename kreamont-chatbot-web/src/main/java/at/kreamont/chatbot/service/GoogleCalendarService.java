package at.kreamont.chatbot.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.kreamont.chatbot.model.FulfillmentResponse;

@Service
public class GoogleCalendarService {
	public FulfillmentResponse get() {
		RestTemplate client = new RestTemplate();
		String url = "https://clients6.google.com/calendar/v3/calendars/32kbr4dsdljsqgpsromm2det5c@group.calendar.google.com/events?calendarId=32kbr4dsdljsqgpsromm2det5c@group.calendar.google.com&singleEvents=true&timeZone=Europe/Vienna&maxAttendees=1&maxResults=3&sanitizeHtml=true&key=AIzaSyBNlYH01_9Hc5S1J9vuFmu2nUqBZJNAXxs&timeMin=";
		// 2018-02-26T00:00:00+01:00
		DateTimeFormatter googleDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00+01:00");
		url += LocalDateTime.now().format(googleDateTimeFormat);
		ResponseEntity<String> response = client.getForEntity(url, String.class);
		String jsonStr = response.getBody();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json;
		try {
			json = mapper.readTree(jsonStr);
		} catch (IOException e) {
			throw new RuntimeException("failed to read google cal json: " + jsonStr + "; " + e.getMessage(), e);
		}
		StringBuilder buf = new StringBuilder();
		buf.append("Die nächsten Termine: \n");
		json.get("items").elements().forEachRemaining(item -> {
			String start = item.get("start").get("date").asText();
			LocalDate localDate = LocalDate.parse(start);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
			buf.append(item.get("summary").asText()).append(" am ").append(formatter.format(localDate)).append("\n");
		});
		FulfillmentResponse result = new FulfillmentResponse();
		result.setSpeech(buf.toString());
		return result;
	}
}
package at.kreamont.chatbot.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ai.api.GsonFactory;
import ai.api.model.AIResponse;
import at.kreamont.chatbot.model.FulfillmentResponse;
import at.kreamont.chatbot.service.GoogleCalendarService;
import at.kreamont.chatbot.service.TelefonlisteService;
import at.kreamont.chatbot.service.WordpressNewsService;

@RestController
@RequestMapping("telefonliste")
public class DialogflowWebhookResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(DialogflowWebhookResource.class);

	private final Gson gson = GsonFactory.getDefaultFactory().getGson();

	@Autowired
	private GoogleCalendarService googleCalendarService;

	@Autowired
	private WordpressNewsService wordpressNewsService;

	@Autowired
	private TelefonlisteService telefonlisteService;

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
		} else if (StringUtils.containsIgnoreCase(action, "GetPersonDataFromTelephoneList")) {
			String firstname = getParameter(request, "firstname");
			String lastname = getParameter(request, "lastname");
			LOGGER.info("use personName: " + firstname + " " + lastname);
			return telefonlisteService.getFulfillment(firstname, lastname);
		} else {
			FulfillmentResponse out = new FulfillmentResponse();
			out.setSpeech(
					"Ups. Leider kann ich mit der action=" + action + " nix anfangen. Bitte einen dev kontaktieren!");
			return out;
		}
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
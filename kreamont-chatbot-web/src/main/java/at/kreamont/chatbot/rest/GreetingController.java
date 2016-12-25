package at.kreamont.chatbot.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GreetingController {

	@RequestMapping("/hello/{name}")
	public String hello(@PathVariable String name) {
		return "Hello, " + name + "!";
	}

	@RequestMapping("/exception")
	public String exception() {
		throw new IllegalArgumentException("because i programmed it so");
	}
}
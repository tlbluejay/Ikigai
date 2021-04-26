package lewis.trenton.ikigai.controllers;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import lewis.trenton.ikigai.models.Beehive;
import lewis.trenton.ikigai.models.User;
import lewis.trenton.ikigai.repositories.BeehiveJpaRepository;
import lewis.trenton.ikigai.repositories.UserJpaRepository;

@RestController
@RequestMapping(path = "/users")
public class UserController {

	@Autowired
	private UserJpaRepository userRepository;

	@Autowired
	private BeehiveJpaRepository hiveRepository;

	final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
	private static final String QUEUE_NAME = "ikigai-email-sqs.fifo";

	// TODO rethink logic for password change
	@PostMapping(path = "/passChange")
	public String changePassword(@RequestParam(value = "username") String email, @RequestBody String password) {
		String emailActual = new JsonParser().parse(email).getAsJsonObject().get("email").getAsString();
		User u = userRepository.findByEmail(emailActual);
		if (u == null)
			return setResponseJSON(
					"{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User with email.\" }");
		u.setPassword(password);
		SendMessageRequest send_msg_request = new SendMessageRequest()
				.withQueueUrl("https://sqs.us-west-2.amazonaws.com/793939683837/ikigai-email-sqs.fifo")
				.withMessageBody("PASSWORDCHANGE;Password changed;" + u.getEmail() + ";")
				.withMessageGroupId("PASSWORDCHANGE");
		sqs.sendMessage(send_msg_request);
		return setResponseJSON("{ \"Status\": 200, \"Message_Header\": \"OK\", \"Message\": \"Email Sent.\" }");

	}

	// TODO rethink logic for password reset
	@PostMapping(path = "/passReset")
	public String resetPassword(@RequestBody String email) {
		String emailActual = new JsonParser().parse(email).getAsJsonObject().get("email").getAsString();
		User u = userRepository.findByEmail(emailActual);
		if (u == null)
			return setResponseJSON(
					"{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User with email.\" }");
		String reset = generatePassword();
		u.setPassword(reset);
		SendMessageRequest send_msg_request = new SendMessageRequest()
				.withQueueUrl("https://sqs.us-west-2.amazonaws.com/793939683837/ikigai-email-sqs.fifo")
				.withMessageBody("PASSWORDRESET;Password reset;" + u.getEmail() + ";" + reset)
				.withMessageGroupId("PASSWORDRESET");
		sqs.sendMessage(send_msg_request);
		return setResponseJSON("{ \"Status\": 200, \"Message_Header\": \"OK\", \"Message\": \"Email Sent.\", "
				+ "\"data\": { \"task\": \"PASSWORDRESET;Password reset;" + u.getEmail() + ";" + reset + "\" }}");
	}

	@PostMapping(path = "")
	public String createUser(@RequestBody User user) {
		if (userRepository.findByEmail(user.getEmail()) != null) {
			return setResponseJSON(
					"{ \"Status\": 200, \"Message_Header\": \"EMAILINUSE\", \"Message\": \"Email already in use.\" }");
		} else {
			user.setUUID(UUID.randomUUID().toString());

			Beehive hive = new Beehive();
			hive.setUUID(UUID.randomUUID().toString());
			hive.setOwnerUUID(user.getUUID());
			hive.setBeeCount(new Random().nextInt(100) + 1);
			hive.setNickname("Your Hive");
			user.setHive(hive);
			hiveRepository.save(hive);

			userRepository.save(user);
			return setResponseJSON(
					"{ \"Status\": 200, \"Message_Header\": \"CREATED\", \"Message\": \"New User created.\", "
							+ "\"data\": { \"username\": \"" + user.getUsername() + "\", \"uuid\": \"" + user.getUUID()
							+ "\", \"isAdmin\": \"" + user.isAdmin() + "\" }}");
		}
	}
	
	@PatchMapping(path = "/update")
	public String updateUser(@RequestParam(value = "uuid") String uuid, @RequestBody Map<String, Object> updates) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		User user = userRepository.findByUUID(uuid);
		if (user == null)
			return setResponseJSON(
					"{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User.\" }");

			Class<?> userType = user.getClass();

			for (String propertyName : updates.keySet()) {
				if (!propertyName.contentEquals("id")) {
					Field field = userType.getDeclaredField(propertyName);
					field.setAccessible(true);
					field.set(user, updates.get(propertyName));
					field.setAccessible(false);
				}
			}
			return setResponseJSON(
					"{ \"Status\": 200, \"Message_Header\": \"UPDATED\", \"Message\": \"Updated User Data.\", "
							+ "\"data\": { \"username\": \"" + user.getUsername() + "\", \"uuid\": \"" + user.getUUID()
							+ "\", \"isAdmin\": \"" + user.isAdmin() + "\", \"email\": \"" + user.getEmail() + "\", \"name\": \"" + user.getName() + "\" }}");
	}
	
	@GetMapping(path = "/fetchUpdateDetails")
	public String fetchDetails(@RequestParam(value = "uuid") String uuid) {
		User user = userRepository.findByUUID(uuid);
		if (user == null)
			return setResponseJSON(
					"{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User.\" }");
		return setResponseJSON(
				"{ \"Status\": 200, \"Message_Header\": \"FETCHED\", \"Message\": \"Fetched User Data.\", "
						+ "\"data\": { \"username\": \"" + user.getUsername() + "\", \"uuid\": \"" + user.getUUID()
						+ "\", \"isAdmin\": \"" + user.isAdmin() + "\", \"email\": \"" + user.getEmail() + "\", \"name\": \"" + user.getName() + "\" }}");
	}

	@GetMapping(path = "/fetchUser")
	public String fetch(@RequestParam(value = "username") String username) {
		User user = userRepository.findByUsername(username);
		if (user == null)
			return setResponseJSON(
					"{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User with username.\" }");
		return setResponseJSON(
				"{ \"Status\": 200, \"Message_Header\": \"FETCHED\", \"Message\": \"Fetched User Data.\", "
						+ "\"data\": { \"username\": \"" + user.getUsername() + "\", \"uuid\": \"" + user.getUUID()
						+ "\", \"isAdmin\": \"" + user.isAdmin() + "\", \"cookie\": \"" + user.getPassword() + "\" }}");
	}

	private String generatePassword() {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

		SecureRandom sr = new SecureRandom();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < 9; i++) {
			int randIndex = sr.nextInt(chars.length());
			sb.append(chars.charAt(randIndex));
		}

		return sb.toString();
	}

	private String setResponseJSON(String content) {
		return new Gson().toJson(new JsonParser().parse(content).getAsJsonObject());
	}

}

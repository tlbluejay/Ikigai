package lewis.trenton.ikigai.controllers;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import lewis.trenton.ikigai.models.Beehive;
import lewis.trenton.ikigai.models.User;
import lewis.trenton.ikigai.repositories.UserJpaRepository;

@RestController
@RequestMapping(path = "/bees")
public class BeehiveController {
	
	@Autowired
	private UserJpaRepository userRepository;
	
	@GetMapping(path = "/grow")
	public String growHive(@RequestParam(value = "uuid") String uuid) {
		User user = userRepository.findByUUID(uuid);
		if (user == null) return setResponseJSON("{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User with uuid.\" }");
		Beehive hive = user.getHive();
		int oldPopulation = hive.getBeeCount();
		hive.setBeeCount((new Random().nextInt(100) + 150) + oldPopulation);
		user.setHive(hive);
		userRepository.save(user);
		
		return setResponseJSON(
				"{ \"Status\": 200, \"Message_Header\": \"OK\", \"Message\": \" Beehive has grown.\"}");
	}

	@GetMapping(path = "/fetch")
	public String fetchHive(@RequestParam(value = "uuid") String uuid) {
		User user = userRepository.findByUUID(uuid);
		if (user == null) return setResponseJSON("{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User with uuid.\" }");
		return setResponseJSON(
				"{ \"Status\": 200, \"Message_Header\": \"FETCHED\", \"Message\": \"Tasks for User found.\", "
						+ "\"data\": { \"hive\": " + new Gson().toJson(user.getHive())  + "}}");

	}
	
	@PostMapping(path = "/changeName")
	public String change(@RequestParam(value = "uuid") String uuid, @RequestBody String nickname) {
		User user = userRepository.findByUUID(uuid);
		if (user == null) return setResponseJSON("{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User with uuid.\" }");
		Beehive hive = user.getHive();
		String nick = new JsonParser().parse(nickname).getAsJsonObject().get("nickname").getAsString();
		hive.setNickname(nick);
		user.setHive(hive);
		userRepository.save(user);
		return setResponseJSON("{ \"Status\": 200, \"Message_Header\": \"OK\", \"Message\": \"Nickname changed.\" }");
	}
	
	private String setResponseJSON(String content) {
		return new Gson().toJson(new JsonParser().parse(content).getAsJsonObject());
	}
	
}

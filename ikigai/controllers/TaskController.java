package lewis.trenton.ikigai.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

import lewis.trenton.ikigai.models.Task;
import lewis.trenton.ikigai.models.User;
import lewis.trenton.ikigai.repositories.TaskJpaRepository;
import lewis.trenton.ikigai.repositories.UserJpaRepository;

@RestController
@RequestMapping(path = "/tasks")
public class TaskController {

	@Autowired
	private UserJpaRepository userRepository;
	
	@Autowired
	private TaskJpaRepository taskRepository;

	final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
	private static final String QUEUE_NAME = "ikigai-email-sqs.fifo";
	
	@PostMapping(path = "")
	public String createTask(@RequestBody Task task) {
		taskRepository.save(task);
		User user = userRepository.findByUUID(task.getTaskOwnerUUID());
		user.getTasks().add(task);
		userRepository.save(user);
		SendMessageRequest send_msg_request = new SendMessageRequest()
				.withQueueUrl("https://sqs.us-west-2.amazonaws.com/793939683837/ikigai-email-sqs.fifo")
				.withMessageBody("TASKCREATED;Task Created;" + user.getEmail() + ";" + task.toString())
				.withMessageGroupId("TASKCREATED");
		sqs.sendMessage(send_msg_request);
		return setResponseJSON(
				"{ \"Status\": 200, \"Message_Header\": \"CREATED\", \"Message\": \"New Task created.\"}");
	}

	@GetMapping(path = "/fetch")
	public String fetchTasks(@RequestParam(value = "uuid") String uuid) {
		User user = userRepository.findByUUID(uuid);
		if (user == null) return setResponseJSON("{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find User with uuid.\" }");
		return setResponseJSON(
				"{ \"Status\": 200, \"Message_Header\": \"FETCHED\", \"Message\": \"Tasks for User found.\", "
						+ "\"data\": { \"tasks\": [" + taskListToJson(user.getTasks())  + "]}}");

	}
	
	@GetMapping(path = "/complete")
	public String completeTask(@RequestParam(value = "id") long id) {
		Task task = taskRepository.findById(id).orElse(null);
		if (task == null) return setResponseJSON("{ \"Status\": 404, \"Message_Header\": \"NOTFOUND\", \"Message\": \"Unable to find Task.\" }");
		User user = userRepository.findByUUID(task.getTaskOwnerUUID());
		user.getTasks().remove(task);
		userRepository.save(user);
		SendMessageRequest send_msg_request = new SendMessageRequest()
				.withQueueUrl("https://sqs.us-west-2.amazonaws.com/793939683837/ikigai-email-sqs.fifo")
				.withMessageBody("COMPLETEDTASK;Task Completed!;" + user.getEmail() + ";" + task.toString())
				.withMessageGroupId("COMPLETEDTASK");
		sqs.sendMessage(send_msg_request);
		return setResponseJSON(
				"{ \"Status\": 200, \"Message_Header\": \"COMPLETED\", \"Message\": \"Task completed.\"}");
	}
	
	private String taskListToJson(List<Task> tasks) {
		StringBuilder sb = new StringBuilder("");
		
		for (Task task : tasks) {
			sb.append(new Gson().toJson(task).toString() + ", ");
		}
		if (sb.toString().length() > 0) sb = new StringBuilder(sb.toString().trim().substring(0, sb.toString().length() - 2));
		return sb.toString();
		
	}

	private String setResponseJSON(String content) {
		return new Gson().toJson(new JsonParser().parse(content).getAsJsonObject());
	}
}

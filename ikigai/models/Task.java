package lewis.trenton.ikigai.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Task {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String title;

	private String content;

	private String taskOwnerUUID;

	private String category;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTaskOwnerUUID() {
		return taskOwnerUUID;
	}

	public void setTaskOwnerUUID(String taskOwnerUUID) {
		this.taskOwnerUUID = taskOwnerUUID;
	}

	public String toJson() {
		return "{ \"title\": \"" + this.getTitle() + "\", \"category\": \"" + this.getCategory() + "\", \"content\": \"" + this.getContent() + "\"}";
	}
	
}

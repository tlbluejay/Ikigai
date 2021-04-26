package lewis.trenton.ikigai.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Beehive {
	
	@Id
	private String UUID;
	
	@Column(nullable = true)
	private String nickname;
	
	private String ownerUUID;
	
	private int beeCount;
	
	private String description;
	
	public Beehive() {}
	
	public Beehive(String uuid, String nickname, String ownerUUID, int beeCount, String description) {
		this.setUUID(uuid);
		this.setNickname(nickname);
		this.setOwnerUUID(ownerUUID);
		this.setBeeCount(beeCount);
		this.setDescription(description);
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String UUID) {
		this.UUID = UUID;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getOwnerUUID() {
		return ownerUUID;
	}

	public void setOwnerUUID(String ownerUUID) {
		this.ownerUUID = ownerUUID;
	}

	public int getBeeCount() {
		return beeCount;
	}

	public void setBeeCount(int beeCount) {
		this.beeCount = beeCount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}

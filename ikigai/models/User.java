package lewis.trenton.ikigai.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
public class User implements UserDetails {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	@Id
	private String UUID;
	
	private String email;
	
	@Column(nullable = false)
	private String username;
	
	@Column(nullable = false)
	private String password;
	
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> rawAuthorities = new ArrayList<>();
	
	@ElementCollection(fetch = FetchType.LAZY)
	private List<Task> tasks = new ArrayList<>();
	
	@Embedded
	private Beehive hive;
	
	private boolean isAdmin;
	
	public User() {}

	public User(String name, String email, 
			String username, String password, List<String> rawAuthorities,
			List<Task> tasks, Beehive hive, String UUID) {
		this.setName(name);
		this.setEmail(email);
		this.setUsername(username);
		this.setPassword(password);
		this.setHive(hive);
		this.setRawAuthorities(rawAuthorities);
		this.setTasks(tasks);
		this.setAdmin(rawAuthorities.contains("ROLE_ADMIN") ? true : false);
		
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public Beehive getHive() {
		return hive;
	}

	public void setHive(Beehive hive) {
		this.hive = hive;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getRawAuthorities() {
		return rawAuthorities;
	}

	public void setRawAuthorities(List<String> rawAuthorities) {
		this.rawAuthorities = rawAuthorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.getRawAuthorities().stream().map(r -> new GrantedAuthority() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getAuthority() {
				return r;
			}
		}).collect(Collectors.toList());
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return this.password;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.username;
	}
}

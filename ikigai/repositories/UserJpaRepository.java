package lewis.trenton.ikigai.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import lewis.trenton.ikigai.models.User;

public interface UserJpaRepository extends JpaRepository<User, String>{

	User findByUsername(String username);
	
	User findByUUID(String uuid);
	
	User findByEmail(String email);
	
}

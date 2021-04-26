package lewis.trenton.ikigai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lewis.trenton.ikigai.models.User;
import lewis.trenton.ikigai.repositories.UserJpaRepository;

public class IkigaiUserDetailsService implements UserDetailsService {

	@Autowired
	private UserJpaRepository repository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User u = repository.findByUsername(username);
		return u;
	}

}

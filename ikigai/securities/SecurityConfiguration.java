package lewis.trenton.ikigai.securities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lewis.trenton.ikigai.services.IkigaiUserDetailsService;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private IkigaiUserDetailsService userDetailsService;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth
			.userDetailsService(userDetailsService)
			.passwordEncoder(encoder);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
			.csrf().disable()
			.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers(HttpMethod.POST, "/users").permitAll()
				.antMatchers("/users/fetchUser").permitAll()
				.antMatchers("/tasks/fetch").permitAll()
				.antMatchers("/tasks/complete").permitAll()
				.antMatchers("/bees/fetch").permitAll()
				.antMatchers("/bees/grow").permitAll()
				.antMatchers("/bees/changeName").permitAll()
				.antMatchers("/users/**").permitAll()
				.antMatchers(HttpMethod.POST, "/tasks").permitAll()
				.anyRequest().hasAnyAuthority()
			.and()
			.httpBasic();
	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public IkigaiUserDetailsService ikigaiUserDetailsService() {
		return new IkigaiUserDetailsService();
	}
	
}

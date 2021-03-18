package com.fourthwoods.starter;

import com.fourthwoods.starter.authentication.AuthenticationFilter;
import com.fourthwoods.starter.authentication.CustomAuthenticationFailureHandler;
import com.fourthwoods.starter.authentication.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@SpringBootApplication
public class StarterApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(StarterApplication.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(StarterApplication.class);
  }

  @Configuration
  @EnableWebSecurity
  public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    final static Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public static String[] antPatterns = {
        "/",
        "/favicon.ico",
        "/webjars/**",
        "/js/**",
        "/css/**",
        "/login**"
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.authorizeRequests()
          .antMatchers(antPatterns).permitAll()
					.anyRequest()
						.authenticated()
          .and()
						.formLogin()
						.loginPage("/login")
						.permitAll()
            .failureHandler(customAuthenticationFailureHandler)
          .and()
						.logout()
            .logoutSuccessUrl("/?logout_successful")
						.permitAll();

      http.addFilterAfter(new AuthenticationFilter(), BasicAuthenticationFilter.class);
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    protected static class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
      private static String roleHierarchyDefinition =
              "ROLE_ADMIN > ROLE_MODERATOR\n" +
                "ROLE_MODERATOR > ROLE_USER\n" +

              "ROLE_OWNER > ROLE_EDITOR\n" +
                "ROLE_EDITOR > ROLE_USER\n" +

              "ROLE_USER > ROLE_GUEST ";

      @Autowired
      private AuthenticationProvider authenticationProvider;

      @Bean
      public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
      }

      @Bean
      public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();

        roleHierarchy.setHierarchy(roleHierarchyDefinition);

        return roleHierarchy;
      }

      @Bean
      @Autowired
      public RoleHierarchyVoter roleVoter(RoleHierarchy roleHierarchy) {
        return new RoleHierarchyVoter(roleHierarchy);
      }

      @Bean
      @Autowired
      public AuthenticationProvider authenticationProvider(UserService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
      }

      @Autowired
      public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
      }
    }
  }
}

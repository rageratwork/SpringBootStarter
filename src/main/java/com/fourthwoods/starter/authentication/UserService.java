package com.fourthwoods.starter.authentication;

import com.fourthwoods.starter.admin.UsernameAlreadyExistsException;
import com.fourthwoods.starter.authorization.Role;
import com.fourthwoods.starter.authorization.RoleService;
import com.fourthwoods.starter.utils.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
  final static Logger logger = LoggerFactory.getLogger(UserService.class);

  private UserRepository userRepository;
  private RoleService roleService;
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  public UserService(UserRepository userRepository, RoleService roleService, BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleService = roleService;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public User createUser(User user) throws UsernameAlreadyExistsException {
    User newUser = (User)getUserByUsername(user.getUsername());
    if(newUser != null) {
      throw new UsernameAlreadyExistsException("Username (" + user.getUsername() + ") already exists.");
    }

    newUser = new User();

    newUser.setId(IdGenerator.getId());
    newUser.setUsername(user.getUsername());
    newUser.setPassword(passwordEncoder.encode(user.getPassword()));
    newUser.setPasswordExpired(true);
    newUser.setFirstName(user.getFirstName());
    newUser.setLastName(user.getLastName());
    newUser.setEmail(user.getEmail());

    userRepository.createUser(newUser);

    List<Role> roles = new ArrayList<>();

    for(String role : user.getRoles()) {
      Role r = roleService.getRoleByRole(role);
      roles.add(r);
    }

    if(!roles.isEmpty()) {
      userRepository.updateUserRoles(newUser, roles);
    }

    return getUser(newUser.getId());
  }

  public User getUser(String id) {
    return userRepository.getUser(id);
  }

  public User getUserByUsername(String username) {
    return (User)loadUserByUsername(username);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = null;
    try {
      user = userRepository.getUserByUsername(username);
    } catch(EmptyResultDataAccessException ex) {
      logger.warn(ex.getMessage());
    }

    if(user == null) {
      String message = "Username not found: " + username;
      logger.warn(message);
      throw new UsernameNotFoundException(message);
    }

    return user;
  }

  public List<User> getUsers() {
    return userRepository.getUsers();
  }

  @Transactional
  public User updateUserPassword(User user, String newPassword) {
    User existingUser = userRepository.getUser(user.getId());

    String encodedPassword = passwordEncoder.encode(newPassword);
    existingUser.setPassword(encodedPassword);
    existingUser.setPasswordExpired(false);

    return userRepository.updateUser(existingUser);
  }

  @Transactional
  public User changePassword(User user, String oldPassword, String newPassword) throws IncorrectPasswordException {
    User existingUser = userRepository.getUser(user.getId());
    if(!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
      throw new IncorrectPasswordException("The user's password is incorrect.");
    }

    String encodedPassword = passwordEncoder.encode(newPassword);
    existingUser.setPassword(encodedPassword);
    existingUser.setPasswordExpired(false);

    return userRepository.updateUser(existingUser);
  }

  public static User getLoggedInUser() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Object principal = null;

      if (authentication != null) {
          principal = authentication.getPrincipal();
      }

      if (principal != null && principal instanceof User) {
          return (User)principal;
      }

      return null;
  }
}

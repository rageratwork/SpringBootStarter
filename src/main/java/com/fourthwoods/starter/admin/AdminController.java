package com.fourthwoods.starter.admin;

import com.fourthwoods.starter.authentication.User;
import com.fourthwoods.starter.authentication.UserService;
import com.fourthwoods.starter.authorization.Role;
import com.fourthwoods.starter.authorization.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {
  final static Logger logger = LoggerFactory.getLogger(AdminController.class);

  private static String TEMPLATE_LOCATION = "admin/";

  private UserService userService;
  private RoleService roleService;

  @Autowired
  public AdminController(UserService userService, RoleService roleService) {
    this.userService = userService;
    this.roleService = roleService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping(value={"", "/"}, method = RequestMethod.GET)
  public String index(Model model) {
    List<User> users = userService.getUsers();
    model.addAttribute("users", users);

    return TEMPLATE_LOCATION + "admin";
  }

  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping(value = "/create_user", method = RequestMethod.GET)
  public String createUser(Model model) {
    User user = new User();
    model.addAttribute("user", user);

    List<Role> roles = roleService.getRoles();
    model.addAttribute("roles", roles);

    return TEMPLATE_LOCATION + "create_user";
  }

  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping(value = "/create_user", method = RequestMethod.POST)
  public String createUser(@ModelAttribute("user") User user, Model model) {
    try {
      userService.createUser(user);
    } catch(UsernameAlreadyExistsException ex) {
      logger.warn("Cannot create user. " + ex.getMessage());

      List<Role> roles = roleService.getRoles();
      model.addAttribute("roles", roles);

      model.addAttribute("message", ex.getMessage());

      return TEMPLATE_LOCATION + "create_user";
    }

    return "redirect:/" + TEMPLATE_LOCATION;
  }
}

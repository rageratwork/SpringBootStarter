package com.fourthwoods.starter.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class LoginController {
    final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    private static String TEMPLATE_LOCATION = "authentication/";

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpServletRequest request, HttpServletResponse response, Model model){
        User user = UserService.getLoggedInUser();
        if(user != null) {
            return "redirect:/home";
        }

        if(request.getParameterMap().containsKey("bad_credentials")) {
            model.addAttribute("message", "Incorrect username or password.");
        }

        return TEMPLATE_LOCATION + "login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response){
        return TEMPLATE_LOCATION + "logout";
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/change_password", method = RequestMethod.GET)
    public String changePassword() {
        return TEMPLATE_LOCATION + "change_password";
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/change_password", method = RequestMethod.POST)
    public String changePassword(@RequestParam(value = "oldPassword") String oldPassword,
                                 @RequestParam(value = "newPassword") String newPassword,
                                 Model model) {
        User user = UserService.getLoggedInUser();

        try {
            User updatedUser = userService.changePassword(user, oldPassword, newPassword);
            user.setPassword(updatedUser.getPassword());
            user.setPasswordExpired(updatedUser.isPasswordExpired());
        } catch(IncorrectPasswordException ex) {
            // debating if I should send a redirect here instead like I do for BadCredentialsException
            model.addAttribute("message", ex.getMessage());

            return TEMPLATE_LOCATION + "change_password";
        }
        return "redirect:/home";
    }
}

package com.fourthwoods.starter.home;

import com.fourthwoods.starter.authentication.User;
import com.fourthwoods.starter.authentication.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {
    final static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping(value={"", "/"}, method = RequestMethod.GET)
    public String index() {
        User user = UserService.getLoggedInUser();
        if(user != null) {
            return "redirect:/home";
        }

        return "index";
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(HttpServletRequest request) throws Exception {
        return "home";
    }
}

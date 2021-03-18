package com.fourthwoods.starter.authentication;

import com.fourthwoods.starter.StarterApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.RequestPath;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthenticationFilter extends GenericFilterBean {
    final static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private String CHANGE_PASSWORD_PATH = "/change_password";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(!isUnauthenticatedUrl(request)) {
            User user = UserService.getLoggedInUser();
            if (user != null) {
                if(user.isPasswordExpired()) {
                    ((HttpServletResponse)response).sendRedirect(CHANGE_PASSWORD_PATH);
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isUnauthenticatedUrl(ServletRequest request) {
        List<String> patterns = new ArrayList();
        patterns.add(CHANGE_PASSWORD_PATH);
        patterns.addAll(Arrays.asList(StarterApplication.WebSecurityConfig.antPatterns));

        PathPatternParser parser = new PathPatternParser();
        RequestPath requestPath = ServletRequestPathUtils.parseAndCache((HttpServletRequest) request);
        for(String pattern : patterns) {
            PathPattern pathPattern = parser.parse(pattern);
            if(pathPattern.matches(requestPath)) {
                return true;
            }
        }
        return false;
    }
}

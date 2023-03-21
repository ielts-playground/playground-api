package org.ielts.playground.security;

import java.util.List;
import java.util.Optional;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.ielts.playground.security.filter.PrivateResourceFilter;
import org.ielts.playground.security.filter.ServletRequestChecker;
import org.ielts.playground.security.filter.JwtFilter;
import org.ielts.playground.security.filter.PathAuthenticable;
import org.ielts.playground.utils.PrivateResourceUtils;
import org.ielts.playground.utils.JwtUtils;

import lombok.Getter;
import lombok.Setter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties properties;

    protected final UserDetailsService userService;
    protected final PasswordEncoder passwordEncoder;
    protected final ServletRequestChecker requestChecker;

    public SecurityConfig(
            UserDetailsService userService,
            PasswordEncoder passwordEncoder,
            ServletRequestChecker requestChecker) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.requestChecker = requestChecker;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder);
    }

    protected void configureWithFilter(HttpSecurity http, Filter filter) throws Exception {
        final String[] whitelist = Optional.ofNullable(this.properties.getWhitelist())
                .map(list -> list.toArray(new String[list.size()]))
                .orElse(new String[] {});

        http.antMatcher(((PathAuthenticable) filter).getPath())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(whitelist).permitAll()
                .anyRequest().authenticated();

        http.csrf().disable().httpBasic();
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "server.security.authentication")
    public static class SecurityProperties {
        private List<String> whitelist;
    }

    @Order(1)
    @EnableWebSecurity
    public static class JwtConfig extends SecurityConfig {
        private final JwtUtils utils;

        public JwtConfig(
                UserDetailsService userService,
                PasswordEncoder passwordEncoder,
                ServletRequestChecker requestChecker,
                JwtUtils utils) {
            super(userService, passwordEncoder, requestChecker);
            this.utils = utils;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            this.configureWithFilter(http, new JwtFilter(requestChecker, utils, userService));
        }
    }

    @Order(2)
    @EnableWebSecurity
    public static class PrivateResourceConfig extends SecurityConfig {
        private final PrivateResourceUtils utils;

        public PrivateResourceConfig(
                UserDetailsService userService,
                PasswordEncoder passwordEncoder,
                ServletRequestChecker requestChecker,
                PrivateResourceUtils utils) {
            super(userService, passwordEncoder, requestChecker);
            this.utils = utils;

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            this.configureWithFilter(http, new PrivateResourceFilter(requestChecker, utils));
        }
    }
}

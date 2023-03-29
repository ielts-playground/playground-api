package org.ielts.playground.security;

import java.util.List;
import java.util.Optional;

import javax.servlet.Filter;

import org.ielts.playground.security.filter.DefaultSecurityFilter;
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
import org.ielts.playground.security.filter.PathAuthorizable;
import org.ielts.playground.utils.PrivateResourceUtils;
import org.ielts.playground.utils.JwtUtils;

import lombok.Getter;
import lombok.Setter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    protected final UserDetailsService userService;
    protected final PasswordEncoder passwordEncoder;
    protected final ServletRequestChecker requestChecker;
    private final SecurityProperties properties;

    public SecurityConfig(
            UserDetailsService userService,
            PasswordEncoder passwordEncoder,
            ServletRequestChecker requestChecker,
            SecurityProperties properties) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.requestChecker = requestChecker;
        this.properties = properties;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userService)
                .passwordEncoder(passwordEncoder);
    }

    protected void configureWithFilter(HttpSecurity http, Filter filter) throws Exception {
        final String[] whitelist = Optional.ofNullable(this.properties.getWhitelist())
                .map(list -> list.toArray(new String[0]))
                .orElse(new String[] {});

        http.antMatcher(((PathAuthorizable) filter).getPath())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(whitelist).permitAll()
                .anyRequest().authenticated();

        http.csrf().disable();
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
                SecurityProperties properties,
                JwtUtils utils) {
            super(userService, passwordEncoder, requestChecker, properties);
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
                SecurityProperties properties,
                PrivateResourceUtils utils) {
            super(userService, passwordEncoder, requestChecker, properties);
            this.utils = utils;

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            this.configureWithFilter(http, new PrivateResourceFilter(requestChecker, utils));
        }
    }

    @Order(3)
    @EnableWebSecurity
    public static class DefaultSecurityConfig extends SecurityConfig {
        public DefaultSecurityConfig(
                UserDetailsService userService,
                PasswordEncoder passwordEncoder,
                ServletRequestChecker requestChecker,
                SecurityProperties properties) {
            super(userService, passwordEncoder, requestChecker, properties);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            this.configureWithFilter(http, new DefaultSecurityFilter());
        }
    }
}

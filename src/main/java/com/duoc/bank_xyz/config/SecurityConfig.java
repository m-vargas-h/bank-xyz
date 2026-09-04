package com.duoc.bank_xyz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${bff.security.web.user}")
    private String webUser;
    @Value("${bff.security.web.password}")
    private String webPassword;

    @Value("${bff.security.mobile.user}")
    private String mobileUser;
    @Value("${bff.security.mobile.password}")
    private String mobilePassword;

    @Value("${bff.security.atm.user}")
    private String atmUser;
    @Value("${bff.security.atm.password}")
    private String atmPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
            User.withUsername(webUser)
                .password(encoder.encode(webPassword))
                .roles("WEB")
                .build(),
            User.withUsername(mobileUser)
                .password(encoder.encode(mobilePassword))
                .roles("MOBILE")
                .build(),
            User.withUsername(atmUser)
                .password(encoder.encode(atmPassword))
                .roles("ATM")
                .build()
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/web/**").hasRole("WEB")
                .requestMatchers("/mobile/**").hasRole("MOBILE")
                .requestMatchers("/atm/**").hasRole("ATM")
                .requestMatchers("/jobs/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {});
        return http.build();
    }
}
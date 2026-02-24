package com.example.mensfashionstore.config;

import com.example.mensfashionstore.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
                return (web) -> web.ignoring()
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/*.ico");
        }

        /**
         * @param http
         * @return
         * @throws Exception
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/products/**", "/product/**",
                                                                "/register", "/login")
                                                .permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/cart/**").permitAll()
                                                .requestMatchers("/order/**", "/user/**").hasRole("USER")
                                                .requestMatchers("/checkout", "/checkout-process").authenticated()
                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler((request, response, authentication) -> {
                                                        boolean isAdmin = authentication.getAuthorities().stream()
                                                                        .anyMatch(a -> a.getAuthority()
                                                                                        .equals("ROLE_ADMIN"));
                                                        if (isAdmin) {
                                                                response.sendRedirect("/admin/dashboard");
                                                        } else {
                                                                response.sendRedirect("/");
                                                        }
                                                })
                                                .failureUrl("/login?error=true")
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())

                                .sessionManagement(session -> session
                                                .sessionConcurrency(concurrency -> concurrency
                                                                .maximumSessions(1)
                                                                .expiredUrl("/login?expired")))

                                .csrf(Customizer.withDefaults())

                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; " +
                                                                                                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://checkout.razorpay.com; "
                                                                                                +
                                                                                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                                                                                +
                                                                                                "img-src 'self' data: https: http:; "
                                                                                                +
                                                                                                "font-src 'self' https://cdn.jsdelivr.net; "
                                                                                                +
                                                                                                "connect-src 'self' https://checkout.razorpay.com https://api.razorpay.com https://lumberjack.razorpay.com; "
                                                                                                +
                                                                                                "frame-src 'self' https://checkout.razorpay.com https://api.razorpay.com;")));

                http.authenticationProvider(authenticationProvider());

                return http.build();
        }

}

package org.agard.InventoryManagement.config;

import org.agard.InventoryManagement.controllers.CustomErrorController;
import org.agard.InventoryManagement.util.ViewNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.headers.FrameOptionsDsl;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.thymeleaf.spring6.expression.Mvc;

import java.util.function.Function;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@EnableWebSecurity
@EnableMethodSecurity()
@Configuration
public class SecurityConfig{


    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector){
        return new MvcRequestMatcher.Builder(introspector);
    }


    @Bean
    public SecurityFilterChain securityChain(HttpSecurity httpSecurity, MvcRequestMatcher.Builder mvc) throws Exception{
        httpSecurity.formLogin(Customizer.withDefaults());
        httpSecurity.authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(mvc.pattern("/attributes/**")).hasRole(UserRole.EDITOR.name)
                        .requestMatchers(mvc.pattern("/users/**")).hasRole(UserRole.ADMIN.name)
                        .requestMatchers(mvc.pattern("/styles/**")).permitAll()
                        .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                        .anyRequest().hasRole(UserRole.USER.name)
                )
                .formLogin((formLogin) -> formLogin
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers(antMatcher("/h2-console/**"))
                )
                .headers((headers) -> headers
                                .frameOptions((frameOptions) -> frameOptions.disable()) //for h2 console
                        )
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .accessDeniedPage(CustomErrorController.RESOURCE_DENIED_PATH)
                );
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy(){
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(UserRole.ADMIN.authority + " > " + UserRole.EDITOR.authority + " \n "
                                    + UserRole.EDITOR.authority + " > " + UserRole.USER.authority);
        return roleHierarchy;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(){
        DefaultMethodSecurityExpressionHandler mseh = new DefaultMethodSecurityExpressionHandler();
        mseh.setRoleHierarchy(roleHierarchy());
        return mseh;
    }

}



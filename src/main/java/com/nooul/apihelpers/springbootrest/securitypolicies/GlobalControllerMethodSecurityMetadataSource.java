package com.nooul.apihelpers.springbootrest.securitypolicies;


import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.method.AbstractFallbackMethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.access.annotation.Jsr250SecurityConfig.DENY_ALL_ATTRIBUTE;

// based on https://www.baeldung.com/spring-deny-access
/* you need to add this configuration

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
// https://www.baeldung.com/spring-deny-access
public class DenyAllMethodsSecurityConfig extends GlobalMethodSecurityConfiguration {
    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
        return new GlobalControllerMethodSecurityMetadataSource();
    }

}

and change your web security to be similar to:

@Configuration
//based on: https://auth0.com/blog/implementing-jwt-authentication-on-spring-boot/
//and https://www.devglan.com/spring-security/jwt-role-based-authorization
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private PasswordEncoderProvider passwordEncoderProvider;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    public void globalUserDetails(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(globalUserDetailsService)
                .passwordEncoder(passwordEncoderProvider.getEncoder());
    }


    @Bean
    public JwtAuthenticationFilter authenticationTokenFilterBean() {
        return new JwtAuthenticationFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                headers()
                    .frameOptions().disable().and()
            .cors().and().csrf().disable()
              .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
    }
 }

 */


public class GlobalControllerMethodSecurityMetadataSource
        extends AbstractFallbackMethodSecurityMetadataSource {
    @Override
    protected Collection findAttributes(Class<?> clazz) { return null; }

    @Override
    protected Collection findAttributes(Method method, Class<?> targetClass) {
        if (isControllerAnnotatedWithPrePostAuthorize(targetClass)) {
            return null;
        }
        if (isMethodAnnotatedWithPrePostAuthorize(method)) {
            return null;
        }
        return addDenyAllToMethodAttributesOfClass(targetClass);
    }

    private boolean isControllerAnnotatedWithPrePostAuthorize(Class<?> targetClass) {
        Annotation[] classAnnotations = targetClass.getAnnotations();
        if (AnnotationUtils.findAnnotation(targetClass, Controller.class) != null && classAnnotations != null) {
            for (Annotation a : classAnnotations) {
                if (a instanceof PreAuthorize || a instanceof PostAuthorize) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMethodAnnotatedWithPrePostAuthorize(Method method) {
        Annotation[] methodAnnotations = method.getAnnotations();
        if (methodAnnotations != null) {
            for (Annotation a : methodAnnotations) {
                // but not if the method has at least a PreAuthorize or PostAuthorize annotation
                if (a instanceof PreAuthorize || a instanceof PostAuthorize) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set addDenyAllToMethodAttributesOfClass(Class<?> targetClass) {
        Set attributes = new HashSet<>();
        if (AnnotationUtils.findAnnotation(targetClass, Controller.class) != null) {
            attributes.add(DENY_ALL_ATTRIBUTE);
        }
        return attributes;
    }



    @Override
    public Collection getAllConfigAttributes() { return null; }
}
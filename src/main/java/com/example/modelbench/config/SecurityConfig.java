package com.example.modelbench.config;

import com.example.modelbench.exception.GestionnaireErreursSecurite;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String secret;
    private final GestionnaireErreursSecurite gestionnaireErreurs;

    public SecurityConfig(@Value("${security.jwt.secret}") String secret,
                          GestionnaireErreursSecurite gestionnaireErreurs) {
        this.secret = secret;
        this.gestionnaireErreurs = gestionnaireErreurs;
    }

    @Bean
    public PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder();
    }

    private SecretKeySpec cleSymetrique() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder encodeurJeton() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(cleSymetrique()));
    }

    @Bean
    public JwtDecoder decodeurJeton() {
        return NimbusJwtDecoder.withSecretKey(cleSymetrique())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Transforme la revendication "roles" du jeton en autorites prefixees ROLE_, ce qui rend
     * hasRole("ADMIN") operant. Sans cela, Spring Security lirait la revendication "scope".
     */
    @Bean
    public JwtAuthenticationConverter convertisseurJeton() {
        JwtGrantedAuthoritiesConverter convertisseurAutorites = new JwtGrantedAuthoritiesConverter();
        convertisseurAutorites.setAuthoritiesClaimName("roles");
        convertisseurAutorites.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertisseur = new JwtAuthenticationConverter();
        convertisseur.setJwtGrantedAuthoritiesConverter(convertisseurAutorites);
        return convertisseur;
    }

    @Bean
    public SecurityFilterChain chaineDeFiltres(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(entetes -> entetes.frameOptions(cadre -> cadre.sameOrigin()))
                .authorizeHttpRequests(requetes -> requetes
                        .requestMatchers(
                                "/api/auth/login",
                                "/v3/api-docs/**",
                                "/swagger",
                                "/swagger/**",
                                "/swagger-ui/**",
                                "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(gestion -> gestion
                        .authenticationEntryPoint(gestionnaireErreurs)
                        .accessDeniedHandler(gestionnaireErreurs))
                .oauth2ResourceServer(serveur -> serveur
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertisseurJeton()))
                        .authenticationEntryPoint(gestionnaireErreurs));

        return http.build();
    }
}

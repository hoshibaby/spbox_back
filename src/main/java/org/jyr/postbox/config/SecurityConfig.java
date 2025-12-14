package org.jyr.postbox.config;

import lombok.RequiredArgsConstructor;
import org.jyr.postbox.repository.UserRepository;
import org.jyr.postbox.security.JwtAuthenticationFilter;
import org.jyr.postbox.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static org.springframework.http.HttpMethod.OPTIONS;

import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(OPTIONS, "/**").permitAll()

                        // ✅ 로그인/회원가입
                        .requestMatchers("/api/auth/**").permitAll()

                        // ✅ 공개 박스 조회 (방문자 페이지용)
                        .requestMatchers(GET, "/api/boxes/**").permitAll()

                        // ✅ 공개 박스에 메시지 작성 (서버에서 allowAnonymous로 다시 검증)
                        .requestMatchers(POST, "/api/message").permitAll()

                        // ✅ 관리자
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ✅ 그 외는 인증 필요 (me 포함)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 프론트 주소(Vite)
        config.addAllowedOrigin("http://localhost:5173");

        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        // 쿠키/인증정보 허용 여부 (보통 토큰 방식이라도 true 해두면 안전)
        config.setAllowCredentials(true);

        // 프론트에서 Authorization 헤더를 읽을 때 필요할 수 있음
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}

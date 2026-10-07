package br.com.sgf.seguranca.controller;

import br.com.sgf.seguranca.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Exige `Authorization: Bearer <token>` em /api/**, exceto login e fotos. */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String ATRIBUTO_USUARIO = "usuarioId";
    public static final String ATRIBUTO_TOKEN = "token";

    private final AuthService auth;

    public AuthInterceptor(AuthService auth) {
        this.auth = auth;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(req.getMethod())) return true;
        String cabecalho = req.getHeader("Authorization");
        String token = cabecalho != null && cabecalho.startsWith("Bearer ") ? cabecalho.substring(7) : null;
        var usuario = auth.trabalhadorDaSessao(token);
        if (usuario.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"erros\":[\"Sessão expirada. Entre novamente.\"]}");
            return false;
        }
        req.setAttribute(ATRIBUTO_USUARIO, usuario.get());
        req.setAttribute(ATRIBUTO_TOKEN, token);
        return true;
    }

    @Configuration
    static class Registro implements WebMvcConfigurer {
        private final AuthInterceptor interceptor;

        Registro(AuthInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/login", "/api/login/opcoes", "/api/fotos/**");
        }
    }
}

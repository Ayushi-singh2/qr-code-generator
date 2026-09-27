package com.example.qrgenerator.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards browser-navigated SPA routes (e.g. /dashboard, /history, /profile)
 * to index.html so React Router can take over client-side, instead of
 * returning a 404 on a direct page load or refresh.
 *
 * Deliberately excludes /api/** (handled by REST controllers) and any path
 * containing a dot (static assets like /assets/app.js, /favicon.ico).
 */
@Controller
public class SpaForwardController {

    @RequestMapping({
            "/",
            "/login",
            "/register",
            "/dashboard",
            "/history",
            "/profile",
            "/{path:^(?!api|assets)[^\\.]*}",
            "/{path:^(?!api|assets)[^\\.]*}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}

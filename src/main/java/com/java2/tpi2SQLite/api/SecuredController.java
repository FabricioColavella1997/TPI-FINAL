package com.java2.tpi2SQLite.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecuredController {
    @GetMapping("/secured-all")
    public String securedAll(Authentication authentication) {
        String username = authentication.getName();
        return "Welcome " + username + ".";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/secured-admin")
    public String securedAdmin(Authentication authentication) {
        String username = authentication.getName();
        return "Welcome ADMIN " + username + ".";
    }
}

package com.java2.tpi2SQLite;

import com.java2.tpi2SQLite.api.SecuredController;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecuredControllerTest {

    @Test
    void testSecuredAll() {
        SecuredController controller = new SecuredController();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        String result = controller.securedAll(auth);

        assertEquals("Welcome user1.", result);
    }

    @Test
    void testSecuredAdmin() {
        SecuredController controller = new SecuredController();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin1");

        String result = controller.securedAdmin(auth);

        assertEquals("Welcome ADMIN admin1.", result);
    }
}
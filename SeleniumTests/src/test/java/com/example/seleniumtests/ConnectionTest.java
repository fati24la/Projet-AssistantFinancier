package com.example.seleniumtests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {
    @Test
    public void testConnexionSuccess() {
        String result = Connection.testConnexionSuccess("admin", "123456");
        Assertions.assertEquals("Tableau de bord", result);
    }

    @Test
    public void testConnectionWrongPassword() {
        String result = Connection.testConnectionWrongPassword("admin", "wrongPassword");
        Assertions.assertEquals("Invalid credentials", result);
    }

    @Test
    public void testConnectionEmptyFields() {
        boolean result = Connection.testConnectionEmptyFields();
        Assertions.assertFalse(result);
    }
}
package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    @Test
    void testCreateTables() {
        assertDoesNotThrow(Database::createTables);
    }

    @Test
    void testSaveJourney() {
        Database.createTables();

        boolean result = Database.saveJourney(100, 2);

        assertTrue(result);
    }

    @Test
    void testGetJourneys() {
        Database.createTables();

        Database.saveJourney(200, 4);

        assertFalse(Database.getJourneys().isEmpty());
    }
}
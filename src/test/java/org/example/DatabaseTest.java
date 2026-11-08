package org.example;

import org.example.database.UnitDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {
    @BeforeEach
    void initializeIsolatedDatabase() {
        System.setProperty("app.db.url", "jdbc:h2:mem:test_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        Database.createTables();
    }

    @Test
    void initializesSchemaAndSeededLocalizedUnits() throws Exception {
        assertEquals(3, new UnitDAO().getUnitsByCategory("fi", "temperature").size());
        assertEquals("Celsius", new UnitDAO().getUnitsByCategory("unknown", "temperature").get(0).getName());
    }

    @Test
    void savesJourneyAndRejectsInvalidValues() {
        assertTrue(Database.saveJourney(100, 2));
        assertEquals(1, Database.getJourneys().size());
        assertFalse(Database.saveJourney(0, 2));
        assertFalse(Database.saveJourney(10, Double.NaN));
    }

    @Test
    void storesConversionAndDisplaysUnitNamesInSelectedLanguage() {
        assertTrue(Database.saveConversion(32, "fahrenheit", "celsius", 0, "en"));
        assertTrue(Database.getConversionHistory("fi").get(0).contains("Fahrenheit"));
        assertTrue(Database.getConversionHistory("fi").get(0).contains("Celsius"));
        assertFalse(Database.saveConversion(10, "not-a-unit", "celsius", 2, "en"));
        assertEquals(1, Database.getConversionHistory("en").size());
    }
}

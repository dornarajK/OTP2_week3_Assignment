package org.example;

import org.example.database.UnitDAO;
import org.example.model.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitAndDaoTest {
    @BeforeEach
    void initializeDatabase() {
        System.setProperty("app.db.url", "jdbc:h2:mem:units_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        Database.createTables();
    }

    @Test
    void unitExposesAllFieldsAndReadableString() {
        Unit unit = new Unit(17, "celsius", "temperature", "°C", "Celsius", "Temperature unit");

        assertEquals(17, unit.getUnitId());
        assertEquals("celsius", unit.getUnitKey());
        assertEquals("temperature", unit.getCategory());
        assertEquals("°C", unit.getSymbol());
        assertEquals("Celsius", unit.getName());
        assertEquals("Temperature unit", unit.getDescription());
        assertEquals("Celsius (°C)", unit.toString());
    }

    @Test
    void daoLoadsLocalizedUnitsAndFiltersByCategory() throws Exception {
        UnitDAO dao = new UnitDAO();
        List<Unit> allFinnish = dao.getUnits("fi");
        List<Unit> temperature = dao.getUnitsByCategory("ja", "temperature");
        List<Unit> missingCategory = dao.getUnitsByCategory("en", "not-a-category");

        assertEquals(9, allFinnish.size());
        assertEquals("Celsius", allFinnish.get(0).getName());
        assertTrue(allFinnish.get(0).getDescription().startsWith("Lämpötila"));
        assertEquals(3, temperature.size());
        assertEquals("摂氏", temperature.get(0).getName());
        assertTrue(missingCategory.isEmpty());
    }
}

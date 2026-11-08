package org.example.database;



import org.example.Database;
import org.example.model.Unit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UnitDAO {

    // Get all units in the selected language
    public List<Unit> getUnits(String languageCode) throws Exception {

        List<Unit> units = new ArrayList<>();

        String sql = """
            SELECT
                u.unit_id,
                u.unit_key,
                u.category,
                u.symbol,
                COALESCE(t.name, en.name) AS name,
                COALESCE(t.description, en.description) AS description

            FROM unit u

            LEFT JOIN unit_translation t
                ON u.unit_id = t.unit_id
                AND t.language_code = ?

            LEFT JOIN unit_translation en
                ON u.unit_id = en.unit_id
                AND en.language_code = 'en'

            ORDER BY u.unit_id
            """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, languageCode);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Unit unit = new Unit(
                            result.getInt("unit_id"),
                            result.getString("unit_key"),
                            result.getString("category"),
                            result.getString("symbol"),
                            result.getString("name"),
                            result.getString("description")
                    );

                    units.add(unit);
                }
            }
        }

        return units;
    }

    // Get units belonging to a specific category
    public List<Unit> getUnitsByCategory(
            String languageCode, String category) throws Exception {

        List<Unit> units = new ArrayList<>();

        String sql = """
            SELECT
                u.unit_id,
                u.unit_key,
                u.category,
                u.symbol,
                COALESCE(t.name, en.name) AS name,
                COALESCE(t.description, en.description) AS description

            FROM unit u

            LEFT JOIN unit_translation t
                ON u.unit_id = t.unit_id
                AND t.language_code = ?

            LEFT JOIN unit_translation en
                ON u.unit_id = en.unit_id
                AND en.language_code = 'en'

            WHERE u.category = ?

            ORDER BY u.unit_id
            """;

        try (Connection connection = Database.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, languageCode);
            statement.setString(2, category);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Unit unit = new Unit(
                            result.getInt("unit_id"),
                            result.getString("unit_key"),
                            result.getString("category"),
                            result.getString("symbol"),
                            result.getString("name"),
                            result.getString("description")
                    );

                    units.add(unit);
                }
            }
        }

        return units;
    }
}

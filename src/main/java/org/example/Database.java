package org.example;

import org.h2.tools.RunScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Database {
    private static final String DEFAULT_URL = "jdbc:h2:./temperaturedb";
    private static final DateTimeFormatter HISTORY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Database() { }

    public static Connection connect() throws Exception {
        String url = System.getProperty("app.db.url");
        if (url == null || url.isBlank()) {
            url = System.getenv().getOrDefault("APP_DB_URL", DEFAULT_URL);
        }
        return DriverManager.getConnection(url);
    }

    public static void createTables() {
        try (Connection connection = connect()) {
            InputStream input = Database.class.getResourceAsStream("/init.sql");
            if (input == null) {
                throw new IllegalStateException("init.sql not found on the classpath");
            }
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                RunScript.execute(connection, reader);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize database", e);
        }
    }

    public static boolean saveJourney(double distance, double travelTime) {
        if (!Double.isFinite(distance) || !Double.isFinite(travelTime)
                || distance <= 0 || travelTime <= 0) {
            return false;
        }
        String insertJourney = "INSERT INTO journey DEFAULT VALUES";
        String insertDetails = "INSERT INTO journey_details "
                + "(journey_id, distance, travel_time, speed) VALUES (?, ?, ?, ?)";
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                int journeyId;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertJourney, Statement.RETURN_GENERATED_KEYS)) {
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new IllegalStateException("No journey ID was generated");
                        }
                        journeyId = keys.getInt(1);
                    }
                }
                try (PreparedStatement details = connection.prepareStatement(insertDetails)) {
                    details.setInt(1, journeyId);
                    details.setDouble(2, distance);
                    details.setDouble(3, travelTime);
                    details.setDouble(4, distance / travelTime);
                    details.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getJourneys() {
        String sql = "SELECT j.id, j.journey_date, d.distance, d.travel_time, d.speed "
                + "FROM journey j JOIN journey_details d ON j.id = d.journey_id "
                + "ORDER BY j.id DESC";
        List<String> journeys = new ArrayList<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                journeys.add("ID: " + result.getInt("id")
                        + " | Date: " + result.getDate("journey_date")
                        + " | Distance: " + result.getDouble("distance") + " km"
                        + " | Time: " + result.getDouble("travel_time") + " hours"
                        + " | Speed: " + result.getDouble("speed") + " km/h");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return journeys;
    }

    public static boolean saveConversion(double input, String fromUnitKey,
                                         String toUnitKey, double result,
                                         String languageCode) {
        if (!Double.isFinite(input) || !Double.isFinite(result)
                || fromUnitKey == null || toUnitKey == null || languageCode == null) {
            return false;
        }
        String sql = "INSERT INTO conversion_history "
                + "(input_value, from_unit_id, to_unit_id, result_value, language_code) "
                + "SELECT ?, f.unit_id, t.unit_id, ?, ? FROM unit f, unit t "
                + "WHERE f.unit_key = ? AND t.unit_key = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, input);
            statement.setDouble(2, result);
            statement.setString(3, languageCode);
            statement.setString(4, fromUnitKey);
            statement.setString(5, toUnitKey);
            return statement.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getConversionHistory(String languageCode) {
        String sql = "SELECT h.input_value, h.result_value, h.converted_at, "
                + "COALESCE(tf.name, ef.name) AS from_name, "
                + "COALESCE(tt.name, et.name) AS to_name "
                + "FROM conversion_history h "
                + "JOIN unit uf ON h.from_unit_id = uf.unit_id "
                + "JOIN unit ut ON h.to_unit_id = ut.unit_id "
                + "LEFT JOIN unit_translation tf ON uf.unit_id = tf.unit_id AND tf.language_code = ? "
                + "LEFT JOIN unit_translation ef ON uf.unit_id = ef.unit_id AND ef.language_code = 'en' "
                + "LEFT JOIN unit_translation tt ON ut.unit_id = tt.unit_id AND tt.language_code = ? "
                + "LEFT JOIN unit_translation et ON ut.unit_id = et.unit_id AND et.language_code = 'en' "
                + "ORDER BY h.converted_at DESC, h.id DESC";
        List<String> history = new ArrayList<>();
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, languageCode);
            statement.setString(2, languageCode);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    history.add(HISTORY_DATE_FORMAT.format(
                            result.getTimestamp("converted_at").toLocalDateTime())
                            + " | " + result.getDouble("input_value") + " "
                            + result.getString("from_name") + " → "
                            + result.getDouble("result_value") + " "
                            + result.getString("to_name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }
}

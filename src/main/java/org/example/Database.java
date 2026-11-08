package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
public class Database {
    public static List<String> getJourneys() {

        List<String> journeys = new ArrayList<>();

        String sql = """
            SELECT journey.id, journey.journey_date,
                   journey_details.distance,
                   journey_details.travel_time,
                   journey_details.speed
            FROM journey
            JOIN journey_details
            ON journey.id = journey_details.journey_id
            """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                String journey = "ID: " + result.getInt("id")
                        + " | Date: " + result.getDate("journey_date")
                        + " | Distance: " + result.getDouble("distance") + " km"
                        + " | Time: " + result.getDouble("travel_time") + " hours"
                        + " | Speed: " + result.getDouble("speed") + " km/h";

                journeys.add(journey);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return journeys;
    }
    private static final String URL = "jdbc:h2:./temperaturedb";

    public static Connection connect() throws Exception {
        return DriverManager.getConnection(URL);
    }

    public static void createTables() {

        String journeyTable = """
                CREATE TABLE IF NOT EXISTS journey (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    journey_date DATE DEFAULT CURRENT_DATE
                )
                """;

        String detailsTable = """
                CREATE TABLE IF NOT EXISTS journey_details (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    journey_id INT,
                    distance DOUBLE,
                  
                    travel_time DOUBLE,
                    speed DOUBLE,
                    FOREIGN KEY (journey_id) REFERENCES journey(id)
                )
                """;

        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute(journeyTable);
            statement.execute(detailsTable);

            System.out.println("Database tables created!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean saveJourney(double distance, double travelTime) {

        String insertJourney = "INSERT INTO journey DEFAULT VALUES";

        String insertDetails = """
            INSERT INTO journey_details
            (journey_id, distance, travel_time, speed)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection connection = connect()) {

            // Create journey
            var statement = connection.prepareStatement(
                    insertJourney,
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );

            statement.executeUpdate();

            var keys = statement.getGeneratedKeys();
            keys.next();

            int journeyId = keys.getInt(1);

            // Calculate speed
            double speed = distance / travelTime;

            // Save journey details
            var details = connection.prepareStatement(insertDetails);

            details.setInt(1, journeyId);
            details.setDouble(2, distance);
            details.setDouble(3, travelTime);
            details.setDouble(4, speed);

            details.executeUpdate();

            System.out.println("Journey saved!");

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
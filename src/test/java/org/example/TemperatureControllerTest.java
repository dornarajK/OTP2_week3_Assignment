package org.example;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemperatureControllerTest {
    private static boolean toolkitStarted;

    @BeforeAll
    static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            toolkitStarted = started.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            toolkitStarted = true;
        }
        assertTrue(toolkitStarted, "JavaFX toolkit did not start");
        Platform.setImplicitExit(false);
    }

    @BeforeEach
    void initializeDatabase() {
        System.setProperty("app.db.url", "jdbc:h2:mem:controller_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        Database.createTables();
    }

    @Test
    void initializesAndSwitchesAllSupportedLanguages() throws Exception {
        onFxThread(() -> {
        FxFixture fx = createFixture();
        assertEquals(5, fx.languageBox.getItems().size());
        assertEquals(3, fx.conversionBox.getItems().size());
        assertEquals("English", fx.languageBox.getValue());

        for (String language : new String[]{"Finnish", "Swedish", "Japanese", "Arabic", "English"}) {
            fx.languageBox.setValue(language);
            fx.languageBox.getOnAction().handle(null);
            assertEquals(3, fx.conversionBox.getItems().size());
        }
        assertEquals(javafx.geometry.NodeOrientation.LEFT_TO_RIGHT, fx.root.getNodeOrientation());
        fx.languageBox.setValue("Arabic");
        fx.languageBox.getOnAction().handle(null);
        assertEquals(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT, fx.root.getNodeOrientation());
        return null;
        });
    }

    @Test
    void convertsAllConfiguredTemperaturePairsAndHandlesInvalidInput() throws Exception {
        onFxThread(() -> {
        FxFixture fx = createFixture();
        fx.temperatureInput.setText("32");
        fx.convertButton.fire();
        assertTrue(fx.resultLabel.getText().contains("0"));
        assertEquals(1, fx.history.getItems().size());

        fx.conversionBox.getSelectionModel().select(1);
        fx.temperatureInput.setText("100");
        fx.convertButton.fire();
        assertTrue(fx.resultLabel.getText().contains("212"));

        fx.conversionBox.getSelectionModel().select(2);
        fx.temperatureInput.setText("273.15");
        fx.convertButton.fire();
        assertTrue(fx.resultLabel.getText().contains("0"));

        fx.temperatureInput.setText("not a number");
        fx.convertButton.fire();
        assertTrue(fx.resultLabel.getText().contains("valid"));
        fx.temperatureInput.setText("NaN");
        fx.convertButton.fire();
        assertTrue(fx.resultLabel.getText().contains("valid"));
        fx.temperatureInput.setText("10");
        fx.conversionBox.setValue(null);
        fx.convertButton.fire();
        assertFalse(fx.resultLabel.getText().isEmpty());
        return null;
        });
    }

    @Test
    void validatesAndSavesJourneyInputs() throws Exception {
        onFxThread(() -> {
        FxFixture fx = createFixture();
        fx.distanceInput.setText("abc");
        fx.travelTimeInput.setText("2");
        fx.saveJourneyButton.fire();
        assertTrue(fx.databaseResultLabel.getText().toLowerCase(Locale.ROOT).contains("valid"));

        fx.distanceInput.setText("0");
        fx.travelTimeInput.setText("2");
        fx.saveJourneyButton.fire();
        assertFalse(fx.databaseResultLabel.getText().isEmpty());

        fx.distanceInput.setText("100");
        fx.travelTimeInput.setText("2");
        fx.saveJourneyButton.fire();
        assertTrue(fx.databaseResultLabel.getText().length() > 0);
        assertTrue(fx.distanceInput.getText().isEmpty());
        assertTrue(fx.travelTimeInput.getText().isEmpty());
        assertEquals(1, fx.journeys.getItems().size());
        return null;
        });
    }

    private FxFixture createFixture() throws Exception {
        return onFxThread(() -> {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/temperature-view.fxml"),
                    ResourceBundle.getBundle("MessagesBundle", Locale.US));
            Parent ignored = loader.load();
            TemperatureController controller = loader.getController();
            return new FxFixture(field(controller, "languageBox"), field(controller, "conversionBox"),
                    field(controller, "root"), field(controller, "temperatureInput"),
                    field(controller, "convertButton"), field(controller, "resultLabel"),
                    field(controller, "conversionHistoryListView"), field(controller, "distanceInput"),
                    field(controller, "travelTimeInput"), field(controller, "saveJourneyButton"),
                    field(controller, "databaseResultLabel"), field(controller, "journeyListView"));
        });
    }

    private static <T> T field(Object controller, String name) throws ReflectiveOperationException {
        var field = TemperatureController.class.getDeclaredField(name);
        field.setAccessible(true);
        @SuppressWarnings("unchecked") T value = (T) field.get(controller);
        return value;
    }

    private static <T> T onFxThread(Callable<T> action) throws Exception {
        if (Platform.isFxApplicationThread()) return action.call();
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }

    private record FxFixture(ComboBox<String> languageBox,
                             ComboBox<?> conversionBox,
                             VBox root,
                             TextField temperatureInput,
                             Button convertButton,
                             Label resultLabel,
                             ListView<String> history,
                             TextField distanceInput,
                             TextField travelTimeInput,
                             Button saveJourneyButton,
                             Label databaseResultLabel,
                             ListView<String> journeys) { }
}

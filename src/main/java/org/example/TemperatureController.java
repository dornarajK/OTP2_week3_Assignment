package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class TemperatureController {

    @FXML private ComboBox<String> languageBox;
    @FXML private ComboBox<String> conversionBox;
    @FXML private Label appTitleLabel;
    @FXML private Label languageLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label conversionLabel;
    @FXML private Label resultLabel;
    @FXML private TextField temperatureInput;
    @FXML private Button convertButton;
    @FXML private TextField distanceInput;
    @FXML private ListView<String> journeyListView;
    @FXML private TextField travelTimeInput;
    @FXML private void loadJourneys() {
        journeyListView.getItems().clear();
        journeyListView.getItems().addAll(Database.getJourneys());
    }
    @FXML private Label databaseResultLabel;
    private ResourceBundle messages;
    private final TemperatureConverter converter = new TemperatureConverter();


    private static final String F_TO_C = "Fahrenheit → Celsius";
    private static final String C_TO_F = "Celsius → Fahrenheit";
    private static final String K_TO_C = "Kelvin → Celsius";

    @FXML
    public void initialize() {
        languageBox.getItems().addAll("English", "Finnish", "Swedish", "Japanese", "Arabic");
        languageBox.setValue("English");
        languageBox.setOnAction(e -> changeLanguage());

        conversionBox.getItems().addAll(F_TO_C, C_TO_F, K_TO_C);
        conversionBox.setValue(F_TO_C);

        changeLanguage();
        loadJourneys();
    }

    private void changeLanguage() {
        Locale locale = switch (languageBox.getValue()) {
            case "Finnish" -> new Locale("fi", "FI");
            case "Swedish" -> new Locale("sv", "SE");
            case "Japanese" -> new Locale("ja", "JP");
            case "Arabic" -> new Locale("ar", "SA");
            default -> new Locale("en", "US");
        };

        try {
            messages = ResourceBundle.getBundle("MessagesBundle", locale);
        } catch (Exception e) {

            resultLabel.setText("Invalid choice, using English.");
            messages = ResourceBundle.getBundle("MessagesBundle", Locale.US);
        }

        appTitleLabel.setText(messages.getString("app.title"));
        languageLabel.setText(messages.getString("select.language"));
        conversionLabel.setText(messages.getString("choice.prompt"));
        temperatureLabel.setText(messages.getString("temperature.label"));
        temperatureInput.setPromptText(messages.getString("temperature.prompt"));
        convertButton.setText(messages.getString("convert.button"));
        resultLabel.setText(messages.getString("result.placeholder"));
    }

    @FXML
    private void saveJourney() {

        try {
            double distance = Double.parseDouble(distanceInput.getText());
            double travelTime = Double.parseDouble(travelTimeInput.getText());

            if (distance <= 0 || travelTime <= 0) {
                databaseResultLabel.setText("Values must be greater than 0.");
                return;
            }

            boolean saved = Database.saveJourney(distance, travelTime);

            if (saved) {
                databaseResultLabel.setText("Journey saved!");
                distanceInput.clear();
                travelTimeInput.clear();
                loadJourneys();
            }else {
                databaseResultLabel.setText("Could not save journey.");
            }

        } catch (NumberFormatException e) {
            databaseResultLabel.setText("Please enter valid numbers.");
        }
    }

    @FXML
    private void handleConvert() {
        try {
            double input = Double.parseDouble(temperatureInput.getText().trim());
            double output;
            String templateKey;

            switch (conversionBox.getValue()) {
                case F_TO_C -> {
                    output = converter.fahrenheitToCelsius(input);
                    templateKey = "result.fahrenheitToCelsius";
                }
                case C_TO_F -> {
                    output = converter.celsiusToFahrenheit(input);
                    templateKey = "result.celsiusToFahrenheit";
                }
                case K_TO_C -> {
                    output = converter.kelvinToCelsius(input);
                    templateKey = "result.kelvinToCelsius";
                }
                default -> throw new IllegalStateException("Unknown conversion");
            }


            String template = messages.getString(templateKey);
            resultLabel.setText(MessageFormat.format(template, input, output));

        } catch (NumberFormatException e) {
            resultLabel.setText(messages.getString("result.invalid"));
        }
    }
}
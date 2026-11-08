package org.example;

import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.database.UnitDAO;
import org.example.model.Unit;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class TemperatureController {
    @FXML private ComboBox<String> languageBox;
    @FXML private ComboBox<ConversionOption> conversionBox;
    @FXML private VBox root;
    @FXML private Label appTitleLabel;
    @FXML private Label languageLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label conversionLabel;
    @FXML private Label resultLabel;
    @FXML private TextField temperatureInput;
    @FXML private Button convertButton;
    @FXML private Label historyLabel;
    @FXML private ListView<String> conversionHistoryListView;
    @FXML private Label journeyLabel;
    @FXML private Label journeyInputLabel;
    @FXML private Button saveJourneyButton;
    @FXML private TextField distanceInput;
    @FXML private ListView<String> journeyListView;
    @FXML private TextField travelTimeInput;
    @FXML private Label databaseResultLabel;

    private final UnitDAO unitDAO = new UnitDAO();
    private final TemperatureConverter converter = new TemperatureConverter();
    private String currentLanguageCode = "en";
    private ResourceBundle messages;

    @FXML
    public void initialize() {
        languageBox.getItems().addAll("English", "Finnish", "Swedish", "Japanese", "Arabic");
        languageBox.setValue("English");
        languageBox.setOnAction(event -> changeLanguage());
        changeLanguage();
        loadJourneys();
    }

    private void loadConversionOptions() {
        try {
            List<Unit> units = unitDAO.getUnitsByCategory(currentLanguageCode, "temperature");
            conversionBox.getItems().clear();
            Unit celsius = null;
            Unit fahrenheit = null;
            Unit kelvin = null;
            for (Unit unit : units) {
                switch (unit.getUnitKey()) {
                    case "celsius" -> celsius = unit;
                    case "fahrenheit" -> fahrenheit = unit;
                    case "kelvin" -> kelvin = unit;
                    default -> { }
                }
            }
            if (celsius != null && fahrenheit != null && kelvin != null) {
                conversionBox.getItems().addAll(new ConversionOption(fahrenheit, celsius),
                        new ConversionOption(celsius, fahrenheit), new ConversionOption(kelvin, celsius));
                conversionBox.setValue(conversionBox.getItems().get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultLabel.setText("Could not load units from database.");
        }
    }

    private static final class ConversionOption {
        private final Unit from;
        private final Unit to;

        private ConversionOption(Unit from, Unit to) {
            this.from = from;
            this.to = to;
        }

        private Unit getFrom() { return from; }
        private Unit getTo() { return to; }

        @Override
        public String toString() { return from.getName() + " → " + to.getName(); }
    }

    private void changeLanguage() {
        String selectedLanguage = languageBox.getValue();
        if (selectedLanguage == null) selectedLanguage = "English";
        Locale locale = switch (selectedLanguage) {
            case "Finnish" -> new Locale("fi", "FI");
            case "Swedish" -> new Locale("sv", "SE");
            case "Japanese" -> new Locale("ja", "JP");
            case "Arabic" -> new Locale("ar", "SA");
            default -> new Locale("en", "US");
        };
        currentLanguageCode = locale.getLanguage();
        root.setNodeOrientation(currentLanguageCode.equals("ar")
                ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        messages = ResourceBundle.getBundle("MessagesBundle", locale);
        loadConversionOptions();
        appTitleLabel.setText(messages.getString("app.title"));
        languageLabel.setText(messages.getString("select.language"));
        conversionLabel.setText(messages.getString("choice.prompt"));
        temperatureLabel.setText(messages.getString("temperature.label"));
        temperatureInput.setPromptText(messages.getString("temperature.prompt"));
        convertButton.setText(messages.getString("convert.button"));
        resultLabel.setText(messages.getString("result.placeholder"));
        historyLabel.setText(messages.getString("history.title"));
        journeyLabel.setText(messages.getString("journey.title"));
        journeyInputLabel.setText(messages.getString("journey.input.title"));
        distanceInput.setPromptText(messages.getString("journey.distance.prompt"));
        travelTimeInput.setPromptText(messages.getString("journey.time.prompt"));
        saveJourneyButton.setText(messages.getString("journey.save.button"));
        databaseResultLabel.setText("");
        loadConversionHistory();
    }

    private void loadConversionHistory() {
        conversionHistoryListView.getItems().setAll(Database.getConversionHistory(currentLanguageCode));
    }

    @FXML
    private void loadJourneys() {
        journeyListView.getItems().setAll(Database.getJourneys());
    }

    @FXML
    private void saveJourney() {
        try {
            double distance = Double.parseDouble(distanceInput.getText().trim());
            double travelTime = Double.parseDouble(travelTimeInput.getText().trim());
            if (!Double.isFinite(distance) || !Double.isFinite(travelTime)
                    || distance <= 0 || travelTime <= 0) {
                databaseResultLabel.setText(messages.getString("journey.positive"));
                return;
            }
            if (Database.saveJourney(distance, travelTime)) {
                databaseResultLabel.setText(messages.getString("journey.saved"));
                distanceInput.clear();
                travelTimeInput.clear();
                loadJourneys();
            } else {
                databaseResultLabel.setText(messages.getString("journey.failed"));
            }
        } catch (NumberFormatException e) {
            databaseResultLabel.setText(messages.getString("journey.invalid"));
        }
    }

    @FXML
    private void handleConvert() {
        try {
            double input = Double.parseDouble(temperatureInput.getText().trim());
            if (!Double.isFinite(input)) throw new NumberFormatException("Temperature must be finite");
            ConversionOption selected = conversionBox.getValue();
            if (selected == null) return;
            String from = selected.getFrom().getUnitKey();
            String to = selected.getTo().getUnitKey();
            double output;
            String templateKey;
            if (from.equals("fahrenheit") && to.equals("celsius")) {
                output = converter.fahrenheitToCelsius(input);
                templateKey = "result.fahrenheitToCelsius";
            } else if (from.equals("celsius") && to.equals("fahrenheit")) {
                output = converter.celsiusToFahrenheit(input);
                templateKey = "result.celsiusToFahrenheit";
            } else if (from.equals("kelvin") && to.equals("celsius")) {
                output = converter.kelvinToCelsius(input);
                templateKey = "result.kelvinToCelsius";
            } else {
                return;
            }
            resultLabel.setText(MessageFormat.format(messages.getString(templateKey), input, output));
            Database.saveConversion(input, from, to, output, currentLanguageCode);
            loadConversionHistory();
        } catch (NumberFormatException e) {
            resultLabel.setText(messages.getString("result.invalid"));
        }
    }
}

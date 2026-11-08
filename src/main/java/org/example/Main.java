
package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Database.createTables();
        Locale locale = Locale.US;

        ResourceBundle bundle =
                ResourceBundle.getBundle("MessagesBundle", locale);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/temperature-view.fxml"),
                bundle
        );

        Scene scene = new Scene(loader.load(), 400, 350);

        stage.setTitle(bundle.getString("app.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

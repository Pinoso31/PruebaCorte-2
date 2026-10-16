package com.example.pruebacorte2.PapeleriaCampusSources;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PapeleriaCampusApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PapeleriaCampusApplication.class.getResource("/com/example/pruebacorte2/papeleriaCampus.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Catálogo de productos - Papelería Campus");
        stage.setScene(scene);
        stage.show();
    }
}

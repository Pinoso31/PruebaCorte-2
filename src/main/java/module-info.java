module com.example.pruebacorte2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.pruebacorte2 to javafx.fxml;
    exports com.example.pruebacorte2;


    exports com.example.pruebacorte2.PapeleriaCampusSources;
    opens com.example.pruebacorte2.PapeleriaCampusSources to javafx.fxml;

    exports com.example.pruebacorte2.Modelos;
    opens com.example.pruebacorte2.Modelos to javafx.base;


}
package org.example;

import javafx.application.Application;
import javafx.collections.transformation.FilteredList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class Main extends Application {

    private TableView<Empleado> tableView;

    // Lista de empleados
    private ObservableList<Empleado> datos =
            FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Ejemplo JDBC");

        tableView = new TableView<>();
        Button eliminar = new Button("Eliminar");
        eliminar.setOnAction(e -> {

            Empleado seleccionado =
                    tableView.getSelectionModel()
                            .getSelectedItem();

            if (seleccionado == null) {

                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("Aviso");
                alerta.setHeaderText(null);
                alerta.setContentText("Selecciona un empleado");

                alerta.showAndWait();

                return;
            }

            eliminarEmpleado(seleccionado);
        });

        // Campo buscar
        TextField buscar = new TextField();
        buscar.setPromptText("Buscar empleado...");

        // Definir columnas
        TableColumn<Empleado, String> nombreCol =
                new TableColumn<>("Nombre");

        TableColumn<Empleado, Integer> salarioCol =
                new TableColumn<>("Salario");

        // Asignar propiedades
        nombreCol.setCellValueFactory(
                new PropertyValueFactory<>("nombre"));

        salarioCol.setCellValueFactory(
                new PropertyValueFactory<>("salario"));

        tableView.getColumns().addAll(nombreCol, salarioCol);

        // Filtrado
        FilteredList<Empleado> filtrados =
                new FilteredList<>(datos, p -> true);

        buscar.textProperty().addListener((obs, oldValue, newValue) -> {

            filtrados.setPredicate(empleado -> {

                // Mostrar todo si está vacío
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Buscar por nombre
                return empleado.getNombre()
                        .toLowerCase()
                        .contains(newValue.toLowerCase());
            });
        });

        // Asignar datos filtrados
        tableView.setItems(filtrados);
        HBox hvox = new HBox(10,eliminar);
        VBox vbox = new VBox(10, buscar, tableView,hvox);

        Scene scene = new Scene(vbox, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.show();

        cargarDatos();
    }

    private void cargarDatos() {

        String url = "jdbc:oracle:thin:@localhost:1521:xe";

        String user = "SYSTEM";
        String password = "Hg@rodri2005";

        try (
                Connection conn = DriverManager.getConnection(
                        url, user, password);

                Statement stmt = conn.createStatement();

                ResultSet rs = stmt.executeQuery(
                        "SELECT nombre, salario FROM empleado2")
        ) {

            while (rs.next()) {

                String nombre =
                        rs.getString("nombre");

                int salario =
                        rs.getInt("salario");

                datos.add(
                        new Empleado(nombre, salario));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Empleado {

        private final String nombre;
        private final int salario;

        public Empleado(String nombre, int salario) {

            this.nombre = nombre;
            this.salario = salario;
        }

        public String getNombre() {
            return nombre;
        }

        public int getSalario() {
            return salario;
        }
    }
    private void eliminarEmpleado(Empleado empleado) {

        String url = "jdbc:oracle:thin:@localhost:1521:xe";

        String user = "SYSTEM";
        String password = "Hg@rodri2005";

        String sql =
                "DELETE FROM empleado2 WHERE nombre = ?";

        try (
                Connection conn = DriverManager.getConnection(
                        url, user, password);

                PreparedStatement pstmt =
                        conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, empleado.getNombre());

            int filas = pstmt.executeUpdate();

            if (filas > 0) {

                datos.remove(empleado);

                Alert alerta = new Alert(Alert.AlertType.INFORMATION);

                alerta.setTitle("Correcto");
                alerta.setHeaderText(null);
                alerta.setContentText("Empleado eliminado");

                alerta.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
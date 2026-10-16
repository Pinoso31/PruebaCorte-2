package com.example.pruebacorte2.PapeleriaCampusSources;

import com.example.pruebacorte2.Database.ConexionBD;
import com.example.pruebacorte2.Modelos.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class PapeleriaCampusController {
    // Componentes Formulario
    @FXML
    private TextField txtId;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    // Botones Formulario
    @FXML private Button btnLimpiarFormulario;
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    // Filtros y Búsqueda
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroCategoria;
    @FXML private CheckBox chkSoloExistencias;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;
    @FXML private Button btnRecargar;

    // Tabla y Registros
    @FXML private TableView<Producto> colTabla; // Vincular a fx:id del TableView en FXML
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private Label lblRegistros;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarCategorias();
        cargarDatosTabla(null, "Todas", false);

        // Listener para cargar fila seleccionada en el formulario
        colTabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarFormulario(newSelection);
            }
        });
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        txtId.setDisable(true); // Deshabilitar edición de ID
    }

    private void cargarCategorias() {
        cbCategoria.setItems(FXCollections.observableArrayList("Papelería", "Tecnología", "Accesorios"));
        cbFiltroCategoria.setItems(FXCollections.observableArrayList("Todas", "Papelería", "Tecnología", "Accesorios"));
        cbFiltroCategoria.setValue("Todas");
    }

    // --- MÉTODOS DE BÚSQUEDA Y FILTRADO (B1 a B7) ---

    public void cargarDatosTabla(String textoBusqueda, String categoria, boolean soloExistencias) {
        listaProductos.clear();
        StringBuilder sql = new StringBuilder(
                "SELECT id_producto, codigo, nombre, categoria, precio, stock " +
                        "FROM catalogo.producto WHERE 1=1 "
        );

        if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
            sql.append("AND (LOWER(codigo) LIKE LOWER(?) OR LOWER(nombre) LIKE LOWER(?)) ");
        }
        if (categoria != null && !categoria.equals("Todas")) {
            sql.append("AND categoria = ? ");
        }
        if (soloExistencias) {
            sql.append("AND stock > 0 ");
        }

        sql.append("ORDER BY codigo ASC");

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                String paramText = "%" + textoBusqueda.trim() + "%";
                pstmt.setString(paramIndex++, paramText);
                pstmt.setString(paramIndex++, paramText);
            }
            if (categoria != null && !categoria.equals("Todas")) {
                pstmt.setString(paramIndex++, categoria);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listaProductos.add(new Producto(
                        rs.getInt("id_producto"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getBigDecimal("precio"),
                        rs.getInt("stock")
                ));
            }

            colTabla.setItems(listaProductos);
            lblRegistros.setText("Registros: " + listaProductos.size());

            if (listaProductos.isEmpty()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Sin coincidencias en la búsqueda.");
            }

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "Error al consultar PostgreSQL: " + e.getSQLState());
        }
    }

    @FXML
    private void accionBuscar() {
        cargarDatosTabla(txtBuscar.getText(), cbFiltroCategoria.getValue(), chkSoloExistencias.isSelected());
    }

    @FXML
    private void accionLimpiarFiltros() {
        txtBuscar.clear();
        cbFiltroCategoria.setValue("Todas");
        chkSoloExistencias.setSelected(false);
        cargarDatosTabla(null, "Todas", false);
    }

    @FXML
    private void accionRecargar() {
        accionBuscar(); // Mantiene los filtros vigentes al recargar
    }

    // --- ACCIONES DE BOTONES CRUD ---

    @FXML
    private void accionGuardar() {
        if (!validarFormulario(false)) return;

        String sql = "INSERT INTO catalogo.producto (codigo, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtCodigo.getText().trim().toUpperCase());
            pstmt.setString(2, txtNombre.getText().trim());
            pstmt.setString(3, cbCategoria.getValue());
            pstmt.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
            pstmt.setInt(5, Integer.parseInt(txtStock.getText().trim()));

            pstmt.executeUpdate();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto guardado correctamente.");
            accionLimpiarFormulario();
            accionRecargar();

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) { // Violación de restricción UNIQUE
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "El código digitado ya existe.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de BD", "Código SQLState: " + e.getSQLState());
            }
        }
    }

    @FXML
    private void accionActualizar() {
        if (txtId.getText().isEmpty()) { // Validar V10
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Debe seleccionar un producto de la tabla.");
            return;
        }

        if (!validarFormulario(true)) return;

        String sql = "UPDATE catalogo.producto SET codigo = ?, nombre = ?, categoria = ?, precio = ?, stock = ? WHERE id_producto = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtCodigo.getText().trim().toUpperCase());
            pstmt.setString(2, txtNombre.getText().trim());
            pstmt.setString(3, cbCategoria.getValue());
            pstmt.setBigDecimal(4, new BigDecimal(txtPrecio.getText().trim()));
            pstmt.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            pstmt.setInt(6, Integer.parseInt(txtId.getText()));

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas == 0) { // Validar E3
                mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "El producto ya no existe en la base de datos.");
            } else {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            }
            accionLimpiarFormulario();
            accionRecargar();

        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de BD", "Código SQLState: " + e.getSQLState());
        }
    }

    @FXML
    private void accionEliminar() {
        if (txtId.getText().isEmpty()) { // Validar V10
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Debe seleccionar un producto de la tabla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar el producto seleccionado?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM catalogo.producto WHERE id_producto = ?";

            try (Connection conn = ConexionBD.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, Integer.parseInt(txtId.getText()));
                int filasAfectadas = pstmt.executeUpdate();

                if (filasAfectadas == 0) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "El producto no se encontró en la base de datos.");
                } else {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto eliminado correctamente.");
                }
                accionLimpiarFormulario();
                accionRecargar();

            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de BD", "Código SQLState: " + e.getSQLState());
            }
        }
    }

    @FXML
    private void accionLimpiarFormulario() {
        txtId.clear();
        txtCodigo.clear();
        txtNombre.clear();
        cbCategoria.setValue(null);
        txtPrecio.clear();
        txtStock.clear();
        colTabla.getSelectionModel().clearSelection();
    }

    private void cargarFormulario(Producto producto) {
        txtId.setText(String.valueOf(producto.getId_producto()));
        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        cbCategoria.setValue(producto.getCategoria());
        txtPrecio.setText(producto.getPrecio().toString());
        txtStock.setText(String.valueOf(producto.getStock()));
    }

    // --- VALIDADOR UNIFICADO (V1 - V9) ---

    private boolean validarFormulario(boolean esEdicion) {
        // V1: Código (1-10 caracteres)
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty() || codigo.length() > 10) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación V1", "El código debe contener entre 1 y 10 caracteres.");
            return false;
        }

        // V2: Nombre (3-80 caracteres)
        String nombre = txtNombre.getText().trim();
        if (nombre.length() < 3 || nombre.length() > 80) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación V2", "El nombre debe tener entre 3 y 80 caracteres.");
            return false;
        }

        // V3: Categoría
        if (cbCategoria.getValue() == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación V3", "Debe seleccionar una categoría permitida.");
            return false;
        }

        // V4, V5, V6: Precio
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) { // V5: mayor que 0
                mostrarAlerta(Alert.AlertType.ERROR, "Validación V5", "El precio debe ser mayor a 0.");
                return false;
            }
            if (precio.scale() > 2) { // V6: Máximo 2 decimales
                mostrarAlerta(Alert.AlertType.ERROR, "Validación V6", "El precio no debe tener más de 2 decimales.");
                return false;
            }
            if (precio.compareTo(new BigDecimal("99999999.99")) > 0) { // V6: Exceso de rango
                mostrarAlerta(Alert.AlertType.ERROR, "Validación V6", "El precio supera el límite permitido.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación V4", "El precio debe ser un número válido.");
            return false;
        }

        // V7, V8: Stock
        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) { // V8: Negativos no permitidos
                mostrarAlerta(Alert.AlertType.ERROR, "Validación V8", "El stock no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación V7", "El stock debe ser un número entero.");
            return false;
        }

        // V9: Comprobación de duplicados previa
        if (existeCodigoDuplicado(codigo.toUpperCase(), esEdicion ? Integer.parseInt(txtId.getText()) : null)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación V9", "El código ingresado ya está registrado.");
            return false;
        }

        return true;
    }

    private boolean existeCodigoDuplicado(String codigo, Integer idActual) {
        String sql = "SELECT id_producto FROM catalogo.producto WHERE LOWER(codigo) = LOWER(?)";
        if (idActual != null) {
            sql += " AND id_producto <> ?";
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigo);
            if (idActual != null) {
                pstmt.setInt(2, idActual);
            }

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            return false;
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

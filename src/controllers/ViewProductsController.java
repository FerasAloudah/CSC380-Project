package controllers;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import home.Main;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import utils.ConnectionUtil;

public class ViewProductsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField search;

    @FXML
    private Label status;

    @FXML
    private TableView table;

    PreparedStatement preparedStatement;
    Connection connection;

    String ProductsQuery = "";
    int supplierID;

    @FXML
    @SuppressWarnings("Duplicates")
    void HandleSearch(ActionEvent event) {
        String query = "SELECT ID, Name, Price, Quantity FROM Product WHERE ID = \'" + search.getText() + "\'  OR Name LIKE \'%" + search.getText() + "%\' "
                + "OR Price = \'" + search.getText() + "\' OR Quantity = \'" + search.getText() + "\'";
        if (search.getText().isEmpty()) {
            fetRowList(ProductsQuery);
            return;
        }
        table.getItems().removeAll();
        fetRowList(query);
    }

    @FXML
    void initialize() {
        connection = ConnectionUtil.con;
    }

    @SuppressWarnings("Duplicates")
    public void setSupplier(int supplierID, String name) {
        this.supplierID = supplierID;
        status.setText(Main.toTitleCase(name) + "'s Products:");
        ProductsQuery = "SELECT ID, Name, Price, Quantity FROM Product WHERE SupplierID = \'" + supplierID + "\'";
        fetColumnList(ProductsQuery);
        fetRowList(ProductsQuery);
    }

    // Fetch columns only.
    @SuppressWarnings("Duplicates")
    private void fetColumnList(String query) {
        String aliases[] = new String[] {
                "Product ID", "Product Name", "Product Price", "Product Quantity"
        };
        try {
            ResultSet rs = connection.createStatement().executeQuery(query);

            // Query for getting all attributes.
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(aliases[i]);

                col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });

                table.getColumns().removeAll(col);
                table.getColumns().addAll(col);

                System.out.println("Column [" + i + "] ");
            }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());

        }
    }

    // Fetches rows and data from the list
    @SuppressWarnings("Duplicates")
    private void fetRowList(String query) {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        try {
            ResultSet rs = connection.createStatement().executeQuery(query);
            int j = 0;

            while (rs.next()) {
                ObservableList row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }
                System.out.println("Row added " + row);
                data.add(row);
            }

            table.setItems(data);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

}

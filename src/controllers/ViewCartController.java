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

public class ViewCartController {

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

    String CartQuery = "";
    int customerID, cartID;

    @FXML
    @SuppressWarnings("Duplicates")
    void HandleSearch(ActionEvent event) {
        String query = "SELECT T.ID, T.Name, T.Price, T.ProductQuantity FROM (" + CartQuery + ") AS T WHERE T.Name LIKE \'%" + search.getText() + "%\' OR T.Price = \'" +
                search.getText() + "\' OR T.ProductQuantity = \'" + search.getText() + "\'";
        if (search.getText().isEmpty()) {
            fetRowList(CartQuery);
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
    public void setCustomer(int customerID, String name) {
        this.customerID = customerID;
        status.setText(Main.toTitleCase(name) + "'s Cart:");

        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM Cart WHERE customerid=?");
            preparedStatement.setInt(1, customerID);
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            cartID = rs.getInt(1);
            System.out.println("Using Cart ID: " + cartID + " For Customer: " + customerID);
            CartQuery = "SELECT P.ID, P.Name, P.Price, CP.ProductQuantity " +
                    "FROM Cart_Products CP, Product P, Cart CR " +
                    "WHERE CR.customerid = \'" + customerID + "\' AND CP.cartid = \'" + cartID + "\' AND P.id = CP.productid";

            fetColumnList(CartQuery);
            fetRowList(CartQuery);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
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

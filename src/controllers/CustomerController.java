package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import home.Main;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import utils.ConnectionUtil;

import javax.xml.transform.Result;

public class CustomerController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button products;

    @FXML
    private Button cart;

    @FXML
    private Button signout;

    @FXML
    private StackPane stackPane;

    @FXML
    private Button cCheckout, cRemove;

    @FXML
    private TextField cSearch;

    @FXML
    private Label cStatus;

    @FXML
    private TableView cTable;

    @FXML
    private Button pAdd;

    @FXML
    private TextField pSearch;

    @FXML
    private Label pStatus;

    @FXML
    private TableView pTable;

    @FXML
    private Label cTotal;

    private Node productsNode, cartNode, frontChild;

    PreparedStatement preparedStatement;
    Connection connection;
    int customerID, cartID;

    // Fetch Query.
    String ProductQuery = "SELECT ID, Name, Price, Quantity FROM Product";
    String CartQuery = "SELECT P.ID, P.Name, P.Price, CP.ProductQuantity " +
            "FROM Cart_Products CP, Product P, Customer C, Cart CR " +
            "WHERE C.id = \'" + customerID + "\' AND CP.cartid = \'" + cartID + "\' AND P.id = CP.productid";

    @FXML
    @SuppressWarnings("Duplicates")
    void HandleEvents(MouseEvent event) {
        Button b = (Button) event.getSource();
        if (b == pAdd) {
            addToCart();
        } else if (b == cCheckout) {
            checkout();
        } else if (b == cRemove) {
            removeItem();
        } else if (b == products) {
            changeToProducts();
        } else if (b == cart) {
            changeToCart();
        } else if (b == signout) {
            try {
                Node node = (Node) event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.close();
                Scene scene = new Scene(FXMLLoader.load(new File("src/fxml/Login.fxml").toURI().toURL()));
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void removeItem() {
        try {
            String query = "";
            Object selectedItem = cTable.getSelectionModel().getSelectedItem();
            String attributes[] = selectedItem.toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
            int currentQuantity = Integer.parseInt(attributes[3]);
            if (currentQuantity == 1) {
                query = "DELETE FROM Cart_Products WHERE CartID = ? AND ProductID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, cartID);
                preparedStatement.setString(2, attributes[0]);
                preparedStatement.execute();
            } else {
                query = "UPDATE Cart_Products SET ProductQuantity = ? WHERE CartID = ? AND ProductID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, --currentQuantity);
                preparedStatement.setInt(2, cartID);
                preparedStatement.setString(3, attributes[0]);
                preparedStatement.execute();
            }

            calculateTotal();
            fetRowList(CartQuery);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            pStatus.setTextFill(Color.RED);
            pStatus.setText(ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("Please select an element to delete.");
            pStatus.setTextFill(Color.RED);
            pStatus.setText("Please select an element to delete.");
        }
    }

    @SuppressWarnings("Duplicates")
    private void checkout() {
        try {
            String query = "SELECT ProductID, ProductQuantity " +
                    "FROM Cart_Products " +
                    "WHERE cartid = \'" + cartID + "\'";
            ResultSet rs = connection.createStatement().executeQuery(query);
            while (rs.next()) {
                int productID = rs.getInt(1);
                int productQuantity = rs.getInt(2);
                query = "SELECT Quantity FROM Product WHERE ID = \'" + productID +  "\'";
                ResultSet rs2 = connection.createStatement().executeQuery(query);
                rs2.next();
                if (productQuantity > rs2.getInt(1)) {
                    throw new SQLException("Product " + productID + "'s: quantity > store's quantity");
                }
            }

            rs.first();
            do {
                int productID = rs.getInt(1);
                int productQuantity = rs.getInt(2);

                query = "SELECT Quantity FROM Product WHERE ID = \'" + productID +  "\'";
                ResultSet rs2 = connection.createStatement().executeQuery(query);
                rs2.next();

                query = "UPDATE Product SET Quantity = ? WHERE ID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, rs2.getInt(1) - productQuantity);
                preparedStatement.setInt(2, productID);

                preparedStatement.execute();
            } while (rs.next());

            query = "DELETE FROM Cart_Products WHERE CartID = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, cartID);
            preparedStatement.execute();

            calculateTotal();
            fetRowList(CartQuery);
            fetRowList(ProductQuery);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            cStatus.setTextFill(Color.RED);
            cStatus.setText(ex.getMessage());
        }
    }

    @SuppressWarnings("Duplicates")
    private void addToCart() {
        try {
            Object selectedItem = pTable.getSelectionModel().getSelectedItem();
            int productID = Integer.parseInt(selectedItem.toString().replaceAll("\\[", "").replaceAll("]", "").split(", ")[0]);
            String query = "SELECT T.ID, T.ProductQuantity " +
                    "FROM (" +
                        "SELECT P.ID, CR.ID AS CartID, P.Price, CP.ProductQuantity " +
                        "FROM Cart_Products CP, Product P, Cart CR " +
                        "WHERE CR.customerid = \'" + customerID + "\' AND CP.cartid = \'" + cartID + "\' AND P.id = CP.productid" + ") AS T " +
                    "WHERE T.ID = \'" + productID + "\' AND T.CartID = \'" + cartID + "\'";;
            preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int quantity = rs.getInt(2) + 1;
                query = "SELECT Quantity FROM Product WHERE ID = \'" + productID + "\'";
                preparedStatement = connection.prepareStatement(query);
                rs = preparedStatement.executeQuery();
                if (rs.next() && rs.getInt(1) < quantity) {
                    throw new SQLException("Maximum quantity has been reached.");
                }
                query = "UPDATE Cart_Products SET ProductQuantity = ? WHERE ProductID = ? AND CartID = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, quantity);
                preparedStatement.setInt(2, productID);
                preparedStatement.setInt(3, cartID);

                preparedStatement.executeUpdate();
            } else {
                query = "INSERT INTO Cart_Products (CartID, ProductID, ProductQuantity) VALUES (?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, cartID);
                preparedStatement.setInt(2, productID);
                preparedStatement.setInt(3, 1);

                preparedStatement.executeUpdate();
            }

            pStatus.setTextFill(Color.GREEN);
            pStatus.setText("Added Successfully.");

            calculateTotal();
            fetRowList(CartQuery);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            pStatus.setTextFill(Color.RED);
            pStatus.setText(ex.getMessage());
        }
    }

    private void calculateTotal() {
        try {
            int total = 0;
            String query = "SELECT SUM(P.Price * CP.ProductQuantity) " +
                    "FROM Cart_Products CP, Product P, Cart CR " +
                    "WHERE CR.customerid = \'" + customerID + "\' AND CP.cartid = \'" + cartID + "\' AND P.id = CP.productid";
            preparedStatement = connection.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
            cTotal.setText("Total: " + total + "$");
        } catch (SQLException ex) {
            cTotal.setText("Total: " + 0 + "$");
            System.out.println(ex.getMessage());
            cStatus.setTextFill(Color.RED);
            cStatus.setText(ex.getMessage());
        }
    }

    private void changeToProducts() {
        pStatus.setTextFill(Color.BLACK);
        pStatus.setText("Products:");
        productsNode.toFront();
        frontChild = productsNode;
    }

    private void changeToCart() {
        cStatus.setTextFill(Color.BLACK);
        cStatus.setText("Cart:");
        cartNode.toFront();
        frontChild = cartNode;
    }

    @FXML
    void HandleSearch(ActionEvent event) {
        if (frontChild == productsNode) {
            searchProduct();
        } else if (frontChild == cartNode) {
            searchCart();
        }
    }

    @SuppressWarnings("Duplicates")
    private void searchProduct() {
        String query = "SELECT ID, Name, Price, Quantity FROM Product WHERE ID=\'" + pSearch.getText() + "\' OR Name LIKE \'%" + pSearch.getText() + "%\' OR Price = \'" +
                pSearch.getText() + "\' OR Quantity=\'" + pSearch.getText() + "\'";
        if (pSearch.getText().isEmpty()) {
            fetRowList(ProductQuery);
            return;
        }
        pTable.getItems().removeAll();
        try {
            preparedStatement = connection.prepareStatement(query);

            ObservableList<ObservableList> data = FXCollections.observableArrayList();
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

            pTable.setItems(data);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            pStatus.setTextFill(Color.RED);
            pStatus.setText(ex.getMessage());
        }
    }

    @SuppressWarnings("Duplicates")
    private void searchCart() {
        String query = "SELECT T.ID, T.Name, T.Price, T.ProductQuantity FROM (" + CartQuery + ") AS T WHERE T.Name LIKE \'%" + cSearch.getText() + "%\' OR T.Price = \'" +
                cSearch.getText() + "\' OR T.ProductQuantity = \'" + cSearch.getText() + "\'";
        if (cSearch.getText().isEmpty()) {
            fetRowList(CartQuery);
            return;
        }
        cTable.getItems().removeAll();
        try {
            preparedStatement = connection.prepareStatement(query);

            ObservableList<ObservableList> data = FXCollections.observableArrayList();
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

            cTable.setItems(data);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            cStatus.setTextFill(Color.RED);
            cStatus.setText(ex.getMessage());
        }
    }

    @FXML
    void initialize() {
        fetColumnList(ProductQuery);
        fetRowList(ProductQuery);
        ObservableList<Node> children = stackPane.getChildren();
        productsNode = frontChild = children.get(1);
        cartNode = children.get(0);
    }

    public CustomerController() {
        connection = ConnectionUtil.con;
    }

    // Fetch columns only.
    @SuppressWarnings("Duplicates")
    private void fetColumnList(String query) {
        TableView table = pTable;
        boolean isCart = false;
        String aliases[] = new String[3];
        if (query.contains("Cart")) {
            table = cTable;
            isCart = true;
            aliases = new String[]{
                    "Product ID", "Product Name", "Product Price", "Product Quantity"
            };
        }
        try {
            ResultSet rs = connection.createStatement().executeQuery(query);

            // Query for getting all attributes.
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col;
                if (isCart) {
                    col = new TableColumn(aliases[i]);
                } else {
                    col = new TableColumn(rs.getMetaData().getColumnName(i + 1).toUpperCase());
                }

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
        TableView table = query.contains("Cart") ? cTable : pTable;
        ResultSet rs;
        try {
            rs = connection.createStatement().executeQuery(query);
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

    @SuppressWarnings("Duplicates")
    public void checkCart() {
        String query = "SELECT * FROM Cart WHERE customerid=" + customerID;
        try {
            ResultSet rs = connection.createStatement().executeQuery(query);
            if (!rs.next()) {
                query = "INSERT INTO Cart (ID, CustomerID) VALUES (?, ?)";
                preparedStatement =  connection.prepareStatement(query);
                preparedStatement.setString(1, null);
                preparedStatement.setInt(2, customerID);

                preparedStatement.executeUpdate();
                System.out.println("Cart ID added for " + customerID);
            }

            preparedStatement = connection.prepareStatement("SELECT * FROM Cart WHERE customerid=?");
            preparedStatement.setInt(1, customerID);
            rs = preparedStatement.executeQuery();
            rs.next();
            cartID = rs.getInt(1);
            System.out.println("Using Cart ID: " + cartID + " For Customer: " + customerID);
            calculateTotal();
            CartQuery = "SELECT P.ID, P.Name, P.Price, CP.ProductQuantity " +
                    "FROM Cart_Products CP, Product P, Cart CR " +
                    "WHERE CR.customerid = \'" + customerID + "\' AND CP.cartid = \'" + cartID + "\' AND P.id = CP.productid";

            fetColumnList(CartQuery);
            fetRowList(CartQuery);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

}

class Product {

    int id, quantity;

    public Product(int id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

}

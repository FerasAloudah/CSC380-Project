package controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import utils.ConnectionUtil;

public class EmployeeController implements Initializable {

    @FXML
    private TextField pID, pName, pQuantity, pPrice, pSupplier;
    @FXML
    private TextField cID, cName, cEmail, cUsername, cPassword, cAddress;
    @FXML
    private TextField sID, sName;
    @FXML
    private TextField pSearch, cSearch, sSearch;
    @FXML
    private Button pAdd, cAdd, sAdd;
    @FXML
    private Button pDelete, cDelete, sDelete;
    @FXML
    private Button pUpdate, cUpdate, sUpdate;
    @FXML
    private Button cCart, sProducts;
    @FXML
    private Button products, customers, suppliers, signout;
    @FXML
    private Label pStatus, cStatus, sStatus;
    @FXML
    private TableView pTable, cTable, sTable;
    @FXML
    private StackPane stackPane;


    private Node productsNode, customersNode, suppliersNode, frontChild;

    PreparedStatement preparedStatement;
    Connection connection;

    // Fetch Query.
    String MainQuery = "SELECT * FROM ";


    public EmployeeController() {
        connection = ConnectionUtil.con;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (String s : "Product Customer Supplier".split(" ")) {
            fetColumnList(MainQuery + s);
            fetRowList(MainQuery + s);
        }

        ObservableList<Node> children = stackPane.getChildren();
        productsNode = frontChild = children.get(2);
        customersNode = children.get(1);
        suppliersNode = children.get(0);
    }

    @FXML
    @SuppressWarnings("Duplicates")
    private void HandleEvents(MouseEvent event) {
        Button b = (Button) event.getSource();
        if (isAddEvent(b)) {
            if (frontChild == productsNode) {
                addProduct();
            } else if (frontChild == customersNode) {
                addCustomer();
            } else if (frontChild == suppliersNode) {
                addSupplier();
            }
        } else if (isDeleteEvent(b)) {
            if (frontChild == productsNode) {
                deleteProduct();
            } else if (frontChild == customersNode) {
                deleteCustomer();
            } else if (frontChild == suppliersNode) {
                deleteSupplier();
            }
        } else if (isUpdateEvent(b)) {
            if (frontChild == productsNode) {
                updateProduct();
            } else if (frontChild == customersNode) {
                updateCustomer();
            } else if (frontChild == suppliersNode) {
                updateSupplier();
            }
        } else if (b == products) {
            changeToProducts();
        } else if (b == customers) {
            changeToCustomers();
        } else if (b == suppliers) {
            changeToSuppliers();
        } else if (b == cCart) {
            viewCart();
        } else if (b == sProducts) {
            viewProducts();
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
    private void viewCart() {
        try {
            Object selectedItem = cTable.getSelectionModel().getSelectedItem();
            String attributes[] = selectedItem.toString().replaceAll("\\[", "").split(", ");

            FXMLLoader cartLoader = new FXMLLoader(new File("src/fxml/ViewCart.fxml").toURI().toURL());
            cartLoader.load();
            ViewCartController cartController = cartLoader.getController();
            cartController.setCustomer(Integer.parseInt(attributes[0]), attributes[1]);

            Parent p = cartLoader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(p));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Game Hub Store");
            stage.getIcons().add(new Image("file:src/images/gamepad.png"));
            stage.showAndWait();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            cStatus.setTextFill(Color.RED);
            cStatus.setText(ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("Please select an element to view.");
            cStatus.setTextFill(Color.RED);
            cStatus.setText("Please select an element to view.");
        }
    }

    @SuppressWarnings("Duplicates")
    private void viewProducts() {
        try {
            Object selectedItem = sTable.getSelectionModel().getSelectedItem();
            String attributes[] = selectedItem.toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");

            FXMLLoader productsLoader = new FXMLLoader(new File("src/fxml/ViewProducts.fxml").toURI().toURL());
            productsLoader.load();
            ViewProductsController productsController = productsLoader.getController();
            productsController.setSupplier(Integer.parseInt(attributes[0]), attributes[1]);

            Parent p = productsLoader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(p));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Game Hub Store");
            stage.getIcons().add(new Image("file:src/images/gamepad.png"));
            stage.showAndWait();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            sStatus.setTextFill(Color.RED);
            sStatus.setText(ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("Please select an element to view.");
            sStatus.setTextFill(Color.RED);
            sStatus.setText("Please select an element to view.");
        }
    }

    private boolean isAddEvent(Button b) {
        return b == pAdd || b == cAdd || b == sAdd;
    }

    private boolean isDeleteEvent(Button b) {
        return b == pDelete || b == cDelete || b == sDelete;
    }

    private boolean isUpdateEvent(Button b) {
        return b == pUpdate || b == cUpdate || b == sUpdate;
    }

    @FXML
    void HandleSearch(ActionEvent event) {
        if (frontChild == productsNode) {
            searchProduct();
        } else if (frontChild == customersNode) {
            searchCustomer();
        } else if (frontChild == suppliersNode) {
            searchSupplier();
        }
    }

    @FXML
    void HandleAdd(ActionEvent event) {
        if (frontChild == productsNode) {
            addProduct();
        } else if (frontChild == customersNode) {
            addCustomer();
        } else if (frontChild == suppliersNode) {
            addSupplier();
        }
    }

    private void changeToProducts() {
        pStatus.setTextFill(Color.BLACK);
        pStatus.setText("Products:");
        productsNode.toFront();
        frontChild = productsNode;
    }

    private void changeToCustomers() {
        cStatus.setTextFill(Color.BLACK);
        cStatus.setText("Customers:");
        customersNode.toFront();
        frontChild = customersNode;
    }

    private void changeToSuppliers() {
        sStatus.setTextFill(Color.BLACK);
        sStatus.setText("Suppliers:");
        suppliersNode.toFront();
        frontChild = suppliersNode;
    }

    private void addProduct() {
        if (pName.getText().isEmpty() || pQuantity.getText().isEmpty() || pPrice.getText().isEmpty() || pSupplier.getText().isEmpty()) {
            pStatus.setTextFill(Color.RED);
            pStatus.setText("Enter all of the details.");
        } else {
            pStatus.setTextFill(Color.BLACK);
            pStatus.setText("Products:");
            saveDataProduct();
        }
    }

    @SuppressWarnings("Duplicates")
    private void deleteProduct() {
        String query = "DELETE FROM Product WHERE ID = ?";
        try {
            Object selectedItem = pTable.getSelectionModel().getSelectedItem();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, selectedItem.toString().replaceAll("\\[", "").split(", ")[0]);
            System.out.println("Row deleted " + selectedItem);

            preparedStatement.execute();
            pStatus.setTextFill(Color.GREEN);
            pStatus.setText("Deleted Successfully.");

            pTable.getItems().removeAll(selectedItem);
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
    private void updateProduct() {
        String query = "UPDATE Product SET Name = ?, Price = ?, Quantity = ?, SupplierID = ? WHERE ID = ?";
        try {
            Object selectedItem = pTable.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                throw new SQLException("Please select an element to update.");
            }
            preparedStatement = connection.prepareStatement(query);
            FXMLLoader updateLoader = new FXMLLoader(new File("src/fxml/UpdateProduct.fxml").toURI().toURL());
            updateLoader.load();
            UpdateProductController updateController = updateLoader.getController();

            Parent p = updateLoader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(p));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Game Hub Store");
            stage.getIcons().add(new Image("file:src/images/gamepad.png"));
            stage.showAndWait();

            String attributes[] = selectedItem.toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
            preparedStatement.setString(1, updateController.pName.getText().isEmpty() ? attributes[1] : updateController.pName.getText());
            preparedStatement.setString(2, updateController.pPrice.getText().isEmpty() ? attributes[2] : updateController.pPrice.getText());
            preparedStatement.setString(3, updateController.pQuantity.getText().isEmpty() ? attributes[3] : updateController.pQuantity.getText());
            preparedStatement.setString(4, updateController.pSupplier.getText().isEmpty() ? attributes[4] : updateController.pSupplier.getText());
            preparedStatement.setString(5, attributes[0]);

            preparedStatement.execute();
            pStatus.setTextFill(Color.GREEN);
            pStatus.setText("Updated Successfully.");
            fetRowList(MainQuery + "Product");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            pStatus.setTextFill(Color.RED);
            pStatus.setText(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            pStatus.setTextFill(Color.RED);
            pStatus.setText(ex.getMessage());
        }
    }

    @SuppressWarnings("Duplicates")
    private void searchProduct() {
        String query = "SELECT * FROM Product WHERE ID = \'" + pSearch.getText() + "\' OR Name LIKE \'%" + pSearch.getText() + "%\' OR Price = \'" +
                pSearch.getText() + "\' OR Quantity = \'" + pSearch.getText() + "\' OR SupplierID = \'" + pSearch.getText() + "\'";
        if (pSearch.getText().isEmpty()) {
            fetRowList(MainQuery + "Product");
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

    private void addCustomer() {
        if (cName.getText().isEmpty() || cEmail.getText().isEmpty() || cUsername.getText().isEmpty() || cPassword.getText().isEmpty() || cAddress.getText().isEmpty()) {
            cStatus.setTextFill(Color.RED);
            cStatus.setText("Enter all of the details.");
        } else {
            cStatus.setTextFill(Color.BLACK);
            cStatus.setText("Customers:");
            saveDataCustomer();
        }
    }

    @SuppressWarnings("Duplicates")
    private void deleteCustomer() {
        String query = "DELETE FROM Customer WHERE ID = ? AND Email = ? AND Username = ?";
        try {
            Object selectedItem = cTable.getSelectionModel().getSelectedItem();
            String attributes[] = selectedItem.toString().replaceAll("\\[", "").split(", ");
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, attributes[0]);
            preparedStatement.setString(2, attributes[2]);
            preparedStatement.setString(3, attributes[3]);
            System.out.println("Row deleted " + selectedItem);

            preparedStatement.execute();
            cStatus.setTextFill(Color.GREEN);
            cStatus.setText("Deleted Successfully.");

            cTable.getItems().removeAll(selectedItem);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            cStatus.setTextFill(Color.RED);
            cStatus.setText(ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("Please select an element to delete.");
            cStatus.setTextFill(Color.RED);
            cStatus.setText("Please select an element to delete.");
        }
    }

    @SuppressWarnings("Duplicates")
    private void updateCustomer() {
        String query = "UPDATE Customer SET Name = ?, Email = ?, Username = ?, Password = ?, Address = ? WHERE ID = ?";
        try {
            Object selectedItem = cTable.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                throw new SQLException("Please select an element to update.");
            }
            preparedStatement = connection.prepareStatement(query);
            FXMLLoader updateLoader = new FXMLLoader();
            updateLoader.setLocation(new File("src/fxml/UpdateCustomer.fxml").toURI().toURL());
            updateLoader.load();
            UpdateCustomerController updateController = updateLoader.getController();

            Parent p = updateLoader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(p));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Game Hub Store");
            stage.getIcons().add(new Image("file:src/images/gamepad.png"));
            stage.showAndWait();

            String attributes[] = selectedItem.toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
            preparedStatement.setString(1, updateController.cName.getText().isEmpty() ? attributes[1] : updateController.cName.getText());
            preparedStatement.setString(2, updateController.cEmail.getText().isEmpty() ? attributes[2] : updateController.cEmail.getText());
            preparedStatement.setString(3, updateController.cUsername.getText().isEmpty() ? attributes[3] : updateController.cUsername.getText());
            preparedStatement.setString(4, updateController.cPassword.getText().isEmpty() ? attributes[4] : updateController.cPassword.getText());
            preparedStatement.setString(5, updateController.cAddress.getText().isEmpty() ? attributes[5] : updateController.cAddress.getText());
            preparedStatement.setString(6, attributes[0]);

            preparedStatement.execute();
            cStatus.setTextFill(Color.GREEN);
            cStatus.setText("Updated Successfully.");
            fetRowList(MainQuery + "Customer");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            cStatus.setTextFill(Color.RED);
            cStatus.setText(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            cStatus.setTextFill(Color.RED);
            cStatus.setText(ex.getMessage());
        }
    }

    @SuppressWarnings("Duplicates")
    private void searchCustomer() {
        String query = "SELECT * FROM Customer WHERE ID = \'" + cSearch.getText() + "\' OR Name LIKE \'%" + cSearch.getText() + "%\' OR Email LIKE \'%"
                + cSearch.getText() + "%\' OR Username LIKE \'%" + cSearch.getText() + "%\' OR Password = \'" + cSearch.getText()
                + "\' OR Address LIKE \'%" + cSearch.getText() + "%\'";
        if (cSearch.getText().isEmpty()) {
            fetRowList(MainQuery + "Customer");
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

    private void addSupplier() {
        if (sName.getText().isEmpty()) {
            sStatus.setTextFill(Color.RED);
            sStatus.setText("Enter all of the details.");
        } else {
            sStatus.setTextFill(Color.BLACK);
            sStatus.setText("Suppliers:");
            saveDataSupplier();
        }
    }

    @SuppressWarnings("Duplicates")
    private void deleteSupplier() {
        String query = "DELETE FROM Supplier WHERE ID = ?";
        try {
            Object selectedItem = sTable.getSelectionModel().getSelectedItem();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, selectedItem.toString().replaceAll("\\[", "").split(", ")[0]);
            System.out.println("Row deleted " + selectedItem);

            preparedStatement.execute();
            sStatus.setTextFill(Color.GREEN);
            sStatus.setText("Deleted Successfully.");

            sTable.getItems().removeAll(selectedItem);
            fetRowList(MainQuery + "Product");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sStatus.setTextFill(Color.RED);
            sStatus.setText(ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("Please select an element to delete.");
            sStatus.setTextFill(Color.RED);
            sStatus.setText("Please select an element to delete.");
        }
    }

    @SuppressWarnings("Duplicates")
    private void updateSupplier() {
        String query = "UPDATE Supplier SET Name = ? WHERE ID = ?";
        try {
            Object selectedItem = sTable.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                throw new SQLException("Please select an element to update.");
            }
            preparedStatement = connection.prepareStatement(query);
            FXMLLoader updateLoader = new FXMLLoader();
            updateLoader.setLocation(new File("src/fxml/UpdateSupplier.fxml").toURI().toURL());
            updateLoader.load();
            UpdateSupplierController updateController = updateLoader.getController();

            Parent p = updateLoader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(p));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Game Hub Store");
            stage.getIcons().add(new Image("file:src/images/gamepad.png"));
            stage.showAndWait();

            String attributes[] = selectedItem.toString().replaceAll("\\[", "").replaceAll("]", "").split(", ");
            preparedStatement.setString(1, updateController.sName.getText().isEmpty() ? attributes[1] : updateController.sName.getText());
            preparedStatement.setString(2, attributes[0]);

            preparedStatement.execute();
            sStatus.setTextFill(Color.GREEN);
            sStatus.setText("Updated Successfully.");
            fetRowList(MainQuery + "Supplier");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sStatus.setTextFill(Color.RED);
            sStatus.setText(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            sStatus.setTextFill(Color.RED);
            sStatus.setText(ex.getMessage());
        }
    }

    @SuppressWarnings("Duplicates")
    private void searchSupplier() {
        String query = "SELECT * FROM Supplier WHERE ID = \'" + sSearch.getText() + "\' OR Name LIKE \'%" + sSearch.getText() + "%\'";
        if (sSearch.getText().isEmpty()) {
            fetRowList(MainQuery + "Supplier");
            return;
        }
        sTable.getItems().removeAll();
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

            sTable.setItems(data);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sStatus.setTextFill(Color.RED);
            sStatus.setText(ex.getMessage());
        }
    }

    @SuppressWarnings("Duplicates")
    private void clearFields() {
        if (frontChild == productsNode) {
            pID.clear();
            pName.clear();
            pPrice.clear();
            pQuantity.clear();
            pSupplier.clear();
        } else if (frontChild == customersNode) {
            cID.clear();
            cName.clear();
            cEmail.clear();
            cUsername.clear();
            cPassword.clear();
            cAddress.clear();
        } else if (frontChild == suppliersNode) {
            sID.clear();
            sName.clear();
        }
    }

    @SuppressWarnings("Duplicates")
    private int saveDataProduct() {
        try {
            String query = "INSERT INTO Product (ID, Name, Price, Quantity, SupplierID) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, pID.getText().isEmpty() ? null : pID.getText());
            preparedStatement.setString(2, pName.getText());
            preparedStatement.setString(3, pPrice.getText());
            preparedStatement.setString(4, pQuantity.getText());
            preparedStatement.setString(5, pSupplier.getText());

            preparedStatement.executeUpdate();
            pStatus.setTextFill(Color.GREEN);
            pStatus.setText("Added Successfully.");

            fetRowList(MainQuery + "Product");
            clearFields();
            return 1;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            pStatus.setTextFill(Color.RED);
            pStatus.setText(ex.getMessage());
            return -1;
        }
    }

    @SuppressWarnings("Duplicates")
    private int saveDataCustomer() {
        try {
            String query = "INSERT INTO Customer (ID, Name, Email, Username, Password, Address) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, cID.getText().isEmpty() ? null : cID.getText());
            preparedStatement.setString(2, cName.getText());
            preparedStatement.setString(3, cEmail.getText());
            preparedStatement.setString(4, cUsername.getText());
            preparedStatement.setString(5, cPassword.getText());
            preparedStatement.setString(6, cAddress.getText());

            preparedStatement.executeUpdate();
            cStatus.setTextFill(Color.GREEN);
            cStatus.setText("Added Successfully.");

            fetRowList(MainQuery + "Customer");
            clearFields();
            return 1;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            cStatus.setTextFill(Color.RED);
            cStatus.setText(ex.getMessage());
            return -1;
        }
    }

    @SuppressWarnings("Duplicates")
    private int saveDataSupplier() {
        try {
            String query = "INSERT INTO Supplier (ID, Name) VALUES (?, ?)";
            preparedStatement =  connection.prepareStatement(query);
            preparedStatement.setString(1, sID.getText().isEmpty() ? null : sID.getText());
            preparedStatement.setString(2, sName.getText());

            preparedStatement.executeUpdate();
            sStatus.setTextFill(Color.GREEN);
            sStatus.setText("Added Successfully.");

            fetRowList(MainQuery + "Supplier");
            clearFields();
            return 1;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            sStatus.setTextFill(Color.RED);
            sStatus.setText(ex.getMessage());
            return -1;
        }
    }

    // Fetch columns only.
    @SuppressWarnings("Duplicates")
    private void fetColumnList(String query) {
        TableView table = query.contains("Product") ? pTable : query.contains("Customer") ? cTable : sTable;
        try {
            ResultSet rs = connection.createStatement().executeQuery(query);

            // Query for getting all attributes.
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1).toUpperCase());
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
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
        TableView table = query.contains("Product") ? pTable : query.contains("Customer") ? cTable : sTable;
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

}

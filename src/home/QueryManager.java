package home;

import controllers.UpdateProductController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SuppressWarnings("Duplicates")
public class QueryManager {

    public static Connection connection;
    public static PreparedStatement preparedStatement;

    public static void fetColumnList(String query, TableView table) {
        try {
            ResultSet rs = connection.createStatement().executeQuery(query);

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1).toUpperCase());
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

    public static void fetRowList(String query, TableView tableView) {
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

            tableView.setItems(data);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public int saveData(String query, String args[], Label status) {
        try {
            preparedStatement =  connection.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setString(i+1, args[i]);
            }

            preparedStatement.executeUpdate();
            status.setTextFill(Color.GREEN);
            status.setText("Added Successfully.");

            // fetRowList(MainQuery + "Supplier");
            // clearFields();
            return 1;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            status.setTextFill(Color.RED);
            status.setText(ex.getMessage());
            return -1;
        }
    }

    public static void deleteProduct(String table, TableView tableView, Label status) {
        String query = "DELETE FROM " + table + " WHERE ID = ?";
        try {
            Object selectedItem = tableView.getSelectionModel().getSelectedItem();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, selectedItem.toString().replaceAll("\\[", "").split(", ")[0]);
            System.out.println("Row deleted " + selectedItem);

            preparedStatement.execute();
            status.setTextFill(Color.GREEN);
            status.setText("Deleted Successfully.");

            tableView.getItems().removeAll(selectedItem);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            status.setTextFill(Color.RED);
            status.setText(ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("Please select an element to delete.");
            status.setTextFill(Color.RED);
            status.setText("Please select an element to delete.");
        }
    }

    public static void updateProduct(String query, String args[], Label status, FXMLLoader loader) {
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

}

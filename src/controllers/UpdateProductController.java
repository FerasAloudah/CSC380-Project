package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class UpdateProductController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    public TextField pName;

    @FXML
    public TextField pPrice;

    @FXML
    public TextField pQuantity;

    @FXML
    public TextField pSupplier;

    @FXML
    public Button pUpdate;

    @FXML
    void HandleEvents(MouseEvent event) {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onEnter(ActionEvent event) {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {

    }
}
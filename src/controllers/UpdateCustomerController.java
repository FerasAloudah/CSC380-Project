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

public class UpdateCustomerController {

    @FXML
    public ResourceBundle resources;

    @FXML
    public URL location;

    @FXML
    public TextField cName;

    @FXML
    public TextField cEmail;

    @FXML
    public TextField cUsername;

    @FXML
    public TextField cPassword;

    @FXML
    public Button cUpdate;

    @FXML
    public TextField cAddress;

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

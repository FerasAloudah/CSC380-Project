package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.ResourceBundle;

import home.QueryManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.ConnectionUtil;

public class LoginController implements Initializable {

    @FXML
    private Label lblErrors, sError;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtPassword;

    @FXML
    private TextField sName, sEmail, sUsername, sPassword, sAddress;

    @FXML
    private Button btnSignin;

    @FXML
    private Button btnSignup;

    @FXML
    private Button sGoBack;

    @FXML
    private Button sSignup;

    @FXML
    private StackPane stackPane;

    private Node signInNode, signUpNode, frontChild;

    Connection con = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    int id;

    @FXML
    public void handleButtonAction(MouseEvent event) {
        Button b = (Button) event.getSource();
        if (b == btnSignin) {
            doLogIn(event);
        } else if (b == btnSignup) {
            changeToSignUp();
        } else if (b == sGoBack) {
            changeToLogIn();
        } else if (b == sSignup) {
            addCustomer();
        }
    }

    private void changeToLogIn() {
        signInNode.toFront();
        frontChild = signInNode;
        lblErrors.setText("");
    }

    private void changeToSignUp() {
        signUpNode.toFront();
        frontChild = signUpNode;
        sError.setText("");
    }

    @SuppressWarnings("Duplicates")
    private void addCustomer() {
        if (sName.getText().isEmpty() || sEmail.getText().isEmpty() || sUsername.getText().isEmpty() || sPassword.getText().isEmpty() || sAddress.getText().isEmpty()) {
            System.err.println("Enter all of the details.");
            sError.setText("Enter all of the details.");
        } else {
            try {
                String query = "SELECT * FROM Customer WHERE Email = \'" + sEmail.getText() + "\' OR Username = \'" + sUsername.getText()
                        + "\' OR Email = \'" + sUsername.getText() + "\' OR Username = \'" + sEmail.getText() + "\'";
                preparedStatement = con.prepareStatement(query);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    throw new SQLException("Username or Email already exists.");
                }

                query = "INSERT INTO Customer (Name, Email, Username, Password, Address) VALUES (?, ?, ?, ?, ?)";
                preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, sName.getText());
                preparedStatement.setString(2, sEmail.getText());
                preparedStatement.setString(3, sUsername.getText());
                preparedStatement.setString(4, sPassword.getText());
                preparedStatement.setString(5, sAddress.getText());

                preparedStatement.executeUpdate();
                changeToLogIn();
                clearFields();
                lblErrors.setTextFill(Color.GREEN);
                lblErrors.setText("Signed up successfully.");
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                sError.setText(ex.getMessage());
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void clearFields() {
        txtUsername.clear();
        txtPassword.clear();
        sName.clear();
        sEmail.clear();
        sUsername.clear();
        sPassword.clear();
        sAddress.clear();
    }


    @FXML
    void onEnter(ActionEvent event) {
        if (frontChild == signInNode) {
            doLogIn(event);
        } else {
            addCustomer();
        }

    }

    private void doLogIn(EventObject event) {
        int result = logIn();
        if (result > 0) {
            String pathName = result == 1 ? "src/fxml/Employee.fxml" : "src/fxml/Customer.fxml";
            try {
                Node node = (Node) event.getSource();
                Stage stage = (Stage) node.getScene().getWindow();
                stage.close();
                FXMLLoader loader = new FXMLLoader(new File(pathName).toURI().toURL());
                Scene scene = new Scene(loader.load());

                if (result == 2) {
                    CustomerController customerController = loader.getController();
                    customerController.customerID = id;
                    customerController.checkCart();
                }

                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (con == null) {
            lblErrors.setTextFill(Color.RED);
            lblErrors.setText("Server is down.");
            con = ConnectionUtil.conDB();
        } else {
            lblErrors.setTextFill(Color.GREEN);
            lblErrors.setText("Server is up.");
        }

        ObservableList<Node> children = stackPane.getChildren();
        signInNode = frontChild = children.get(1);
        signUpNode = children.get(0);
    }

    public LoginController() {
        con = ConnectionUtil.conDB();
        QueryManager.connection = con;
    }

    private int logIn() {
        int i;
        String email = txtUsername.getText();
        String password = txtPassword.getText();

        // Log in Query.
        String sql = "SELECT * FROM Employee WHERE (Email = ? or Username = ?) and Password = ?";
        for (i = 1; i <= 2; i++) {
            try {
                preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, password);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                    lblErrors.setTextFill(Color.GREEN);
                    lblErrors.setText("Login Successful... Redirecting...");
                    System.out.println("Successful Login as " + (i == 1 ? "Employee." : "Customer."));
                    return i;
                }
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
                return -1;
            }
            // Search for customer login next.
            sql = "SELECT * FROM Customer WHERE (Email = ? or Username = ?) and Password = ?";
        }
        lblErrors.setTextFill(Color.RED);
        lblErrors.setText("Enter Correct Email/Password");
        System.err.println("Wrong Email or Password");
        return 0;
    }

}

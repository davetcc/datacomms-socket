package com.thecoderscorner.example.datacomms.rs232commands.ui;

import com.thecoderscorner.example.datacomms.rs232commands.protocol.ArduinoInterfaceControl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * This class creates the application that communicates between the arduino device
 * and the Java FX application. You can think of this as a starting point for
 * creating control interfaces between Arduino boards and a computer.
 *
 * First, we create the initial dialog that requests the communication port and
 * speed, then if the serial port is successfully set up, it displays the main
 * window. Otherwise, it will exit.
 */
public class ArduinoControlApp extends Application {
    private ArduinoInterfaceControl arduinoInterfaceControl = null;

    @Override
    public void start(Stage primaryStage) throws Exception {

        // first before anything else, we need to know what to connect to.
        presentConnectionDialog(primaryStage);

        if(arduinoInterfaceControl == null) {
            primaryStage.close();
            presentError();
            return;
        }

        // create the main window.
        primaryStage.setTitle("Arduino Control by thecoderscorner.com");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainWindow.fxml"));
        Pane myPane = loader.load();
        loader.<ArduinoControlController>getController().initialise(arduinoInterfaceControl);
        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show();
    }

    private void presentError() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Not able to start");
        alert.setHeaderText("Serial port not selected");
        alert.setContentText("application will now exit");
        alert.showAndWait();
    }

    private void presentConnectionDialog(Window owner) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/SerialConfiguration.fxml"));
        Pane myPane = loader.load();
        Stage dialogStage = new Stage();

        SerialCommunicationController controller = loader.<SerialCommunicationController>getController();
        controller.initialise();
        controller.setDialogStage(dialogStage);

        dialogStage.setTitle("Serial settings..");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setScene(new Scene(myPane));
        dialogStage.showAndWait();

        if(controller.getArdiunoInterface() != null) {
            arduinoInterfaceControl = controller.getArdiunoInterface();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

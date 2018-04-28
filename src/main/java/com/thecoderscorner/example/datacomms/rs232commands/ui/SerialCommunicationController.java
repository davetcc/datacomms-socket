package com.thecoderscorner.example.datacomms.rs232commands.ui;

import com.thecoderscorner.example.datacomms.rs232commands.protocol.ArduinoInterfaceControl;
import com.thecoderscorner.example.datacomms.rs232commands.protocol.SimpleRS232ArduinoController;
import gnu.io.NRSerialPort;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.Collection;

public class SerialCommunicationController {
    public static final String LOCAL_SIMULATOR = "Local Simulator";
    @FXML public ComboBox<Integer> baudRateCombo;
    @FXML public ComboBox<String> serialPortCombo;
    private ArduinoInterfaceControl arduinoInterface;
    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void initialise() {
        baudRateCombo.getItems().add(9600);
        baudRateCombo.getItems().add(19200);

        Collection<String> availablePorts = NRSerialPort.getAvailableSerialPorts();
        serialPortCombo.getItems().add(LOCAL_SIMULATOR);
        serialPortCombo.getItems().addAll(availablePorts);

    }

    public void connectPressed(ActionEvent actionEvent) {

        // someone pressed connect, check the an acceptable combination is selected
        if(!settingCombinationsAreAllowed()) {
            showSerialSettingErrorDialog();
            return;
        }

        // now we create either the simulator that allows running without an Arduino, or the real interface.
        if(serialPortCombo.getValue().equals(LOCAL_SIMULATOR)) {
            arduinoInterface = new SimulatedArduinoControl(serialPortCombo.getValue(), baudRateCombo.getValue());
        }
        else {
            arduinoInterface = new SimpleRS232ArduinoController(serialPortCombo.getValue(), baudRateCombo.getValue());
        }
        dialogStage.close();
    }

    private void showSerialSettingErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invalid serial settings");
        alert.setHeaderText("Invalid serial settings entered");
        alert.setContentText("Please ensure you have entered a port and speed.");
        alert.showAndWait();
    }

    private boolean settingCombinationsAreAllowed() {
        return baudRateCombo.getValue() != null && serialPortCombo.getValue() != null;
    }

    public ArduinoInterfaceControl getArdiunoInterface() {
        return arduinoInterface;
    }
}

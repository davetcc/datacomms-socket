package com.thecoderscorner.example.datacomms.rs232commands.ui;

import com.google.common.base.Strings;
import com.thecoderscorner.example.datacomms.rs232commands.protocol.ArduinoInterfaceControl;
import com.thecoderscorner.example.datacomms.rs232commands.protocol.ArduinoStatusReply;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * controller class for the arduino control panel example.
 */
public class ArduinoControlController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ArduinoInterfaceControl arduinoInterfaceControl;

    @FXML private TextField messageText;
    @FXML private Label connectionStatus;
    @FXML private Label lastCmdStatus;
    @FXML private CheckBox ledIndicator1;
    @FXML private CheckBox ledIndicator2;
    @FXML private CheckBox ledIndicator3;
    @FXML private CheckBox ledIndicator4;
    @FXML private CheckBox ledIndicator5;
    @FXML private CheckBox ledIndicator6;

    @FXML private Circle buttonStates1;
    @FXML private Circle buttonStates2;
    @FXML private Circle buttonStates3;
    @FXML private Circle buttonStates4;
    @FXML private Circle buttonStates5;
    @FXML private Circle buttonStates6;

    CheckBox[] ledCheckBoxes;

    public void initialise(ArduinoInterfaceControl arduinoInterfaceControl) {
        this.arduinoInterfaceControl = arduinoInterfaceControl;
        this.arduinoInterfaceControl.addConnectionChangeConsumer(this::connectionChanged);
        this.arduinoInterfaceControl.addReplyConsumer(this::replyReceived);

        ledCheckBoxes = new CheckBox[]{ledIndicator1, ledIndicator2, ledIndicator3, ledIndicator4,
                ledIndicator5, ledIndicator6};
    }

    private void replyReceived(ArduinoStatusReply reply) {
        Platform.runLater(() -> {
            lastCmdStatus.setText(reply.isLastCommandSuccessful()?"OK":"ERROR");
            updateStatesForInputButtons(reply);
        });
    }

    private void updateStatesForInputButtons(ArduinoStatusReply reply) {
        int i = 0;
        for (CheckBox checkBox : ledCheckBoxes) {
            checkBox.setSelected(reply.isLedOn(++i));
        }

        Color onColour = new Color(1.0, 1.0, 0, 1.0);
        Color offColour = new Color(1.0, 0, 0, 1.0);

        buttonStates1.setFill(reply.isButtonPressed(1)?onColour:offColour);
        buttonStates2.setFill(reply.isButtonPressed(2)?onColour:offColour);
        buttonStates3.setFill(reply.isButtonPressed(3)?onColour:offColour);
        buttonStates4.setFill(reply.isButtonPressed(4)?onColour:offColour);
        buttonStates5.setFill(reply.isButtonPressed(5)?onColour:offColour);
        buttonStates6.setFill(reply.isButtonPressed(6)?onColour:offColour);

    }

    public void connectionChanged(boolean connected) {
        Platform.runLater(() -> connectionStatus.setText(connected ? "Connected" : "Disconnected"));
    }

    public void onSendPressed(ActionEvent actionEvent) {
        String text = messageText.getText();
        if(Strings.isNullOrEmpty(text) || text.contains("\n")) {
            logger.info("Pressed send message with no text.");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message text error");
            alert.setHeaderText("Invalid message entered");
            alert.setContentText("Please ensure you have entered some text, and it does not contain line feeds.");
            alert.showAndWait();
        }
        else {
            arduinoInterfaceControl.sendPrintCommand(text);
            enterWaitingState();
        }
    }

    private void enterWaitingState() {
        lastCmdStatus.setText("Waiting..");
    }

    public void onLed1Changed(ActionEvent actionEvent) {
        ledChanged(1);
    }

    public void onLed2Changed(ActionEvent actionEvent) {
        ledChanged(2);
    }

    public void onLedChanged3(ActionEvent actionEvent) {
        ledChanged(3);
    }

    public void onLedChanged4(ActionEvent actionEvent) {
        ledChanged(4);
    }

    public void onLedChanged5(ActionEvent actionEvent) {
        ledChanged(5);
    }

    public void onLedChanged6(ActionEvent actionEvent) {
        ledChanged(6);
    }

    private void ledChanged(int i) {
        arduinoInterfaceControl.sendLedCommand(i, ledCheckBoxes[i-1].isSelected());
        enterWaitingState();
    }

}

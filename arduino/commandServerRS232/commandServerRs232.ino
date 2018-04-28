
#include "LiquidCrystal.h"
#include "CommandParser.h"

// these are the pins to which you have connected your switches
#define BUTTON1_PIN  24
#define BUTTON2_PIN  25
#define BUTTON3_PIN  26

// this is the speed at which to initiate the serial over USB connection 
#define PORT_SPEED 9600

// this is the standard configuration of the LCD display. Change the pin assignments
#define RS_PIN 28
#define ENABLE_PIN 29
#define DISPLAY_CONTRAST_PIN 3
#define D0_PIN 30
#define D1_PIN 31
#define D2_PIN 32
#define D3_PIN 33
LiquidCrystal lcd(RS_PIN, ENABLE_PIN, D0_PIN, D1_PIN, D2_PIN, D3_PIN);

ErrorCondition lastExecutionStatus = OK;
uint16_t ledStates;

void setup()
{
	// before anything else we need to initialise the lcd library, 
	// set the display size in the begin method, my display is 20x4
	lcd.begin(20, 4);

	// here we set the display contrast using PWM, use the keyboardTest sketch
	// to set up your keyboard and workout the brightness level first.
	pinMode(DISPLAY_CONTRAST_PIN, OUTPUT);
	analogWrite(DISPLAY_CONTRAST_PIN, 30);

	// and set the pins with switches connected as inputs.
	pinMode(BUTTON1_PIN, INPUT);
	pinMode(BUTTON2_PIN, INPUT);
	pinMode(BUTTON3_PIN, INPUT);

	Serial.begin(PORT_SPEED);

	printCurrentLedState();
}

void loop()
{
	// intialise the text on the screen
	lcd.setCursor(0, 0);
	lcd.print("Comms Test         ");

	// we attempt to read a whole message from the wire, timing out if not possible.
	char commandBuffer[30];
	int bytesRead = Serial.readBytesUntil('\n', commandBuffer, sizeof(commandBuffer)-1);

	// if we actually got the message then process it and set the execution status
	if (bytesRead > 0) {
		commandBuffer[bytesRead] = 0;
		lastExecutionStatus = commandRecieved(commandBuffer);
	}

	// we always update the status on every loop.
	sendStatus(lastExecutionStatus);
}

// We now have a command that we've received, so we try and process it. Some 
// things to bear in mind, when we first connect the buffer may have stuff in
// it, so the first command may be corrupt. we just skip any broken data.
ErrorCondition commandRecieved(const char* command) {
	Parser parser(command);
	const char* cmd = parser.nextWord();
	if (cmd == NULL) {
		return MISSING_PARAMETER;
	}

	if (strcmp(cmd, "led")==0) {
		return handleLedCommand(parser);
	}
	else if (strcmp(cmd, "print")==0) {
		return handlePrintCommand(parser);
	}
	else {
		return CMD_NOT_FOUND;
	}
}

// We've been asked to print something, so clear the 2nd line and print
// any parameters that were sent with the command.
ErrorCondition handlePrintCommand(Parser parser) {
	// clear the line first.
	lcd.setCursor(0, 1);
	lcd.print("                   ");
	
	// then go through each word printing them
	lcd.setCursor(0, 1);
	const char* toPrint = parser.nextWord();
	while (toPrint != NULL) {
		lcd.print(toPrint);
		lcd.print(" ");
		toPrint = parser.nextWord();
	}
	return OK;
}

// We have recevied a command to turn on or off an LED. We store the led status
// in a single integer, one LED per bit so we need to update the integer.
ErrorCondition handleLedCommand(Parser parser) {
	// first read the requested LED, then get it into bitwise format.
	const char* str = parser.nextWord();
	uint16_t ledChange = (int)strtol(str, NULL, 10);
	uint16_t bitChanged = 1 << (ledChange - 1);

	// now read the status of the change, true or false (on/off).
	const char* strChange = parser.nextWord();
	bool changedVal = strcmp(strChange, "true") == 0;
	if (changedVal) {
		// if it is true, then enable the bit by performing an "or"
		ledStates |= bitChanged;
	}
	else {
		// its off, remove the bit by anding with all other bits.
		ledStates &= ~bitChanged;
		lcd.setCursor(0, 1);
	}

	printCurrentLedState();

	return OK;
}

// Here we update the display to show our virtual LEDs, again it isolates each
// bit out from the integer.
void printCurrentLedState() {
	for (int i = 0; i < 5; ++i) {
		lcd.setCursor(i * 4, 2);
		lcd.print("L");
		lcd.print((char)('1' + i));
		lcd.setCursor(i * 4, 3);
		lcd.print((ledStates & (1 << i)) ? "On " : "Off");
	}
}

// here we formulate the status reply message that the UI uses to update
// the user interface. Notice importantly we call flush at the end.
void sendStatus(ErrorCondition cond) {
	Serial.write("statusReply ");
	Serial.print(ledStates);
	Serial.write(" ");
	Serial.print(getButtonStates());
	Serial.write(" ");
	Serial.write(lastExecutionStatus == OK ? "true" : "false");
	Serial.write("\n");
	Serial.flush();
}

// This takes the state of each of the buttons and converts it into a bitwise
// integer that is easier to pass between processes. This saves space by packing
// all the bits into a single integer.
int getButtonStates() {
	int button1 = digitalRead(BUTTON1_PIN);
	int button2 = digitalRead(BUTTON2_PIN);
	int button3 = digitalRead(BUTTON3_PIN);

	return button1 + (button2 << 1) + (button3 << 2);
}

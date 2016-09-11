// CommandManagement.h

#ifndef _COMMANDMANAGEMENT_h
#define _COMMANDMANAGEMENT_h

#if defined(ARDUINO) && ARDUINO >= 100
	#include "arduino.h"
#else
	#include "WProgram.h"
#endif

#include "LiquidCrystal.h"

// the space and tab characters that we can expect to separate commands.
#define SPACE_ASCII  32
#define TAB_ASCII 8

/* This class is responsible for breaking up a message string and providing it 
   word at a time 
 */
class Parser {
private:
	char argument[30];
	const char* text;
	int currentPosition;
public:
	Parser(const char* command);
	const char * nextWord();
};

enum ErrorCondition { OK, CMD_NOT_FOUND, MISSING_PARAMETER, ERROR_EXECUTING };

#endif


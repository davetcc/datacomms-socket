#include "CommandParser.h"

Parser::Parser(const char * command) {
	currentPosition = 0;
	text = command;
}

const char* Parser::nextWord() {
	// if we've already reached the end, then return nothing.
	if (text[currentPosition] == 0) {
		return NULL;
	}
	else {
		// we've got more to go, skip any white space.
		while ((text[currentPosition] == SPACE_ASCII || text[currentPosition] == TAB_ASCII) && text[currentPosition] != 0) {
			currentPosition++;
		}

		// make sure there's still more to go..
		if (text[currentPosition] == 0) {
			return NULL;
		}

		// then read the word until the next whitespace and return it.
		int len = 0;
		while ((text[currentPosition] != SPACE_ASCII && text[currentPosition] != TAB_ASCII && text[currentPosition] != 0) && len < (sizeof(argument) - 1)) {
			argument[len] = text[currentPosition];
			++currentPosition;
			++len;
		}
		argument[len] = 0;
	}
	return argument;
}


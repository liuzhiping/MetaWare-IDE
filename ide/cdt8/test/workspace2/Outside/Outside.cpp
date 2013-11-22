//============================================================================
// Name        : Outside.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C, Ansi-style
//============================================================================

#include <stdio.h>
#include <stdlib.h>

extern "C" void hello();

int main(void) {
	hello();
	return EXIT_SUCCESS;
}

//============================================================================
// Name        : $(baseName).cpp
// Author      : $(author)
// Version     :
// Copyright   : $(copyright)
// Description : Hello World in C++, Ansi-style
//============================================================================

#ifdef __GNUC__
#include <iostream>
using namespace std;
#else
#include <iostream.h>
#endif

int main() {
	cout << "$(message)" << endl; // prints $(message)
	return 0;
}

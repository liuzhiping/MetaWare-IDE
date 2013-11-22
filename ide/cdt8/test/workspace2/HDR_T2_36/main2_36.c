#include <stdio.h>

int change, all;

extern int func1(int);
extern int func2(int);

int main() { int n0=29;
	n0 = func1(func2(n0));
	return 0;
}
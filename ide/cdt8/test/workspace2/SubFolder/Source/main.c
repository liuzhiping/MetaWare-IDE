#include <stdio.h>
#include "headerfile.h"

int myfunc(int param);

int main(void)
{
#ifdef MYDEFINE
	bogosity
#else
	int res;
	printf("From main()\n");

	res = myfunc(100);

	printf("Res = %d\n", res);

	return 1;
#endif
}

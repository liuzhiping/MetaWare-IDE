#include <stdio.h>

extern void my_func (void);

main()
{
	printf("String 1\n");
	my_func();
	printf("String 2\n");
}

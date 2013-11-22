#include <stdlib.h>

int main() { int *a, k;
	a = malloc(10*sizeof(int));
	for(k=0; k<10; k++)
		a[k] = k;
	return 0;
}
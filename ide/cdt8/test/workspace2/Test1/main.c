// main.c (Project: Test1)

#include <stdio.h>

int k;
int main(int argc, char **argv) { int i_unused;
    printf("argc=%d\n", argc);
    for(k=1; k<argc; k++)
        printf("  argv[%d]=%s\n", k, argv[k]);
    k = nada();
    return 0;
} // main

int nada(void) { int z;
    z=5;
    return z;
}

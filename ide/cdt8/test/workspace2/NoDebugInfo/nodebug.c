#include <stdio.h>

int k;
int main(int argc, char **argv) {
    printf("NoDebugInfo: argc=%d\n", argc);
    for(k=1; k<argc; k++)
        printf("  argv[%d]=%s\n", k, argv[k]);
    return 0;
} // main
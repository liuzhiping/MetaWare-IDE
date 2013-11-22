#include <stdio.h>

class C {
public:
    C(int y){
    	int x = y;
        printf("one\n", x++);
        printf("two\n",x++);
        printf("three\n",x++);
    }
};

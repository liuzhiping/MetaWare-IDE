#include <stdio.h>

unsigned char init_array[10] = {'5', '\'', '7', '8', '9', '0', '1', '2', '3',
'4'};

void main(void)
{
        unsigned char my_array[10];
        int i;

        for (i = 0; i < 10; i++)
        {
                my_array[i] =init_array[i];
               printf ("my_array[%d] is %c.\n", i, my_array[i]);
        }
}

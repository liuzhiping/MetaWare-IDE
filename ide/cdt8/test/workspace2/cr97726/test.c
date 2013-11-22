#include <stdio.h>

int main(void)
{
   int percent_done;

   for (percent_done = 0; percent_done < 101; percent_done++) {
      fprintf(stderr, "\r %3d%% ", percent_done);
   } /* Endfor */

   return 0;
}

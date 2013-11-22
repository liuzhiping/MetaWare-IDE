/* From Wirth's Algorithms+Data Structures = Programs. */
/* This program is suitable for a code-generation      */
/* benchmark, especially given common sub-expressions  */
/* in array indexing.  See the Programmer's Guide for  */
/* how to get a machine code interlisting.             */

#include <stdio.h>
#include <stdlib.h>

typedef enum{False,True} Boolean;
typedef int Integer;

#define MAX_SIZE 30
static int size;

#define Asub(I)  A[(I)-1] /* C's restriction that array*/
#define Bsub(I)  B[(I)-2] /* indices start at zero     */
#define Csub(I)  C[(I)+size-1] /* prompts definition of     */
#define Xsub(I)  X[(I)-1] /* macros to do subscripting.*/
			    /* Pascal equivalents:     */
static Boolean A[ MAX_SIZE]; /* A:array[ 1.. 8] of Boolean    */
static Boolean B[MAX_SIZE*2-1]; /* B:array[ 2..16] of Boolean    */
static Boolean C[MAX_SIZE*2-1]; /* C:array[-7.. 7] of Boolean    */
static Integer X[ MAX_SIZE]; /* X:array[ 1.. 8] of Integer    */

void Try(Integer I, Boolean *Q) {
   Integer J = 0;
   do {
      J++; *Q = False;
      if (Asub(J) && Bsub(I+J) && Csub(I-J)) {
	 Xsub(I) = J;
	 Asub(J) = False; 
	 Bsub(I+J) = False; 
	 Csub(I-J) = False;
	 if (I < size) {
	    Try(I+1,Q);
	    if (!*Q) {
	       Asub(J) = True; 
	       Bsub(I+J) = True; 
	       Csub(I-J) = True;
	       }
	    }
	 else *Q = True;
	 }
      }
   while (!(*Q || J==size));
   }
void main (int argc, char **argv) {
   Integer I; Boolean Q = False;
   size = 8;  /* default */
   if (argc > 1){
       long i = atoi(argv[1]);
       if (i <= 0 ) printf("Bad size; 8 assumed\n");
       else
       if ( i > MAX_SIZE) printf("Size too big; %d assumed\n",MAX_SIZE),
		 size = MAX_SIZE;
       else
	    size = i;
       }
   printf("%s\n","go");
   for (I =  1; I <=  size; Asub(I++) = True);
   for (I =  2; I <= size*2; Bsub(I++) = True);
   for (I = -size+1; I <=  size-1; Csub(I++) = True);
   Try(1,&Q);
   if (Q)
      for (I = 1; I <= size;) {
	 printf("%4d",Xsub(I++));
	 }
   printf("\n");
   exit(!Q);
   }

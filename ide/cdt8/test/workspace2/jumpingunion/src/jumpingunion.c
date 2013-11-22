/*
 ============================================================================
 Name        : jumpingunion.c
 Author      :
 Version     :
 Copyright   : Your copyright notice
 Description : Hello World in C, Ansi-style
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

union pw {
   int thisnumber;
   char namefirst[10];
   char namelast[10];
   int thisage;
};


int main(void)
{

  union pw person;

  person.thisnumber = 1;
  strcpy (person.namefirst, "Andrew");
  strcpy (person.namelast, "Walker");
  person.thisage = 10;
return 0;
}


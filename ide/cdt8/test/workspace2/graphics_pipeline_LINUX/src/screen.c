/************************************************************************/
/************************************************************************/
/**                                                                     */
/** Copyright (c) ARC International 2008.                               */
/** All rights reserved                                                 */
/**                                                                     */
/** This software embodies materials and concepts which are             */
/** confidential to ARC International and is made                       */
/** available solely pursuant to the terms of a written license         */
/** agreement with ARC International                                    */
/**                                                                     */
/** For more information, contact support@arc.com                       */
/**                                                                     */
/**                                                                     */
/************************************************************************/
/************************************************************************/



#include "screen.h"

/* vt100 screen functions */

void
screen_clear (void)
{
  printf ("\033[H\033[J");      /* vt100 <ESC>[H<ESC>[J */
}


void
screen_coord (int x, int y)
{
//  printf ("\033[%d;%df", y+1, x+1);
    printf ("\033[%d;%dH", y+1, x+1);
}


void
eh (char *file, int line, char *error)
{
  screen_clear ();
  printf ("ERROR: file %s, line %d\n", file, line);
  printf ("----\n");
  printf ("%s\n", error);
  printf ("----\n");
  printf ("<press [enter] to exit>\n");
  getchar ();
  vr_shutdown();
  vr_artificial_crash();
}


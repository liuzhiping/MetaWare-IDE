/************************************************************************/
/*                                                                      */
/* Copyright (C) ARC International, Inc. 2007.                          */
/*                                                                      */
/* All Rights Reserved.                                                 */
/*                                                                      */
/* This software is the property of Arc International.  It is           */
/* furnished under a specific licensing agreement.  It may be used or   */
/* copied only under terms of the licensing agreement.                  */
/*                                                                      */
/* For more information, contact info@arc.com                           */
/*                                                                      */
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
  printf ("\033[%d;%df", y+1, x+1);
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


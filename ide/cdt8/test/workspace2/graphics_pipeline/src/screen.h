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

#ifndef _SCREEN_H
#define _SCREEN_H 1


/* vt100 screen functions */

extern void
screen_clear(void);

extern void
screen_coord(int x, int y);

extern void
eh(char *file, int line, char *error);

#define EH(err) eh(__FILE__,__LINE__, (err))

#endif /* _SCREEN_H  */


/************************************************************************/
/*                                                                      */
/* Copyright (C) ARC International, Inc. 2007.                          */
/*                                                                      */
/* All Rights Reserved.                                                 */
/*                                                                      */
/* This software is the property of ARC International, Inc.  It is      */
/* furnished under a specific licensing agreement.  It may be used or   */
/* copied only under terms of the licensing agreement.                  */
/*                                                                      */
/* For more information, contact support@arc.com                        */
/*                                                                      */
/************************************************************************/


#ifndef _GRAPI_H
#define _GRAPI_H 1

#include <math.h>
#include "grapi_types.h"

/* 2D Graphics API */

extern void
gr_start_new_frame(int frame_number);

extern void
gr_set_draw_char (char ch);

#ifndef USE_IMAGE_VIEWER
extern void
gr_putstr(int x, int y, char *str);
#endif

extern void
gr_show(void);


/* 3D Graphics API */

extern void
gr_observation_point (real x, real y, real z);

extern void
gr_observation_window (real l, real r, real b, real t);

extern void
gr_zpoint (real x, real y, real z);

extern void
gr_zmoveto (real x, real y, real z);

extern void
gr_zlineto (real x, real y, real z);

extern void
gr_zrmoveto (real x, real y, real z);

extern void
gr_zrlineto (real x, real y, real z);


#endif /* _GRAPI_H */


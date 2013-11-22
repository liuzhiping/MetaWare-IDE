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

#ifndef _PLOT3D_H
#define _PLOT3D_H 1

#include "common_types.h"
#include "screen.h"
#include "plot.h"


#define Di ((real)60.0)  /* distance from eye to screen (in) */
#define S  ((real)15.0)  /* Size of screen (in). My laptop is 9in x 7in */


typedef struct _point3
{
  real x;
  real y;
  real z;
} point3;


typedef struct _t_screen3d
{

  real currentx;
  real currenty;
  real currentz;

  real xo;
  real yo;
  real zo;

  real reflineratio;

  real xor;
  real yor;
  real zor;

  real Vsx;
  real Vcx;

  real Vsy;
  real Vcy;

  real Vxr;
  real Vxl;
  real Vyt;
  real Vyb;

  screen scr;

} screen3d;


extern void
init_3d (screen3d * s);

extern void
observation_point (screen3d * s, real x, real y, real z);

extern void
observation_window (screen3d * s, real l, real r, real b, real t);

extern void
zpoint (screen3d * s, real x, real y, real z);

extern void
zmoveto (screen3d * s, real x, real y, real z);

extern void
zlineto (screen3d * s, real x, real y, real z);

extern void
zrmoveto (screen3d * s, real x, real y, real z);

extern void
zrlineto (screen3d * s, real x, real y, real z);

extern void
close_3d (screen3d * s);


#endif /* _PLOT3D_H */


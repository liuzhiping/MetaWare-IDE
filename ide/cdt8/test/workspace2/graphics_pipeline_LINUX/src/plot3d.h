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


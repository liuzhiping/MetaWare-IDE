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




#include <string.h>

#include "plot3d.h"

#if 0
#define DEBUG 1
#endif


static void convertxy (screen3d * s, real * px, real * py, real * pz);  /* forward ref */
static void xform (screen3d * s, real * px, real * py, real * pz);      /* forward ref */

/*
   Macros
*/
#define setcurrentpoint(s)      (s)->currentx=x; \
                                (s)->currenty=y; \
                                (s)->currentz=z;

#define changecurrentpoint(s)   (s)->currentx+=x; \
                                (s)->currenty+=y; \
                                (s)->currentz+=z; \
                                x=(s)->currentx; \
                                y=(s)->currenty; \
                                z=(s)->currentz;

#define convertxyz(s)      xform((s),&x,&y,&z); \
                           convertxy((s),&x,&y,&z);


void
observation_window (screen3d * s, real l, real r, real b, real t)
{
  s->Vxr = r;
  s->Vxl = l;
  s->Vyt = t;
  s->Vyb = b;
  s->Vsx = ((s->Vxr - s->Vxl) / 2);
  s->Vcx = ((s->Vxr + s->Vxl) / 2);
  s->Vsy = ((s->Vyt - s->Vyb) / 2);
  s->Vcy = ((s->Vyt + s->Vyb) / 2);
  return;
}


void
observation_point (screen3d * s, real x, real y, real z)
{
  real refline, reflinez;
  s->xo = x;
  s->yo = y;
  s->zo = z;
  refline = (real) sqrt ((double) (s->xo * s->xo + s->yo * s->yo));
  reflinez = (real) sqrt ((double) (refline * refline + s->zo * s->zo));
  s->reflineratio = refline / reflinez;
  s->xor = s->xo / refline;
  s->yor = s->yo / refline;
  s->zor = s->zo / reflinez;
  return;
}


/* Translate to point of observation */
static void
t1 (screen3d * s, real * px, real * py, real * pz)
{
  *px -= s->xo;
  *py -= s->yo;
  *pz -= s->zo;
  return;
}


/* Rotate about x axis by -90 degrees */
static void
t2 (real * py, real * pz)
{
  real t;
  /* Swap y & z */
  t = *pz;
  *pz = *py;
  *py = t;
  *pz = -*pz;                   /* negate z */
  return;
}


static void
t3 (screen3d * s, real * px, real * pz)
{
  real x, z;
  real ztmp = *pz;
  real xtmp = *px;
  real xor = s->xor;
  real yor = s->yor;
  x = (xtmp * (-yor)) + (ztmp * (-xor));
  /* y as is */
  z = (xtmp * xor) + (ztmp * (-yor));
  *px = x;
  *pz = z;
  return;
}


static void
t4 (screen3d * s, real * py, real * pz)
{
  real y, z;
  /* x as is */
  y = (*py * s->reflineratio) + (*pz * (-s->zor));
  z = (*py * s->zor) + (*pz * s->reflineratio);
  *py = y;
  *pz = z;
  return;
}


/* Reverse sense of the z axis */
static void
t5 (real * pz)
{
  *pz = -*pz;
  return;
}


/* Scale by viewing parameters Di/S */
static void
n (real * px, real * py)
{
  *px = *px * (Di / S);
  *py = *py * (Di / S);
  /* z as is */
  return;
}


static void
xform (screen3d * s, real * px, real * py, real * pz)
{
  t1 (s, px, py, pz);
#if DEBUG
  printf ("t1: x=%f, y=%f, z=%f\n", *px, *py, *pz);
#endif
  t2 (py, pz);
#if DEBUG
  printf ("t2: x=%f, y=%f, z=%f\n", *px, *py, *pz);
#endif
  t3 (s, px, pz);
#if DEBUG
  printf ("t3: x=%f, y=%f, z=%f\n", *px, *py, *pz);
#endif
  t4 (s, py, pz);
#if DEBUG
  printf ("t4: x=%f, y=%f, z=%f\n", *px, *py, *pz);
#endif
  t5 (pz);
#if DEBUG
  printf ("t5: x=%f, y=%f, z=%f\n", *px, *py, *pz);
#endif
  n (px, py);
#if DEBUG
  printf ("n: x=%f, y=%f, z=%f\n", *px, *py, *pz);
#endif
  return;
}


static real
convertx (screen3d * s, real x, real z)
{
  /* If z==0, what does that mean */
  if (z == 0.0)
    {
      EH ("z==0 in convertx(). Error!");
      return 0.0;
    }
  else
    return ((x / z) * s->Vsx + s->Vcx);
}


static real
converty (screen3d * s, real y, real z)
{
  /* If z==0, what does that mean */
  if (z == 0.0)
    {
      EH ("z==0 in converty(). Error!");
      return 0.0;
    }
  else
    return ((y / z) * s->Vsy + s->Vcy);
}


static void
convertxy (screen3d * s, real * px, real * py, real * pz)
{
  real x, y;
  x = convertx (s, *px, *pz);
  y = converty (s, *py, *pz);
  *px = x;
  *py = y;
  *pz = 0.0;                    /* zero just in case */
  return;
}


void
init_3d (screen3d * s)
{
  extern void zmoveto (screen3d * s, real x, real y, real z);
  //int maxcol = MAXC;
  //int maxrow = MAXR;

  initt (&s->scr);
  set_draw_char (&s->scr, '@');

  /* Set the window to the dimensions of the screen */
  observation_window (s, 0.0, (real) (72), 0.0, (real) (72));
  observation_point (s, 0., 0., 0.);
  dwindo (&s->scr, 0., (real) (72), 0., (real) (72) * SRATIO);
}


/* Place a point at x,y,z */
void
zpoint (screen3d * s, real x, real y, real z)
{
  setcurrentpoint (s);
  convertxyz (s);
  pointa (&s->scr, x, y);
}


/* move to x, y, z */
void
zmoveto (screen3d * s, real x, real y, real z)
{
  setcurrentpoint (s);
  convertxyz (s);
  movea (&s->scr, x, y);
}


/* draw a line from the current position to x,y,z */
void
zlineto (screen3d * s, real x, real y, real z)
{
  setcurrentpoint (s);
  convertxyz (s);
  drawa (&s->scr, x, y);
}


/* move relative by x,y,z */
void
zrmoveto (screen3d * s, real x, real y, real z)
{
  changecurrentpoint (s);
  convertxyz (s);
  movea (&s->scr, x, y);
}


/* draw a line relative by x,y,z */
void
zrlineto (screen3d * s, real x, real y, real z)
{
  changecurrentpoint (s);
  convertxyz (s);
  drawa (&s->scr, x, y);
}


/* cause the page to be printed */
void
close_3d (screen3d * s)
{
  finitt (&s->scr, 0, 0);
}


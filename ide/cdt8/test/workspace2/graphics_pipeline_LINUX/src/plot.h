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



#ifndef _PLOT_H
#define _PLOT_H 1

#include <string.h>
#include <stdlib.h>
#include <math.h>
#include "common_types.h"
#include "screen.h"
#include "config.h"

#define Posit(r,c)   screen_coord ((c), (r));

#define EMPTY   0
#define LEFT    1
#define RIGHT   2
#define BOTTOM  4
#define TOP     8


#ifdef USE_IMAGE_VIEWER

#define MAXR 144
#define MAXC 176
/* CIF */
//#define MAXR 240
//#define MAXC 352
/* 2CIF */
//#define MAXR 240
//#define MAXC 704
/* 4CIF */
//#define MAXR 480
//#define MAXC 704

#define SRATIO    ((real)MAXR/(real)MAXC)
#else
#define MAXR 40
#define MAXC 80
#define SRATIO    (1.5757575757*(real)MAXR/(real)MAXC)
#endif




typedef struct _raster {

  byte memory[MAXR * MAXC];

} raster;


typedef struct _t_screen
{

  int xc;
  int yc;

  int minx;
  int miny;
  int maxx;
  int maxy;

  real xf;
  real yf;

  real xmin;
  real ymin;
  real xmax;
  real ymax;

  real afast;
  real bfast;
  real cfast;
  real dfast;

  byte draw_char;

  raster *r;

} screen;


extern void
screen_erase (screen * scr);

extern void
set_draw_char (screen * scr, byte ch);

extern void
twindo (screen * scr, int xl, int xr, int yb, int yt);

extern void
dwindo (screen * scr, real xl, real xr, real yb, real yt);


extern void
movabs (screen * scr, int x, int y);

extern void
pntabs (screen * scr, int x, int y);

extern void
drwabs (screen * scr, int x, int y);


extern void
movrel (screen *scr, int x, int y);

extern void
pntrel (screen *scr, int x, int y);

extern void
drwrel (screen *scr, int x, int y);


extern void
movea (screen *scr, real x, real y);

extern void
pointa (screen *scr, real x, real y);

extern void
drawa (screen *scr, real x, real y);


extern void
mover (screen *scr, real x, real y);

extern void
pointr (screen *scr, real x, real y);

extern void
drawr (screen *scr, real x, real y);

#ifndef USE_IMAGE_VIEWER
extern void
putstr(screen *scr, int x, int y, char *str);

extern void
putstr_down (screen * scr, int x, int y, char *str);
#endif

extern void
show(screen *scr);

extern void
erase_raster(raster *rstr);

extern void
show_raster(raster *primary, raster *rstr);

extern void
initt (screen *scr);

extern void
finitt (screen *scr, int x, int y);

#endif  /* _PLOT_H  */


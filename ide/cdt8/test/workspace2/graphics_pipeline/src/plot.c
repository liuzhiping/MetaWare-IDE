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

#include "plot.h"

#ifdef USE_IMAGE_VIEWER
#define IX(r,c)   ((MAXR-1-(r))*MAXC+(c))
#else
#define IX(r,c)   ((r)*MAXC+(c))
#endif

#if 0
#define DO_VALIDATION 1
#endif

#ifdef DO_VALIDATION
static void
valid_r (int r)
{
  char str[40];
  if (r >= MAXR)
    {
      sprintf (str, "r too high %d", r);
      EH (str);
    }
  if (r < 0)
    {
      sprintf (str, "r negative %d", r);
      EH (str);
    }
}


static void
valid_c (int c)
{
  char str[40];
  if (c >= MAXC)
    {
      sprintf (str, "c too high %d", c);
      EH (str);
    }
  if (c < 0)
    {
      sprintf (str, "c negative %d", c);
      EH (str);
    }
}


static void
valid_rc (int r, int c)
{
  valid_r (r);
  valid_c (c);
}
#else

#define valid_rc(r,c)  ;

#endif


#define WSCR(scr,row,col,ch) \
           valid_rc((row),(col)); \
           (scr)->r->memory[IX ((row), (col))] = (ch);


void
screen_erase (screen * scr)
{
#ifdef USE_IMAGE_VIEWER
  memset((void *)scr->r->memory, 0x0, MAXR*MAXC);
#else
  memset((void *)scr->r->memory, ' ', MAXR*MAXC);
#endif
}


void
set_draw_char (screen * scr, byte ch)
{
  scr->draw_char = ch;
}


static void
indo (screen * scr)
{
  scr->afast = ((real) (scr->minx - scr->maxx)) / (scr->xmin - scr->xmax);
  scr->cfast = ((real) (scr->miny - scr->maxy)) / (scr->ymin - scr->ymax);
  scr->bfast = ((real) scr->minx) - scr->afast * scr->xmin;
  scr->dfast = ((real) scr->miny) - scr->cfast * scr->ymin;
}


void
twindo (screen * scr, int xl, int xr, int yb, int yt)
{
  scr->minx = xl;
  scr->maxx = xr;
  scr->miny = yb;
  scr->maxy = yt;
  indo (scr);
}


void
dwindo (screen * scr, real xl, real xr, real yb, real yt)
{
  scr->xmin = xl;
  scr->xmax = xr;
  scr->ymin = yb;
  scr->ymax = yt;
  indo (scr);
}


#define movabs_macro(scr,x,y)  { \
                                (scr)->xc = (x); \
                                (scr)->yc = (y); \
                               }

#define pntabs_macro(scr,x,y)  { \
                                 movabs_macro(scr,x,y); \
                                 WSCR ((scr), (y), (x), (scr)->draw_char); \
                               }

void
movabs (screen * scr, int x, int y)
{
   movabs_macro(scr,x,y)
}


void
pntabs (screen * scr, int x, int y)
{
   pntabs_macro(scr,x,y);
}


void
drwabs (screen * scr, int x, int y)
{
  int length, i, xl, yl;
  int tl;
  real xt, yt, xi, yi;
  pntabs_macro (scr, scr->xc, scr->yc);
  xl = x - scr->xc;
  yl = y - scr->yc;
  length = abs (xl);
  tl = abs (yl);
  if (tl > length)
    length = tl;
  if (length > 0)
    {
      xi = (real) xl / (real) length;
      yi = (real) yl / (real) length;
      xt = (real) scr->xc + .5;
      yt = (real) scr->yc + .5;
      for (i = 1; i <= length; i++)
        {
          pntabs_macro (scr, (int) xt, (int) yt);
          xt += xi;
          yt += yi;
        }
    }
  pntabs_macro (scr, x, y);
}


void
movrel (screen * scr, int x, int y)
{
  movabs (scr, scr->xc + x, scr->yc + y);
}


void
pntrel (screen * scr, int x, int y)
{
  pntabs (scr, scr->xc + x, scr->yc + y);
}


void
drwrel (screen * scr, int x, int y)
{
  drwabs (scr, scr->xc + x, scr->yc + y);
}


static int
convy (screen * scr, real y)
{
  int yy = (int) (scr->cfast * y + scr->dfast);
  return yy;
}


static int
convx (screen * scr, real x)
{
  int xx = (int) (scr->afast * x + scr->bfast);
  return xx;
}


static int
clip_to_boarder (screen * scr, real x, real y)
{
  int c = EMPTY;
  if (x < scr->xmin)
    c = LEFT;
  else if (x > scr->xmax)
    c = RIGHT;
  if (y < scr->ymin)
    c += BOTTOM;
  else if (y > scr->ymax)
    c += TOP;
  return c;
}


void
movea (screen * scr, real x, real y)
{
  scr->xf = x;
  scr->yf = y;
  movabs (scr, convx (scr, x), convy (scr, y));
}


void
pointa (screen * scr, real x, real y)
{
  int r = convy (scr, y);
  int c = convx (scr, x);
  scr->xf = x;
  scr->yf = y;
  if (c < 0 || c >= MAXC || r < 0 || r >= MAXR)
    movabs (scr, c, r);         /* out-of-range so just set point */
  else
    pntabs (scr, c, r);
}


void
drawa (screen * scr, real x, real y)
{
  int c, c1, c2;
  real xt, yt, xs, ys;
  c1 = clip_to_boarder (scr, scr->xf, scr->yf);
  c2 = clip_to_boarder (scr, x, y);
  xs = x;
  ys = y;
  xt = x;
  yt = y;
  while (((c1 | c2) != EMPTY) && ((c1 & c2) == EMPTY))
    {
      c = c1;
      if (c == EMPTY)
        c = c2;
      if ((c & LEFT) != EMPTY)
        {                       /* cross left edge */
          yt =
            scr->yf + (y - scr->yf) * (scr->xmin - scr->xf) / (x - scr->xf);
          xt = scr->xmin;
        }
      else if ((c & RIGHT) != EMPTY)
        {                       /* cross right edge */
          yt =
            scr->yf + (y - scr->yf) * (scr->xmax - scr->xf) / (x - scr->xf);
          xt = scr->xmax;
        }
      if ((c & BOTTOM) != EMPTY)
        {                       /* cross bottom edge */
          xt =
            scr->xf + (x - scr->xf) * (scr->ymin - scr->yf) / (y - scr->yf);
          yt = scr->ymin;
        }
      else if ((c & TOP) != EMPTY)
        {                       /* cross top edge */
          xt =
            scr->xf + (x - scr->xf) * (scr->ymax - scr->yf) / (y - scr->yf);
          yt = scr->ymax;
        }
      if (c == c1)
        {
          scr->xf = xt;
          scr->yf = yt;
          c1 = clip_to_boarder (scr, xt, yt);
        }
      else
        {
          x = xt;
          y = yt;
          c2 = clip_to_boarder (scr, xt, yt);
        }
    }
  if ((c1 | c2) == EMPTY)
    {
      movea (scr, scr->xf, scr->yf);
      drwabs (scr, convx (scr, x), convy (scr, y));
    }
  pointa (scr, xs, ys);
}


void
mover (screen * scr, real x, real y)
{
  movrel (scr, convx (scr, x), convy (scr, y));
}


void
pointr (screen * scr, real x, real y)
{
  pntrel (scr, convx (scr, x), convy (scr, y));
}


void
drawr (screen * scr, real x, real y)
{
  drwrel (scr, convx (scr, x), convy (scr, y));
}


#ifndef USE_IMAGE_VIEWER
void
putstr (screen * scr, int x, int y, char *str)
{
  byte ch;
  int r = y;
  int c = x;
  movabs_macro (scr, x, y);
  while (1)
    {
      ch = *str++;
      if (ch == 0)
        break;
      WSCR (scr, r, c, ch);
      ++c;
      if (c >= MAXC)
        {
          c = 0;
          ++r;
        }
      movabs_macro (scr, c, r);
    }
}


void
putstr_down (screen * scr, int x, int y, char *str)
{
  byte ch;
  int r = y;
  int c = x;
  while (1)
    {
      movabs_macro (scr, c, r);
      ch = *str++;
      if (ch == 0)
        break;
      WSCR (scr, r, c, ch);
      --r;
      if (r < 0)
        {
          break;
        }
    }
}
#endif


void
initt (screen * scr)
{
  //int i, j;
  //screen_erase (scr);
  //screen_clear ();
  scr->xmax = 10.0;
  scr->xmin = 0.0;
  scr->ymax = 10.0 * SRATIO;
  scr->ymin = 0.0;
  twindo (scr, 0, MAXC - 1, 0, MAXR - 1);
  set_draw_char (scr, '#');
  movea (scr, 0.0, 0.0);
}


void
finitt (screen * scr, int x, int y)
{
}


void
erase_raster(raster *rstr)
{
#ifdef USE_IMAGE_VIEWER
   memset((void *)&(rstr->memory[0]), 0x0, MAXR*MAXC);
#else
   memset((void *)&(rstr->memory[0]), ' ', MAXR*MAXC);
#endif
}


#if 1
#define TO_CONSOLE 1
#endif

void
show_raster(raster *primary, raster *rstr)
{
  int r;
  int c;
  byte is;
  byte was;
  int need_positioning;
  for (r = MAXR-1; r >= 0; r--)
    {
      need_positioning = 1;
      for (c = 0; c < MAXC; c++)
        {
          int i = IX (r, c);
          byte *p = &(primary->memory[i]);
          was = *p;
          is = rstr->memory[i];
          if (is != was)
            {
              if (need_positioning) {
#ifdef TO_CONSOLE
                screen_coord (c, MAXR - 1 - r);
#endif
                need_positioning = 0;
              }
              *p = is;
#ifdef TO_CONSOLE
              putchar (is);
#endif
            }
          else
            need_positioning = 1;
        }
    }
#ifdef TO_CONSOLE
  fflush (stdout);
#endif
}


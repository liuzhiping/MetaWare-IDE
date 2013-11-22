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



#include "track_state.h"
#include "screen.h"
#include "grapi.h"
#include "config.h"

#define FPS 45.0

#define SQR 1.45

static real globalr = 15.;
static real globaltheta = 90.;
static real globalz = 6.0 * SQR;
static real globalx;
static real globaly;
static point3d m;

static int frame_number = 0;


static void
update (void)
{
  globalx = globalr * (real) sin ((double)globaltheta*(TPI/360.));
  globaly = globalr * (real) cos ((double)globaltheta*(TPI/360.));
                                      /* TODO: ^ can cast to real */

//  globalz = (real) sin ((double)(globaltheta*((real)TPI/(real)360.))) * 6.0 + 6.0;
}


static void
thetaminus ()
{
  globaltheta -= (360. / FPS);
  if (globaltheta < 0.0)
    globaltheta += 360.;
  update ();
}


static void
circle_in_zplane (real z, real radius)
{
#define ANGEL_ITERATION (18.0)
  real theta;
  real x, y;
  for (theta = 0.; theta <= 360.0; theta += ANGEL_ITERATION)
    {
      real radians = theta * TPI/360.; /* TODO: could cast to real */
      x = radius * (real) cos ((double) radians);
      y = radius * (real) sin ((double) radians);
      if (theta == 0.0)
        gr_zmoveto (x, y, z);
      else
        gr_zlineto (x, y, z);
    }
}


static void
relative_cube (real r, point3d * p)
{
  real r2;
  real x;
  real y;
  real z;

#ifdef USE_IMAGE_VIEWER
  gr_set_draw_char ((char)0xff);
#else
  gr_set_draw_char ('*');
#endif

  x = p->x;
  y = p->y;
  z = p->z;

  r2 = 2.0 * r;
  gr_zpoint (x - r, y + r, z - r);
  gr_zrlineto (r2, 0.0, 0.0);
  gr_zrlineto (0.0, -r2, 0.0);
  gr_zrlineto (-r2, 0.0, 0.0);
  gr_zrlineto (0.0, r2, 0.0);   /* bottom box */
  gr_zrlineto (0.0, 0.0, r2);   /* go up, left line */
  gr_zrlineto (r2, 0.0, 0.0);
  gr_zrlineto (0.0, -r2, 0.0);
  gr_zrlineto (-r2, 0.0, 0.0);
  gr_zrlineto (0.0, r2, 0.0);   /* top box */
  gr_zpoint (x + r, y + r, z + r);
  gr_zrlineto (0.0, 0.0, -r2);  /* front line */
  gr_zpoint (x + r, y - r, z + r);
  gr_zrlineto (0.0, 0.0, -r2);  /* right side line */
  gr_zpoint (x - r, y - r, z + r);
  gr_zrlineto (0.0, 0.0, -r2);  /* back line */
}


static void
init_function (void)
{
  m.x = 0.0;
  m.y = 0.0;
  m.z = 0.0;
  return;
}


static void
paint_one_frame (int fcount)
{
#ifndef USE_IMAGE_VIEWER
  char str[20];
#endif
  real radius;
  real step;

  thetaminus ();                /* move camera */
  gr_observation_point (globalx, globaly, globalz);

#if 0
  /* draw axis */
  gr_set_draw_char ('x');
  gr_zmoveto (0., 0., 0.);
  gr_zlineto (2.0 * SQR, 0., 0.);

  gr_set_draw_char ('y');
  gr_zmoveto (0., 0., 0.);
  gr_zlineto (0.0, 2.0 * SQR, 0.);

  gr_set_draw_char ('z');
  gr_zmoveto (0., 0., 0.);
  gr_zlineto (0.0, 0.0, 2.0 * SQR);
#endif

#ifdef USE_IMAGE_VIEWER
  gr_set_draw_char ((char)0x80);
#else
  gr_set_draw_char ('.');
#endif
  radius = (real) sqrt (2.0 * SQR * SQR);
  for (step = 0.; step <= (SQR * 3.0); step += SQR)
    circle_in_zplane (-SQR, radius + step);

  relative_cube (SQR, &m);

#ifndef USE_IMAGE_VIEWER
  sprintf (str, "frame %d", fcount);
  gr_putstr (1, 1, str);
#endif
}


/* graphics creator entry point */
void
_grmain (void)
{

  state_change(STATE_INITIALIZING, 0);

#ifndef USE_IMAGE_VIEWER
  screen_clear ();
#endif
  printf ("Graphics Pipeline Demonstration v1.01\n");
  printf ("ARC International\n");
  printf ("\n");
  printf ("Graphics Creation Stage\n");
  printf (" Note: A %c char indicates thread was stalled for message pool.\n", MP_FAIL_CHAR);
  printf ("       A %c char indicates thread was stalled because outgoing channel was full.\n", CHANNEL_FULL_CHAR);
  printf ("\n");

  init_function ();

  do
    {

      gr_start_new_frame(frame_number);

      /* paint frame */
      paint_one_frame(frame_number);

      gr_show();

      printf("\nframe %d ", frame_number);
      fflush(stdout);

      ++frame_number;

    }
  while (1);

  /* shouldn't get here */
  vr_artificial_crash ();

}


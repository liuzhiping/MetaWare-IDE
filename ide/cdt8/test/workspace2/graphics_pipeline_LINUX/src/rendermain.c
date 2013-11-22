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
#include "common.h"
#include "plot3d.h"
#include "grapi_types.h"


static screen3d s3d;

static int cmd_count = 0;

static int frame_number = 0;

static int thd_id;

static int
paint_frame (void)
{
  msghldr message;
  gr_message *m;
  int ret;
  int show = 0;
  point3d *pt;
  bbox *b;
  do
    {
      ret = vr_channel_receive (IN_CHANNEL,
                                (void *) &message,
                                sizeof (msghldr),
                                0);
      if (ret == sizeof (msghldr))
        {
          state_change_d(STATE_PROCESSING, thd_id, frame_number);

          m = (gr_message *) message.msg;

          cmd_count++;

          switch (m->gr_cmd)
            {

            case GR_CMD_NEW_FRAME:
              cmd_count = 1; /* reset command count */
              frame_number = m->msg.f.frame_number;
              break;
            case GR_CMD_SHOW:
              show = 1;
              break;
            case GR_CMD_OBSERVATION_WINDOW:
              b = &(m->msg.box);
              observation_window (&s3d, b->l, b->r, b->b, b->t);
              break;
            case GR_CMD_OBSERVATION_POINT:
              pt = &(m->msg.point);
              observation_point (&s3d, pt->x, pt->y, pt->z);
              break;
            case GR_CMD_ZPOINT:
              pt = &(m->msg.point);
              zpoint (&s3d, pt->x, pt->y, pt->z);
              break;
            case GR_CMD_ZMOVETO:
              pt = &(m->msg.point);
              zmoveto (&s3d, pt->x, pt->y, pt->z);
              break;
            case GR_CMD_ZLINETO:
              pt = &(m->msg.point);
              zlineto (&s3d, pt->x, pt->y, pt->z);
              break;
            case GR_CMD_ZRMOVETO:
              pt = &(m->msg.point);
              zrmoveto (&s3d, pt->x, pt->y, pt->z);
              break;
            case GR_CMD_ZRLINETO:
              pt = &(m->msg.point);
              zrlineto (&s3d, pt->x, pt->y, pt->z);
              break;
            case GR_CMD_SET_DRAW_CHAR:
              set_draw_char (&s3d.scr, m->msg.s.str[0]);
              break;
#ifndef USE_IMAGE_VIEWER
            case GR_CMD_PUT_STRING:
              {
                point2d *p;
                p = &(m->msg.s.p);
                putstr (&s3d.scr, p->x, p->y, &(m->msg.s.str[0]));
              }
              break;
#endif
            default: {
              printf("\nERROR: Unknown message type %d 0x%x, m=0x%x\n",
                            m->gr_cmd,
                            m->gr_cmd,
                            (int)m);
              }
              vr_artificial_crash ();
              break;
            }

          ret = vr_memory_pool_put_node (grcmd_pool, (void *) m);
          if (ret < 0)
            {
              printf ("\nERROR: vr_memory_pool_put_node returned %d\n", ret);
              vr_artificial_crash ();
            }

        }
      else if (ret <= 0)
        {
          state_change_d(STATE_IDLE, thd_id, frame_number);

#ifdef VR_MQX_ARC
          vr_sleep (0, SLEEPTM);
          /* sleep for a bit until data appears on channel */
#endif
        }
      else
        {
          /* ERROR */
          printf ("\nERROR: vr_channel_receive returned %d\n", ret);
          vr_artificial_crash ();
        }

    }
  while (!show);
  return 0;
}


/* render thread entry point */
void
_rendermain (void)
{
  display_msg msg;
  raster *r;
  vr_thread_t id = vr_get_thread_id ();
  int ret;

  thd_id = id - thd_rendermain0;

  state_change_d(STATE_INITIALIZING, thd_id, 0);

#ifndef USE_IMAGE_VIEWER
  screen_clear ();
#endif
  printf ("Graphics Pipeline Demonstration v1.01\n");
  printf ("ARC International\n");
  printf ("\n");
  printf ("Render thread %s\n", vr_get_thread_name_for_id(id));
  printf ("\n");

  init_3d (&s3d);

  do
    {

      r = (raster *) vr_memory_pool_get_node (raster_pool);
      if (r != (raster *) NULL)
        {
          state_change_d(STATE_PROCESSING, thd_id, frame_number);

          erase_raster (r);

          /* attach the raster to the screen3d struct */
          s3d.scr.r = r;

          /* paint frame */
          paint_frame ();

          /* construct the message to the display thread */
          msg.frame_number = frame_number;
          msg.frame = (void *) r;

          /* send the message to the display thread */
          do
            {
              ret =
                vr_channel_send (OUT_CHANNEL, (void *) &msg,
                                 sizeof (display_msg), 0);
              if (ret == sizeof (display_msg))
                break;          /* success */
              putchar (CHANNEL_FULL_CHAR);
              fflush (stdout);
              state_change_d(STATE_WAITING_FOR_CHANNEL, thd_id, frame_number);
              vr_sleep (0, SLEEPTM);
              /* sleep for a bit to wait for channel to drain */
            }
          while (1);
          state_change_d(STATE_PROCESSING, thd_id, frame_number);

          printf ("\nSent frame %d, raster 0x%08x, %d commands ",
                    frame_number,
                    (int) r,
                    cmd_count);
          fflush (stdout);


        }
      else
        {
          state_change_d(STATE_WAITING_FOR_POOL, thd_id, frame_number);
          putchar (MP_FAIL_CHAR);
          fflush (stdout);
          vr_sleep (0, SLEEPTM);
          /* sleep for a bit to wait for rasters to free up */
        }

    }
  while (1);

  /* shouldn't get here */
  vr_artificial_crash ();

}


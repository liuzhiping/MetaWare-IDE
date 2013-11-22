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

#include "track_state.h"
#include "grapi.h"
#include "common.h"

static int channel_index = 0;
static vr_channel_t channel_list[] = OUT_CHANNEL_LIST;

static int gr_frame_number = 0;

#define NUM_CHANNELS (sizeof(channel_list)/sizeof(vr_channel_t))


static gr_message *
gr_allocate_message (int msgtype)
{
  gr_message *m;
  do {
    m = (gr_message *) vr_memory_pool_get_node (grcmd_pool);
    if (m != (gr_message *) NULL)
      break;
    putchar (MP_FAIL_CHAR);
    fflush (stdout);
    state_change(STATE_WAITING_FOR_POOL, gr_frame_number);
    vr_sleep (0, SLEEPTM); /* sleep for a bit until pool has more nodes */
  } while (1);
  m->gr_cmd = msgtype;
  state_change(STATE_PROCESSING, gr_frame_number);
  return m;
}


int
gr_send_msg (gr_message * m)
{
  int ret;
  msghldr message;

  message.msg = (void *)m;
  do
    {
      ret =
        vr_channel_send (channel_list[channel_index],
                        (void *) &message,
                        sizeof (msghldr),
                        0);
      if (ret == sizeof (msghldr))
        break;                  /* success */
      putchar (CHANNEL_FULL_CHAR);
      fflush (stdout);
      state_change_d(STATE_WAITING_FOR_CHANNEL,
                     channel_index,
                     gr_frame_number);
      vr_sleep (0, SLEEPTM); /* sleep for a bit to wait for channel to drain */
    }
  while (1);
  state_change(STATE_PROCESSING, gr_frame_number);
  return 0;
}


/* 2D Graphics API */

void
gr_start_new_frame (int frame_number)
{
  gr_message *m = gr_allocate_message (GR_CMD_NEW_FRAME);
  m->msg.f.frame_number = frame_number;
  gr_frame_number = frame_number;
  gr_send_msg (m);
}


void
gr_set_draw_char (char ch)
{
  gr_message *m = gr_allocate_message (GR_CMD_SET_DRAW_CHAR);
  m->msg.s.str[0] = ch;
  gr_send_msg (m);
}


#ifndef USE_IMAGE_VIEWER
void
gr_putstr (int x, int y, char *str)
{
  gr_message *m = gr_allocate_message (GR_CMD_PUT_STRING);
  m->msg.s.p.x = x;
  m->msg.s.p.y = y;
  strncpy(&m->msg.s.str[0], str, MAXSTRLN);
  //m->msg.s.str[MAXSTRLN-1] = '\0'; /* make sure NUL terminated */
  gr_send_msg (m);
}
#endif


void
gr_show (void)
{
  gr_message *m = gr_allocate_message (GR_CMD_SHOW);
  gr_send_msg (m);

  if (NUM_CHANNELS > 1) {
  /* If there is more than one rendering cpu,
     load balance the work-load of creating each frame
   */
    if (++channel_index >= NUM_CHANNELS)
      channel_index = 0;
  }

}


/* 3D Graphics API */

void gr_observation_point (real x, real y, real z)
{
  gr_message *m = gr_allocate_message (GR_CMD_OBSERVATION_POINT);
  m->msg.point.x = x;
  m->msg.point.y = y;
  m->msg.point.z = z;
  gr_send_msg (m);
}


void gr_observation_window (real l, real r, real b, real t)
{
  gr_message *m = gr_allocate_message (GR_CMD_OBSERVATION_WINDOW);
  m->msg.box.l = l;
  m->msg.box.r = r;
  m->msg.box.b = b;
  m->msg.box.t = t;
  gr_send_msg (m);
}


void gr_zpoint (real x, real y, real z)
{
  gr_message *m = gr_allocate_message (GR_CMD_ZPOINT);
  m->msg.point.x = x;
  m->msg.point.y = y;
  m->msg.point.z = z;
  gr_send_msg (m);
}


void gr_zmoveto (real x, real y, real z)
{
  gr_message *m = gr_allocate_message (GR_CMD_ZMOVETO);
  m->msg.point.x = x;
  m->msg.point.y = y;
  m->msg.point.z = z;
  gr_send_msg (m);
}


void gr_zlineto (real x, real y, real z)
{
  gr_message *m = gr_allocate_message (GR_CMD_ZLINETO);
  m->msg.point.x = x;
  m->msg.point.y = y;
  m->msg.point.z = z;
  gr_send_msg (m);
}


void gr_zrmoveto (real x, real y, real z)
{
  gr_message *m = gr_allocate_message (GR_CMD_ZRMOVETO);
  m->msg.point.x = x;
  m->msg.point.y = y;
  m->msg.point.z = z;
  gr_send_msg (m);
}


void gr_zrlineto (real x, real y, real z)
{
  gr_message *m = gr_allocate_message (GR_CMD_ZRLINETO);
  m->msg.point.x = x;
  m->msg.point.y = y;
  m->msg.point.z = z;
  gr_send_msg (m);
}


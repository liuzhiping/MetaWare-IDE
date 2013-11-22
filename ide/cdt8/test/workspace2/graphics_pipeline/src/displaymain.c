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

#include "track_state.h"
#include "plot.h"
#include "common.h"


raster primary_raster __attribute__ ((section (".frame"), aligned (32)));


#ifndef USE_ARC_CHANNEL
static int channel_index = 0;
static vr_channel_t channel_list[] = IN_CHANNEL_LIST;
#define NUM_CHANNELS (sizeof(channel_list)/sizeof(vr_channel_t))
#endif


/* display thread entrypoint */
void
_displaymain (void)
{
  int ret;
  display_msg msg;

  state_change(STATE_INITIALIZING, 0);

  erase_raster(&primary_raster);

#ifndef USE_IMAGE_VIEWER
  screen_clear(); /* raster & display are all blank */
#else
  printf ("Graphics Pipeline Demonstration v1.01\n");
  printf ("ARC International\n");
  printf ("display thread\n");
  printf ("Use address 0x%08x in the MetaWare debugger to view the image.\n",
                                               (int)&primary_raster);
  printf ("The image is %dx%d, 8-bit gray scale\n", MAXC, MAXR);
  printf ("\n");
#endif

  do
    {

#ifdef USE_ARC_CHANNEL
      ret = vr_channel_receive (display_channel,
                               (void *) &msg,
                               sizeof (display_msg),
                               0);
#else
      ret = vr_channel_receive (channel_list[channel_index],
                               (void *) &msg,
                               sizeof (display_msg),
                               0);
#endif
      if (ret == sizeof (display_msg))
        {
#ifdef USE_ARC_CHANNEL
          state_change_d(STATE_PROCESSING, 0, msg.frame_number);
#else
          state_change_d(STATE_PROCESSING, channel_index, msg.frame_number);
#endif

#ifdef USE_IMAGE_VIEWER
          memcpy((void *)&primary_raster,
                 (void *)msg.frame,
                 sizeof(raster));

          //printf ("\nFrame %d, raster 0x%08x ",
          //        msg.frame_number,
          //        (int) msg.frame);
          //fflush (stdout);
#else
          show_raster(&primary_raster, (raster *)msg.frame);
#endif

          ret = vr_memory_pool_put_node (raster_pool, msg.frame);
          if (ret < 0) {
            printf ("ERROR: vr_memory_pool_put_node returned %d\n", ret);
            vr_artificial_crash ();
          }

          /* TODO: Could wait here for FPS rate so that each frame
             appears only at that FPS rate. In simulation, no need.
           */

#ifndef USE_ARC_CHANNEL
         if (NUM_CHANNELS > 1) {
           /* If there is more than one rendering cpu,
              pick up each frame alternately from each of
              the channels arriving from a render cpu, to preserve
              frame order.
            */
           if (++channel_index >= NUM_CHANNELS)
             channel_index = 0;
         }
#endif

        }
      else if (ret <= 0) /* nothing received */
        {
          state_change(STATE_IDLE, 0);
          vr_sleep (0, SLEEPTM);
          /* sleep for a bit until data appears on channel */
        }
      else
        {
          /* ERROR */
          printf ("ERROR: vr_channel_receive returned %d\n", ret);
          vr_artificial_crash ();
        }

    }
  while (1);

  /* shouldn't get here */
  vr_artificial_crash ();

}


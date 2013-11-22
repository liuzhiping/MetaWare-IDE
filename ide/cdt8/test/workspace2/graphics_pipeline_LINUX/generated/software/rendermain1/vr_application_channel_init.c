
/**************************************************************************
 *
 * Generated by mcsg
 * Version 1.0.0
 * Build trunk
 * Thu May 29 15:34:38 PDT 2008
 * ARC International (c) 2008
 * This is a generated file. All manual edits will be lost.
 *
 **************************************************************************/



#include "vr_application_include.h"


static int vr_meta_channels_init (void);

VR_CONST int vr_n_channels = 4;
uint32_t render_channel0_dest_threads [] = { swarch_thd_rendermain0_thread_id };
uint32_t display_channel0_dest_threads [] = { swarch_thd_displaymain_thread_id };
uint32_t render_channel1_dest_threads [] = { swarch_thd_rendermain1_thread_id };
uint32_t display_channel1_dest_threads [] = { swarch_thd_displaymain_thread_id };
VR_CONST VrMetaChannel * vr_meta_channel[5] = { 0, 0, 0, 0, 0 };
void * vr_channel[5] = { 0, 0, 0, 0, 0 };

/* Auxiliary code for channel render_channel1_id */
VrCircularBuffer_render_channel1 render_channel1_channel_impl __attribute__ ((section(".render_channel1"), aligned(4)));
int render_channel1_channel_impl_send_count = 131072;
int render_channel1_channel_impl_full_count = 0;
int render_channel1_channel_impl_recv_count = 131072;

/* Auxiliary code for channel display_channel1_id */
VrCircularBuffer_display_channel1 display_channel1_channel_impl __attribute__ ((section(".display_channel1"), aligned(4)));
int display_channel1_channel_impl_send_count = 196608;
int display_channel1_channel_impl_full_count = 0;
int display_channel1_channel_impl_recv_count = 196608;

int
vr_channel_setup_global (void)
{

  if (vr_meta_channels_init () < 0)
    return -1;

  return 0;
}

int
vr_channel_setup_global_as_receiver (void)
{

  return 0;
}

int
vr_channel_setup_sender_process (void)
{
  void ** __vr_channel_impl_ptr VR_UNUSED_VARIABLE_ATTRIBUTE = 0;


  do
    {
      __vr_channel_impl_ptr = &vr_get_channel_impl (display_channel1_id);
      * __vr_channel_impl_ptr = (void *) (&display_channel1_channel_impl);
      VrCircularBufferNoSync_setup ((VrCircularBuffer_display_channel1 *) (((char *) (* __vr_channel_impl_ptr))));
    }
  while (0);

  return 0;
}

int
vr_channel_setup_receiver_process (void)
{
  void ** __vr_channel_impl_ptr VR_UNUSED_VARIABLE_ATTRIBUTE = 0;


  do
    {
      __vr_channel_impl_ptr = &vr_get_channel_impl (render_channel1_id);
      * __vr_channel_impl_ptr = (void *) (&render_channel1_channel_impl);
    }
  while (0);

  return 0;
}

int
vr_channel_setup_sender_thread (uint32_t thread_id)
{

  switch (thread_id)
    {
    case swarch_thd_rendermain1_thread_id:

      do
        {
          void ** __vr_channel_impl_ptr VR_UNUSED_VARIABLE_ATTRIBUTE = &vr_get_channel_impl (display_channel1_id);
        }
      while (0);

      break;
    }

  return 0;
}

int
vr_channel_setup_receiver_thread (uint32_t thread_id)
{

  switch (thread_id)
    {
    case swarch_thd_rendermain1_thread_id:

      do
        {
          void ** __vr_channel_impl_ptr VR_UNUSED_VARIABLE_ATTRIBUTE = &vr_get_channel_impl (render_channel1_id);
        }
      while (0);

      break;
    }

  return 0;
}

int
vr_meta_channels_init (void)
{

  if (vr_meta_channel_init (0, "NULL_channel_id", 0, 0) < 0)
    return -1;

  if (vr_meta_channel_init (render_channel0_id, "render_channel0", 1, render_channel0_dest_threads) < 0)
    return -1;
  if (vr_meta_channel_init (display_channel0_id, "display_channel0", 1, display_channel0_dest_threads) < 0)
    return -1;
  if (vr_meta_channel_init (render_channel1_id, "render_channel1", 1, render_channel1_dest_threads) < 0)
    return -1;
  if (vr_meta_channel_init (display_channel1_id, "display_channel1", 1, display_channel1_dest_threads) < 0)
    return -1;

  return 0;
}

extern VR_CONST int vr_n_channels;
extern void * vr_channel[];
extern VR_CONST VrMetaChannel * vr_meta_channel[];

extern inline int
vr_channel_send (vr_channel_t __vr_channel_id, void * __vr_msg, int __vr_msg_size, int flags)
{
#define __vr_channel_impl ((void *) vr_get_channel_impl (__vr_channel_id))
  int __vr_result = -1;

  switch (__vr_channel_id)
    {

    case display_channel0_id:
      do 
        {
          VrCircularBuffer_display_channel0* cir_buf = ((VrCircularBuffer_display_channel0 *) __vr_channel_impl);
          VrCircularBufferNoSync_rp_write (cir_buf, 256, __vr_msg, __vr_msg_size, 1, 0, __vr_result, __vr_result);
          if (__vr_result==__vr_msg_size) {
            display_channel0_channel_impl_send_count++;
          } else {
            display_channel0_channel_impl_full_count++;
          }
        } while(0);
      break;
    }
  return __vr_result;
#undef __vr_channel_impl
}

extern inline int
vr_channel_receive (vr_channel_t __vr_channel_id, void * __vr_msg, int __vr_msg_size, int flags)
{
#define __vr_channel_impl ((void *) vr_get_channel_impl (__vr_channel_id))
  int __vr_result = -1;

  switch (__vr_channel_id)
    {

    case render_channel0_id:
      do 
        {
          VrCircularBufferNoSync_rp_read (((VrCircularBuffer_render_channel0 *) __vr_channel_impl), 512, __vr_msg, __vr_msg_size, 1, 0, __vr_result, __vr_result);

          if (__vr_result==__vr_msg_size) {
            render_channel0_channel_impl_recv_count++;
          }
        } while(0);
      break;
    }
  return __vr_result;
#undef __vr_channel_impl
}

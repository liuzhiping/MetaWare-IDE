extern VR_CONST int vr_n_channels;
extern void * vr_channel[];
extern VR_CONST VrMetaChannel * vr_meta_channel[];

extern inline int
vr_channel_send (vr_channel_t __vr_channel_id, void * __vr_msg, int __vr_msg_size, int flags)
{
#define __vr_channel_impl ((void *) vr_get_channel_impl (__vr_channel_id))
  int __vr_result = -1;

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

    case display_channel0_id:
      do 
        {
          VrCircularBufferNoSync_rp_read (((VrCircularBuffer_display_channel0 *) __vr_channel_impl), 256, __vr_msg, __vr_msg_size, 1, 0, __vr_result, __vr_result);

          if (__vr_result==__vr_msg_size) {
            display_channel0_channel_impl_recv_count++;
          }
        } while(0);
      break;

    case display_channel1_id:
      do 
        {
          VrCircularBufferNoSync_rp_read (((VrCircularBuffer_display_channel1 *) __vr_channel_impl), 256, __vr_msg, __vr_msg_size, 1, 0, __vr_result, __vr_result);

          if (__vr_result==__vr_msg_size) {
            display_channel1_channel_impl_recv_count++;
          }
        } while(0);
      break;
    }
  return __vr_result;
#undef __vr_channel_impl
}
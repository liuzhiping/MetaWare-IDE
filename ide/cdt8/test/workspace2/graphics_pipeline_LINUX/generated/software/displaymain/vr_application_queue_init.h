extern VR_CONST int vr_n_queues;
extern void * vr_queue[];
extern inline int
vr_queue_enqueue (vr_queue_t __vr_queue_id, void * __vr_node)
{
#define __vr_queue_impl ((void *) vr_get_queue_impl (__vr_queue_id))
  int __vr_result = 0;
  return __vr_result;
#undef __vr_queue_impl
}
extern inline void *
vr_queue_dequeue (vr_queue_t __vr_queue_id)
{
#define __vr_queue_impl ((void *) vr_get_queue_impl (__vr_queue_id))
  void * __vr_result = 0;
  return __vr_result;
#undef __vr_queue_impl
}
extern inline int
vr_queue_is_empty (vr_queue_t __vr_queue_id)
{
#define __vr_queue_impl ((void *) vr_get_queue_impl (__vr_queue_id))
  int __vr_result = 0;
  return __vr_result;
#undef __vr_queue_impl
}
extern inline int
vr_queue_get_size (vr_queue_t __vr_queue_id)
{
#define __vr_queue_impl ((void *) vr_get_queue_impl (__vr_queue_id))
  int __vr_result = 0;
  return __vr_result;
#undef __vr_queue_impl
}

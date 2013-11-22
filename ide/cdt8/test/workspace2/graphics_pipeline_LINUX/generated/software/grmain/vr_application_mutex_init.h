extern VR_CONST int vr_n_mutexes;
extern void * vr_mutex[];
extern inline int
vr_mutex_lock (vr_mutex_t __vr_mutex_id)
{
#define __vr_mutex_impl ((void *) vr_get_mutex_impl (__vr_mutex_id))
  int __vr_result = -1;
  return __vr_result;
#undef __vr_mutex_impl
}
extern inline 
int
vr_mutex_trylock (vr_mutex_t __vr_mutex_id)
{
#define __vr_mutex_impl ((void *) vr_get_mutex_impl (__vr_mutex_id))
  int __vr_result = -1;
  return __vr_result;
#undef __vr_mutex_impl
}
extern inline 
int
vr_mutex_unlock (vr_mutex_t __vr_mutex_id)
{
#define __vr_mutex_impl ((void *) vr_get_mutex_impl (__vr_mutex_id))
  int __vr_result = -1;
  return __vr_result;
#undef __vr_mutex_impl
}

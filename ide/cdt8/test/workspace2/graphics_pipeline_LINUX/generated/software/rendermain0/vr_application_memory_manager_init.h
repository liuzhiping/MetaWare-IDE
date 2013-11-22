extern VR_CONST unsigned int vr_n_memory_pools;
extern void * vr_memory_pool[];
extern VR_CONST VrMetaMemoryPool * vr_meta_memory_pool[];
extern inline void *
vr_memory_pool_get_node (vr_memory_pool_t __vr_mempool_id)
  {
    void * __vr_node = 0;
    #define __vr_mempool_impl ((void *) vr_get_memory_pool_impl (__vr_mempool_id))

    switch (__vr_mempool_id)
      {

      case raster_pool_id:
        do
          {
            int bytes_read;
            VrCircularBuffer_raster_pool* cir_buf = ((VrCircularBuffer_raster_pool *) __vr_mempool_impl);
            VrCircularBufferSync_rp_fixed_size_read (cir_buf, 64, &__vr_node, 4, VrHWMutexNW, 1, 0, bytes_read, bytes_read);
          }
        while (0);
        break;

      case grcmd_pool_id:
        do
          {
            VrStackHWSync_rp_pop ((VrHWNode **) &VrQueue_get_head ((VrQueue_VrHWMutexNW *) __vr_mempool_impl), (VrHWMutexNW *) &VrQueue_get_mutex ((VrQueue_VrHWMutexNW *) __vr_mempool_impl), __vr_node);
          }
        while (0);
        break;
      default:
        vr_set_errno (VR_INVALID_INDEX);
        return 0;
      }

    return (void *) __vr_node;
    #undef __vr_mempool_impl
  }
extern inline int 
vr_memory_pool_put_node (vr_memory_pool_t __vr_mempool_id, void * __vr_node)
  {
    #define __vr_mempool_impl ((void *) vr_get_memory_pool_impl (__vr_mempool_id))

    switch (__vr_mempool_id)
      {

      case raster_pool_id:
        do
          {
            int bytes_written;
            VrCircularBuffer_raster_pool* cir_buf = ((VrCircularBuffer_raster_pool *) __vr_mempool_impl);
            VrCircularBufferNoSync_rp_fixed_size_write (cir_buf, 64, &__vr_node, 4, 1, 0, bytes_written, bytes_written);
          }
        while (0);
        break;

      case grcmd_pool_id:
        do
          {
            VrStackHWSync_push ((VrHWNode **) &VrQueue_get_head ((VrQueue_VrHWMutexNW *) __vr_mempool_impl), (VrHWMutexNW *) &VrQueue_get_mutex ((VrQueue_VrHWMutexNW *) __vr_mempool_impl), __vr_node);
          }
        while (0);
        break;
      default:
        vr_set_errno (VR_INVALID_INDEX);
        return -1;
      }
    return 0;
    #undef __vr_mempool_impl
  }
extern inline int
vr_memory_pool_get_id_from_node (vr_memory_pool_t __vr_mempool_id, void * __vr_node)
  {
    int __vr_index = -1;
    #define __vr_mempool_impl ((void *) vr_get_memory_pool_impl (__vr_mempool_id))

    switch (__vr_mempool_id)
      {

      case raster_pool_id:
        do
          {
            __vr_index = (((vr_uint_t) __vr_node - (vr_uint_t)&raster_pool_mempool_buffer) / 25344);
          }
        while (0);
        break;

      case grcmd_pool_id:
        do
          {
            __vr_index = (((vr_uint_t) __vr_node - (vr_uint_t)&grcmd_pool_mempool_buffer) >> 7);
          }
        while (0);
        break;
      default:
        vr_set_errno (VR_INVALID_INDEX);
        return -1;
      }

    return __vr_index;
    #undef __vr_mempool_impl
  }
extern inline void *
vr_memory_pool_get_node_from_id (vr_memory_pool_t __vr_mempool_id, int __vr_index)
  {
    void * __vr_node = 0;
    #define __vr_mempool_impl ((void *) vr_get_memory_pool_impl (__vr_mempool_id))

    switch (__vr_mempool_id)
      {

      case raster_pool_id:
        do
          {
            __vr_node = (void *)((vr_uint_t)&raster_pool_mempool_buffer + (__vr_index * 25344));
          }
        while (0);
        break;

      case grcmd_pool_id:
        do
          {
            __vr_node = (void *)((vr_uint_t)&grcmd_pool_mempool_buffer + (__vr_index << 7));
          }
        while (0);
        break;
      default:
        vr_set_errno (VR_INVALID_INDEX);
        return 0;
      }

    return __vr_node;
    #undef __vr_mempool_impl
  }

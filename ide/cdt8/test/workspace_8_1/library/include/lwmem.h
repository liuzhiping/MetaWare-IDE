#ifndef __lwmem_h__
#define __lwmem_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: lwmem.h
***
*** Comments: 
***    This file contains the structure definitions and constants for an
*** application which will be using MQX.
***    All compiler provided header files must be included before mqx.h.
*** 
***
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                    DATATYPE DECLARATIONS
*/

typedef pointer _lwmem_pool_id;

/*
** LWMEM POOL STRUCT
**
** This structure is used to define the information that defines what
** defines a light weight memory pool.
*/
typedef struct lwmem_pool_struct
{
   /* Used to link light weight memory pools together */   
   QUEUE_ELEMENT_STRUCT LINK;

   /* Used to verify if handle is valid */
   _mqx_uint            VALID;

   /* The address of the start of Memory Pool blocks. */
   pointer              POOL_ALLOC_START_PTR;

   /* The address of the end of the Memory Pool */
   pointer              POOL_ALLOC_END_PTR;

   /* The address of the head of the memory pool free list */
   pointer              POOL_FREE_LIST_PTR;

   /* Pointer used when walking free list by lwmem_alloci */
   pointer              POOL_ALLOC_PTR;

   /* Pointer used when freeing memory by lwmem_free */
   pointer              POOL_FREE_PTR;

   /* Pointer used when testing memory by lwmem_test */
   pointer              POOL_TEST_PTR;

   /* Pointer used when testing memory by lwmem_test */
   pointer              POOL_TEST2_PTR;

   /* Pointer used by lwmem_cleanup_internal */
   pointer              POOL_DESTROY_PTR;

} LWMEM_POOL_STRUCT, _PTR_ LWMEM_POOL_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                  PROTOTYPES OF FUNCTIONS
*/

#ifdef __cplusplus
extern "C" {
#endif
#ifndef __TAD_COMPILE__

extern pointer          _lwmem_alloc(_mem_size);
extern pointer          _lwmem_alloc_zero(_mem_size);
extern pointer          _lwmem_alloc_from(_lwmem_pool_id, _mem_size);
extern pointer          _lwmem_alloc_zero_from(_lwmem_pool_id, _mem_size);

extern pointer          _lwmem_alloc_system(_mem_size);
extern pointer          _lwmem_alloc_system_zero(_mem_size);
extern pointer          _lwmem_alloc_system_from(_lwmem_pool_id, _mem_size);
extern pointer          _lwmem_alloc_system_zero_from(_lwmem_pool_id,
   _mem_size);

extern _lwmem_pool_id   _lwmem_create_pool(LWMEM_POOL_STRUCT_PTR, pointer,
   _mem_size);
extern _mqx_uint        _lwmem_free(pointer);
extern _mqx_uint        _lwmem_get_size(pointer);
extern _lwmem_pool_id   _lwmem_set_default_pool(_lwmem_pool_id);
extern _mqx_uint        _lwmem_test(_lwmem_pool_id _PTR_, pointer _PTR_);
extern _mqx_uint        _lwmem_transfer(pointer, _task_id, _task_id);

#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: lwm_xfer.c
***
*** Comments :
***   Thie file contains the function for tranfering ownership of a memory
*** block between tasks.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
 * 
 * Function Name    : _lwmem_transfer
 * Returned Value   : _mqx_uint MQX_OK, or a mqx error code.
 * Comments         :
 *   This routine transfers the ownership of a block of memory from
 *   an owner task to another task.
 *
 *END*--------------------------------------------------------*/

_mqx_uint _lwmem_transfer
   (  
      /* [IN] the memory block whose ownership is to be transferred */
      pointer  memory_ptr,
      
      /* [IN] the source (owner) task id */
      _task_id source_id,
      
      /* [IN] the target (new owner) task id */
      _task_id target_id
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWMEM_BLOCK_STRUCT_PTR  block_ptr;
   TD_STRUCT_PTR          source_td;
   TD_STRUCT_PTR          target_td;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE4(KLOG_lwmem_transfer, memory_ptr, source_id, target_id);

#if MQX_CHECK_ERRORS
   if (memory_ptr == NULL) {
      _task_set_error(MQX_INVALID_POINTER);
      _KLOGX2(KLOG_lwmem_transfer, MQX_INVALID_POINTER);
      return(MQX_INVALID_POINTER);
   } /* Endif */
#endif

   /* Verify the block */
   block_ptr = GET_LWMEMBLOCK_PTR(memory_ptr);

   source_td = (TD_STRUCT_PTR)_task_get_td(source_id);
   target_td = (TD_STRUCT_PTR)_task_get_td(target_id);
#if MQX_CHECK_ERRORS
   if ( (source_td == NULL) || (target_td == NULL) ) {
      _task_set_error(MQX_INVALID_TASK_ID);
      _KLOGX2(KLOG_lwmem_transfer, MQX_INVALID_TASK_ID);
      return(MQX_INVALID_TASK_ID);
   } /* Endif */
#endif
#if MQX_CHECK_ERRORS
   if (block_ptr->U.TASK_ID != source_td->TASK_ID) {
      _task_set_error(MQX_NOT_RESOURCE_OWNER);
      return(MQX_NOT_RESOURCE_OWNER);
   } /* Endif */
#endif

   block_ptr->U.TASK_ID = target_td->TASK_ID;

   _KLOGX2(KLOG_lwmem_transfer, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */

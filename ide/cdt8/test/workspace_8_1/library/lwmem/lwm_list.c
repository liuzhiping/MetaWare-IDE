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
*** File: lwm_list.c
***
*** Comments :
***  This file finds the next owned block for a given task descriptor
*** from the kernel pool, it is only called if the LWMEM allocator is
*** being used as the default memory allocator.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_get_next_block_internal
* Returned Value   : pointer.
* Comments         :
*    Find the next block associated with the specified Task
*
*END*---------------------------------------------------------*/

pointer _lwmem_get_next_block_internal
   ( 
      /* [IN] the td whose blocks are being looked for */
      TD_STRUCT_PTR  td_ptr,

      /* [IN] the block last obtained */
      pointer        in_block_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWMEM_POOL_STRUCT_PTR  lwmem_pool_ptr;
   LWMEM_BLOCK_STRUCT_PTR block_ptr = in_block_ptr;

   _GET_KERNEL_DATA(kernel_data);

   lwmem_pool_ptr = kernel_data->KERNEL_LWMEM_POOL;
   if (block_ptr == NULL) {
      block_ptr = lwmem_pool_ptr->POOL_ALLOC_START_PTR;
   /* Start CR 338 */
   } else {
      block_ptr = GET_LWMEMBLOCK_PTR(in_block_ptr);
      block_ptr = (LWMEM_BLOCK_STRUCT_PTR)((uchar_ptr)block_ptr + 
         block_ptr->BLOCKSIZE);
   /* End CR 338 */
   } /* Endif */

   _int_disable();
   while ((uchar_ptr)block_ptr < (uchar_ptr)lwmem_pool_ptr->POOL_ALLOC_END_PTR){
      if (block_ptr->U.TASK_ID == td_ptr->TASK_ID) {
         /* This block is owned by the target task */
         _int_enable();
         return((pointer)((uchar_ptr)block_ptr + sizeof(LWMEM_BLOCK_STRUCT)));
      } /* Endif */
      block_ptr = (LWMEM_BLOCK_STRUCT_PTR)((uchar_ptr)block_ptr + 
         block_ptr->BLOCKSIZE);
   } /* Endwhile */
   _int_enable();

   return(NULL);
   
} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */

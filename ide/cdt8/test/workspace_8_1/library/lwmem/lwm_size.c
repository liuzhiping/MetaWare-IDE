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
*** File: lwm_size.c
***
*** Comments :
***   This file contains the function that returns the size of a memory block.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _lwmem_get_size
* Returned Value   : _mem_size
* Comments         :
*    This routine returns the allocated size (in bytes) of a block
*    allocated using the MQX storage allocator (_lwmem_alloc).
*
*END*--------------------------------------------------------*/

_mem_size _lwmem_get_size
   (
      /* [IN] the address of the memory block whose size is wanted */
      pointer mem_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWMEM_BLOCK_STRUCT_PTR block_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
#if MQX_CHECK_ERRORS
   if (mem_ptr == NULL) {
      _task_set_error(MQX_INVALID_POINTER);
      return(0);
   } /* Endif */
#endif
   
   /* Compute the start of the block  */
   block_ptr = GET_LWMEMBLOCK_PTR(mem_ptr);
   /* The size includes the block overhead, which the user is not
   ** interested in. If the size is less than the overhead,
   ** then there is a bad block or bad block pointer.
   */
#if MQX_CHECK_ERRORS
   if ( block_ptr->BLOCKSIZE <= (_mem_size)sizeof(LWMEM_BLOCK_STRUCT) ) {
      _task_set_error(MQX_INVALID_POINTER);
      return(0);
   } /* Endif */
#endif

   return ( block_ptr->BLOCKSIZE - (_mem_size)sizeof(LWMEM_BLOCK_STRUCT) );

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */

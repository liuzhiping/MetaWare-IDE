/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: mem_size.c
***
*** Comments :
***   This file contains the function that returns the size of a memory block.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_get_size
* Returned Value   : _mem_size
*   MQX_INVALID_POINTER, MQX_CORRUPT_STORAGE_POOL, MQX_INVALID_CHECKSUM, 
* Comments         :
*    This routine returns the allocated size (in bytes) of a block
*    allocated using the MQX storage allocator (_mem_alloc).
*
*END*--------------------------------------------------------*/

_mem_size _mem_get_size
   (
      /* [IN] the address of the memory block whose size is wanted */
      pointer mem_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   STOREBLOCK_STRUCT_PTR  block_ptr;
   _mem_size              size;
   
   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (mem_ptr == NULL) {
      _task_set_error(MQX_INVALID_POINTER);
      return(0);
   } /* Endif */
#endif
   
   /* Compute the start of the block  */
   block_ptr = GET_MEMBLOCK_PTR(mem_ptr);
   
#if MQX_CHECK_ERRORS
   if (! _MEMORY_ALIGNED(block_ptr)) {
      _task_set_error(MQX_INVALID_POINTER);
      return(0);
   } /* Endif */
#endif

   size = block_ptr->BLOCKSIZE;

#if MQX_CHECK_ERRORS
   /* For all free blocks, a check is made to ensure that the user
    * has not corrupted the storage pool. This is done by checking the
    * 'magic value', which should not be corrupted. Alternatively, the
    * user could have passed in an invalid memory pointer.
    */
   if ( BLOCK_IS_FREE(block_ptr) ) {
      kernel_data->KD_POOL.POOL_BLOCK_IN_ERROR = block_ptr;
      _task_set_error(MQX_CORRUPT_STORAGE_POOL);
      return(0);
   } /* Endif */
   
#endif

#if MQX_CHECK_VALIDITY
   if ( (! VALID_CHECKSUM(block_ptr)) ) {
      kernel_data->KD_POOL.POOL_BLOCK_IN_ERROR = block_ptr;
      _task_set_error(MQX_INVALID_CHECKSUM);
      return(0);
   } /* Endif */
#endif
   
   /* The size includes the block overhead, which the user is not
   ** interested in. If the size is less than the overhead,
   ** then there is a bad block or bad block pointer.
   */
#if MQX_CHECK_ERRORS
   if ( size <= (_mem_size)FIELD_OFFSET(STOREBLOCK_STRUCT,USER_AREA) ) {
      kernel_data->KD_POOL.POOL_BLOCK_IN_ERROR = block_ptr;
      _task_set_error(MQX_CORRUPT_STORAGE_POOL);
      return(0);
   } /* Endif */
#endif

   return ( size - (_mem_size)FIELD_OFFSET(STOREBLOCK_STRUCT,USER_AREA) );

} /* Endbody */

/* EOF */

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
*** File: mem_util.c
***
*** Comments :
***   This file contains functions that return information about the
*** memory pool.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_get_highwater
* Returned Value   : pointer
* Comments         :
*   This function returns the highest memory address ever used in the
*   kernel memory area.
*
*END*--------------------------------------------------------*/

pointer _mem_get_highwater
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

   return (kernel_data->KD_POOL.POOL_HIGHEST_MEMORY_USED);

} /* Endbody */


/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_get_highwater_pool
* Returned Value   : pointer
* Comments         :
*   This function returns the highest memory address ever used in the
*   specified memory pool.
*
*END*--------------------------------------------------------*/

pointer _mem_get_highwater_pool
   (
      _mem_pool_id pool_id
   )
{ /* Body */
   MEMPOOL_STRUCT_PTR     mem_pool_ptr = (MEMPOOL_STRUCT_PTR)pool_id;

   return (mem_pool_ptr->POOL_HIGHEST_MEMORY_USED);

} /* Endbody */


/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_get_error
* Returned Value   : pointer
* Comments         :
*   This function returns the last memory block which 
*   caused a corrupt memory pool error in kernel data.
*
*END*--------------------------------------------------------*/

pointer _mem_get_error
   (
      void
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR kernel_data;
   register pointer                user_ptr;

   _GET_KERNEL_DATA(kernel_data);

   user_ptr = (pointer)((uchar_ptr)kernel_data->KD_POOL.POOL_BLOCK_IN_ERROR +
      FIELD_OFFSET(STOREBLOCK_STRUCT,USER_AREA));
   return (user_ptr);

} /* Endbody */


/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_get_error_pool
* Returned Value   : pointer
* Comments         :
*   This function returns the last memory block which 
*   caused a corrupt memory pool error in the specified pool.
*
*END*--------------------------------------------------------*/

pointer _mem_get_error_pool
   (
      _mem_pool_id pool_id
   )
{ /* Body */
   MEMPOOL_STRUCT_PTR  mem_pool_ptr = (MEMPOOL_STRUCT_PTR)pool_id;
   register pointer    user_ptr;

   user_ptr = (pointer)((uchar_ptr)mem_pool_ptr->POOL_BLOCK_IN_ERROR +
      FIELD_OFFSET(STOREBLOCK_STRUCT,USER_AREA));
   return (user_ptr);

} /* Endbody */

/* EOF */

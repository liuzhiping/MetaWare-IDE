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
*** File: lwm_xftd.c
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
 * Function Name    : _lwmem_transfer_td_internal
 * Returned Value   : _mqx_uint MQX_OK, or a mqx error code.
 * Comments         :
 *   This routine transfers the ownership of a block of memory from
 *   an owner task to another task.
 *
 *END*--------------------------------------------------------*/

_mqx_uint _lwmem_transfer_td_internal
   (  
      /* [IN] the memory block whose ownership is to be transferred */
      pointer       memory_ptr,
      
      /* [IN] the source (owner) td */
      TD_STRUCT_PTR source_td,
      
      /* [IN] the target (new owner) td */
      TD_STRUCT_PTR target_td
   )
{ /* Body */
   LWMEM_BLOCK_STRUCT_PTR  block_ptr;

   block_ptr = GET_LWMEMBLOCK_PTR(memory_ptr);
   block_ptr->U.TASK_ID = target_td->TASK_ID;

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */

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
*** File: lwm_xfri.c
***
*** Comments :
***   This file contains the function for transferring memory between
*** tasks.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
 * 
 * Function Name    : _lwmem_transfer_internal
 * Returned Value   : _mqx_uint MQX_OK or an MQX error code
 * Comments         :
 *   This routine transfers the ownership of a block of memory from
 *   an owner task to another task.  
 *
 *END*--------------------------------------------------------*/

void _lwmem_transfer_internal
   (
     /* [IN] the address of the USER_AREA in the memory block to transfer */
     pointer       memory_ptr,

     /* [IN] the target task descriptor */
     TD_STRUCT_PTR target_td
   )
{ /* Body */
   LWMEM_BLOCK_STRUCT_PTR  block_ptr;

   /* Verify the block */
   block_ptr = GET_LWMEMBLOCK_PTR(memory_ptr);
   block_ptr->U.TASK_ID = target_td->TASK_ID;
 
} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */

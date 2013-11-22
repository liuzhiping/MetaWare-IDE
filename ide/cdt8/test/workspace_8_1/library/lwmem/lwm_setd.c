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
*** File: lwm_setd.c
***
*** Comments :
***   Thie file contains the function for setting the default pool id.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwmem.h"
#include "lwmemprv.h"

#if MQX_USE_LWMEM
/*FUNCTION*-----------------------------------------------------
 * 
 * Function Name    : _lwmem_set_default_pool
 * Returned Value   : _lwmem_pool_id the old value.
 * Comments         :
 *   This routine sets the value of the default MQX light weight
 *   memory pool.
 *
 *END*--------------------------------------------------------*/

_lwmem_pool_id _lwmem_set_default_pool
   (  
      /* [IN] the source (owner) task id */
      _lwmem_pool_id pool_id
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   _lwmem_pool_id         old_pool_id;


   _GET_KERNEL_DATA(kernel_data);

   old_pool_id = kernel_data->KERNEL_LWMEM_POOL;
   kernel_data->KERNEL_LWMEM_POOL = pool_id;
   return(old_pool_id);

} /* Endbody */
#endif /* MQX_USE_LWMEM */

/* EOF */

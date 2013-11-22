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
*** File: pa_sallo.c
***
*** Comments :
***  This file contains the function for allocating a partition block
*** owned by the system.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "part.h"
#include "part_prv.h"

#if MQX_USE_PARTITIONS
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _partition_alloc_system
* Returned Value   : pointer
* Comments         :
*    Allocates a partition block owned by the system.
*
*END*--------------------------------------------------------*/

pointer _partition_alloc_system
   (
      /* [IN] the partition from which to obtain the memory block */
      _partition_id partition
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   pointer                result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_partition_alloc_system, partition);

   result = _partition_alloc_internal((PARTPOOL_STRUCT_PTR)partition, 
      SYSTEM_TD_PTR(kernel_data));

   _KLOGX2(KLOG_partition_alloc_system, result);
   return (result);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */

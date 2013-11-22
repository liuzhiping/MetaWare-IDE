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
*** File: pa_alloc.c
***
*** Comments :
***  This file contains the function for allocating a block from a partition.
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
* Function Name    : _partition_alloc
* Returned Value   : pointer
*    NULL is returned upon error.
* Comments         :
*    returns a fixed size memory block from the partition.
*
*END*---------------------------------------------------------*/

pointer _partition_alloc
   (
      /* [IN] the partition to obtain the memory block from */
      _partition_id partition
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   pointer                result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_partition_alloc, partition);

   result = _partition_alloc_internal((PARTPOOL_STRUCT_PTR)partition,
      kernel_data->ACTIVE_PTR);   

   _KLOGX2(KLOG_partition_alloc, result);

   return(result);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */

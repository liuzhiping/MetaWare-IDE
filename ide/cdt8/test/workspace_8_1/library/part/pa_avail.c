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
*** File: pa_avail.c
***
*** Comments :
***  This file contains the function for returning how many blocks are
*** available in a partition.
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
* Function Name    : _partition_get_free_blocks
* Returned Value   : _mqx_uint
*    MAX_MQX_UINT is returned upon error
* Comments         :
*    returns the number of free blocks in the partition
*
*END*---------------------------------------------------------*/

_mqx_uint _partition_get_free_blocks
   (
      /* [IN] the partition to obtaint information about */
      _partition_id partition
   )
{ /* Body */
   PARTPOOL_STRUCT_PTR   partpool_ptr = (PARTPOOL_STRUCT_PTR)partition;

#if MQX_CHECK_VALIDITY
   if (partpool_ptr->POOL.VALID != PARTITION_VALID) {
      _task_set_error(PARTITION_INVALID);
      return(MAX_MQX_UINT);
   } /* Endif */
#endif

   return(partpool_ptr->AVAILABLE);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */

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
*** File: _mem_sizeess.c
***
*** Comments :
***    This file contains the function that tests the system memory pool 
*** validity.
*** It checks for incorrect checksums, and incorrect memory pointers.
*** This function can run concurrently with any other memory functions.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_test
* Returned Value   : _mqx_uint
*    A task error code on error, MQX_OK if no error
*    CORRUPT_STORAGE_POOL_POINTERS, CORRUPT_STORAGE_POOL,
*    INVALID_CHECKSUM, CORRUPT_STORAGE_POOL_FREE_LIST
* Comments         :
*   This function checks the system's memory pool for any errors.
*
*END*--------------------------------------------------------*/

_mqx_uint _mem_test
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   _mqx_uint              result;


   _GET_KERNEL_DATA(kernel_data);

   result = _mem_test_pool((_mem_pool_id)&kernel_data->KD_POOL);

   return(result);

} /* Endbody */

/* EOF */

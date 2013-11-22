/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: io_pcb2.c
***
*** Comments:      
***   This file contains the functions for use with IO PCB device drivers.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "part.h"
#include "fio.h"
#include "io.h"
#include "io_prv.h"
#include "io_pcb.h"
#include "iopcbprv.h"


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _io_pcb_destroy_pool
* Returned Value   : MQX_OK or an error code
* Comments         :
*   Destroys a pool of pcbs.
*END*-------------------------------------------------------------------------*/

_mqx_uint _io_pcb_destroy_pool
   (
      /* The PCB pool to destroy */
      _io_pcb_pool_id pool
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   IO_PCB_POOL_STRUCT_PTR pool_ptr = IO_PCB_GET_POOL_PTR(pool);
   _mqx_uint              result;
   
#if MQX_CHECK_VALIDITY
   if (pool_ptr->VALID != IO_PCB_VALID) {
      return(IO_PCB_POOL_INVALID);
   }/* Endif */
#endif

   pool_ptr->VALID = 0;
  
   result = _partition_destroy(pool_ptr->PARTITION);
   if (result != MQX_OK ) {
      return(result);
   }/* Endif */

  _lwsem_destroy(&pool_ptr->PCB_LWSEM);
   _GET_KERNEL_DATA(kernel_data);
  _queue_unlink((QUEUE_STRUCT_PTR)(&kernel_data->IO_PCB_POOLS), 
     &pool_ptr->QUEUE);
  return(_mem_free(pool_ptr));

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _io_pcb_test
* Returned Value   : MQX_OK or an error code
* Comments         :
*   test all pcb pools
*END*-------------------------------------------------------------------------*/

_mqx_uint _io_pcb_test
   (
      /* The PCB pool in error */
      pointer _PTR_ pool_in_error,

      /* The PCB in error */
      pointer _PTR_ pcb_in_error
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   IO_PCB_POOL_STRUCT_PTR pool_ptr;
   _mqx_uint              i;
   _mqx_uint              result;
   
   _GET_KERNEL_DATA(kernel_data);

   *pool_in_error = NULL;
   *pcb_in_error = NULL;
   /* START CR 2252 */
   //_INT_DISABLE();
   _int_disable();
   /* END CR 2252 */
   result = _queue_test((QUEUE_STRUCT_PTR)(&kernel_data->IO_PCB_POOLS), 
      pool_in_error);
   if (result != MQX_OK) {
      _int_enable();
      return(result);
   }/* Endif */
   pool_ptr = (pointer)kernel_data->IO_PCB_POOLS.NEXT;
   for (i = 0; i < 
      _queue_get_size((QUEUE_STRUCT_PTR)(&kernel_data->IO_PCB_POOLS)); i++) 
   {
      if (pool_ptr->VALID != IO_PCB_VALID) {
         /* START CR 2062 */
		 _int_enable();
         /* END CR 2062 */
         *pool_in_error = (pointer)pool_ptr;
         return(IO_PCB_POOL_INVALID);
      }/* Endif */
      pool_ptr = (pointer)pool_ptr->QUEUE.NEXT;
   } /* Endfor */

   /* START CR 2062 */
   _int_enable();
   /* END CR 2062 */

   return(MQX_OK);

} /* Endbody */

/* EOF */

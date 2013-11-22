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
*** File: lws_crea.c
***
*** Comments:      
***   This file contains the functions for manipulating the 
***   light weight semaphores.  These semaphores have low memory
***   requirements, and no extra features.  Tasks are suspended
***   in fifo order while waiting for a post.  No limits on values
***   are imposed.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _lwsem_create
* Returned Value   : _mqx_uint - MQX_OK or an error code
*   queue
* Comments         :
*   This function initializes a light weight semaphore.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwsem_create
   ( 
      /* [IN] the address of the semaphore to initialize */
      LWSEM_STRUCT_PTR sem_ptr,

      /* [IN] the inital number of semaphores available  */
      _mqx_int         initial_number
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

/* START CR 2365 */
#if MQX_CHECK_ERRORS
   LWSEM_STRUCT_PTR  sem_chk_ptr;
#endif
/* END CR 2365 */

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_lwsem_create, initial_number);

   sem_ptr->VALUE  = initial_number;
   _QUEUE_INIT(&sem_ptr->TD_QUEUE, 0);
   _int_disable();

#if MQX_CHECK_ERRORS
   /* Check if lwsem is already initialized */
   sem_chk_ptr = (LWSEM_STRUCT_PTR)((pointer)kernel_data->LWSEM.NEXT);
   while (sem_chk_ptr != (LWSEM_STRUCT_PTR)((pointer)&kernel_data->LWSEM)) {
      if (sem_chk_ptr == sem_ptr) {
	     _int_enable();
         _KLOGX2(KLOG_lwsem_create, MQX_EINVAL);
         return(MQX_EINVAL);
      } /* Endif */
      sem_chk_ptr = (LWSEM_STRUCT_PTR)((pointer)sem_chk_ptr->NEXT);
   } /* Endwhile */
#endif

   _QUEUE_ENQUEUE(&kernel_data->LWSEM, sem_ptr);
   sem_ptr->VALID  = LWSEM_VALID;
   _int_enable();

   _KLOGX2(KLOG_lwsem_create, MQX_OK);

   return(MQX_OK);
   
} /* Endbody */

/* EOF */

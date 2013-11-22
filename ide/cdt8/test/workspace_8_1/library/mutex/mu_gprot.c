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
*** File: mu_gprot.c
***
*** Comments:      
***   This file contains the function for returning the scheduling protocol
*** field of the mutex attributes structure.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "mutex.h"
#include "mutx_prv.h"

#if MQX_USE_MUTEXES
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mutatr_get_sched_protocol
* Returned Value   : _mqx_uint MQX_EOK or POSIX error code
* Comments         :
*    Used by a task to get a mutex attributes structure scheduling
* protocol.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mutatr_get_sched_protocol
   (
      /* [IN] the mutex attributes address */
      MUTEX_ATTR_STRUCT_PTR attr_ptr,

      /* [IN] the protocol address */
      _mqx_uint_ptr          protocol_ptr
   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if ((attr_ptr == NULL) || (protocol_ptr == NULL)) {
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (attr_ptr->VALID != MUTEX_VALID) {
      return(MQX_EINVAL);
   } /* Endif */
#endif

   *protocol_ptr = attr_ptr->SCHED_PROTOCOL;
   return(MQX_EOK);
   
} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */

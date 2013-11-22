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
*** File: mu_gspin.c
***
*** Comments:      
***   This file contains the function for obtaining the limited spin
*** count of a mutex attributes structure.
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
* Function Name    : _mutatr_get_spin_limit
* Returned Value   : _mqx_uint MQX_EOK or POSIX error code
* Comments         :
*    Used by a task to get the limited spin from a mutex attributes structure.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _mutatr_get_spin_limit
   (
      /* [IN] the mutex attributes address */
      MUTEX_ATTR_STRUCT_PTR attr_ptr,

      /* [IN] the protocol address */
      _mqx_uint_ptr          spin_count_ptr
   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if ((attr_ptr == NULL) || (spin_count_ptr == NULL)) {
      return(MQX_EINVAL);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (attr_ptr->VALID != MUTEX_VALID) {
      return(MQX_EINVAL);
   } /* Endif */
#endif

   *spin_count_ptr = attr_ptr->COUNT;
   return(MQX_EOK);
   
} /* Endbody */
#endif /* MQX_USE_MUTEXES */

/* EOF */

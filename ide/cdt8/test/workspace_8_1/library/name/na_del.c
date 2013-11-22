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
*** File: na_del.c
***
*** Comments:      
***   This file contains the function for deleting a name.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"

#if MQX_USE_NAME
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _name_delete
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function removes a name from the name component.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_delete
   (
      /* [IN] the name to be deleted */
      char_ptr name
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   pointer                handle;
   _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_name_delete, name);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_name_delete, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   handle = kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT];
#if MQX_CHECK_ERRORS
   if (handle == NULL) {
      _KLOGX2(KLOG_name_delete, MQX_COMPONENT_DOES_NOT_EXIST);
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif
   result = _name_delete_internal(handle, name);
   _KLOGX2(KLOG_name_delete, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */

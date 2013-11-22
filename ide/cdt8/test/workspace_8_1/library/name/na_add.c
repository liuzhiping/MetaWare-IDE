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
*** File: na_add.c
***
*** Comments:      
***   This file contains the function for adding a name to the
*** name component.
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
* Function Name    : _name_add
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function adds a name to the name table along with the 32bit
* quantity it wished to associate with the name.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_add
   (
      /* [IN] the name to be associated with the number */
      char _PTR_ name, 

      /* [IN] the number to be associated with the name */
      _mqx_max_type  number
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   pointer                handle;
   _mqx_uint               result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_name_add, name, number);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_name_add, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   handle = kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT];
   if (handle == NULL) {
      result = _name_create_component(NAME_DEFAULT_INITIAL_NUMBER,
         NAME_DEFAULT_GROW_NUMBER, NAME_DEFAULT_MAXIMUM_NUMBER);
      handle = kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT];
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
      if (handle == NULL) {
         _KLOGX2(KLOG_name_add, result);
         return(result);
      } /* Endif */
#endif
   } /* Endif */

   result =_name_add_internal(handle, name, number);
   _KLOGX2(KLOG_name_add, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */

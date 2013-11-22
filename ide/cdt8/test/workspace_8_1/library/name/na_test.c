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
*** File: na_test.c
***
*** Comments:      
***   This file contains the function for testing the name component.
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
* Function Name    : _name_test
* Returned Value   : an MQX Error Code
* Comments         :
*   This function tests the name component for consistency and validity.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_test
   (
      /* [OUT] the address of the base name component in error */
      pointer _PTR_ name_error_ptr,

      /* [OUT] the address of the name component extension in error */
      pointer _PTR_ name_extension_error_ptr
   )
{ /* Body */              
   KERNEL_DATA_STRUCT_PTR    kernel_data;
   NAME_COMPONENT_STRUCT_PTR name_component_ptr;
   _mqx_uint                 result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_name_test, name_error_ptr, name_extension_error_ptr);

   *name_error_ptr = NULL;
   *name_extension_error_ptr = NULL;

   name_component_ptr = (NAME_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT];
   if (name_component_ptr == NULL) {
      _KLOGX2(KLOG_name_test, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   result = _name_test_internal(
      name_component_ptr,
      name_error_ptr, name_extension_error_ptr);

   _KLOGX2(KLOG_name_test, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */

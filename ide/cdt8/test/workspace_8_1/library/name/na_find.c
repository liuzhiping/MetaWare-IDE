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
*** File: na_find.c
***
*** Comments:      
***   This file contains the function for finding the number associated
*** with a given name.
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
* Function Name    : _name_find
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function finds a name in the name component.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_find
   (
      /* [IN] the name to be found */
      char_ptr          name,

      /* [OUT] the location where the number is to be returned */
      _mqx_max_type_ptr number_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   pointer                handle;
   _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_name_find, name, number_ptr);

   handle = kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT];
#if MQX_CHECK_ERRORS
   if (handle == NULL) {
      _KLOGX2(KLOG_name_find, MQX_COMPONENT_DOES_NOT_EXIST);
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif
   result = _name_find_internal(handle, name, number_ptr);

   _KLOGX3(KLOG_name_find, result, *number_ptr);

   return(result);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */

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
*** File: na_findn.c
***
*** Comments:      
***   This file contains the function for finding a name, given
*** the number associated with the name.
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
* Function Name    : _name_find_by_number
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function finds a name in the name component, given the number.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_find_by_number
   (
      /* [IN] the number to find */
      _mqx_max_type number,

      /* [OUT] the location where the name is to be written */
      char_ptr      name_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   pointer                handle;
   _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_name_find_name, number, name_ptr);

   handle = kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT];
   if (handle == NULL) {
      _KLOGX2(KLOG_name_find_name, MQX_COMPONENT_DOES_NOT_EXIST);
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */

   result = _name_find_name_internal(handle, number, name_ptr);

   _KLOGX2(KLOG_name_find_name, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */

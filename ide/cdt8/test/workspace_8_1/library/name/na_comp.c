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
*** File: na_comp.c
***
*** Comments:      
***   This file contains the function for creating the Name component.
*** This component can be installed as a generic name
*** service for user tasks, associating a string name with a  _mqx_max_type
*** quantity.  The component is also used by other kernel components to
*** manage names.
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
* Function Name    : _name_create_component
* Returned Value   : _mqx_uint MQX error code
* Comments         :
*   This function creates the name component.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _name_create_component
   (
      /* [IN] the initial number of names that can be stored  */
      _mqx_uint initial_number,

      /* [IN] the number of names to be added when table full */
      _mqx_uint grow_number,

      /* [IN] the maximum number of names that can be stored  */
      _mqx_uint maximum_number
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR    kernel_data;
   _mqx_uint                 result;
   pointer                   handle;
   NAME_COMPONENT_STRUCT_PTR component_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE4(KLOG_name_create_component, initial_number, grow_number, maximum_number);

   component_ptr = (NAME_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT];
   if (component_ptr != NULL){
      if (maximum_number > component_ptr->MAX_NUMBER) {
         component_ptr->MAX_NUMBER = maximum_number;
      } /* Endif */
      _KLOGX2(KLOG_name_create_component, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   result = _name_create_handle_internal(&handle,
      initial_number, grow_number, maximum_number, initial_number);

   /* We must exclude all ISRs at this point */
   if (result == MQX_OK) {
      _int_disable();
      if (kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT] != NULL){
         _int_enable();
         _name_destroy_handle_internal(handle);
         component_ptr = (NAME_COMPONENT_STRUCT_PTR)
            kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT];
         if (maximum_number > component_ptr->MAX_NUMBER) {
            component_ptr->MAX_NUMBER = maximum_number;
         } /* Endif */
         _KLOGX2(KLOG_name_create_component, MQX_OK);
         return(MQX_OK);
      } /* Endif */
      kernel_data->KERNEL_COMPONENTS[KERNEL_NAME_MANAGEMENT] = handle;
      _int_enable();
   } /* Endif */

   _KLOGX2(KLOG_name_create_component, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_NAME */

/* EOF */

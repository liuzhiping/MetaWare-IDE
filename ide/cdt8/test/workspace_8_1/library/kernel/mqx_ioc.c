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
*** File: mqx_ioc.c
***
*** Comments:      
***   This file contains the support functions for the MQX I/O
*** components.
***                                                               
***
**************************************************************************
*END*********************************************************************/


#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_io_component_handle
* Returned Value   : pointer handle
* Comments         : 
*    This function returns the I/O Component handle for the specified
*    I/O component
*
*END*----------------------------------------------------------------------*/

pointer _mqx_get_io_component_handle
   (
      /* [IN] the component number */
      _mqx_uint component
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

   _GET_KERNEL_DATA(kernel_data);
 
#if MQX_CHECK_ERRORS
   if (component >= MAX_IO_COMPONENTS) {
      _task_set_error(MQX_INVALID_PARAMETER);
      return(NULL);
   } /* Endif */
#endif

   return kernel_data->IO_COMPONENTS[component];

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_set_io_component_handle
* Returned Value   : pointer handle
* Comments         : 
*    This function sets the I/O Component handle for the specified
*    I/O component, and returns the previous value.
*
*END*----------------------------------------------------------------------*/

pointer _mqx_set_io_component_handle
   (
      /* [IN] the component number */
      _mqx_uint component,

      /* [IN] the new handle */
      pointer handle
   )
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   pointer  old_handle;

   _GET_KERNEL_DATA(kernel_data);
 
#if MQX_CHECK_ERRORS
   if (component >= MAX_IO_COMPONENTS) {
      _task_set_error(MQX_INVALID_PARAMETER);
      return(NULL);
   } /* Endif */
#endif

   _int_disable();
   old_handle = kernel_data->IO_COMPONENTS[component];
   if (old_handle == NULL) {
      kernel_data->IO_COMPONENTS[component] = handle;
   } else {
      if (handle == NULL) {
         kernel_data->IO_COMPONENTS[component] = handle;
      } /* Endif */
   } /* Endif */
   _int_enable();
   return old_handle;

} /* Endbody */

#if MQX_COMPONENT_DESTRUCTION
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_get_io_component_cleanup
* Returned Value   : pointer cleanup
* Comments         : 
*    This function returns the I/O Component cleanup for the specified
*    I/O component
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _mqx_get_io_component_cleanup
   (
      /* [IN] the component number */
      _mqx_uint component
   ))(pointer)
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;

   _GET_KERNEL_DATA(kernel_data);
 
#if MQX_CHECK_ERRORS
   if (component >= MAX_IO_COMPONENTS) {
      _task_set_error(MQX_INVALID_PARAMETER);
      return(NULL);
   } /* Endif */
#endif

   return (void (_CODE_PTR_)(pointer))
      kernel_data->IO_COMPONENT_CLEANUP[component];

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _mqx_set_io_component_cleanup
* Returned Value   : pointer cleanup
* Comments         : 
*    This function sets the I/O Component cleanup for the specified
*    I/O component, and returns the previous value.
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _mqx_set_io_component_cleanup
   (
      /* [IN] the component number */
      _mqx_uint component,

      /* [IN] the new cleanup */
      void (_CODE_PTR_ cleanup)(pointer)
   ))(pointer)
{ /* Body */
   register KERNEL_DATA_STRUCT_PTR  kernel_data;
   void (_CODE_PTR_ old_cleanup)(TD_STRUCT_PTR);

   _GET_KERNEL_DATA(kernel_data);
 
#if MQX_CHECK_ERRORS
   if (component >= MAX_IO_COMPONENTS) {
      _task_set_error(MQX_INVALID_PARAMETER);
      return(NULL);
   } /* Endif */
#endif

   _int_disable();
   old_cleanup = kernel_data->IO_COMPONENT_CLEANUP[component];
   kernel_data->IO_COMPONENT_CLEANUP[component] =
      (void (_CODE_PTR_)(TD_STRUCT_PTR))cleanup;
   _int_enable();
   return (void (_CODE_PTR_)(pointer))old_cleanup;

} /* Endbody */
#endif /* MQX_COMPONENT_DESTRUCTION */

/* EOF */

/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: io_ioctl.c
***
*** Comments:      
***   This file contains the function _io_ioctl.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_ioctl
* Returned Value   : _mqx_int 
* Comments         :
*    The returned value is IO_EOF or a MQX error code.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_ioctl
   ( 
      /* [IN] the stream to perform the operation on */
      FILE_PTR    file_ptr,

      /* [IN] the ioctl command */
      _mqx_uint   cmd,

      /* [IN] the ioctl parameters */
      pointer     param_ptr
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR   dev_ptr;
   _mqx_uint_ptr          tmp_ptr;
   _mqx_uint              result = MQX_OK;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_EOF);
   } /* Endif */
#endif

   tmp_ptr = (_mqx_uint_ptr)param_ptr;

   switch (cmd) {
      case IO_IOCTL_GET_FLAGS:
         *tmp_ptr = file_ptr->FLAGS;
         break;
      case IO_IOCTL_SET_FLAGS:
         file_ptr->FLAGS = *tmp_ptr;
         break;
      default:
         dev_ptr = file_ptr->DEV_PTR;
         if (dev_ptr->IO_IOCTL != NULL) {
            result = (*dev_ptr->IO_IOCTL)(file_ptr, cmd, param_ptr);
         } /* Endif */
         break;
   } /* Endswitch */

   return(result);

} /* Endbody */

/* EOF */

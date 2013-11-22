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
*** File: io_write.c
***
*** Comments:      
***   Contains the function _io_write.
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
* Function Name    : _io_write
* Returned Value   : _mqx_int 
* Comments         :
*    The returned value is IO_ERROR or the number of characters written
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_write
   ( 
      /* [IN] the stream to perform the operation on */
      FILE_PTR    file_ptr,

      /* [IN] the data location to write from */
      pointer     data_ptr,

      /* [IN] the number of bytes to write */
      _mqx_int     num      
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR   dev_ptr;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_ERROR);
   } /* Endif */
#endif

   dev_ptr = file_ptr->DEV_PTR;
#if MQX_CHECK_ERRORS
   if (dev_ptr->IO_WRITE == NULL) {
      file_ptr->ERROR = MQX_IO_OPERATION_NOT_AVAILABLE;
      return(IO_ERROR);
   } /* Endif */
#endif

   return((*dev_ptr->IO_WRITE)(file_ptr, data_ptr, num));

} /* Endbody */

/* EOF */

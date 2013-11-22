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
*** File: io_fstat.c
***
*** Comments:      
***   This file contains the function for checking the status of input.
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "fio.h"
#include "fio_prv.h"
#include "io.h"
#include "io_prv.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_fstatus
* Returned Value   : boolean
* Comments         :
*  This function checks the HAVE_UNGOT_CHARACTER of the input_ucb, if none is
*  available, it calls the device status function.
*
*END*----------------------------------------------------------------------*/

boolean _io_fstatus
   (
      /* [IN] the stream whose status is desired */
      FILE_PTR file_ptr
   )
{ /* Body */
   IO_DEVICE_STRUCT_PTR   dev_ptr;
   boolean                result;

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return (FALSE);
   } /* Endif */
#endif

   if ( file_ptr->HAVE_UNGOT_CHARACTER ) {
      return (TRUE);
   } else {
      dev_ptr = file_ptr->DEV_PTR;
      if (dev_ptr->IO_IOCTL != NULL) {   
         (*dev_ptr->IO_IOCTL)(file_ptr, IO_IOCTL_CHAR_AVAIL, &result);
         return(result);
      } /* Endif */
   } /* Endif */
   return (FALSE);

} /* Endbody */

/* EOF */

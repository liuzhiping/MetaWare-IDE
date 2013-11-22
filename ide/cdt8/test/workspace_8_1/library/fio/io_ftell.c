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
*** File: io_ftell.c
***
*** Comments:      
***   This file contains the function for returning the current location
*** in a file.
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
* Function Name    : _io_ftell
* Returned Value   : _mqx_int Location or IO_ERROR.
* Comments         :
*   This function returns the current file position, or IO_ERROR on error.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_ftell
   (
      /* [IN] the stream to use */
      FILE_PTR file_ptr
   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_ERROR);
   } /* Endif */
#endif

   return(file_ptr->LOCATION);

} /* Endbody */

/* EOF */

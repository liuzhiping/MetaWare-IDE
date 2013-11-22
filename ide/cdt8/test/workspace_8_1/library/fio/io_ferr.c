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
*** File: io_ferr.c
***
*** Comments:      
***   Contains the function io_ferror.
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
* Function Name    : _io_ferror
* Returned Value   : _mqx_int 
* Comments         :
*    The returned value is the current file error code.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_ferror
   ( 
      /* [IN] the stream to perform the operation on */
      FILE_PTR    file_ptr
   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_ERROR);
   } /* Endif */
#endif

   return(file_ptr->ERROR);

} /* Endbody */

/* EOF */

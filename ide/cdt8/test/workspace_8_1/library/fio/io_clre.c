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
*** File: io_clre.c
***
*** Comments:      
***   Contains the function io_clearerr.
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
* Function Name    : _io_clearerr
* Returned Value   : none
* Comments         :
*    Clears the end of file and error indicators.
*
*END*----------------------------------------------------------------------*/

void _io_clearerr
   ( 
      /* [IN] the stream to perform the operation on */
      FILE_PTR    file_ptr
   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return;
   } /* Endif */
#endif

   file_ptr->ERROR = 0;
   file_ptr->FLAGS &= ~IO_FLAG_AT_EOF;

} /* Endbody */

/* EOF */

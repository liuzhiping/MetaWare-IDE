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
*** File: io_feof.c
***
*** Comments:      
***   Contains the function io_feof.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "fio_prv.h"
#include "io_prv.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _io_feof
* Returned Value   : _mqx_int - non-zero if the end of file indicator is set
* Comments         :
*    Determines if end of file has been reached
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_feof
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

   return((file_ptr->FLAGS & IO_FLAG_AT_EOF) ? 1 : 0);

} /* Endbody */

/* EOF */

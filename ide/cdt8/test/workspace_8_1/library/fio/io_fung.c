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
*** File: io_fung.c
***
*** Comments:      
***   This file contains the function for ungetting a character.
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
* Function Name    : _io_fungetc
* Returned Value   : _mqx_int the char pushed back
* Comments         :
*   this function pushes back a character where it will be returned
* on the next read.  Only 1 pushback character allowed.
* The function returns the character pushed back, or IO_EOF on error.
*
*END*----------------------------------------------------------------------*/

_mqx_int _io_fungetc
   (
      /* [IN] the character to return */
      _mqx_int   character,
      
      /* [IN] the stream to return it to */
      FILE_PTR file_ptr
   )
{ /* Body */

#if MQX_CHECK_ERRORS
   if (file_ptr == NULL) {
      return(IO_EOF);
   } /* Endif */
#endif

   if (file_ptr->HAVE_UNGOT_CHARACTER) {
      return(IO_EOF);
   } /* Endif */
      
   file_ptr->HAVE_UNGOT_CHARACTER = TRUE;
   file_ptr->UNGOT_CHARACTER      = character;
   return(character);

} /* Endbody */

/* EOF */

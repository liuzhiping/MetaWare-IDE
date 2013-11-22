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
*** File: int_sr.c
***
*** Comments:      
***   This file contains functions to manipulate the SR register
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _psp_set_sr
* Returned Value   : old sr value
* Comments         :
*  sets the value of the SR register and returns the old value
*
*END*-------------------------------------------------------------------------*/

uint_32 _psp_set_sr
   (
      /* [IN] the new value for the status register */
      uint_32 sr_value
   )
{ /* Body */
   uint_32 old_sr_value;

   _PSP_GET_SR(old_sr_value);
//   old_sr_value >>= 25;
   _PSP_SET_SR(sr_value);
   return(old_sr_value);

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : _psp_get_sr
* Returned Value   : sr value
* Comments         :
*  returns the current SR register value
*
*END*-------------------------------------------------------------------------*/

uint_32 _psp_get_sr
   (
      void
   )
{ /* Body */
   uint_32 old_sr_value;

   _PSP_GET_SR(old_sr_value);
   /* Start CR 2353 */
   //old_sr_value >>= 25;
   /* End CR 2353 */
   return(old_sr_value);

} /* Endbody */

/* EOF */

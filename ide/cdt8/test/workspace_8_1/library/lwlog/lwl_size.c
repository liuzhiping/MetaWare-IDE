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
*** File: lwl_crea.c
***
*** Comments:      
***   This file contains the function for calculating the amount of
*** memory needed to create a LW log given the number of entries.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwlog.h"
#include "lwlogprv.h"

#if MQX_USE_LWLOGS
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _lwlog_calculate_size
* Returned Value   : _mem_size - size needed for lw log
* Comments         :
*   This function calculates the size necessary to contain a LW log
*
*END*----------------------------------------------------------------------*/

_mem_size _lwlog_calculate_size
   (

      /* [IN] the maximum number of entries */
      _mqx_uint entries

   )
{ /* Body */
   _mem_size result;

#if MQX_CHECK_ERRORS
   if (entries == 0) {
      return 0;
   } /* Endif */
#endif

   result = (_mem_size)sizeof(LWLOG_HEADER_STRUCT) +
      (_mem_size)(entries-1) * (_mem_size)sizeof(LWLOG_ENTRY_STRUCT);

   return result;

} /* Endbody */
#endif /* MQX_USE_LWLOGS */

/* EOF */

/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: bsp_clk.c
***
*** Comments:      
***   This file contains the functions for determining the system clock speed
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "bsp.h"
#include "bsp_prv.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_get_system_clock
* Returned Value   : uint_32 - clock frequency in Hz
* Comments         :
*    This function determines the system clock frequency from the AA3's
* dip switch settings 
*
*END*----------------------------------------------------------------------*/

uint_32 _bsp_get_system_clock
   (
      void
   )
{ /* Body */
   uint_32 speed;
   uint_32 settings;

   settings = _psp_get_aux(0x56);
   if (!settings) {
      /* 
      ** This case most likely means MQX is running in the
      ** context of the SeeCode ISS
      */
      return (BSP_CRYSTAL_FREQUENCY >> 3);
   } /* Endif */
   settings &= 0x0C;

   switch (settings) {
      case 0x00:
         /* Divide by 1 */
         speed = BSP_CRYSTAL_FREQUENCY;
         break;
      case 0x08:
         /* Divide by 4 */
         speed = BSP_CRYSTAL_FREQUENCY >> 2;
         break;
      case 0x04:
         /* Divide by 2 */
         speed = BSP_CRYSTAL_FREQUENCY >> 1;
         break;
      default:
         /* Divide by 8 */
         speed = BSP_CRYSTAL_FREQUENCY >> 3;
         break;
   } /* Endswitch */

   return speed;

} /* Endbody */

/* EOF */

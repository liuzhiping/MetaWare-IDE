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
*** File: enetsini.c
***
*** Comments:      
***   Contains board specific enet initialization code
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_init
*  Returned Value : void
*  Comments       :
*
*END*-----------------------------------------------------------------*/

void _bsp_enet_init
   (
      /* [IN] which device? */
      uint_32 devnum,

      /* [IN] Enable RTS? */
      uint_32 enet_enable_rts,

      /* [IN] Flags from ENET_initialize() */
      uint_32 flags
   )
{ /* Body */
   volatile EMWSIM_STRUCT _PTR_ dev_ptr = (pointer)BSP_SIM_ENET_BASE;

   switch (enet_enable_rts) {
      case EMWSIM_ENET_INIT:
         break;
      case EMWSIM_ENET_INIT_RTS:
         break;
      default:
         break;
   } /* Endswitch */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_enet_get_base
*  Returned Value : pointer base address of enet device
*  Comments       :
*
*END*-----------------------------------------------------------------*/

pointer _bsp_enet_getbase
   (
      /* [IN] the device to be used */
      uint_32 dev_num
   )
{ /* Body */

   return((pointer)BSP_SIM_ENET_BASE);

} /* Endbody */

/* EOF */

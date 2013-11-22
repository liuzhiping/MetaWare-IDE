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
*** File: usb_bsp.c
***
*** Comments:      
***   This file contains board-specific USB routines.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_get_usb_vector
*  Returned Value : interrupt vector number
*  Comments       :
*        Get the vector number for the specified device number
*END*-----------------------------------------------------------------*/

uint_8 _bsp_get_usb_vector
   (
      uint_8 device_number
   )
{ /* Body */
   switch (device_number) {
      case 0:
         return BSP_VUSB11_HOST_VECTOR0;
      case 1:
         return BSP_VUSB11_DEVICE_VECTOR0;
      default:
         break;
   } /* Endswitch */
} /* EndBody */

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : _bsp_get_usb_base
*  Returned Value : Address of the VUSB1.1 register base
*  Comments       :
*        Get the USB register base address
*END*-----------------------------------------------------------------*/

pointer _bsp_get_usb_base
   (
      uint_8 device_number
   )
{ /* Body */
   switch (device_number) {
      case 0:
         return (pointer)BSP_VUSB11_HOST_BASE_ADDRESS0;
      case 1:
         return (pointer)BSP_VUSB11_DEVICE_BASE_ADDRESS0;
      default:
         break;
   } /* Endswitch */
} /* EndBody */

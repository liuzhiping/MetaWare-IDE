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
*** File: initvsb11.c
***
*** Comments:      
***   This file contains the default callback function for the vusb device and 
***   host.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "usbdprv.h"

USB_CALLBACK_FUNCTIONS_STRUCT _bsp_vusbd11_callback_table =
{
   0,
  
   /* The Host/Device init function */
   _usb_dci_vusb11_init,

   /* The function to send data */
   _usb_dci_vusb11_submit_transfer,

   /* The function to receive data */
   _usb_dci_vusb11_submit_transfer,
   
   /* The function to cancel the transfer */
   _usb_dci_vusb11_cancel_transfer,
         
   _usb_dci_vusb11_init_endpoint,
   
   _usb_dci_vusb11_deinit_endpoint,
   
   _usb_dci_vusb11_unstall_endpoint,
   
   _usb_dci_vusb11_get_endpoint_status,
   
   _usb_dci_vusb11_set_endpoint_status,
   
   _usb_dci_vusb11_shutdown,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL
};

/* EOF */



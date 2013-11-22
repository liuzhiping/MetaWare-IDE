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
#include "usbhprv.h"

USB_CALLBACK_FUNCTIONS_STRUCT _bsp_vusbh11_callback_table =
{
   0,
         
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
   
   NULL,
  
   /* The Host/Device init function */
   _usb_hci_vusb11_init,

   /* The function to shutdown the host/device */
   _usb_hci_vusb11_shutdown,

   /* The function to send data */
   _usb_hci_vusb11_send_data,

   /* The function to send setup data */
   _usb_hci_vusb11_send_setup,

   /* The function to receive data */
   _usb_hci_vusb11_recv_data,
   
   /* The function to get the transfer status */
   NULL,
   
   /* The function to cancel the transfer */
   _usb_hci_vusb11_cancel_transfer,
   
   /* The function to do USB sleep */
   NULL,
   
   /* The function for USB bus control */
   _usb_hci_vusb11_bus_control

};

/* EOF */



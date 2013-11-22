/*HEADER****************************************************************
************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: enerr.c
***
*** Comments:  This file contains the Ethernet error reporting
***            functions.
***
************************************************************************
*END*******************************************************************/

#include <mqx.h>
#include "enet.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_strerror
*  Returned Value : pointer to error string
*  Comments       :
*        Returns a string describing an error code
*
*END*-----------------------------------------------------------------*/

static const char_ptr ENET_errlist[ENETERR_MAX - ENETERR_MIN + 1] = {
   "Invalid device number",                   /* ENETERR_INVALID_DEVICE */
   "Device already initialized",              /* ENETERR_INIT_DEVICE    */
   "Couldn't allocate state (out of memory)", /* ENETERR_ALLOC_CFG      */
   "Couldn't allocate PCBs (out of memory)",  /* ENETERR_ALLOC_PCB      */
   "Couldn't allocate buffer descriptors",    /* ENETERR_ALLOC_BD       */
   "Couldn't install Ethernet notifier",      /* ENETERR_INSTALL_ISR    */
   "Some PCBs are still in use",              /* ENETERR_FREE_PCB       */
   "Out of memory",                           /* ENETERR_ALLOC_ECB      */
   "Protocol already open",                   /* ENETERR_OPEN_PROT      */
   "Not an open protocol",                    /* ENETERR_CLOSE_PROT     */
   "Packet too short",                        /* ENETERR_SEND_SHORT     */
   "Packet too long",                         /* ENETERR_SEND_LONG      */
   "Not a multicast address",                 /* ENETERR_JOIN_MULTICAST */
   "Out of memory",                           /* ENETERR_ALLOC_MCB      */
   "Not a joined group",                      /* ENETERR_LEAVE_GROUP    */
   "Tranmit ring full",                       /* ENETERR_SEND_FULL      */
   "IP Table full of IP pairs",               /* ENETERR_IP_TABLE_FULL  */
   "Generic alloc failed",                    /* ENETERR_ALLOC          */
   "Device failed to initialize",             /* ENETERR_INIT_FAILED    */
   "Device read or write timeout"             /* ENETERR_DEVICE_TIMEOUT */
};

const char_ptr ENET_strerror
   (
      /* [IN] the ENET error code */
      _mqx_uint  error
   )
{ /* Body */

   if (error == ENET_OK) {
      return "OK";
   } /* Endif */
   if ((error < ENETERR_MIN) || (error > ENETERR_MAX)) {
      return "Unknown error";
   } /* Endif */
   return ENET_errlist[error - ENETERR_MIN];

} /* Endbody */


/* EOF */

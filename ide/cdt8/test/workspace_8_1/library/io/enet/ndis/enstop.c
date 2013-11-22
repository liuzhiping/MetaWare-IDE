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
*** File: enstop.c
***
*** Comments:  This file contains the NDIS packet driver Ethernet shutdown
***            functions.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "packet32.h"
#include "enet.h"
#include "enetprv.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_shutdown
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Stops the chip.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_shutdown
   (
      /* [IN] the Ethernet state structure */
      _enet_handle   handle
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR enet_ptr = (ENET_CFG_STRUCT_PTR)handle;

   /*
   ** Make sure all PCBs are free
   */
   if (enet_ptr->RxEntries != RX_RING_LEN) {
      return ENETERR_FREE_PCB;
   } /* Endif */

   /* Close the packet driver */
   PacketCloseAdapter(enet_ptr->DEV_HANDLE);

   EnetHandle = NULL;

   /* Free resources */
   ENET_memfree(enet_ptr->PCB_BASE);
   ENET_memfree(enet_ptr);

   return ENET_OK;

} /* Endbody */

/* EOF */

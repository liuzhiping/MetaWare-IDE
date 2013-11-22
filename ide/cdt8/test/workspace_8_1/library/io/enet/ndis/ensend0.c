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
*** File: ensend.c
***
*** Comments:  This file contains the NDIS Packet driver send functions
***
***
************************************************************************
*END*******************************************************************/

/* Start SPR P155-0122-01 */

#include "mqx_inc.h"
#include "bsp.h"
#include "packet32.h"
#include "enet.h"
#include "enetprv.h"


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_send_MAC
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Sends a packet.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_send_MAC
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr,

      /* [IN] the packet to send */
      PCB_PTR              packet,

      /* [IN] total size of the packet */
      uint_32              size,

      /* [IN] total fragments in the packet */
      uint_32              frags,

      /* [IN] optional flags, zero = default */
      uint_32              flags
   )
{ /* Body */
   PCB_FRAGMENT_PTR frag_ptr;
   pointer          packet_ptr;
   uchar_ptr        data_ptr;
   uchar_ptr        curr_pos;
   uint_32          total;

   /*
   ** The packet driver expects a single packet to be provided to it.
   ** So, we must copy each fragment from the PCB into the contiguous packet memory
   */

   /* Allocate packet a object */
   EnterCriticalSection(&ENET_cs);
   packet_ptr = PacketAllocatePacket(enet_ptr->DEV_HANDLE);
   LeaveCriticalSection(&ENET_cs);
   if (packet_ptr == NULL) {
      return ENETERR_ALLOC_BD;
   } /* Endif */

   /* Allocate a buffer that we can copy all of the fragments into */
   data_ptr = ENET_memalloc(size);
   if (data_ptr == NULL) {
      /* Deallocate the packet object */
      EnterCriticalSection(&ENET_cs);
      PacketFreePacket(packet_ptr);
      LeaveCriticalSection(&ENET_cs);
      return ENETERR_ALLOC_BD;
   } /* Endif */  

   /* Go through the fragments and copy each one into the buffer */
   curr_pos = data_ptr;
   total = 0;
   for (frag_ptr = packet->FRAG; frag_ptr->LENGTH; frag_ptr++) {
      total += frag_ptr->LENGTH;
      if (total > size) {
         ENET_memfree(data_ptr);
         /* Deallocate the packet object */
         EnterCriticalSection(&ENET_cs);
         PacketFreePacket(packet_ptr);
         LeaveCriticalSection(&ENET_cs);
         return ENETERR_ALLOC_BD;
      }/* Endif */
      ENET_memcopy(frag_ptr->FRAGMENT, curr_pos, frag_ptr->LENGTH);
      curr_pos += frag_ptr->LENGTH;
   } /* Endfor */

   /* Release the PCB */
   PCB_free(packet);

   /* Point the packet to the data */
   EnterCriticalSection(&ENET_cs);
   PacketInitPacket(packet_ptr, data_ptr, size);
   LeaveCriticalSection(&ENET_cs);

   /* Send it */
   EnterCriticalSection(&ENET_cs);
   PacketSendPacket(enet_ptr->DEV_HANDLE, packet_ptr, TRUE);
   LeaveCriticalSection(&ENET_cs);

   ENET_tx_record(enet_ptr,0,0);

   /* Deallocate the packet object */
   EnterCriticalSection(&ENET_cs);
   PacketFreePacket(packet_ptr);
   LeaveCriticalSection(&ENET_cs);

   /* Deallocate the copy buffer */
   ENET_memfree(data_ptr);

   return ENET_OK;

} /* Endbody */

/* End SPR P155-0122-01 */

/* EOF */

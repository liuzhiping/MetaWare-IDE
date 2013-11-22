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
*** Comments:  This file contains the Ethernet send
***            interface function.
***
************************************************************************
*END*******************************************************************/

#include <mqx.h>
#include <bsp.h>

#include "enet.h"
#include "enetprv.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_send
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Sends a packet.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_send
   (
      /* [IN] the Ethernet state structure */
      _enet_handle   handle,

      /* [IN] the packet to send */
      PCB_PTR        packet,

      /* [IN] the protocol */
      uint_16        type,

      /* [IN] the destination Ethernet address */
      _enet_address  dest,

      /* [IN] optional flags, zero = default */
      uint_32        flags
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR  enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   ENET_HEADER_PTR      packet_ptr;
   uchar_ptr            type_ptr;
   PCB_FRAGMENT_PTR     frag_ptr;
   uint_32              hwhdr, swhdr, size, frags;
   uint_32              error;

   hwhdr = sizeof(ENET_HEADER);
   swhdr = sizeof(ENET_HEADER);
   if (flags & ENET_OPT_8021QTAG) {
      hwhdr += sizeof(ENET_8021QTAG_HEADER);
      swhdr += sizeof(ENET_8021QTAG_HEADER);
   } /* Endif */
/* Start CR 305 */
#if 0
   if (flags & ENET_OPT_8023) {
      swhdr += sizeof(ENET_8022_HEADER);
   } /* Endif */
#endif
/* End CR 305 */

   /*
   ** Make sure the first fragment is long enough for the Ethernet
   ** frame header.  This isn't strictly necessary, but it's impractical
   ** to split a 14-26 byte header over multiple fragments.
   */
#if MQX_CHECK_ERRORS
   if (packet->FRAG[0].LENGTH < swhdr) {
      enet_ptr->STATS.ST_TX_DISCARDED++;
      PCB_free(packet);
      return ENETERR_SEND_SHORT;
   } /* Endif */
#endif

   /*
   ** Make sure that no fragment exceeds a maximum packet length.
   ** We check every fragment because we want to prevent something
   ** like FRAG[0].LENGTH = 2000, FRAG[1].LENGTH = -1000.  This
   ** situation would not be detected if we only check the total
   ** length.
   */
   size = frags = 0;
   for (frag_ptr = packet->FRAG; frag_ptr->LENGTH; frag_ptr++) {
#if MQX_CHECK_ERRORS
      if (frag_ptr->LENGTH > (hwhdr + ENET_FRAMESIZE_MAXDATA)) {
         enet_ptr->STATS.ST_TX_DISCARDED++;
         PCB_free(packet);
         return ENETERR_SEND_LONG;
      } /* Endif */
#endif
      size += frag_ptr->LENGTH;
      frags++;
   } /* Endfor */

   /*
   ** Make sure that the total sum of the fragments doesn't exceed
   ** a maximum packet length.
   */
#if MQX_CHECK_ERRORS
   if (size > (hwhdr + ENET_FRAMESIZE_MAXDATA)) {
      enet_ptr->STATS.ST_TX_DISCARDED++;
      PCB_free(packet);
      return ENETERR_SEND_LONG;
   } /* Endif */
#endif

   /*
   ** Everything checks out -- fill in the header.
   */
   packet_ptr = (ENET_HEADER_PTR)packet->FRAG[0].FRAGMENT;
   htone(packet_ptr->DEST, dest);
   htone(packet_ptr->SOURCE, enet_ptr->ADDRESS);
   type_ptr = packet_ptr->TYPE;

   if (flags & ENET_OPT_8021QTAG) {
      ENET_8021QTAG_HEADER_PTR tag_ptr = (ENET_8021QTAG_HEADER_PTR)(type_ptr+2);
      uint_16 tag;
      tag = ENET_GETOPT_8021QPRIO(flags) << 13;
      htons(type_ptr, 0x8100);
      htons(tag_ptr->TAG, tag);
      type_ptr = tag_ptr->TYPE;
   } /* Endif */

   if (flags & ENET_OPT_8023) {
      ENET_8022_HEADER_PTR llc_ptr = (ENET_8022_HEADER_PTR)(type_ptr+2);
      htons(type_ptr, size - swhdr);
      htonc(llc_ptr->DSAP, 0xAA);
      htonc(llc_ptr->SSAP, 0xAA);
      htonc(llc_ptr->COMMAND, 0x03);
      htonc(&llc_ptr->OUI[0], 0x00);
      htonc(&llc_ptr->OUI[1], 0x00);
      htonc(&llc_ptr->OUI[2], 0x00);
      type_ptr = llc_ptr->TYPE;
   } /* Endif */

   htons(type_ptr, type);

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself, and with ENET_ISR().
   */
   ENET_lock();
   error = ENET_send_MAC(enet_ptr, packet, size, frags, flags);
   ENET_unlock();

   if (error) {
      PCB_free(packet);
   } /* Endif */

   return error;

} /* Endbody */


/* EOF */

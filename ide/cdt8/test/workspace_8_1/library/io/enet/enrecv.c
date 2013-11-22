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
*** File: enrecv.c
***
*** Comments:  This file contains the Ethernet receive
***            support function.
***
************************************************************************
*END*******************************************************************/

#include <mqx.h>
#include <bsp.h>

#include "enet.h"
#include "enetprv.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_recv
*  Returned Value : ECB for received packet, or NULL.
*  Comments       :
*        Finds an application for a received packet.
*
*END*-----------------------------------------------------------------*/

ENET_ECB_STRUCT_PTR ENET_recv
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr,

      /* [IN] the received packet */
      PCB_PTR              pcb_ptr
   )
{ /* Body */
   ENET_ECB_STRUCT_PTR  ecb_ptr;
   ENET_MCB_STRUCT_PTR  mcb_ptr;
   ENET_HEADER_PTR      packet_ptr;
   uchar_ptr            type_ptr;
   uint_32              hdrlen;
   _enet_address        dest;
   uint_16              type;

   hdrlen = sizeof(ENET_HEADER);
   packet_ptr = (ENET_HEADER_PTR)pcb_ptr->FRAG[0].FRAGMENT;
   type_ptr = packet_ptr->TYPE;
   type = ntohs(type_ptr);

   if (type == 0x8100) {
      ENET_8021QTAG_HEADER_PTR tag_ptr = (ENET_8021QTAG_HEADER_PTR)(type_ptr+2);
      hdrlen += sizeof(ENET_8021QTAG_HEADER);
      type_ptr = tag_ptr->TYPE;
      type = ntohs(type_ptr);
   } /* Endif */

   if (type <= ENET_FRAMESIZE_MAXDATA) {
      ENET_8022_HEADER_PTR llc_ptr = (ENET_8022_HEADER_PTR)(type_ptr+2);
      if ((ntohc(llc_ptr->DSAP) != 0xAA)
       || (ntohc(llc_ptr->SSAP) != 0xAA)) {
         return NULL;
      } /* Endif */
      /* Start CR 305 */
      /* hdrlen += sizeof(ENET_8022_HEADER); */
      /* End CR 305 */
      if (pcb_ptr->FRAG[0].LENGTH < hdrlen + type) {
         return NULL;
      } /* Endif */
      pcb_ptr->FRAG[0].LENGTH = hdrlen + type;
      type_ptr = llc_ptr->TYPE;
      type = ntohs(type_ptr);
   } /* Endif */

   for (ecb_ptr = enet_ptr->ECB_HEAD; ecb_ptr; ecb_ptr = ecb_ptr->NEXT) {
      if (ecb_ptr->TYPE == type) {

         ntohe(packet_ptr->DEST, dest);
         if ((dest[0] & 1) && !((dest[0] == 0xFF)
                             && (dest[1] == 0xFF)
                             && (dest[2] == 0xFF)
                             && (dest[3] == 0xFF)
                             && (dest[4] == 0xFF)
                             && (dest[5] == 0xFF))) {

            /*
            ** The destination is a multicast address.
            ** Check the joined mulicast groups.
            */
            for (mcb_ptr = ecb_ptr->MCB_HEAD; mcb_ptr; mcb_ptr = mcb_ptr->NEXT) {
               if ((dest[0] == mcb_ptr->GROUP[0])
                && (dest[1] == mcb_ptr->GROUP[1])
                && (dest[2] == mcb_ptr->GROUP[2])
                && (dest[3] == mcb_ptr->GROUP[3])
                && (dest[4] == mcb_ptr->GROUP[4])
                && (dest[5] == mcb_ptr->GROUP[5])) {
                  break;
               } /* Endif */
            } /* Endfor */

            if (!mcb_ptr) {
               /*
               ** We received a packet multicasted to a group we
               ** haven't joined.  Break out of the big for loop
               ** and discard the packet.  We don't continue the
               ** big for loop because there is only one ECB per
               ** type and we already found it.
               */
               ecb_ptr = NULL;
            } /* Endif */
         } /* Endif */

         break;
      } /* Endif */
   } /* Endfor */

   return ecb_ptr;

} /* Endbody */


/* EOF */

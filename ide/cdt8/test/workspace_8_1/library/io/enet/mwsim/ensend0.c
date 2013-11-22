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
*** File: ensend0.c
***
*** Comments:  This file contains the Ethernet send
***            support function.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx.h"
#include "bsp.h"
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
   volatile EMWSIM_STRUCT _PTR_ dev_ptr = enet_ptr->DEV_PTR;
   PCB_FRAGMENT_PTR             frag_ptr;
   uchar_ptr                    buf_ptr;

   /* Enforce the minimum frame size of 64 bytes total */
   if (size < (ENET_FRAMESIZE_MIN - ENET_FRAMESIZE_TAIL)) {
      size = ENET_FRAMESIZE_MIN - ENET_FRAMESIZE_TAIL;
   } else if (size > ENET_FRAMESIZE_MAX) {
      return ENETERR_SEND_LONG;
   } /* Endif */

   /* We cant send, no more room */
   if (!(dev_ptr->STATUS & EMWSIM_STATUS_TX_AVAILABLE)){
      enet_ptr->STATS.ST_TX_MISSED++;
      return ENETERR_SEND_FULL;
   } /* Endif */

   frag_ptr = packet->FRAG;
   if (frags > 1) {
      buf_ptr  = (uchar_ptr)enet_ptr->TxBuffer;
      while (frag_ptr->LENGTH) {
         _mem_copy(frag_ptr->FRAGMENT, buf_ptr, frag_ptr->LENGTH);
         buf_ptr += frag_ptr->LENGTH;
         frag_ptr++;
      } /* Endwhile */
      buf_ptr  = (uchar_ptr)enet_ptr->TxBuffer;
   } else {
      buf_ptr  = (uchar_ptr)frag_ptr->FRAGMENT;
   } /* Endif */

   _int_disable();
   dev_ptr->TX_BUFFER = buf_ptr;
   dev_ptr->TX_SIZE   = size;
   dev_ptr->CONTROL   = EMWSIM_CONTROL_TRANSMIT;
   _int_enable();

   PCB_free(packet);

   enet_ptr->STATS.ST_TX_TOTAL++;

   return ENET_OK;

} /* Endbody */

/* EOF */

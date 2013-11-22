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
*** Comments:  This file contains the VMAC Ethernet send
***            support function.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "enet.h"
#include "enetprv.h"

/* Start CR 809 */
#if !VMAC_ENABLE_TX_CHAINING
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
   VMAC_BUFFER_DESCR_STRUCT_PTR        bd_ptr;
   VMAC_REG_STRUCT _PTR_               dev_ptr;
   PCB_FRAGMENT_PTR                    frag_ptr;
   uint_32                             tmp;
   uchar_ptr                           buf_ptr;
   uchar_ptr                           saved_buf_ptr;

   dev_ptr = enet_ptr->DEV_PTR;

   /*
   ** Ensure that there is room to put the packet on the transmit ring.
   */
   if (frags > enet_ptr->TXENTRIES) {
      enet_ptr->STATS.ST_TX_MISSED++;
      return ENETERR_SEND_FULL;
   } /* Endif */

   buf_ptr = (enet_ptr->TXCOMPLETEPACKETS + 
      (enet_ptr->TXNEXT * ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX)));

   /*
   ** Enqueue the packet on the transmit ring.
   */
   bd_ptr = enet_ptr->TX_RING + enet_ptr->TXNEXT;
   
   _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, _BSP_READ_VMAC(&bd_ptr->CTRL_INFO) 
      & ~VMAC_TX_CTL_INFO_CPU_TX_LEN);
   
   /* Initialize the buffer pointer to point to the packet to xmit */
   _BSP_WRITE_VMAC(&bd_ptr->BUFFER_PTR, buf_ptr);

   saved_buf_ptr = buf_ptr;
   for (frag_ptr = packet->FRAG; frag_ptr->LENGTH; 
      buf_ptr += frag_ptr->LENGTH, frag_ptr++) 
   {
      ENET_memcopy(frag_ptr->FRAGMENT, buf_ptr, frag_ptr->LENGTH);
   } /* Endfor */
   
   /* Enforce the minimum frame size of 64 bytes total */
   if (size < ENET_FRAMESIZE_MIN) {
      ENET_memzero(buf_ptr, ENET_FRAMESIZE_MIN - size);
      size = ENET_FRAMESIZE_MIN;
   } /* Endif */

   _DCACHE_FLUSH_MLINES(saved_buf_ptr, size);

   /* Set the xmit size to size + pad size */
   tmp  = _BSP_READ_VMAC(&bd_ptr->CTRL_INFO);
   tmp  = (tmp & VMAC_SIZE_CONST) | size;
   _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, tmp);

   /* Set the first and last BDs */
   tmp |= (VMAC_TX_CTL_INFO_FIRST | VMAC_TX_CTL_INFO_LAST);
   _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, tmp);
   
   /* Xfer the ownership of the BD to VMAC */
   tmp |= VMAC_TX_CTL_INFO_OWN;
   _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, tmp);

   /* Force tx polling */   
   tmp = VMAC_ISR_TXPOLL;
   _BSP_WRITE_VMAC(&dev_ptr->INT_STATUS,tmp);
   
   enet_ptr->TXENTRIES--;
   TX_INC(enet_ptr->TXNEXT);

   PCB_free(packet);

   return ENET_OK;

} /* Endbody */
#else
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
   VMAC_REG_STRUCT _PTR_               dev_ptr;
   PCB_FRAGMENT_PTR                    frag_ptr;
   VMAC_BUFFER_DESCR_STRUCT_PTR        first_bd_ptr;
   VMAC_BUFFER_DESCR_STRUCT_PTR        bd_ptr;
   uint_32                             tmp;
   boolean                             extra;

   dev_ptr = enet_ptr->DEV_PTR;

   extra = size < ENET_FRAMESIZE_MIN;

   /*
   ** Ensure that there is room to put the packet on the transmit ring.
   */
   if ((frags + extra) > enet_ptr->TXENTRIES) {
      enet_ptr->STATS.ST_TX_MISSED++;
      return ENETERR_SEND_FULL;
   } /* Endif */


   /*
   ** Enqueue the packet on the transmit ring.  Don't set the ready
   ** bit in the first descriptor until all descriptors are enqueued.
   */
   bd_ptr = enet_ptr->TX_RING + enet_ptr->TXNEXT;
   first_bd_ptr = bd_ptr;
   frag_ptr = packet->FRAG;

   tmp = frag_ptr->LENGTH;
   _BSP_WRITE_VMAC(&first_bd_ptr->CTRL_INFO, tmp);
   _BSP_WRITE_VMAC(&first_bd_ptr->BUFFER_PTR, frag_ptr->FRAGMENT);
   _DCACHE_FLUSH_MLINES(frag_ptr->FRAGMENT, frag_ptr->LENGTH);
 
   enet_ptr->TXENTRIES--;
   frag_ptr++;

   while (frag_ptr->LENGTH) {
      TX_INC(enet_ptr->TXNEXT);
      bd_ptr = enet_ptr->TX_RING + enet_ptr->TXNEXT;
      tmp = frag_ptr->LENGTH | VMAC_TX_CTL_INFO_OWN;
      _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, tmp);
      _BSP_WRITE_VMAC(&bd_ptr->BUFFER_PTR, frag_ptr->FRAGMENT);
      _DCACHE_FLUSH_MLINES(frag_ptr->FRAGMENT, frag_ptr->LENGTH);
      frag_ptr++;
      enet_ptr->TXENTRIES--;
   } /* Endif */
   
   /* Setup the last BD */
   if (extra) {
      TX_INC(enet_ptr->TXNEXT);
      bd_ptr = enet_ptr->TX_RING + enet_ptr->TXNEXT;
      tmp = (ENET_FRAMESIZE_MIN - size) | VMAC_TX_CTL_INFO_LAST | 
         VMAC_TX_CTL_INFO_ADDCRC | VMAC_TX_CTL_INFO_OWN;
      _BSP_WRITE_VMAC(&bd_ptr->BUFFER_PTR, enet_ptr->PAD_ARRAY);
      enet_ptr->TXENTRIES--;
   } else {
      tmp |= VMAC_TX_CTL_INFO_LAST | VMAC_TX_CTL_INFO_ADDCRC;
   } /* Endif */
   _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, tmp);

   /* Finish setting up the first BD */
   tmp  = _BSP_READ_VMAC(&first_bd_ptr->CTRL_INFO);
   tmp |= VMAC_TX_CTL_INFO_FIRST | VMAC_TX_CTL_INFO_OWN | VMAC_TX_CTL_INFO_ADDCRC;
   _BSP_WRITE_VMAC(&first_bd_ptr->CTRL_INFO, tmp);

   enet_ptr->TXPCBS[enet_ptr->TXNEXT] = packet;
   TX_INC(enet_ptr->TXNEXT);

   return ENET_OK;

} /* Endbody */
#endif
/* End   CR 809 */
/* EOF */

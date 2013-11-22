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
*** File: eninit.c
***
*** Comments:  This file contains the Ethernet initialization
***            functions.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "enetprv.h"


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_initialize
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Initializes the chip.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_initialize
   (
      /* [IN] the device to initialize */
      uint_32              devnum,

      /* [IN] the local Ethernet address */
      _enet_address        address,

      /* [IN] optional flags */
      uint_32              flags,

      /* [OUT] the Ethernet state structure */
      _enet_handle _PTR_   handle
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR           enet_ptr;
   VMAC_BUFFER_DESCR_STRUCT_PTR  bd_ptr;
   VMAC_REG_STRUCT_PTR           dev_ptr;
   PCB_FRAGMENT_PTR              frag_ptr;
   uchar_ptr                     pcb_base, buf_ptr;
   PCB_PTR                       pcb_ptr;
   pointer                       alloc_ptr;
   uint_32                       i;
   uint_32                       speed;

   /* Start CR 660  */
   if (!_bsp_enet_get_phyid(devnum)) {
      return ENETERR_INIT_DEVICE;
   } /* Endif */
   /* End CR 660 */
 
   if (ENET_get_handle(devnum) != NULL) {
      return ENETERR_INIT_DEVICE;
   } /* Endif */

   /* Multiple devices would have different base addresses */
   dev_ptr = _bsp_enet_getbase(devnum);

   /* Start CR 751 */
   if (!dev_ptr) {
      return ENETERR_INIT_DEVICE;
   } /* Endif */
   /* End   CR 751 */

   /* Allocate the state structure */
   enet_ptr = ENET_memalloc(sizeof(ENET_CFG_STRUCT));
   if (enet_ptr == NULL) {
      return ENETERR_ALLOC_CFG;
   } /* Endif */

   /* Initialize the state structure */
   enet_ptr->ECB_HEAD    = NULL;
   enet_ptr->DEV_NUM     = devnum;
   enet_ptr->DEV_PTR     = dev_ptr;
   enet_ptr->DEV_VEC     = _bsp_enet_getvec(devnum);
   enet_ptr->FULL_DUPLEX = FALSE;

   /*******************************************************************
   **
   **    Initialize the device
   */

   /* Disable all VMAC interrupts */
   _BSP_WRITE_VMAC(&dev_ptr->INT_ENABLE,0);

   /* Set the MAC address */
   for (i = 0; i < 6; i++) {
      enet_ptr->ADDRESS[i] = address[i];
   } /* Endfor */

   /* Write MAC address in little endian */
   _BSP_WRITE_VMAC(&dev_ptr->MAC_ADDR_L,enet_ptr->ADDRESS[0]);
   _BSP_WRITE_VMAC(&dev_ptr->MAC_ADDR_L,_BSP_READ_VMAC(&dev_ptr->MAC_ADDR_L) | (enet_ptr->ADDRESS[1] << 8));
   _BSP_WRITE_VMAC(&dev_ptr->MAC_ADDR_L,_BSP_READ_VMAC(&dev_ptr->MAC_ADDR_L) | (enet_ptr->ADDRESS[2] << 16));
   _BSP_WRITE_VMAC(&dev_ptr->MAC_ADDR_L,_BSP_READ_VMAC(&dev_ptr->MAC_ADDR_L) | (enet_ptr->ADDRESS[3] << 24));
   _BSP_WRITE_VMAC(&dev_ptr->MAC_ADDR_H,enet_ptr->ADDRESS[4]);
   _BSP_WRITE_VMAC(&dev_ptr->MAC_ADDR_H,_BSP_READ_VMAC(&dev_ptr->MAC_ADDR_H) | (enet_ptr->ADDRESS[5] << 8));

   /* Clear the multicast filter */
   _BSP_WRITE_VMAC(&dev_ptr->MAC_LAF_L,0);
   _BSP_WRITE_VMAC(&dev_ptr->MAC_LAF_H,0);

   if (flags & ENET_AUTONEGOTIATE) {
      /* Do board specific initialization */
      _bsp_enet_init((pointer)enet_ptr, 0, 0);
   } else if (BSP_SYSTEM_CLOCK <= 25000000) {
      speed = ENET_get_speed((_enet_handle)enet_ptr);
      if (speed > 10) {
         /*
         ** We need to force auto-negotiation because the VMAC can't handle
         ** 100mbit bit rates unless the clock is more than 25 MHz
         */
         _bsp_enet_init((pointer)enet_ptr, 0, 0);
      } /* Endif */
   } /* Endif */
   
   /* 
   ** Set the no. of Tx and Rx BDTs to TX_RING_LEN and RX_RING_LEN 
   ** respectively 
   ** 
   ** Note: Ring length should be such that the Tx and Rx rings should 
   ** fall on word boundaries
   */
   _BSP_WRITE_VMAC(&dev_ptr->CONTROL, ((RX_RING_LEN << 16) | (TX_RING_LEN << 24)));

   /* Set the poll rate */
   _BSP_WRITE_VMAC(&dev_ptr->POLLRATE, BSP_VMAC_TX_POLLRATE);

   /* 
   ** Set the Tx and Rx RINGPTR address base 
   ** 
   ** Note: Ensure that the Rings start on a 32-bit aligned address 
   */
   alloc_ptr = ENET_memalloc(ENET_DESCR_ALIGN((TX_RING_LEN + RX_RING_LEN) * 
      (sizeof(VMAC_BUFFER_DESCR_STRUCT))) + ENET_DESCR_ALIGN(1));

   if (alloc_ptr == NULL) {
      ENET_memfree(enet_ptr);
      return ENETERR_ALLOC_BD;
   } /* Endif */
      
   enet_ptr->START_TX_RING = alloc_ptr;

   enet_ptr->TX_RING = 
      (VMAC_BUFFER_DESCR_STRUCT_PTR)ENET_DESCR_ALIGN((uint_32)alloc_ptr);

   _BSP_WRITE_VMAC(&dev_ptr->TXRINGPTR,enet_ptr->TX_RING);

   enet_ptr->RX_RING = (enet_ptr->TX_RING + TX_RING_LEN);
   _BSP_WRITE_VMAC(&dev_ptr->RXRINGPTR, enet_ptr->RX_RING);

   /* Initialize the state structure */
   enet_ptr->RXHEAD    = NULL;
   enet_ptr->RXTAIL    = NULL;
   enet_ptr->RXENTRIES = 0;
   enet_ptr->RXNEXT    = 0;
   enet_ptr->RXLAST    = 0;
   enet_ptr->TXENTRIES = TX_RING_LEN;
   enet_ptr->TXNEXT    = 0;
   enet_ptr->TXLAST    = 0;


   /********************************************************************
   ** Allocate the PCBs for receiving packets
   */

   pcb_base = ENET_memalloc(RX_RING_LEN * (sizeof(PCB) + sizeof(PCB_FRAGMENT) +
      ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX) + ENET_FRAMESIZE_ALIGN(1)));
   if (pcb_base == NULL) {
      ENET_memfree(enet_ptr);
      ENET_memfree(alloc_ptr);
      return ENETERR_ALLOC_PCB;
   } /* Endif */
   
   enet_ptr->PCB_BASE  = pcb_base;
   
   bd_ptr = enet_ptr->TX_RING;
   for (i=0;i < TX_RING_LEN; i++) {
      _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO,0);
      bd_ptr++;
   } /* Endfor */

   /* Build a queue of PCBs for receiving into */
   pcb_ptr = (PCB_PTR)pcb_base;
   buf_ptr = pcb_base + RX_RING_LEN*(sizeof(PCB)+sizeof(PCB_FRAGMENT));
   buf_ptr = (uchar_ptr)ENET_FRAMESIZE_ALIGN((uint_32)buf_ptr);
   for (i = 0; i < RX_RING_LEN; i++) {
      frag_ptr = pcb_ptr->FRAG;
      pcb_ptr->FREE    = ENET_rx_add;
      pcb_ptr->PRIVATE = (pointer)enet_ptr;
      /* frag_ptr[0].LENGTH filled in by RxCatchup */
      frag_ptr[0].FRAGMENT = buf_ptr;
      frag_ptr[1].LENGTH   = 0;
      frag_ptr[1].FRAGMENT = NULL;
      ENET_rx_add(pcb_ptr);
      pcb_ptr = (PCB_PTR)((uchar_ptr)pcb_ptr+sizeof(PCB)+sizeof(PCB_FRAGMENT));
      buf_ptr += ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX);
   } /* Endfor */

/* Start CR 809 */
#if !VMAC_ENABLE_TX_CHAINING
   /* Build a queue for assembling Tx packets */
   alloc_ptr = ENET_memalloc(TX_RING_LEN * 
      (ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX) + 
      ENET_FRAMESIZE_ALIGN(1)));
      
   if (alloc_ptr == NULL)  {
      ENET_memfree(enet_ptr->START_TX_RING);
      ENET_memfree(pcb_base);
      ENET_memfree(enet_ptr);
      return ENETERR_ALLOC_BD;
   } /* Endif */
      
   enet_ptr->START_TXCOMPLETEPACKETS = alloc_ptr;

   enet_ptr->TXCOMPLETEPACKETS = (uchar_ptr)ENET_FRAMESIZE_ALIGN((uint_32)alloc_ptr);
#endif
/* End   CR 809 */

   /********************************************************************
   **
   **    Enable interrupts
   */

   /* Install the Ethernet ISR */
   enet_ptr->OLDISR_PTR  = _int_get_isr(enet_ptr->DEV_VEC);
   enet_ptr->OLDISR_DATA = _int_get_isr_data(enet_ptr->DEV_VEC);
   if (!_int_install_isr(enet_ptr->DEV_VEC, ENET_ISR, (pointer)enet_ptr)) {
      ENET_memfree(alloc_ptr);
      ENET_memfree(enet_ptr->START_TX_RING);
      ENET_memfree(pcb_base);
      ENET_memfree(enet_ptr);
      return ENETERR_INSTALL_ISR;
   } /* Endif */
  
   /* Clear all Interrupt Status bits */
   _BSP_WRITE_VMAC(&dev_ptr->INT_STATUS,0xFFFFFFFF);

   /* Enable all interrupts */
   _BSP_WRITE_VMAC(&dev_ptr->INT_ENABLE,~VMAC_ISR_MDIO);
   
   /* 
   ** Check the MDIO to see what duplex we should be in and configure the
   ** VMAC accordingly
   */
   _bsp_enet_init((pointer)enet_ptr, 1, 0);

   /* Enable Ethernet, Tx and Rx and route all interrupts through Tx interrupt */
   _BSP_WRITE_VMAC(&dev_ptr->CONTROL,  _BSP_READ_VMAC(&dev_ptr->CONTROL)
      | VMAC_CTRL_ENABLE | VMAC_CTRL_TXRUN | VMAC_CTRL_RXRUN
      | VMAC_CTRL_RXINT2TX);

#ifdef BSP_ENABLE_VMAC_POLLING
   /* Start polling duplex mode  */
   if (flags & ENET_POLL_PEER_PERIODICALLY) {
      _VMAC_init_poll_mode((pointer)enet_ptr);
   } /* Endif */
#endif
   
   *handle = (_enet_handle)(pointer)enet_ptr;

   return ENET_OK;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_get_handle
*  Returned Value : Initialized Ethernet handle or NULL
*  Comments       :
*        Retrieves an initialized Ethernet handle.
*
*END*-----------------------------------------------------------------*/

_enet_handle ENET_get_handle
   (
      /* [IN] the initialized ENET channel */
      uint_32  devnum
   )
{ /* Body */
   uint_32 chipvector = _bsp_enet_getvec(devnum);

   /*
   ** Determine if Ethernet has been initialized
   */
   if (_int_get_isr(chipvector) != ENET_ISR) {
      return NULL;
   } /* Endif */

   /*
   ** Find the handle and return it
   */
   return (_enet_handle)_int_get_isr_data(chipvector);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_rx_add
*  Returned Value : void
*  Comments       :
*        Enqueues a PCB onto the receive ring.
*
*END*-----------------------------------------------------------------*/

void ENET_rx_add
   (
      /* [IN] the PCB to enqueue */
      PCB_PTR  pcb_ptr
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR enet_ptr = (ENET_CFG_STRUCT_PTR)pcb_ptr->PRIVATE;
   VMAC_BUFFER_DESCR_STRUCT_PTR  bd_ptr;
   uint_32                       tmp;

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself.
   */
   ENET_lock();
   
   /* Get the address of the buffer descriptor from the Rx ring */
   bd_ptr = enet_ptr->RX_RING + enet_ptr->RXLAST;
   /* Initialize the BD with the Rx buffer address and size of buffer */
   _BSP_WRITE_VMAC(&bd_ptr->BUFFER_PTR, pcb_ptr->FRAG[0].FRAGMENT);
   tmp = _BSP_READ_VMAC(&bd_ptr->CTRL_INFO) & ~VMAC_RX_CTL_INFO_RX_LEN;
   _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, tmp); 
   tmp |= ENET_FRAMESIZE_MAX;
   _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, tmp); 
   /* Set the OWN bit to submit the BD to VMAC */
   tmp |= VMAC_RX_CTL_INFO_OWN;
   _BSP_WRITE_VMAC(&bd_ptr->CTRL_INFO, tmp); 

   /*
   ** Add the PCB to the receive PCB queue (linked via PRIVATE) and
   ** increment the tail to the next descriptor
   */
   QADD(enet_ptr->RXHEAD, enet_ptr->RXTAIL, pcb_ptr);
   RX_INC(enet_ptr->RXLAST);
   enet_ptr->RXENTRIES++;
   ENET_unlock();

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_rx_catchup
*  Returned Value : void
*  Comments       :
*        Processes received packets.
*
*END*-----------------------------------------------------------------*/

void ENET_rx_catchup
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr
   )
{ /* Body */
   VMAC_BUFFER_DESCR_STRUCT_PTR  bd_ptr;
   ENET_ECB_STRUCT_PTR           ecb_ptr;
   PCB_PTR                       pcb_ptr;
   uint_32                       length;

   /* Dequeue all received packets */
   while (enet_ptr->RXENTRIES > 0) {

      /* Get the address of the BD from the RX ring */
      bd_ptr = enet_ptr->RX_RING + enet_ptr->RXNEXT;
      
      if (_BSP_READ_VMAC(&bd_ptr->CTRL_INFO) & VMAC_TX_CTL_INFO_OWN) {
         break;
      } /* Endif */   
      
      enet_ptr->STATS.ST_RX_TOTAL++;

      /* Get the PCB from the PCB queue */
      QGET(enet_ptr->RXHEAD, enet_ptr->RXTAIL, pcb_ptr);
      pcb_ptr->PRIVATE = (pointer)enet_ptr;
      RX_INC(enet_ptr->RXNEXT);
      enet_ptr->RXENTRIES--;
      
      if ((!(_BSP_READ_VMAC(&bd_ptr->CTRL_INFO) & VMAC_RX_CTL_INFO_FIRST)) || 
         (!(_BSP_READ_VMAC(&bd_ptr->CTRL_INFO) & VMAC_RX_CTL_INFO_LAST))) {
         ENET_rx_add(pcb_ptr);
      } else  {
         /* Get the length of the packet received and subtract the FCS 
         ** from the length of the packet 
         */
         length = (_BSP_READ_VMAC(&bd_ptr->CTRL_INFO) & VMAC_RX_CTL_INFO_RX_LEN)
            - ENET_FRAMESIZE_TAIL;

         _DCACHE_INVALIDATE_MLINES((pointer)_BSP_READ_VMAC(&bd_ptr->BUFFER_PTR), 
            length);

         /* Start CR 552 */
         /* if ((length > (ENET_FRAMESIZE_MAX)) || */
         if ((length > (ENET_FRAMESIZE_MAX - ENET_FRAMESIZE_EXTRA)) || 
         /* End   CR 552 */
            (length < (ENET_FRAMESIZE_MIN)))  
         {
            ENET_rx_add(pcb_ptr);
         } else {
            
            pcb_ptr->FRAG[0].LENGTH = length;
   
            /* Check if we should receive this packet */
            ecb_ptr = ENET_recv(enet_ptr, pcb_ptr);
   
            /* If the packet is for us then pass it to upper layer 
            ** else discard 
            */
            if (ecb_ptr) {
               ecb_ptr->SERVICE(pcb_ptr, ecb_ptr->PRIVATE);
               pcb_ptr = NULL;
            } else {
               enet_ptr->STATS.ST_RX_DISCARDED++;
            } /* Endif */
         
            if (pcb_ptr) {
               ENET_rx_add(pcb_ptr);
            } /* Endif */
         } /* Endif */
      } /* Endif */
   } /* Endwhile */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_tx_record
*  Returned Value : void
*  Comments       :
*        Records statistics following frame transmission.
*
*END*-----------------------------------------------------------------*/

void ENET_tx_record
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr,
      /* [IN] the descriptor status word (all buffers) */
      uint_32              status
   )
{ /* Body */
   uint_32  coll;

   enet_ptr->STATS.ST_TX_TOTAL++;

   if (status & (VMAC_TX_CTL_INFO_CARR_LOSS
     | VMAC_TX_CTL_INFO_UFLO
     | VMAC_TX_CTL_INFO_RETRY
     | VMAC_TX_CTL_INFO_LATE_COLL)) 
   {

      /* If an error occurred, record it */
      enet_ptr->STATS.ST_TX_ERRORS++;
      if (status & VMAC_TX_CTL_INFO_CARR_LOSS) {
         enet_ptr->STATS.ST_TX_CARRIER++;
      } /* Endif */
      if (status & VMAC_TX_CTL_INFO_UFLO) {
         enet_ptr->STATS.ST_TX_UNDERRUN++;
      } /* Endif */
      if (status & (VMAC_TX_CTL_INFO_RETRY 
         | VMAC_TX_CTL_INFO_DROPPED)) {
         enet_ptr->STATS.ST_TX_EXCESSCOLL++;
      } /* Endif */
      if (status & VMAC_TX_CTL_INFO_LATE_COLL) {
         enet_ptr->STATS.ST_TX_LATECOLL++;
      } /* Endif */

   } else {
      coll = ((status & VMAC_TX_CTL_INFO_RETRY) >> 24);
      enet_ptr->STATS.ST_TX_COLLHIST[coll]++;
   } /* Endif */

   if (status & VMAC_TX_CTL_INFO_DEFER) {
      enet_ptr->STATS.ST_TX_DEFERRED++;
   } /* Endif */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_tx_catchup
*  Returned Value : void
*  Comments       :
*        Processes transmitted packets.
*
*END*-----------------------------------------------------------------*/

void ENET_tx_catchup
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr
   )
{ /* Body */
   VMAC_BUFFER_DESCR_STRUCT_PTR  bd_ptr;
   uint_32                       status;

   /* Dequeue all transmitted frames */
   while (enet_ptr->TXENTRIES < TX_RING_LEN) {
      /* Get the address of the Tx descriptor */
      bd_ptr = &enet_ptr->TX_RING[enet_ptr->TXLAST];

      /* Make sure VMAC doesn't OWN the BD */
      status = _BSP_READ_VMAC(&bd_ptr->CTRL_INFO);
      if (status & VMAC_TX_CTL_INFO_OWN) {
         break;
      } /* Endif */

/* Start CR 809 */
#if VMAC_ENABLE_TX_CHAINING
      enet_ptr->TX_ERRORS |= status;
      status = enet_ptr->TX_ERRORS;

      /* Record statistics for each frame (not each buffer) */
      if (status & VMAC_TX_CTL_INFO_LAST) {
         PCB_PTR                       pcb_ptr;

         enet_ptr->TX_ERRORS = 0;

         ENET_tx_record(enet_ptr, status);

         pcb_ptr = enet_ptr->TXPCBS[enet_ptr->TXLAST];
         PCB_free(pcb_ptr);
      } /* Endif */
#else
      ENET_tx_record(enet_ptr, status);
#endif
/* End   CR 809 */

      TX_INC(enet_ptr->TXLAST);
      enet_ptr->TXENTRIES++;
   } /* Endwhile */

} /* Endbody */


/*NOTIFIER*-------------------------------------------------------------
*
*  Function Name  : ENET_ISR
*  Returned Value : void
*  Comments       :
*        The ISR of the chip
*
*END*-----------------------------------------------------------------*/

void ENET_ISR
   (
      /* [IN] the Ethernet state structure */
      pointer  enet
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR   enet_ptr = (ENET_CFG_STRUCT_PTR)enet;
   VMAC_REG_STRUCT_PTR   dev_ptr = enet_ptr->DEV_PTR;
   uint_32               status;


   for (;;) {
      /* Get the cause of the interrupt. */
      /* Start CR 420 */
      /* status = _BSP_READ_VMAC(&dev_ptr->INT_STATUS); */
      /* Don't clear the MDIO interrupt */
      status = _BSP_READ_VMAC(&dev_ptr->INT_STATUS) & (~VMAC_ISR_MDIO);
      /* End CR 420 */
      
      if (!status) break;

      /* Clear all interrupt status bits */
      _BSP_WRITE_VMAC(&dev_ptr->INT_STATUS, status);

      /* Packet Received. Read all the packets and pass them to upper 
      ** layer or discard them if they are not for us 
      */
      if (status & VMAC_ISR_RXINT) {
         ENET_rx_catchup(enet_ptr);
      } /* Endif */

      /* Tx done. Update the Stats and xmit pending packets */
      if (status & VMAC_ISR_TXINT) {
         ENET_tx_catchup(enet_ptr);
      }/* Endif */

      /* Check for errors and update the statistics */
      if (status & VMAC_ISR_ERR) {

         if (status & VMAC_ISR_SQE) {
            enet_ptr->STATS.ST_TX_ERRORS++;
            enet_ptr->STATS.ST_TX_SQE++;
         } /* Endif */
         
         if (status & VMAC_ISR_BUFFERR) {
           enet_ptr->STATS.ST_TX_ERRORS++;
           _task_set_error(MQX_INVALID_CONFIGURATION);
         } /* Endif */ 
         
         if (status & (VMAC_ISR_RXCRC | VMAC_ISR_RXFRAME | VMAC_ISR_RXOFLOW)) {
            enet_ptr->STATS.ST_RX_ERRORS++;
            if (status & VMAC_ISR_RXCRC) {
               enet_ptr->STATS.ST_RX_FCS++;
            } /* Endif */

            if (status & VMAC_ISR_RXFRAME) {
               enet_ptr->STATS.ST_RX_ALIGN++;
            } /* Endif */

            if (status & VMAC_ISR_RXOFLOW) {
               enet_ptr->STATS.ST_RX_OVERRUN++;
            } /* Endif */

         } /* Endif */
      } /* Endif */

   } /* Endfor */

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_get_speed
*  Returned Value : uint_32
*  Comments       :
*        Returns speed of connection in megahertz
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_get_speed
   (
      /* [IN] the Ethernet state structure */
      _enet_handle  handle
   )
{ /* Body */
   uint_32    result;

   result = _bsp_enet_get_speed((pointer)handle);

   return result;

} /* Endbody */

/* EOF */

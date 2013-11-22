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
#include "enet.h"
#include "enetprv.h"

/* Global variable for debugging */
ENET_CFG_STRUCT_PTR  ENET_enet_ptr;

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

      /* [IN] optional flags, zero = default, non-zero=use chip enet address */
      uint_32              flags,

      /* [OUT] the Ethernet state structure */
      _enet_handle _PTR_   handle
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR          enet_ptr;
   volatile EMWSIM_STRUCT _PTR_ dev_ptr;
   uchar_ptr                    pcb_base, buf_ptr;
   PCB_PTR                      pcb_ptr;
   uint_32                      i;

   if (devnum != 0) {
      return ENETERR_INVALID_DEVICE;
   } /* Endif */

   if (ENET_get_handle(devnum) != NULL) {
      return ENETERR_INIT_DEVICE;
   } /* Endif */

   /* Multiple devices would have different base addresses */
   dev_ptr = (volatile EMWSIM_STRUCT _PTR_)_bsp_enet_getbase(devnum);

   /* Allocate the state structure */
   enet_ptr = ENET_memalloc(sizeof(ENET_CFG_STRUCT));
   if (enet_ptr == NULL) {
      return ENETERR_ALLOC_CFG;
   } /* Endif */

   ENET_enet_ptr = enet_ptr;

   /* Initialize the state structure */
   ENET_memcopy((uchar_ptr)address, (uchar_ptr)enet_ptr->ADDRESS,
      sizeof(_enet_address));
   enet_ptr->ECB_HEAD = NULL;
   enet_ptr->DEV_NUM  = devnum;
   enet_ptr->DEV_PTR  = dev_ptr;

   _bsp_enet_init(devnum, 0, flags);

   /* Initialize the device */
   dev_ptr->RX_BUFFER = NULL;
   dev_ptr->TX_BUFFER = NULL;
   dev_ptr->CONTROL   = EMWSIM_CONTROL_INITIALIZE;
   if (!(dev_ptr->STATUS & EMWSIM_STATUS_INITIALIZED)){
      ENET_memfree((uchar_ptr)enet_ptr);
      return ENETERR_INVALID_DEVICE;
   } /* Endif */

   /* Allocate the PCBs for receiving packets */
   pcb_base = ENET_memalloc(RX_RING_LEN * (sizeof(PCB) + sizeof(PCB_FRAGMENT) +
      ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX)));
   if (pcb_base == NULL) {
      ENET_memfree((uchar_ptr)enet_ptr);
      return ENETERR_ALLOC_PCB;
   } /* Endif */

   enet_ptr->PCB_BASE  = pcb_base;
   
   /* Build a queue of PCBs for receiving into */
   pcb_ptr = (PCB_PTR)pcb_base;
   buf_ptr = pcb_base + RX_RING_LEN*(sizeof(PCB)+sizeof(PCB_FRAGMENT));
   for (i = 0; i < RX_RING_LEN; i++) {
      pcb_ptr->FREE    = ENET_rx_add;
      pcb_ptr->PRIVATE = (pointer)enet_ptr;
   /* pcb_ptr->FRAG[0].LENGTH filled in by RxCatchup */
      pcb_ptr->FRAG[0].FRAGMENT = buf_ptr;
      pcb_ptr->FRAG[1].LENGTH   = 0;
      pcb_ptr->FRAG[1].FRAGMENT = NULL;
      ENET_rx_add(pcb_ptr);
      pcb_ptr = (PCB_PTR)((uchar_ptr)pcb_ptr+sizeof(PCB)+sizeof(PCB_FRAGMENT));
      buf_ptr += ENET_FRAMESIZE_ALIGN(ENET_FRAMESIZE_MAX);
   } /* Endfor */
   
   /********************************************************************
   **
   **    Enable interrupts
   */

   enet_ptr->DEV_VEC = dev_ptr->INTERRUPT_CONFIGURATION & 0xFFFF;
   /* Install the Ethernet ISR */
   enet_ptr->OLDISR_PTR  = _int_get_isr(enet_ptr->DEV_VEC);
   enet_ptr->OLDISR_DATA = _int_get_isr_data(enet_ptr->DEV_VEC);
   if (!_int_install_isr(enet_ptr->DEV_VEC, ENET_ISR, (pointer)enet_ptr)) {
      ENET_memfree(pcb_base);
      ENET_memfree((uchar_ptr)enet_ptr);
      return ENETERR_INSTALL_ISR;
   } /* Endif */
  
   _bsp_enet_init(devnum, 1, flags);

   dev_ptr->CONTROL   = EMWSIM_CONTROL_INT_ENABLE;

   /* Start the Device */
   QGET(enet_ptr->RxHead, enet_ptr->RxTail, pcb_ptr);
   enet_ptr->RxEntries--;
   enet_ptr->RxCurrent = pcb_ptr;
   dev_ptr->RX_BUFFER = pcb_ptr->FRAG[0].FRAGMENT;
   dev_ptr->CONTROL   = EMWSIM_CONTROL_READY_TO_RX;

   *handle = (_enet_handle _PTR_)((pointer)enet_ptr);

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
      /* [IN] the initialized SCC */
      uint_32  devnum
   )
{ /* Body */
   volatile EMWSIM_STRUCT _PTR_ dev_ptr;
   uint_32 chipvector;

   dev_ptr = (volatile EMWSIM_STRUCT _PTR_)_bsp_enet_getbase(devnum);
   if (dev_ptr == NULL) {
      return NULL;
   } /* Endif */

   chipvector = dev_ptr->INTERRUPT_CONFIGURATION & 0xFFFF;
   /* Determine whether Ethernet has been initialized */
   if (_int_get_isr(chipvector) != ENET_ISR) {
      return NULL;
   } /* Endif */

   /* Find the handle and return it */
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

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself.
   */
   ENET_lock();

   /*
   ** Add the PCB to the receive PCB queue (linked via PRIVATE) and
   ** increment the tail to the next descriptor
   */
   QADD(enet_ptr->RxHead, enet_ptr->RxTail, pcb_ptr);
   enet_ptr->RxEntries++;

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
   volatile EMWSIM_STRUCT _PTR_ dev_ptr = enet_ptr->DEV_PTR;
   ENET_ECB_STRUCT_PTR ecb_ptr;
   PCB_PTR             pcb_ptr;
   boolean             error;
   uint_32             size;

   while (dev_ptr->STATUS & EMWSIM_STATUS_RX_RECEIVED) {

      error = FALSE;
      size = dev_ptr->RX_SIZE;

      /* Record statistics */
      enet_ptr->STATS.ST_RX_TOTAL++;
      if (size < (ENET_FRAMESIZE_MIN - ENET_FRAMESIZE_TAIL)) {
         enet_ptr->STATS.ST_RX_ERRORS++;
         enet_ptr->STATS.ST_RX_RUNT++;
         error = TRUE;
      } else if (size > ENET_FRAMESIZE_MAX) {
         enet_ptr->STATS.ST_RX_ERRORS++;
         enet_ptr->STATS.ST_RX_GIANT++;
         error = TRUE;
      } /* Endif */

      if (!enet_ptr->RxEntries) {
         enet_ptr->STATS.ST_RX_MISSED++;
         error = TRUE;
      } /* Endif */

      if (! error) {
         pcb_ptr = enet_ptr->RxCurrent;
         pcb_ptr->FRAG[0].LENGTH = size;
         ecb_ptr = ENET_recv(enet_ptr, pcb_ptr);
         if (ecb_ptr == NULL) {
            enet_ptr->STATS.ST_RX_DISCARDED++;
            error = TRUE;
         } else {
            /* Forward the PCB to the application */
            ecb_ptr->SERVICE(pcb_ptr, ecb_ptr->PRIVATE);
         } /* Endif */
      } /* Endif */

      if (! error) {
         QGET(enet_ptr->RxHead, enet_ptr->RxTail, pcb_ptr);
         enet_ptr->RxEntries--;
         pcb_ptr->PRIVATE = (pointer)enet_ptr;
         enet_ptr->RxCurrent = pcb_ptr;
      } /* Endif */

      dev_ptr->RX_BUFFER = 0;
      dev_ptr->CONTROL   = EMWSIM_CONTROL_RECEIVED;
      dev_ptr->RX_BUFFER = enet_ptr->RxCurrent->FRAG[0].FRAGMENT;
      dev_ptr->CONTROL   = EMWSIM_CONTROL_READY_TO_RX;

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
   ENET_CFG_STRUCT_PTR          enet_ptr = (ENET_CFG_STRUCT_PTR)enet;
   volatile EMWSIM_STRUCT _PTR_ dev_ptr  = enet_ptr->DEV_PTR;

   enet_ptr->INTS++;
   /* Get the cause of the interrupt. */
   if (dev_ptr->STATUS & EMWSIM_STATUS_RX_RECEIVED) {
      enet_ptr->RX_INTS++;
      ENET_rx_catchup(enet_ptr);
   } /* Endif */

} /* Endbody */


/* Start CR 692 */
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

   return 100;

} /* Endbody */
/* End CR 692 */

/* EOF */

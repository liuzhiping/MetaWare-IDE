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
*** Comments:  This file contains the VMAC Ethernet shutdown
***            functions.
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
   ENET_CFG_STRUCT_PTR     enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   VMAC_REG_STRUCT_PTR     dev_ptr  = enet_ptr->DEV_PTR;

   /* Make sure all PCBs are free */
   if (enet_ptr->RXENTRIES != RX_RING_LEN)  {
      return ENETERR_FREE_PCB;
   } /* Endif */

   /* Disable Ethernet, Tx and Rx */
   _BSP_WRITE_VMAC(&dev_ptr->CONTROL, _BSP_READ_VMAC(&dev_ptr->CONTROL) 
      & ~(VMAC_CTRL_ENABLE | VMAC_CTRL_TXRUN | VMAC_CTRL_RXRUN));
  
   /* Free all resources */
   _int_install_isr(enet_ptr->DEV_VEC, enet_ptr->OLDISR_PTR,
      enet_ptr->OLDISR_DATA);

/* Start CR 809 */
#if !VMAC_ENABLE_TX_CHAINING
   ENET_memfree(enet_ptr->START_TXCOMPLETEPACKETS);
#endif
/* End   CR 809 */
   ENET_memfree(enet_ptr->PCB_BASE);
   ENET_memfree(enet_ptr->START_TX_RING);
   ENET_memfree((pointer)enet_ptr);

   return ENET_OK;

} /* Endbody */


/* EOF */

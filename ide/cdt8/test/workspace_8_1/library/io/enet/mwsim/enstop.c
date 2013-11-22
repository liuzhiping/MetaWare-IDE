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
*** Comments:  This file contains the Ethernet shutdown
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
      _enet_handle handle
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR          enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   volatile EMWSIM_STRUCT _PTR_ dev_ptr  = enet_ptr->DEV_PTR;

   ENET_lock();

   /* Software reset */
   dev_ptr->CONTROL = EMWSIM_CONTROL_SHUTDOWN;

   _bsp_enet_init(0, 2, 0);

   ENET_unlock();

   /* Make sure all PCBs are free */
   if (enet_ptr->RxEntries != RX_RING_LEN)  {
      return ENETERR_FREE_PCB;
   } /* Endif */
  
     /* Free all resources */
   _int_install_isr(enet_ptr->DEV_VEC, enet_ptr->OLDISR_PTR,
      enet_ptr->OLDISR_DATA);

   ENET_memfree(enet_ptr->PCB_BASE);
   ENET_memfree((uchar_ptr)enet_ptr);

   return ENET_OK;

} /* Endbody */


/* EOF */

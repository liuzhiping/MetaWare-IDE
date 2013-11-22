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
*** File: enrejoin.c
***
*** Comments:  This file contains the VMAC Ethernet multicasting
***            interface functions.
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
*  Function Name  : ENET_rejoin
*  Returned Value : void
*  Comments       :
*        Rejoins all joined multicast groups.  Called by ENET_close
*        and ENET_leave.
*
*END*-----------------------------------------------------------------*/

void ENET_rejoin_MAC
   (
      ENET_CFG_STRUCT_PTR  enet_ptr
         /* [IN] the Ethernet state structure */
   )
{ /* Body */
   VMAC_REG_STRUCT_PTR     dev_ptr = enet_ptr->DEV_PTR;
   ENET_ECB_STRUCT_PTR     ecb_ptr;
   ENET_MCB_STRUCT_PTR     mcb_ptr;

   /*
   ** This section needs mutual exclusion.
   */
   ENET_lock();

   /*
   ** Clear the multicast address filter
   */
   _BSP_WRITE_VMAC(&dev_ptr->MAC_LAF_L,0);
   _BSP_WRITE_VMAC(&dev_ptr->MAC_LAF_H,0);

   /*
   ** Add the remaining multicast groups to the group address filter
   */
   for (ecb_ptr = enet_ptr->ECB_HEAD;
        ecb_ptr;
        ecb_ptr = ecb_ptr->NEXT) {

      for (mcb_ptr = ecb_ptr->MCB_HEAD;
           mcb_ptr;
           mcb_ptr = mcb_ptr->NEXT) {

         /* Set the appropriate bit in the hash table */
         if (mcb_ptr->HASH < 32) {
            _BSP_WRITE_VMAC(&dev_ptr->MAC_LAF_L, 
               _BSP_READ_VMAC(&dev_ptr->MAC_LAF_L) | 
               (0x1 << (mcb_ptr->HASH & 0x1F)));
         } else {
            _BSP_WRITE_VMAC(&dev_ptr->MAC_LAF_H, 
               _BSP_READ_VMAC(&dev_ptr->MAC_LAF_H) | 
               (0x1 << (mcb_ptr->HASH & 0x1F)));
         } /* Endif */

      } /* Endfor */
   } /* Endfor */

   ENET_unlock();

} /* Endbody */


/* EOF */

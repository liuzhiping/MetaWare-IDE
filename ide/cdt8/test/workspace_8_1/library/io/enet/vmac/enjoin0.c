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
*** File: enjoin0.c
***
*** Comments:  This file contains the VMAC multicasting
***            support functions.
***
***
************************************************************************
*END*******************************************************************/

#include <mqx.h>
#include <bsp.h>

#include "enet.h"
#include "enetprv.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_join_MAC
*  Returned Value : void
*  Comments       :
*        Joins a multicast group on an Ethernet channel.
*
*END*-----------------------------------------------------------------*/

void ENET_join_MAC
   (
      ENET_CFG_STRUCT_PTR     enet_ptr,
         /* [IN] the Ethernet state structure */
      ENET_MCB_STRUCT_PTR     mcb_ptr
         /* [IN] the multicast control block */
   )
{ /* Body */
   VMAC_REG_STRUCT_PTR     dev_ptr = enet_ptr->DEV_PTR;
   register uint_32        crc = 0xFFFFFFFFL;
            uint_32        i, j;
            uchar          c;

   /* Compute the CRC-32 polynomial on the multicast group */
   for (i = 0; i < 6; i++) {
      c = mcb_ptr->GROUP[i];
      for (j = 0; j < 8; j++) {
         if ((c ^ crc) & 1) {
            crc >>= 1;
            c >>= 1;
            crc ^= 0xEDB88320L;
         } else {
            crc >>= 1;
            c >>= 1;
         } /* Endif */
      } /* Endfor */
   } /* Endfor */

   mcb_ptr->HASH = (crc >> 26) & 0x3F;

   ENET_lock();

   /* Set the appropriate bit in the hash table */
   if (mcb_ptr->HASH < 32) {
      _BSP_WRITE_VMAC(&dev_ptr->MAC_LAF_L, _BSP_READ_VMAC(&dev_ptr->MAC_LAF_L)
         | (0x1 << (mcb_ptr->HASH & 0x1F)));
   } else {
      _BSP_WRITE_VMAC(&dev_ptr->MAC_LAF_H, _BSP_READ_VMAC(&dev_ptr->MAC_LAF_H) 
         | (0x1 << (mcb_ptr->HASH & 0x1F)));
   } /* Endif */

   ENET_unlock();

} /* Endbody */


/* EOF */

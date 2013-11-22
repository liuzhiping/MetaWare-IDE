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
*** File: enjoin.c
***
*** Comments:  This file contains the Ethernet multicasting
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
*  Function Name  : ENET_join
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Joins a multicast group on an Ethernet channel.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_join
   (
      /* [IN] the Ethernet state structure */
      _enet_handle      handle,

      /* [IN] the protocol */
      uint_16           type,

      /* [IN] the multicast group */
      _enet_address     address
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR     enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   ENET_ECB_STRUCT_PTR     ecb_ptr;
   ENET_MCB_STRUCT_PTR     mcb_ptr;
#ifdef ENET_ALLMCAST
   boolean  needjoin;
#endif

   /*
   ** Make sure it's a multicast group
   */
#if MQX_CHECK_ERRORS
   if ((address[0] & 1) == 0) {
      return ENETERR_JOIN_MULTICAST;
   } /* Endif */
#endif

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself and with ENET_leave.
   */
   ENET_lock();

   /*
   ** Make sure it's an open protocol
   */
   for (ecb_ptr = enet_ptr->ECB_HEAD;
        ecb_ptr;
        ecb_ptr = ecb_ptr->NEXT) {

      if (ecb_ptr->TYPE == type) {
         /* Found an existing entry */
         break;
      } /* Endif */
   } /* Endfor */

   /*
   ** No existing entry found
   */
   if (!ecb_ptr) {
      ENET_unlock();
      return ENETERR_CLOSE_PROT;
   } /* Endif */

   /*
   ** Create an entry for this group
   */
   mcb_ptr = ENET_memalloc(sizeof(ENET_MCB_STRUCT));
   if (!mcb_ptr) {
      ENET_unlock();
      return ENETERR_ALLOC_MCB;
   } /* Endif */
   mcb_ptr->GROUP[0] = address[0];
   mcb_ptr->GROUP[1] = address[1];
   mcb_ptr->GROUP[2] = address[2];
   mcb_ptr->GROUP[3] = address[3];
   mcb_ptr->GROUP[4] = address[4];
   mcb_ptr->GROUP[5] = address[5];

   mcb_ptr->NEXT = ecb_ptr->MCB_HEAD;
   ecb_ptr->MCB_HEAD = mcb_ptr;

#ifdef ENET_ALLMCAST
   needjoin = enet_ptr->MCOUNT < ENET_ALLMCAST;
   enet_ptr->MCOUNT++;
   if (needjoin) {
#endif
      ENET_join_MAC(enet_ptr, mcb_ptr);
#ifdef ENET_ALLMCAST
   } /* Endif */
#endif

   ENET_unlock();

   return ENET_OK;

} /* Endbody */


/* EOF */

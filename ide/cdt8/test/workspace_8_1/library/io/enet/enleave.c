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
*** File: enleave.c
***
*** Comments:  This file contains the Ethernet multicasting
***            interface functions.
***
************************************************************************
*END*******************************************************************/

#include <mqx.h>
#include <bsp.h>

#include "enet.h"
#include "enetprv.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_leave
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Leaves a multicast group on an Ethernet channel.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_leave
   (
      /* [IN] the Ethernet state structure */
      _enet_handle      handle,

      /* [IN] the protocol */
      uint_16           type,

      /* [IN] the multicast group */
      _enet_address     address
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR  enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   ENET_ECB_STRUCT_PTR  ecb_ptr;
   ENET_MCB_STRUCT_PTR  mcb_ptr, _PTR_ search_ptr;
#ifdef ENET_ALLMCAST
   boolean  needrejoin;
#endif

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself and with ENET_Join.
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
   ** Make sure it's a joined group
   */
   for (search_ptr = &ecb_ptr->MCB_HEAD;
       *search_ptr;
        search_ptr = &(*search_ptr)->NEXT) {

      if (((*search_ptr)->GROUP[0] == address[0])
       && ((*search_ptr)->GROUP[1] == address[1])
       && ((*search_ptr)->GROUP[2] == address[2])
       && ((*search_ptr)->GROUP[3] == address[3])
       && ((*search_ptr)->GROUP[4] == address[4])
       && ((*search_ptr)->GROUP[5] == address[5])) {
         /* Found the entry -- delete it */
         break;
      } /* Endif */
   } /* Endfor */

   if (!*search_ptr) {
      ENET_unlock();
      return ENETERR_LEAVE_GROUP;
   } /* Endif */

   mcb_ptr = *search_ptr;
   *search_ptr = mcb_ptr->NEXT;

#ifdef ENET_ALLMCAST
   enet_ptr->MCOUNT--;
   needrejoin = enet_ptr->MCOUNT < ENET_ALLMCAST;
#endif

   ENET_unlock();

   ENET_memfree(mcb_ptr);

#ifdef ENET_ALLMCAST
   if (needrejoin) {
#endif
      ENET_rejoin_MAC(enet_ptr);
#ifdef ENET_ALLMCAST
   } /* Endif */
#endif

   return ENET_OK;

} /* Endbody */


/* EOF */

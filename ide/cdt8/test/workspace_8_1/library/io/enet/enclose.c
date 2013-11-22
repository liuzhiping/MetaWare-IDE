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
*** File: enclose.c
***
*** Comments:  This file contains the Ethernet close
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
*  Function Name  : ENET_close
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Unregisters a protocol type on an Ethernet channel.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_close
   (
      /* [IN] the Ethernet state structure */
      _enet_handle      handle,

      /* [IN] the protocol */
      uint_16           type
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   ENET_ECB_STRUCT_PTR ecb_ptr, _PTR_ search_ptr;
   ENET_MCB_STRUCT_PTR mcb_ptr, next_ptr;
#ifdef ENET_ALLMCAST
   uint_32  mcount;
   boolean  needrejoin;
#endif

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself, ENET_open(), ENET_join(), ENET_leave(),
   ** and ENET_ISR().
   */
   ENET_lock();

   /*
   ** Search for an existing entry for type
   */
   for (search_ptr = (ENET_ECB_STRUCT_PTR _PTR_)&enet_ptr->ECB_HEAD;
       *search_ptr; search_ptr = &(*search_ptr)->NEXT) 
   {

      if ((*search_ptr)->TYPE == type) {
         /* Found an existing entry -- delete it */
         break;
      } /* Endif */
   } /* Endfor */

   /*
   ** No existing entry found
   */
   if (!*search_ptr) {
      ENET_unlock();
      return ENETERR_CLOSE_PROT;
   } /* Endif */

   ecb_ptr = *search_ptr;
   *search_ptr = ecb_ptr->NEXT;

#ifdef ENET_ALLMCAST
   mcb_ptr = ecb_ptr->MCB_HEAD;
   mcount = 0;
   while (mcb_ptr) {
      mcount++;
      mcb_ptr = mcb_ptr->NEXT;
   } /* Endwhile */
   enet_ptr->MCOUNT -= mcount;
   needrejoin = mcount && (enet_ptr->MCOUNT < ENET_ALLMCAST);
#endif

   ENET_unlock();

   mcb_ptr = ecb_ptr->MCB_HEAD;
   ENET_memfree(ecb_ptr);
   while (mcb_ptr) {
      next_ptr = mcb_ptr->NEXT;
      ENET_memfree(mcb_ptr);
      mcb_ptr = next_ptr;
   } /* Endwhile */

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

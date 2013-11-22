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
*** File: enopen.c
***
*** Comments:  This file contains the Ethernet open
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
*  Function Name  : ENET_open
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Registers a protocol type on an Ethernet channel.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_open
   (
      /* [IN] the Ethernet state structure */
      _enet_handle      handle,

      /* [IN] the protocol */
      uint_16           type,

      /* [IN] the callback function */
      void (_CODE_PTR_  service)(PCB_PTR, pointer),

      /* [IN] private data for the callback */
      pointer           private
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR enet_ptr = (ENET_CFG_STRUCT_PTR)handle;
   ENET_ECB_STRUCT_PTR ecb_ptr, _PTR_ search_ptr;

   /*
   ** This function can be called from any context, and it needs mutual
   ** exclusion with itself, ENET_close(), and ENET_ISR().
   */
   ENET_lock();

   /*
   ** Search for an existing entry for type
   */
   for (search_ptr = (ENET_ECB_STRUCT_PTR _PTR_)&enet_ptr->ECB_HEAD;
       *search_ptr;
        search_ptr = &(*search_ptr)->NEXT) {

      if ((*search_ptr)->TYPE == type) {
         /* Found an existing entry */
         ENET_unlock();
         return ENETERR_OPEN_PROT;
      } /* Endif */
   } /* Endfor */

   /*
   ** No existing entry found -- create a new one
   */
   ecb_ptr = ENET_memalloc(sizeof(ENET_ECB_STRUCT));
   if (!ecb_ptr) {
      ENET_unlock();
      return ENETERR_ALLOC_ECB;
   } /* Endif */
   ecb_ptr->TYPE     = type;
   ecb_ptr->SERVICE  = service;
   ecb_ptr->MCB_HEAD = NULL;
   ecb_ptr->PRIVATE  = private;
   ecb_ptr->NEXT     = NULL;
   *search_ptr       = ecb_ptr;

   ENET_unlock();
   return ENET_OK;

} /* Endbody */


/* EOF */

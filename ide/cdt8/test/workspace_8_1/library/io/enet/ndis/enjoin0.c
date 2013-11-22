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
*** Comments:  This file contains the Ethernet multicasting
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
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR     enet_ptr,

      /* [IN] the multicast control block */
      ENET_MCB_STRUCT_PTR     mcb_ptr
   )
{ /* Body */

} /* Endbody */

/* EOF */

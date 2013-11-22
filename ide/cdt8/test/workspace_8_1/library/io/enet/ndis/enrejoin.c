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
*** Comments:  This file contains the NDIS packet driver Ethernet multicasting
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
*  Function Name  : ENET_rejoin_MAC
*  Returned Value : void
*  Comments       :
*        Rejoins all joined multicast groups.  Called by ENET_close
*        and ENET_leave.
*
*END*-----------------------------------------------------------------*/

void ENET_rejoin_MAC
   (
      /* [IN] the Ethernet state structure */
      ENET_CFG_STRUCT_PTR  enet_ptr
   )
{ /* Body */

} /* Endbody */

/* EOF */

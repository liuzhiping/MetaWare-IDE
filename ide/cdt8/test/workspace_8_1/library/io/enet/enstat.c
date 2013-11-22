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
*** File: enstat.c
***
*** Comments:  This file contains the ENET_get_stats utility
***            function.
***
************************************************************************
*END*******************************************************************/

#include <mqx.h>
#include <bsp.h>

#include "enet.h"
#include "enetprv.h"

/*FUNCTION*-------------------------------------------------------------
*
*  Function Name  : ENET_get_stats
*  Returned Value : pointer to the statistics structure
*  Comments       :
*        Retrieves the Ethernet statistics for an initialized device.
*
*END*-----------------------------------------------------------------*/

ENET_STATS_PTR ENET_get_stats
   (
      /* [IN] the Ethernet state structure */
      _enet_handle   handle
   )
{ /* Body */

   return (ENET_STATS_PTR)&((ENET_CFG_STRUCT_PTR)handle)->STATS;

} /* Endbody */


/* EOF */

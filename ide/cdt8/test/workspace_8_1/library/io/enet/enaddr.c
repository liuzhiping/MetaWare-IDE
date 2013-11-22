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
*** File: enaddr.c
***
*** Comments:  This file contains the ENET_get_address utility
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
*  Function Name  : ENET_get_address
*  Returned Value : ENET_OK or error code
*  Comments       :
*        Retrieves the Ethernet address of an initialized device.
*
*END*-----------------------------------------------------------------*/

uint_32 ENET_get_address
   (
      /* [IN] the Ethernet state structure */
      _enet_handle   handle,

      /* [OUT] the local Ethernet address */
      _enet_address  address
   )
{ /* Body */
   ENET_CFG_STRUCT_PTR enet_ptr = (ENET_CFG_STRUCT_PTR)handle;

   ENET_memcopy((uchar _PTR_)enet_ptr->ADDRESS, address, sizeof(_enet_address));

   return ENET_OK;

} /* Endbody */


/* EOF */

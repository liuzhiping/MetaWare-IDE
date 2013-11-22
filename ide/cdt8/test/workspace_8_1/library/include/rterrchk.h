#ifndef _rterrchk_h_
#define _rterrchk_h_ 1
/*HEADER************************************************************************
********************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: rterrchk.h
***
*** Comments:
***   This file is intended to be used by applications when including run-time
***   error checking.
***
********************************************************************************
*END***************************************************************************/

/*--------------------------------------------------------------------------*/
/*
** STRUCTURE DEFINITIONS
*/
#ifdef __DCC__
/* Diab Data RTA Suite-specific */
#include <stdlib.h>
#include <rta/rtc.h>
#include <rta/rtaenv.h>  

/* Over-ride the MQX memory manager calls to go through the RTEC memory checker first */
/* Over-ride the MQX memory manager calls to go through the RTEC memory checker first */

#define _mem_alloc(size)      _rterrchk_mem_alloc(size)
#define _mem_alloc_zero(size) _rterrchk_mem_alloc_zero(size)
#define _mem_free(p)          _rterrchk_mem_free(p)

extern pointer   _rterrchk_mem_alloc(_mem_size);
extern pointer   _rterrchk_mem_alloc_zero(_mem_size);
extern _mqx_uint _rterrchk_mem_free(pointer);

#endif /* __DCC__ */

#endif
/* EOF */

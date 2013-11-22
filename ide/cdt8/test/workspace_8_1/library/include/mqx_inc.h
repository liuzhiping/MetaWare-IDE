#ifndef __mqx_inc_h__
#define __mqx_inc_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: mqx_inc.h
***
*** Comments:
***    This file contains include statements for all header files
*** required when compiling the kernel.
***
*** $Header:mqx_inc.h, 9, 2/11/2004 4:24:36 PM, $
***
*** $NoKeywords$
***************************************************************************
*END**********************************************************************/

#ifdef MQX_REDUCE_DEBUG /* CR1446 & CR1434 */
#if defined(__HIGHC__) && !defined(MQX_CRIPPLED_EVALUATION)
   /* Minimize symbolic debug info when MetaWare compiler */
#  pragma on(nodebug)
# endif
#endif

/*
** These are the 'MQX' include files
*/
#include "mqx.h"
#include "mqx_ioc.h"
#include "psp.h"
#include "psp_comp.h"
#include "mem_prv.h"
#include "mqx_prv.h"
#include "psp_prv.h"

#ifdef MQX_REDUCE_DEBUG /* CR1446 & CR1434 */
# if defined(__HIGHC__) && !defined(MQX_CRIPPLED_EVALUATION)
   /* Minimize symbolic debug info when MetaWare compiler */
#  pragma pop(nodebug)
# endif
#endif

#if MQX_KERNEL_LOGGING
#include "lwlog.h"
#include "klog.h"
#endif
#if MQX_USE_LWMEM_ALLOCATOR
#ifndef __MEMORY_MANAGER_COMPILE__
#include "lwmemprv.h"
#endif
#endif

#endif
/* EOF */

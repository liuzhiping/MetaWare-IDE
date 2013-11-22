/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: mqx.c
***
*** Comments:
***   This file contains source that forces the generation of debug
***   info that was deferred when compiling other sources by the
***   MetaWare C/C++ compiler.
***
*** $Header:mqxdebug.c, 2, 2/11/2004 4:24:58 PM, $
***
*** $NoKeywords$
**************************************************************************
*END*********************************************************************/

#ifdef MQX_REDUCE_DEBUG /* CR1446 & CR1434 */
# if defined(__HIGHC__) && !defined(MQX_CRIPPLED_EVALUATION)
   /* Minimize symbolic debug info when MetaWare compiler */
#  pragma on(g_flag)
#  pragma on(forcedebug)
# endif
#endif

#include "mqx_inc.h"

/* EOF */

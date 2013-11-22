#ifndef _rterrprv_h_
#define _rterrprv_h_ 1
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
*** File: rterrprv.h
***
*** Comments:
***   Any definitions for run-time error checking data structures, required
***   internally by MQX, are defined here.
***
********************************************************************************
*END***************************************************************************/

/*--------------------------------------------------------------------------*/
/*
** STRUCTURE DEFINITIONS
*/
typedef struct rterrchk_context {

   pointer     RTC_INFO_PTR_SAVE;
   pointer     RTC_SP_LIMIT_SAVE;
} RTERRCHK_CONTEXT_STRUCT, _PTR_ RTERRCHK_CONTEXT_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
** FUNCTION PROTOTYPES
*/

/* ANSI c prototypes */
#ifdef __cplusplus
extern "C" {
#endif

extern void _rterrchk_initialize_context(RTERRCHK_CONTEXT_STRUCT_PTR);
extern void _rterrchk_finish(RTERRCHK_CONTEXT_STRUCT_PTR);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

#ifndef __rterrprv_h__
#define __rterrprv_h__ 1
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
#ifdef __DCC__
typedef struct rterrchk_context {
   pointer     RTC_INFO_PTR_SAVE;
   pointer     RTC_SP_LIMIT_SAVE;
} RTERRCHK_CONTEXT_STRUCT, _PTR_ RTERRCHK_CONTEXT_STRUCT_PTR;
#else
typedef struct rterrchk_context {
   pointer     DUMMY;
} RTERRCHK_CONTEXT_STRUCT, _PTR_ RTERRCHK_CONTEXT_STRUCT_PTR;
#endif

/*--------------------------------------------------------------------------*/
/*
** FUNCTION PROTOTYPES
*/

/* ANSI c prototypes */
#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern void _rterrchk_finish(RTERRCHK_CONTEXT_STRUCT_PTR);
extern void _rterrchk_initialize_context(RTERRCHK_CONTEXT_STRUCT_PTR);
extern void _rterrchk_restore_context(void);
extern void _rterrchk_save_context(void);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

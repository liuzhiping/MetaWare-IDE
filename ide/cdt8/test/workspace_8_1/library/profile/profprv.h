#ifndef _profprv_h_
#define _profprv_h_ 1
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
*** File: profprv.h
***
*** Comments:
***   Any definitions for profiling data structures,
***   required internally by MQX, are defined here.
***
********************************************************************************
*END***************************************************************************/

/*--------------------------------------------------------------------------*/
/*
** STRUCTURE DEFINITIONS
*/
typedef struct profile_context {
   pointer     PROF_INFO_PTR_SAVE;
} PROFILE_CONTEXT_STRUCT, _PTR_ PROFILE_CONTEXT_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
** FUNCTION PROTOTYPES
*/
/* ANSI c prototypes */
#ifdef __cplusplus
extern "C" {
#endif

extern void _profiler_initialize_context(PROFILE_CONTEXT_STRUCT_PTR);
extern void _profiler_finish(PROFILE_CONTEXT_STRUCT_PTR);

#ifdef __cplusplus
}
#endif
 
#endif
/* EOF */

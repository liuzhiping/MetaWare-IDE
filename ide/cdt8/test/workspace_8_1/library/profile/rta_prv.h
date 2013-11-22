#ifndef _rta_prv_h_
#define _rta_prv_h_ 1
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: rta_prv.h
***
*** Comments: 
*** 
***    This include file is used define constants and data types for the
***  rta profiling code.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */


/*--------------------------------------------------------------------------*/
/*                       DATATYPE DEFINITIONS                               */
typedef struct
{
   /* Is profiling currently enabled? */
   boolean              PROFILING_ACTIVE;

   /* Difference between number of calls to __prof_user_init and __prof_user_finish */
   uint_32              NUM_STARTS;

   /* Information about interrupt we'll be chaining to */
   void     (_CODE_PTR_ OLD_ISR)(pointer);
   pointer              OLD_ISR_DATA;
} RTA_PROFILE_INFO_STRUCT, _PTR_ RTA_PROFILE_INFO_PTR;


/*--------------------------------------------------------------------------*/
/*                       PROTOTYPE DEFINITIONS                              */

#ifdef __cplusplus
extern "C" {
#endif

extern void _profiler_save_context(void);
extern void _profiler_restor_context(void);

extern void __prof_clock(void);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */


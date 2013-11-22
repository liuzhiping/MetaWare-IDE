#ifndef __lwe_prv_h__
#define __lwe_prv_h__ 1
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
*** File: lwe_prv.h
***
*** Comments: 
***    This include file is used to define constants and data types private
*** to the event component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* Used to mark a block of memory as belonging to an event group */
#define LWEVENT_VALID                  ((_mqx_uint)(0x6C65766E))   /* "levn" */

/*--------------------------------------------------------------------------*/
/*                         ANSI C PROTOTYPES                                */
#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
/* Start CR 199 */
extern _mqx_uint _lwevent_wait_internal(LWEVENT_STRUCT_PTR, _mqx_uint, boolean,
   MQX_TICK_STRUCT_PTR, boolean);
/* End CR 199 */
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

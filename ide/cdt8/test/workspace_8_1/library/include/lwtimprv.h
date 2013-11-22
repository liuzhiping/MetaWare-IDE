#ifndef __lwtimprv_h__
#define __lwtimprv_h__ 1
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
*** File: lwtimprv.h
***
*** Comments: 
***    This include file is used to define constants and data types for the
***  light weight timer component's internal use.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* The timer validity check value */
#define LWTIMER_VALID             (_mqx_uint)(0x6C777469)    /* "lwti" */


/*--------------------------------------------------------------------------*/
/*                       PROTOTYPE DEFINITIONS                              */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__

extern void _lwtimer_isr_internal(void);

#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */


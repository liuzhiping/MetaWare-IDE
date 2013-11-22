#ifndef __lwmsgqpr_h__
#define __lwmsgqpr_h__ 1
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
*** File: lwmsgqpr.h
***
*** Comments:
***   This file contains private definitions for use with
*** light weight message queues
***
***************************************************************************
*END**********************************************************************/


/*--------------------------------------------------------------------------*/
/*
**                            MACRO DEFINITIONS
*/

#define LWMSGQ_VALID        (_mqx_uint)(0x6C776D73) /* "lwms" */

#define LWMSGQ_READ_BLOCKED  (0x30 | IS_BLOCKED | TD_IS_ON_AUX_QUEUE)
#define LWMSGQ_WRITE_BLOCKED (0x32 | IS_BLOCKED | TD_IS_ON_AUX_QUEUE)

#endif
/* EOF */

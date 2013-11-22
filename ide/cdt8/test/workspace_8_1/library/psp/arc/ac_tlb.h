#ifndef __ac_tlb_h__
#define __ac_tlb_h__
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
*** File: ac_tlb.h
***
*** Comments:
***
***    This file contains the type definitions for the ARC Compact
*** TLB memory management unit.
***
***************************************************************************
*END***********************************************************************/

#define PSP_AUX_MMU_BUILD                       (0x06F)

/*
** ARC 700 MMU macros
*/
#define PSP_AUX_MMU_BUILD_GET_VERSION(x)        (((x) >> 24) & 0xFF)
#define PSP_AUX_MMU_BUILD_GET_JA(x)             (((x) >> 20) & 0x0F)
#define PSP_AUX_MMU_BUILD_GET_JE(x)             (((x) >> 16) & 0x0F)
#define PSP_AUX_MMU_BUILD_GET_ITLB(x)           (((x) >>  8) & 0xFF)
#define PSP_AUX_MMU_BUILD_GET_DTLB(x)           (((x) >>  0) & 0xFF)


#endif /* __ac_tlb_h__ */
/* EOF */

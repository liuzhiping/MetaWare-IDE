#ifndef __psp_h__
#define __psp_h__
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
*** File: psp.h
***                                                            
*** Comments: 
*** 
***    This file provides a generic header file for use by the mqx kernel
*** for including processor specific information
***
*** $Header:psp.h, 9, 2/12/2004 4:37:33 PM, $
***
*** $NoKeywords$
***************************************************************************
*END***********************************************************************/

#include <arc.h>
#include <psp_time.h>
#include <psp_math.h>

#if MQX_CPU == 0xACA5 /* ARCtangent-A5 Core */
#include <arcta5.h>
/* Start CR 818 */
#elif MQX_CPU == 0xACA52 /* ARCtangent-A5.2 Core */
#include <arcta52.h>
/* End   CR 818 */
#elif MQX_CPU == 0xACA6 /* ARC A600 Core */
#include <arca6.h>
#elif MQX_CPU == 0xACA7 /* ARC 700 Core */
#include <arca7.h>
#else
#error MQX_CPU not defined
#endif

#endif
/* EOF */

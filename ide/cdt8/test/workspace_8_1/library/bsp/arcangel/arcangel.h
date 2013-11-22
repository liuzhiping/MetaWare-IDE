#ifndef __arcangel_h__
#define __arcangel_h__ 1
/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: arcangel.h
***
*** Comments:
***   This include file is used to provide information needed by
***   an application program using the kernel running on an
***   ARC Angel III evaluation system.
***
**************************************************************************
*END*********************************************************************/

#ifdef __cplusplus
extern "C" {
#endif

/*
** Define the board type
*/
#define BSP_ARCANGEL            TRUE

#if MQX_CPU == 0xACA6 
 #define MQX_BSP "acaa600"
 #include <acaa600.h>
#elif MQX_CPU == 0xACA7 
 #define MQX_BSP "acaa700"
 #include <acaa700.h>
#endif

#ifndef MQX_BSP
    #error "MQX_BSP is not defined. Aborting"
#endif

#define BSP_SIMUART_NAME        "ttyi:"


/*----------------------------------------------------------------------
**                  HARDWARE INITIALIZATION DEFINITIONS
*/

extern uint_32 _bsp_get_system_clock(void);

/* Required for acaa3ta4 and acaa3ta422t: */
extern void _bsp_soft_int_trigger(uint_32);

#ifdef __cplusplus
}
#endif

#endif /* __arcangel_h__ */
/* EOF */

#ifndef __evmac_h__
#define __evmac_h__
/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: evmac.h
***
*** Comments:      
***   This file contains the definitions of constants and structures
***   required for the ethernet drivers for the VMAC
***
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/* 
**                        FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

extern void _bsp_enet_init (uint_32, uint_32, uint_32);
extern VMAC_REG_STRUCT_PTR _bsp_enet_getbase(uint_32);
extern uint_32 _bsp_enet_getvec(uint_32);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

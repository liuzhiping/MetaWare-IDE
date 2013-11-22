#ifndef __psp_math_h__
#define __psp_math_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: psp_math.h
***                                                            
*** Comments: 
*** 
***    This file contains the definitions for functions that provide
***  mathematics for working on 64 bit, 96 bit and 128 bit numbers.
*** (Needed by the time conversion functions)
*** The operations on the numbers are performed uing the 16-bit array
*** representation of the numbers.
***
***************************************************************************
*END***********************************************************************/


/*-----------------------------------------------------------------------*/
/*
** PSP 64 BIT UNION
**
** The representation of a 64 bit number
**
*/
typedef union psp_64_bit_union
{
   uint_64  LLW;
   uint_32  LW[2];
   uint_16  W[4];
   uint_8   B[8];
} PSP_64_BIT_UNION, _PTR_ PSP_64_BIT_UNION_PTR;

/*-----------------------------------------------------------------------*/
/*
** PSP 96 BIT UNION
**
** The representation of a 96 bit number
**
*/
typedef union psp_96_bit_union
{
   uint_32         LW[3];
   uint_16         W[6];
   uint_8          B[12];
   PSP_TICK_STRUCT TICKS;
} PSP_96_BIT_UNION, _PTR_ PSP_96_BIT_UNION_PTR;

/*-----------------------------------------------------------------------*/
/*
** PSP 128 BIT UNION
**
** The representation of a 128 bit number
**
*/
typedef union psp_128_bit_union
{
   uint_64  LLW[2];
   uint_32  LW[4];
   uint_16  W[8];
   uint_8   B[16];
} PSP_128_BIT_UNION, _PTR_ PSP_128_BIT_UNION_PTR;


/*--------------------------------------------------------------------------*/
/*
**                  PROTOTYPES OF PSP FUNCTIONS
*/

#ifdef __cplusplus
extern "C" {
#endif

/* START CR 2364 */
extern uint_32 _psp_add_element_to_array(PSP_128_BIT_UNION_PTR, uint_32, uint_32, PSP_128_BIT_UNION_PTR);
/* END CR 2364 */

extern uint_32 _psp_div_128_by_32(PSP_128_BIT_UNION_PTR, uint_32, PSP_128_BIT_UNION_PTR);
extern uint_32 _psp_mul_128_by_32(PSP_128_BIT_UNION_PTR, uint_32, PSP_128_BIT_UNION_PTR);

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

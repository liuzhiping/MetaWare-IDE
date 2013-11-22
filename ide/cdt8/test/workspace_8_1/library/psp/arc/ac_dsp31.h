#ifndef __ac_dsp31_h__
#define __ac_dsp31_h__
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
*** File: ac_dsp31.h
***                                                            
*** Comments: 
*** 
***    This file contains the type definitions for the ARC
*** DSP version 3.1 extensions.
***
***************************************************************************
*END***********************************************************************/

#if (defined(__Xxmac_24) || defined(__Xxmac_d16))
#define DSP_XMAC_PRESENT
#endif

/* DSP Extensions */
#if (defined(DSP_XMAC_PRESENT) || defined(__Xcrc) || defined(__Xxy))
#define PSP_DSP_PRESENT
#endif

/* 
** The accumulators, acc1 and acc2, are shared between a number of
** DSP extensions.
*/
#if (defined(DSP_XMAC_PRESENT) || defined(__Xxy))
#define DSP_ACCS_PRESENT
#endif


/*
** The following defines can be used to define how a task uses a particular
** DSP extension. These defines are added to the task's attribute field
** in the task template structure. They can be used in any combination
** as long as the hardware supports it
** They are ARCtangent specific.
*/
#define DSP_USES_XY                    (0x00010000)

/*
** The following erroneous defines were originally documented in version
** 2.50.14.01.  They refer to the X/Y memory extension core registers
** which are not "banks" but pointers into a bank.  The DSP-C compiler
** is free to use all of the X/Y memory extension core registers. Thus, 
** if you use X/Y then MQX must save them appropriately.
**
** NOTE:  The ARCTangent 5 ABI defines MX0,MX1,MY0,MY1 as "volatile". 
**        They are presumed destroyed by a call.  Whereas, MX2,MY2,MX3,MY3
**        are non-volatile and are presumed preserved by a call.  MQX must
**        save all of them during an ISR but only has to preserve the
**        non-volatile ones when called outside an ISR.
*/
#define DSP_USES_XY_BANK_0             DSP_USES_XY
#define DSP_USES_XY_BANK_1             DSP_USES_XY
#define DSP_USES_XY_BANK_2             DSP_USES_XY
#define DSP_USES_XY_BANK_3             DSP_USES_XY

/*
** The DSP-C compiler now uses the XMAC unit and the accumulator and
** aux_macmode registers for normal integer multiplication.
*/
#define DSP_USES_XMAC                  (0)
#define DSP_USES_CRC                   (0x00020000)
#define DSP_USES_VBFDW                 (0x00040000)

#define DSP_USES_ALL 	(DSP_USES_XY | DSP_USES_CRC | DSP_USES_VBFDW)


/*
** Would you like the LSBASEX & LSBASEY saved during context switches
** for tasks that are define DSP_USES_XY?  Typically the answer is no.
** These base registers are generaly defined once and remain constant
** thereafter throughout the whole program.
*/
#ifndef DSP_SAVE_XY_LSBASE_REGISTERS
#define DSP_SAVE_XY_LSBASE_REGISTERS	(0)
#endif

/*
** Would you like XYCONFIG saved during context switches for tasks that
** are define DSP_USES_XY?  Typically the answer is no.
*/
#ifndef DSP_SAVE_XYCONFIG
#define DSP_SAVE_XYCONFIG		(0)
#endif

/*
** Would you like the X/Y memory burst control registers saved during
** context switches for tasks that are define DSP_USES_XY?  Typically
** the answer is yes.
*/
#ifndef DSP_SAVE_XY_BURST_REGISTERS
#define DSP_SAVE_XY_BURST_REGISTERS	(1)
#endif


/*-----------------------------------------------------------------------*/
/*
** PSP DSP EXTENSION REGISTERS
** This defines the DSP extensions v3.1
** 
*/
#ifdef PSP_DSP_PRESENT
typedef struct psp_dsp_ext_registers_struct
{
#ifdef DSP_XMAC_PRESENT
   uint_32 AUX_FBF_STORE_16;
#endif

#ifdef __Xcrc
   uint_32 AUX_CRC_POLY;
   uint_32 AUX_CRC_MODE;
#endif

#ifdef __Xxy
# if DSP_SAVE_XYCONFIG
   uint_32 AUX_XY_CFG;     
# endif
# if DSP_SAVE_XY_BURST_REGISTERS
   uint_32 AUX_XY_BURSTSYS;
   uint_32 AUX_XY_BURSTXYM;
   uint_32 AUX_XY_BURSTSZ;
   uint_32 AUX_XY_BURSTVAL;
# endif
# if DSP_SAVE_XY_LSBASE_REGISTERS
   uint_32 AUX_XY_LSBASEX; 
   uint_32 AUX_XY_LSBASEY; 
# endif

   uint_32 AUX_XY_AX0;     
   uint_32 AUX_XY_AY0;     
   uint_32 AUX_XY_MX00;    
   uint_32 AUX_XY_MX01;    
   uint_32 AUX_XY_MY00;    
   uint_32 AUX_XY_MY01;    

   uint_32 AUX_XY_AX1;     
   uint_32 AUX_XY_AY1;     
   uint_32 AUX_XY_MX10;    
   uint_32 AUX_XY_MX11;    
   uint_32 AUX_XY_MY10;    
   uint_32 AUX_XY_MY11;    

   uint_32 AUX_XY_AX2;     
   uint_32 AUX_XY_AY2;     
   uint_32 AUX_XY_MX20;    
   uint_32 AUX_XY_MX21;    
   uint_32 AUX_XY_MY20;    
   uint_32 AUX_XY_MY21;    

   uint_32 AUX_XY_AX3;     
   uint_32 AUX_XY_AY3;     
   uint_32 AUX_XY_MX30;    
   uint_32 AUX_XY_MX31;    
   uint_32 AUX_XY_MY30;    
   uint_32 AUX_XY_MY31;    

#ifdef __Xvbfdw
   uint_32 AUX_VBFDW_MODE;
   uint_32 AUX_VBFDW_BM0;
   uint_32 AUX_VBFDW_BM1;
   uint_32 AUX_VBFDW_ACCU;
   uint_32 AUX_VBFDW_OFST;
   uint_32 AUX_VBFDW_INTSTAT;
#endif
#endif

#if MQX_CHECK_ERRORS
   _task_id TID;
#endif
} PSP_DSP_EXT_REGISTERS_STRUCT, _PTR_ PSP_DSP_EXT_REGISTERS_STRUCT_PTR;
#endif

/*--------------------------------------------------------------------------*/
/*
**                  PROTOTYPES OF PSP FUNCTIONS
*/
#ifdef __cplusplus
extern "C" {
#endif

#ifdef PSP_DSP_PRESENT
extern pointer   _psp_get_dsp_context(pointer);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

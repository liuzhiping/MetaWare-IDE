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
*** File: bsp_ctx.c
***
*** Comments:      
***   This file is used to generate register save offsets needed
***   for context save/restore functions.  When the MetaWare compiler
***   compiles this file it will generate the appropriate offset
***   info from it's knowlege of the C structure definitions.  This
***   way the compiler and the assembly will be kept in sync.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "psp.h"

/*
 * The following pragma causes the MetaWare compiler to create
 * the file pspcont.s which will contain assembly directives
 * that describe the offsets where registers are supposed to
 * be saved and restored.
 */
#ifdef PSP_DSP_PRESENT
#pragma asm_field_offsets("pspcont.s", "%m_LOC", \
	PSP_DSP_EXT_REGISTERS_STRUCT, \
        PSP_BLOCKED_STACK_STRUCT)
#else
#pragma asm_field_offsets("pspcont.s", "%m_LOC", \
        PSP_BLOCKED_STACK_STRUCT)
#endif

/*
 * We want one external declaration for ANSI conformance.
 * It is NOT necessary to link this var into your application.
 */
char __sadljhfaskjhsdakhgfdsakgsdfl;

#ifndef __ac_bvci_h__
#define __ac_bvci_h__
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
*** File: ac_bvci.h
***                                                            
*** Comments: 
*** 
***    This file contains the type definitions for the ARC Compact
*** BVCI memory controller.
***
***************************************************************************
*END***********************************************************************/

/*
** Chip select struct for ARC Compact BVCI memory controller
*/

typedef volatile _Uncached struct ac_bvci_cs_struct
{
   /* Read configuration register   */
   uint_32 READ_CFG;
   /* Write configuration register  */
   uint_32 WRITE_CFG;
   /* Select configuration register */
   uint_32 SELECT;
   /* Not used - Reserved           */
   uint_32 RESERVED;
} AC_BVCI_CS_STRUCT, _PTR_ AC_BVCI_CS_STRUCT_PTR;


/*
** Structure for ARC Compact BVCI memory controller
*/

typedef volatile _Uncached struct ac_bvci_mem_controller_struct
{
   /* Identification register */
   uint_32             ID;
   /* Chip select registers   */
   AC_BVCI_CS_STRUCT   CS[AC_BVCI_CS_NUM];

} AC_BVCI_MEM_CONTROLLER_STRUCT, _PTR_ AC_BVCI_MEM_CONTROLLER_STRUCT_PTR;

#endif /* __ac_bvci_h__ */
/* EOF */

#ifndef _istrata_h_
#define _istrata_h_
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
*** File: istrata.h
*** 
*** Comments: The file contains functions prototype, defines, structure 
***           definitions specific for the Intel strataflash devices
***
***
**************************************************************************
*END*********************************************************************/


/*----------------------------------------------------------------------*/
/*
**                     FUNCTION PROTOTYPES
*/
#ifdef __cplusplus
extern "C" {
#endif

/* Top level functions visible to the generic flashx driver */
extern boolean _intel_strata_program(IO_FLASHX_STRUCT_PTR, uchar_ptr, uchar_ptr, _mem_size);

/* Top level functions visible to the generic flashx driver */
extern boolean _intel_strata_erase(IO_FLASHX_STRUCT_PTR, uchar_ptr, _mem_size);

/* Start CR 871 */
extern boolean _intel_strata_clear_lock_bits(IO_FLASHX_STRUCT_PTR);
/* End CR 871 */
extern boolean _intel_strata_set_lock_bits(IO_FLASHX_STRUCT_PTR);

#ifdef __cplusplus
}
#endif

#endif

/* EOF */

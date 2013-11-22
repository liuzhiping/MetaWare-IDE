#ifndef __mmu_h__
#define __mmu_h__
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
*** File: mmu.h
***                                                            
*** Comments: 
*** 
***    This file contains the type definitions for the generic MMU interface
*** functions.
***
***************************************************************************
*END***********************************************************************/

/* When a region is added to the mmu, the default is to make this region 
** cachable, copyback, writeable, for both code and data accesses
*/

   /* Write accesses to this page are not allowed. */
#define PSP_MMU_WRITE_PROTECTED         (0x00000001)

   /* This page is NOT to be cached in the code cache */
#define PSP_MMU_CODE_CACHE_INHIBITED    (0x00000002)

   /* This page is NOT to be cached in the data cache */
#define PSP_MMU_DATA_CACHE_INHIBITED    (0x00000004)

   /* This page is NOT to be cached in the code or data cache */
#define PSP_MMU_CACHE_INHIBITED         (0x00000006)

   /* Write accesses immediately propagate to physical memory. */
#define PSP_MMU_WRITE_THROUGH           (0x00000008)

   /* On some MMUs, a write can be made without updating the cache. */
#define PSP_MMU_WRITE_NO_UPDATE         (0x00000020)

   /* On some MMUs, a write can be stored into a write buffer, and 
   ** stores to memory performed at some future time. 
   */
#define PSP_MMU_WRITE_BUFFERED          (0x00000040)

   /* This page is shared with an external hardware device that can snoop. */
#define PSP_MMU_COHERENT                (0x00000080)

   /* This page requires that memory accesses are not out-of-order, 
   ** or that memory not specifically requested by the software be accessed. 
   */
#define PSP_MMU_GUARDED                 (0x00000100)

/* Start CR 973 */
   /* Read accesses to this page are not allowed. */
#define PSP_MMU_READ_PROTECTED          (0x00000200)

   /* Page can contain executable instructions. */
#define PSP_MMU_EXEC_ALLOWED            (0x00000400)

   /* Page is locked. Note: Cannot be used for a virtual context */
#define PSP_MMU_PAGE_LOCKED             (0x00000800)

/* End CR 973 */

/*--------------------------------------------------------------------------*/
/*
** PSP MMU VINIT STRUCT
**
** This structure is used to initialize the virtual memory support
*/
typedef struct psp_mmu_vinit_struct
{
   /* Where MMU pages can exist */
   uchar_ptr MMU_PAGE_TABLE_BASE;

   /* How much memory allocated for the tables */
   uint_32   MMU_PAGE_TABLE_SIZE;

   /* Where does unmapped memory start */
   uchar_ptr UNMAPPED_MEMORY_BASE;

   /* How much memory available */
   uint_32   UNMAPPED_MEMORY_SIZE;

} PSP_MMU_VINIT_STRUCT, _PTR_ PSP_MMU_VINIT_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                  FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
/*
** Start CR 396
** Update function prototypes to MQX2.50 interface
*/
extern _mqx_uint _mmu_add_region(uchar_ptr, _mem_size, _mqx_uint);
extern void      _mmu_disable(void);
extern void      _mmu_enable(void);
extern void      _mmu_init(pointer);

extern _mqx_uint _mmu_add_vregion(pointer, pointer, _mem_size, _mqx_uint);
extern _mem_size _mmu_get_vpage_size(void);
extern _mqx_uint _mmu_get_vmem_attributes(pointer, pointer _PTR_,
   pointer _PTR_, _mem_size_ptr, _mqx_uint_ptr);
extern _mqx_uint _mmu_set_vmem_attributes(pointer, _mqx_uint, _mem_size);
extern _mqx_uint _mmu_vinit(_mqx_uint, pointer);
extern void      _mmu_venable(void);
extern void      _mmu_vdisable(void);
extern _mqx_uint _mmu_vtop(pointer, pointer _PTR_);

extern _mqx_uint _mmu_add_vcontext(_task_id, pointer, _mem_size, _mqx_uint);
extern _mqx_uint _mmu_create_vcontext(_task_id);
extern _task_id  _mmu_create_vtask(_mqx_uint, _mqx_uint, pointer, pointer, 
   _mem_size, _mqx_uint);
extern _mqx_uint _mmu_destroy_vcontext(_task_id);
/* End CR */
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

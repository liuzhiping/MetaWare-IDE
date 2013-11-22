#ifndef __lwmemprv_h__
#define __lwmemprv_h__
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
*** file: lwmemprv.h
***
*** Comments:
***   This file contains definitions private to the light weight
*** memory manger.
***
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                    CONSTANT DEFINITIONS
*/

/* The correct value for the light weight memory pool VALID field */
#define LWMEM_POOL_VALID   (_mqx_uint)(0x6C6D6570)    /* "lmep" */

/* The smallest amount of memory that is allocated */
#define LWMEM_MIN_MEMORY_STORAGE_SIZE \
   ((_mem_size)(sizeof(LWMEM_BLOCK_STRUCT) + PSP_MEMORY_ALIGNMENT) & \
   PSP_MEMORY_ALIGNMENT_MASK)
   

/*--------------------------------------------------------------------------*/
/*
**                      MACROS DEFINITIONS
*/

/*
** get the location of the block pointer, given the address as provided
** to the application by _lwmem_alloc.
*/
#define GET_LWMEMBLOCK_PTR(addr) \
   (LWMEM_BLOCK_STRUCT_PTR)((pointer)((uchar_ptr)(addr) - \
      sizeof(LWMEM_BLOCK_STRUCT)))

/*--------------------------------------------------------------------------*/
/*
**                    DATATYPE DECLARATIONS
*/

/*
** LWMEM BLOCK STRUCT
**
** This structure is used to define the storage blocks used by the
** memory manager in MQX.
*/
typedef struct lwmem_block_struct
{
   /* The size of the block. */
   _mem_size      BLOCKSIZE;
   
   /* The pool the block came from */
   _lwmem_pool_id POOL;

   /*
   ** For an allocated block, this is the task ID of the owning task.
   ** When on the free list, this points to the next block on the free list.
   */
   union {
      pointer     NEXTBLOCK;
      _task_id    TASK_ID;
   } U;

} LWMEM_BLOCK_STRUCT, _PTR_ LWMEM_BLOCK_STRUCT_PTR; 

/*--------------------------------------------------------------------------*/
/*
**                  PROTOTYPES OF FUNCTIONS
*/

#ifdef __cplusplus
extern "C" {
#endif
#ifndef __TAD_COMPILE__

extern pointer   _lwmem_alloc_internal(_mem_size, TD_STRUCT_PTR, _lwmem_pool_id);
extern void      _lwmem_cleanup_internal(TD_STRUCT_PTR);
extern void      _lwmem_transfer_internal(pointer, TD_STRUCT_PTR);
extern _mqx_uint _lwmem_transfer_td_internal(pointer, TD_STRUCT_PTR,
   TD_STRUCT_PTR);
extern _mqx_uint _lwmem_init_internal(void);
extern pointer   _lwmem_get_next_block_internal(TD_STRUCT_PTR,pointer);

#endif
#ifdef __cplusplus
}
#endif

#endif
/* EOF */

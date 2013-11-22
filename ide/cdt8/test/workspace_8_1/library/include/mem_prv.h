#ifndef __mem_prv_h__
#define __mem_prv_h__
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
*** file: mem_prv.h
***
*** Comments:
***   This file contains definitions private to the memory manger.
***
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                    CONSTANT DEFINITIONS
*/

#define MEMPOOL_VALID                (_mqx_uint)(0x6D656D70)     /* "memp" */

/* The smallest amount of memory that is allocated */
#define MQX_MIN_MEMORY_STORAGE_SIZE \
   ((_mem_size)(sizeof(STOREBLOCK_STRUCT) + PSP_MEMORY_ALIGNMENT) & \
   PSP_MEMORY_ALIGNMENT_MASK)
   

/*--------------------------------------------------------------------------*/
/*
**                      MACROS DEFINITIONS
**
** The following macros are defined to calculate block locations and checksums
*/

/*
** get the location of the block pointer, given the address as provided
** to the application by _mem_alloc.
*/
#define GET_MEMBLOCK_PTR(addr) \
   (STOREBLOCK_STRUCT_PTR)((pointer)(((uchar_ptr)(addr) - \
      FIELD_OFFSET(STOREBLOCK_STRUCT,USER_AREA))))

/* 
** Calculate the next physical block (in higher memory) 
** The next physical block is adjacent to the provided block
*/
#define NEXT_PHYS(block) \
   ((STOREBLOCK_STRUCT_PTR)((char _PTR_)block + \
   ((STOREBLOCK_STRUCT_PTR)(block))->BLOCKSIZE))

/* 
** Calculate the previous physical block (in lower memory) 
** The previous physical block is adjacent to the provided block
*/
#define PREV_PHYS(block) \
   ((STOREBLOCK_STRUCT_PTR)(block))->PREVBLOCK


/* 
** Calculate the next block on the free list.
** (assuming the current block is on the free list)
*/
#define NEXT_FREE(block) \
   ((STOREBLOCK_STRUCT_PTR)(block))->NEXTBLOCK

/* 
** Calculate the previous free block on the free list.
** (assuming the current block is on the free list)
*/
#define PREV_FREE(block) \
   ((STOREBLOCK_STRUCT_PTR)(block))->USER_AREA


/* Calculate the checksum for the current block */
#if MQX_CHECK_VALIDITY
#define CALC_CHECKSUM(block) \
   ((STOREBLOCK_STRUCT_PTR)(block))->CHECKSUM = \
   (_mqx_uint)(((STOREBLOCK_STRUCT_PTR)(block))->BLOCKSIZE) + \
   (_mqx_uint)(((STOREBLOCK_STRUCT_PTR)(block))->PREVBLOCK) + \
   (_mqx_uint)(PROC_NUMBER_FROM_TASKID(((STOREBLOCK_STRUCT_PTR)(block))->TASK_ID)) + \
   (_mqx_uint)(TASK_NUMBER_FROM_TASKID(((STOREBLOCK_STRUCT_PTR)(block))->TASK_ID));
#else
#define CALC_CHECKSUM(block)
#endif

/* Validate the checksum of the current block */
#if MQX_CHECK_VALIDITY
#define VALID_CHECKSUM(block) \
   ( ((STOREBLOCK_STRUCT_PTR)(block))->CHECKSUM == \
   ((_mqx_uint)(((STOREBLOCK_STRUCT_PTR)(block))->BLOCKSIZE) + \
   (_mqx_uint)(((STOREBLOCK_STRUCT_PTR)(block))->PREVBLOCK) + \
   (_mqx_uint)(PROC_NUMBER_FROM_TASKID(((STOREBLOCK_STRUCT_PTR)(block))->TASK_ID)) + \
   (_mqx_uint)(TASK_NUMBER_FROM_TASKID(((STOREBLOCK_STRUCT_PTR)(block))->TASK_ID))) )
#else
#define VALID_CHECKSUM(block) (TRUE)
#endif

#define MARK_BLOCK_AS_USED(block, tid) \
   ( ((STOREBLOCK_STRUCT_PTR)(block))->TASK_ID = (_task_id)(tid) )

#define MARK_BLOCK_AS_FREE(block) \
   ( ((STOREBLOCK_STRUCT_PTR)(block))->TASK_ID = (_task_id)0)

#define BLOCK_IS_FREE(block) \
   ( ((STOREBLOCK_STRUCT_PTR)(block))->TASK_ID == (_task_id)0 \
   ? TRUE : FALSE )

#define BLOCK_IS_USED(block) \
   ( ((STOREBLOCK_STRUCT_PTR)(block))->TASK_ID == (_task_id)0 \
   ? FALSE : TRUE )

/*--------------------------------------------------------------------------*/
/*
**                    DATATYPE DECLARATIONS
*/


/*
** The memory management component of MQX maintains pools of memory blocks 
** of variable size.
** These pools contains contiguous neighbouring blocks that may be
** in use (task_id is non-zero) or on the free list (task_id is zero)
**
** To get to the right physical neighbour, the size field can be
** added to the current block's address.
** To get to the left physical neighbour, the PREVBLOCK field
** points to that block.
**
** For an allocated block, NEXTBLOCK points to the next block
** belonging to the TASK who owns this block.
**
** On the free list, the NEXTBLOCK field points to the next
** block on the free list.
** The USERAREA field points to the previous block on the free
** list.
*/

/*
** STOREBLOCK STRUCT
**
** This structure is used to define the storage blocks used by the
** memory manager in MQX.
*/
typedef struct storeblock_struct
{

   /* 
   ** The task ID of the owner of this block of memory when in use. Zero when
   ** block is free. If the block belongs to the system then it will have the
   ** system task's ID.
   */
   _task_id                       TASK_ID;

   /* The size of the block. */
   _mem_size                      BLOCKSIZE;

   /* The memory pool the block was allocated from */
   pointer                        MEM_POOL_PTR;

   /*
   ** For an allocated block, this points to the next block
   ** on the allocation list.  It points to the USER_AREA in the next block.
   ** When on the free list, this points to the next block
   ** on the free list (start of the block)
   */
   pointer                        NEXTBLOCK;

   /* The address of the physical block previous to this block. */
   struct storeblock_struct _PTR_ PREVBLOCK;

   /*
   ** A checksum of the header: a simple sum of the following:
   **   BLOCKSIZE, PREVBLOCK and TASK_ID
   */
   _mqx_uint                      CHECKSUM;

#ifdef PSP_MEM_STOREBLOCK_ALIGNMENT
   /* This makes sure that the USER_AREA field is properly aligned */
   _mqx_uint                      RESERVED[PSP_MEM_STOREBLOCK_ALIGNMENT];
#else
   /* Assume a 32 byte alignment */
   _mqx_uint                      RESERVED[2];
#endif

   /*
   ** The field whose address is provided to the user when the block
   ** is allocated to him.  It is also used by the memory manager
   ** to maintain a reverse link for a block that is on the free list.
   */
   pointer                        USER_AREA;

} STOREBLOCK_STRUCT, _PTR_ STOREBLOCK_STRUCT_PTR; 

/* 
** MEMORY COMPONENT STRUCT
**
**    This structure is used to store information 
** required for the memory component
*/
typedef struct memory_component_struct
{
   /* A queue of all created memory pools */
   QUEUE_STRUCT  POOLS;

   /* A semaphore to protect the creatation of pools */
   LWSEM_STRUCT  SEM;

   /* A validation field for memory pools */
   _mqx_uint     VALID;

} MEMORY_COMPONENT_STRUCT, _PTR_ MEMORY_COMPONENT_STRUCT_PTR;


/*
** Context information for pool extensions. This info. is kept on the top
** of the block of memory to be made into an extension
*/
typedef struct mempool_extension_struct
{
   /* Used to link extensions together in a memory pool */   
   QUEUE_ELEMENT_STRUCT   LINK;

   /* The start address of the extension supplied. */
   pointer                REAL_START;

   /* The start of the memory blocks contained in this particular extension */
   pointer                START;

   /* The size of this particular extension */
   _mem_size              SIZE;

} MEMPOOL_EXTENSION_STRUCT, _PTR_ MEMPOOL_EXTENSION_STRUCT_PTR;

/*
** Context information for a memory pool
*/
typedef struct mempool_struct
{
   /* Used to link memory pools together */   
   QUEUE_ELEMENT_STRUCT   LINK;

   /* Used to verify if handle is valid */
   _mqx_uint              VALID;

   /* The size of the memory pool in bytes */
   _mem_size              POOL_SIZE;

   /* The following are used to check for possible errors */
   _mem_size              POOL_CHECK_POOL_SIZE;
   char _PTR_             POOL_CHECK_POOL_PTR;
   STOREBLOCK_STRUCT_PTR  POOL_CHECK_POOL_END_PTR;

   /* The address of the start of the Memory Pool */
   pointer                POOL_PTR;

   /* The address of the start of Memory Pool blocks. */
   STOREBLOCK_STRUCT_PTR  POOL_ALLOC_START_PTR;

   /* The physical end of the memory pool */
   pointer                POOL_LIMIT;

   /* The highest memory location allocated from kernel memory */
   pointer                POOL_HIGHEST_MEMORY_USED;

   /* 
   ** Used in mem_alloc_x when marching down the free list.
   ** This allows for higher priority tasks to bump a lower one.
   */
   STOREBLOCK_STRUCT_PTR  POOL_ALLOC_CURRENT_BLOCK;

   /* Used by _mem_test_at when checking the physical blocks in the pool */
   volatile STOREBLOCK_STRUCT _PTR_  POOL_PHYSICAL_CHECK_BLOCK;

   /* Used by _mem_test_at when checking the free list in the pool */
   STOREBLOCK_STRUCT_PTR  POOL_FREE_CHECK_BLOCK;

   /* 
   ** Variable used by _mem_free_at when traversing the free list
   ** so as to allow higher priority tasks to run.
   */
   STOREBLOCK_STRUCT_PTR  POOL_FREE_CURRENT_BLOCK;

   /* The memory block that caused the error */
   STOREBLOCK_STRUCT_PTR  POOL_BLOCK_IN_ERROR;

   /* The address of the head of the memory pool free list */
   STOREBLOCK_STRUCT_PTR  POOL_FREE_LIST_PTR;

   /* The address of the last memory pool entry */
   STOREBLOCK_STRUCT_PTR  POOL_END_PTR;

   /* A queue of all extensions to memory pool */
   QUEUE_STRUCT           EXT_LIST;

} MEMPOOL_STRUCT, _PTR_ MEMPOOL_STRUCT_PTR;


#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern boolean   _mem_check_coalesce_internal(STOREBLOCK_STRUCT_PTR);
extern _mqx_uint _mem_create_pool_internal(pointer, pointer, MEMPOOL_STRUCT_PTR);
extern _mqx_uint _mem_extend_pool_internal(pointer, _mem_size, MEMPOOL_STRUCT_PTR);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

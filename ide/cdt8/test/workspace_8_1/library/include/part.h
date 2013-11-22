#ifndef __part_h__
#define __part_h__
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
*** File: part.h
***
*** Comments:
***   This file contains public definitions for the memory partition
*** component
***
***************************************************************************
*END**********************************************************************/

/* Partition error return value */
#define PARTITION_NULL_ID       ((_partition_id)0)

/* Partition error codes */

#define PARTITION_INVALID                 (PART_ERROR_BASE|0x01)
#define PARTITION_OUT_OF_BLOCKS           (PART_ERROR_BASE|0x02)
#define PARTITION_BLOCK_INVALID_ALIGNMENT (PART_ERROR_BASE|0x03)
#define PARTITION_TOO_SMALL               (PART_ERROR_BASE|0x04)
#define PARTITION_INVALID_TASK_ID         (PART_ERROR_BASE|0x05)
#define PARTITION_BLOCK_INVALID_CHECKSUM  (PART_ERROR_BASE|0x06)
#define PARTITION_INVALID_TYPE            (PART_ERROR_BASE|0x07)
#define PARTITION_ALL_BLOCKS_NOT_FREE     (PART_ERROR_BASE|0x08)

/* The definition of a partition id */
typedef pointer _partition_id;

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern pointer       _partition_alloc(_partition_id);
extern pointer       _partition_alloc_system(_partition_id);
extern pointer       _partition_alloc_system_zero(_partition_id);
extern pointer       _partition_alloc_zero(_partition_id);
extern _mem_size     _partition_calculate_size(_mqx_uint, _mem_size);
extern _mqx_uint     _partition_calculate_blocks(_mem_size, _mem_size);
extern _mqx_uint     _partition_create_component(void);
extern _partition_id _partition_create(_mem_size, _mqx_uint, _mqx_uint, _mqx_uint);
extern _partition_id _partition_create_at(pointer, _mem_size, _mem_size);
extern _mqx_uint     _partition_destroy(_partition_id);
extern _mqx_uint     _partition_extend(_partition_id, pointer, _mem_size);
extern _mqx_uint     _partition_free(pointer);
extern _mqx_uint     _partition_get_free_blocks(_partition_id);
extern _mqx_uint     _partition_get_max_used_blocks(_partition_id);
extern _mqx_uint     _partition_get_total_blocks(_partition_id);
extern _mem_size     _partition_get_block_size(_partition_id);
extern _mem_size     _partition_get_total_size(_partition_id);
extern _mqx_uint     _partition_test(_partition_id _PTR_, pointer _PTR_, 
   pointer _PTR_);
extern _mqx_uint     _partition_transfer(pointer, _task_id);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

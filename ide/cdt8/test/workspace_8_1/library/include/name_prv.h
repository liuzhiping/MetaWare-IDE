#ifndef __name_prv_h__
#define __name_prv_h__ 1
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
*** File: name_prv.h
***
*** Comments: 
*** 
***    This include file is used to define constants and data types for the
***  name component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* Queue FIFO */
#define NAME_TASK_QUEUE_POLICY  (0)

#define NAME_VALID              (_mqx_uint)(0x6e616d65)   /* "name" */

/*--------------------------------------------------------------------------*/
/*                      DATA STRUCTURE DEFINITIONS                          */

/* 
** NAME STRUCT
** an individual pair of name/number
*/
typedef struct name_struct
{

   /* The number associated with the name */
   _mqx_max_type NUMBER;

   /* The string name stored in the table, includes null */
   char      NAME[NAME_MAX_NAME_SIZE];

} NAME_STRUCT, _PTR_ NAME_STRUCT_PTR;


/* 
** NAME COMPONENT STRUCT
**
**    This structure is used to store information 
** required for name retrieval.  An initial structure is created,
** and when pool growth is required, duplicate copies of this structure
** are created and linked via the NEXT_TABLE field.
*/
typedef struct name_component_struct
{

   /* The maximum number of names allowed in the entire pool */
   _mqx_uint      MAX_NUMBER;

   /* The total number of names allocated in the entire pool so far */
   _mqx_uint      TOTAL_NUMBER;

   /* The number of names allowed in this block */   
   _mqx_uint      NUMBER_IN_BLOCK;

   /* The number of names allowed to be created in the next block */
   _mqx_uint      GROW_NUMBER;

   /* The number of names used in the name component */
   _mqx_uint      NUMBER;

   /* Light weight semaphore for protecting the name component */
   LWSEM_STRUCT   SEM;

   /* A validation stamp to verify handle correctness */
   _mqx_uint      VALID;

   /* The address of the next block of names */
   struct  name_component_struct _PTR_ NEXT_TABLE;

   /* A variable sized array of name/number pairs */
   NAME_STRUCT    NAMES[1];
   
} NAME_COMPONENT_STRUCT, _PTR_ NAME_COMPONENT_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint _name_add_internal(pointer, char_ptr, _mqx_max_type);
extern _mqx_uint _name_add_internal_by_index(pointer, char_ptr, _mqx_max_type, 
   _mqx_uint);
extern _mqx_uint _name_create_handle_internal(pointer _PTR_, _mqx_uint, _mqx_uint, 
   _mqx_uint, _mqx_uint);
extern _mqx_uint _name_destroy_handle_internal(pointer);
extern _mqx_uint _name_delete_internal(pointer, char_ptr);
extern _mqx_uint _name_delete_internal_by_index(pointer, _mqx_uint);
extern _mqx_uint _name_find_internal(pointer, char_ptr, _mqx_max_type_ptr);
extern _mqx_uint _name_find_name_internal(pointer, _mqx_max_type, char_ptr);
extern _mqx_uint _name_find_internal_by_index(pointer, _mqx_uint, _mqx_max_type_ptr);
extern _mqx_uint _name_init_internal(pointer _PTR_, _mqx_uint, _mqx_uint, _mqx_uint,
   _mqx_uint);
extern _mqx_uint _name_test_internal(NAME_COMPONENT_STRUCT_PTR, pointer _PTR_, 
   pointer _PTR_);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

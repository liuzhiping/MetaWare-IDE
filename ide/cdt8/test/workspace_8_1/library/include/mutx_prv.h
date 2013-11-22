#ifndef __mutx_prv_h__
#define __mutx_prv_h__ 1
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
*** File:  mutx_prv.h
***
*** Comments: 
***    This include file is used to define constants and data types
***  private to the mutex component.
*** 
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/* 
**                      CONSTANT DECLARATIONS
*/


/*
**
** MUTEX COMPONENT STRUCT
**
** This structure defines the mutex component data structure.
*/
typedef struct mutex_component_struct 
{

   /* A queue of all created mutexes */
   QUEUE_STRUCT MUTEXES;
      
   /* A validation field for mutexes */
   _mqx_uint     VALID;
     
} MUTEX_COMPONENT_STRUCT, _PTR_ MUTEX_COMPONENT_STRUCT_PTR;


#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern void _mutex_cleanup(TD_STRUCT_PTR);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: met.c
***
*** Comments:      
***   This file contains the source functions for runtime support
*** for the Metaware Compiler.
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "bsp.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : main
* Returned Value   : error code
* Comments         :
*   This function is called to start up MQX.
*
*END*----------------------------------------------------------------------*/

int main
   (
      void
   )
{ /* Body */
#ifdef MQX_NO_C_COMPILER_RUNTIME  
   extern MQX_INITIALIZATION_STRUCT MQX_init_struct;

   return _mqx( &MQX_init_struct );
#else
   return(0);
#endif

} /* Endbody */

// Override MetaWare errno function
_mqx_int_ptr ___errno() {return (pointer)_task_get_error_ptr();}

/*
** It has been documented that when using the MetaWare profiler
** you have to specify -Hheap=bigvalue to allocate enough heap
** for PC sampling data.  Therefore, we want to use the MetaWare
** malloc when profiling.  Else, it's very difficult to know for
** certain which allocator will perform best for the customer
** application, though the following will save code size.
*/
/* Start CR 2270 */
//#if !defined(__PROFILE__) && defined(MQX_OVERRIDE_MALLOC)
#if !defined(__PROFILE__)
/* End CR 2270 */
/* Override malloc/free and new/delete allocator */
/* START - CR#1711 */
pointer calloc(_mem_size n, _mem_size z) { return _mem_alloc_system_zero(n*z); }
pointer malloc(_mem_size bytes) {return _mem_alloc_system(bytes);}
/* END - CR#1711 */
void free(pointer p) {_mem_free(p);}
#endif

/* EOF */

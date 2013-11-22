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
*** File: mem_swapn.c
***
*** Comments:      
***   This file contains the functions for performing endian
***   swapping of memory fields. This version is length limited
***                                                               
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _mem_swap_endian_len
* Returned Value  : None
* Comments        : convert data from Big to Little Endian
*   byte order ( or vice versa ).
*   The size of the fields in the data are defined by 
*   the null terminated array of 8 bit numbers.
*
*END*------------------------------------------------------------------*/

void _mem_swap_endian_len
   (
      /* 
      ** [IN] the address of a entry size array
      ** The array indicates the sizes of consecutive fields in the
      ** data, in bytes.
      */
      register uchar _PTR_ definition,

      /* [IN] the address of the data to modify */
      pointer              data,

      /* [IN] the fields in the definition array to process */
      _mqx_uint            len
      
   )
{ /* Body */
   register uchar _PTR_ data_ptr;
   register uchar _PTR_ next_ptr;
            uchar _PTR_ b_ptr;
            _mqx_uint   i;
   register _mqx_uint   size;
            uchar       c;

   data_ptr = (uchar _PTR_)data;
   size     = (_mqx_uint)*definition++;
   while ( size && len-- ) {
      switch ( size ) {
         case 0:  /* For compiler optimizations */
            break;
         case 1:  /* No need to swap */
            ++data_ptr;
            break;
         /* Cases 2 & 4 are common sizes */
         case 2:  
            c = data_ptr[0];
            data_ptr[0] = data_ptr[1];
            data_ptr[1] = c;
            data_ptr += 2;
            break;
         case 4:
            c = data_ptr[0];
            data_ptr[0] = data_ptr[3];
            data_ptr[3] = c;
            c = data_ptr[1];
            data_ptr[1] = data_ptr[2];
            data_ptr[2] = c;
            data_ptr += 4;
            break;
         /* All others done long hand */
         default: 
            next_ptr = data_ptr+size;
            b_ptr = data_ptr+size-1;
            i = (size/2) + 1;
            while ( --i ) {
               c = *data_ptr;
               *data_ptr++ = *b_ptr;
               *b_ptr-- = c;
            } /* Endwhile */
            data_ptr = next_ptr;
            break;
      } /* Endswitch */
      size = (_mqx_uint)*definition++;
   } /* Endwhile */

} /* Endbody */

/* EOF */

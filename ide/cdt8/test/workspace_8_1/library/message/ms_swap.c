/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ms_swap.c
***
*** Comments:      
***   This file contains the functions for manipulating the 
***   message queues, and queue ids.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "message.h"
#include "msg_prv.h"

#if MQX_USE_MESSAGES
/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _msg_swap_endian_header
* Returned Value  : None
* Comments        : convert a data messages header from intel to motorola
*   byte order ( or vice versa ).
*
*END*------------------------------------------------------------------*/

static uchar _KRNL_Hdr_def[] = {
  sizeof(_msg_size),
#if MQX_USE_32BIT_MESSAGE_QIDS
  sizeof(uint_16),
#endif
  sizeof(_queue_id), 
  sizeof(_queue_id), 
  0 
};

void _msg_swap_endian_header
   (
      /* [IN] the message whose header is to be byte swapped */
      register MESSAGE_HEADER_STRUCT_PTR message_ptr
   )
{ /* Body */

   _mem_swap_endian( (uchar _PTR_)_KRNL_Hdr_def, (pointer)&(message_ptr->SIZE));
   message_ptr->CONTROL &= ~MSG_HDR_ENDIAN_MASK;
   message_ptr->CONTROL |= MSG_HDR_ENDIAN;

} /* Endbody */


/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _msg_swap_endian_data
* Returned Value  : None
* Comments        : convert a data messages data from intel to motorola
*   byte order ( or vice versa ).
*   The size of the fields in the data are defined by 
*   the null terminated array of 8 bit numbers.  
*
*END*------------------------------------------------------------------*/

void _msg_swap_endian_data
   (
      /* [IN] the address of a entry size array
      ** The array indicates the sizes of consecutive fields in the
      ** data, in bytes.
      */
      uchar _PTR_               definition,

      /* [IN] the message whose data is to be byte swapped */
      register MESSAGE_HEADER_STRUCT_PTR message_ptr
   )
{ /* Body */

   _mem_swap_endian( definition, (pointer)((uchar _PTR_)message_ptr +
      sizeof(MESSAGE_HEADER_STRUCT)) );
   message_ptr->CONTROL &= ~MSG_DATA_ENDIAN_MASK;
   message_ptr->CONTROL |=  MSG_DATA_ENDIAN;

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */

/*HEADER************************************************************************
********************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: edserial.c
***
*** Comments:
***   provide the old interface function call that starts up
***
***
********************************************************************************
*END***************************************************************************/

#include "mqx_inc.h"
#include "fio.h"
#include "pcb.h"
#include "message.h"
#include "serial.h"
#include "edserial.h"
#include "edserprv.h"


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : ED_Serial_add
* Returned Value   : 
* Comments         :
*
*END*-------------------------------------------------------------------------*/

uint_32 ED_Serial_add
   (
      /* [IN] Name of I/O channel to use */
      char_ptr channel_name,

      /* [IN] Speed to set "channel_name" at */
      uint_32  baud_rate,

      /* [IN] Priority of ED Serial Task */
      uint_32  priority
   )
{ /* Body */
   TASK_TEMPLATE_STRUCT  task_templ;
   IO_CHAN_STRUCT _PTR_  chan_data_ptr;
   char                  task_name[] = ED_SERIAL_TASK_NAME;
   _task_id              tid;
   uint_32               tec;
   uint_32               result;

   /* Check to see if this function has already created the task */
   tid = _task_get_id_from_name((char_ptr)&task_name);
   if (tid != MQX_NULL_TASK_ID) {
      return ED_SER_ALREADY_CREATED;
   } /* Endif */  

   chan_data_ptr = 
      (IO_CHAN_STRUCT _PTR_)_mem_alloc_system((uint_32)sizeof(IO_CHAN_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS == 1
   if (chan_data_ptr == NULL) {
      return MQX_OUT_OF_MEMORY;
   } /* Endif */  
#endif
      
   chan_data_ptr->CHANNEL = channel_name;
   chan_data_ptr->BAUD = baud_rate;

   /* preserve this task's error code */   
   tec = _task_get_error();
   
   _mem_zero((uchar_ptr)&task_templ, sizeof(task_templ));
   task_templ.TASK_ADDRESS       = EDS_Serial_task;
   task_templ.TASK_STACKSIZE     = ED_SERIAL_STACK_SIZE;
   task_templ.TASK_PRIORITY      = priority;
   task_templ.TASK_NAME          = (char_ptr)ED_SERIAL_TASK_NAME;
   task_templ.CREATION_PARAMETER = (uint_32)chan_data_ptr;
   tid = _task_create(0, 0, (uint_32)&task_templ);
   if (tid == MQX_NULL_TASK_ID) {
      result = ED_SER_CANT_CREATE;
   } else {
      result = ED_SER_OK;
   } /* Endif */

   /* restore the task error code */   
   _task_set_error(tec);
   
   return result;

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : ED_Serial_remove
* Returned Value   : 
* Comments         : 
*
*END*-------------------------------------------------------------------------*/

uint_32 ED_Serial_remove
   (
      void 
   )
{ /* Body */
   uint_32   err;
   _task_id  tid = _task_get_id_from_name(ED_SERIAL_TASK_NAME);

   if (tid == MQX_NULL_TASK_ID) {
      return ED_SER_CANT_DESTROY;
   } /* Endif */

   err = _task_destroy(tid);
   if (err != MQX_OK ) {
      return ED_SER_CANT_DESTROY;
   } /* Endif */

   return ED_SER_OK;

} /* Endbody */


/*TASK*-------------------------------------------------------------------------
*
* Function Name  : EDS_Serial_task
* Returned Value : void
* Comments       : Runs the serial embedded debug server, using "io_channel" for
*                  communication
*
*END--------------------------------------------------------------------------*/

void EDS_Serial_task
   (
      uint_32    channel_info
   )
{ /* Body */      
   IO_CHAN_STRUCT_PTR   chan_ptr = (IO_CHAN_STRUCT_PTR)channel_info;
   FILE_PTR             io_stream;
   char_ptr             recv_buffer_ptr;
   uint_32              err;

   DEBUGM(printf("Error channel open!\n");)

   /* Open the channel */
   if ((IO_CHAN_STRUCT_PTR)channel_info != NULL) {
      io_stream = fopen(chan_ptr->CHANNEL, (char_ptr)(IO_SERIAL_XON_XOFF | IO_SERIAL_TRANSLATION));
      if (io_stream == NULL) {
         _task_block();
      } else {
         err = _io_ioctl(io_stream, IO_IOCTL_SERIAL_SET_BAUD, &(chan_ptr->BAUD));
         if (err != MQX_OK) {
            _task_block();
         } /* Endif */
      }/* Endif */
   } else {
      /* Use default I/O */
      io_stream = _io_get_handle(IO_STDOUT);
   }/* Endif */

   _mem_free(chan_ptr);

   recv_buffer_ptr = (char_ptr)_mem_alloc_zero(EDS_BUFFER_SIZE);
   if (recv_buffer_ptr == NULL) {
      _task_block();
   }
   while (TRUE) {  
      /* Wait for I/O - Block on error */
      if (!EDS_Serial_process(io_stream, recv_buffer_ptr)) {
            _task_block();
      } /* Endif */      
   } /* Endwhile */
   
} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name  : EDS_Serial_process
* Returned Value : TRUE if processed ok
* Comments       : 
*
*END--------------------------------------------------------------------------*/

boolean EDS_Serial_process
   (
      FILE_PTR io_stream,
      char_ptr recv_buffer_ptr
   )
{ /* Body */
   EDS_OP_STRUCT_PTR    op_ptr;                   
   int_32               num_recvd; 
   int_32               num_sent;

   /* Receive the request */
   num_recvd = EDS_Serial_recv(io_stream, recv_buffer_ptr, EDS_OP_STRUCT_SIZE);
   if (num_recvd != EDS_OP_STRUCT_SIZE) {
      return FALSE;      
   } /* Endif */       

   op_ptr = (EDS_OP_STRUCT_PTR)recv_buffer_ptr; 
                        
   EDS_Serial_check_errors(op_ptr);
   if ((ntohl(op_ptr->EDS_ERROR)) != EDS_OK) {
      EDS_Serial_send(io_stream, (char_ptr)op_ptr, EDS_OP_STRUCT_SIZE); 
   } else {
      /* Determine and handle client request */
      switch(ntohl(op_ptr->OPERATION)) {
     
         case EDS_IDENTIFY:
         case EDS_LITTLE_ENDIAN_IDENTIFY:
            EDS_Serial_identify(op_ptr);
            
            /* Send the identity */
            num_sent = EDS_Serial_send(io_stream, (char_ptr)op_ptr, EDS_OP_STRUCT_SIZE);
            if (num_sent < 0) {
               /* Error! */
               return FALSE; 
            } /* Endif */            
            break;
            
         case EDS_READ:
            /* Send the header */
            num_sent = EDS_Serial_send(io_stream, (char_ptr)op_ptr, EDS_OP_STRUCT_SIZE);
            if (num_sent < 0) {
               /* Error! */
               return FALSE;
            } /* Endif */

            /* Copy the data */         
            READ_BYTES((pointer)(ntohl(op_ptr->ADDRESS)), (pointer)&recv_buffer_ptr[EDS_OP_STRUCT_SIZE],
                        ntohl(op_ptr->SIZE));

            /* Send the requested data */
            num_sent = EDS_Serial_send(io_stream, (char_ptr)(&recv_buffer_ptr[EDS_OP_STRUCT_SIZE]), ntohl(op_ptr->SIZE));
            if (num_sent < 0) {
               /* Error! */
               return FALSE;
            } /* Endif */                          
            break;
            
         case EDS_WRITE:         
            /* Write data directly to memory */
            num_recvd = EDS_Serial_recv(io_stream, (char_ptr)(&recv_buffer_ptr[EDS_OP_STRUCT_SIZE]), ntohl(op_ptr->SIZE));
            WRITE_BYTES((pointer)&recv_buffer_ptr[EDS_OP_STRUCT_SIZE], (pointer)(ntohl(op_ptr->ADDRESS)),
                        ntohl(op_ptr->SIZE));
            if (num_recvd < 0) {
               /* Error! */
               return FALSE;
            } /* Endif */                          
            
            /* Send the result status */
            num_sent = EDS_Serial_send(io_stream, (char_ptr)op_ptr, (uint_32)EDS_OP_STRUCT_SIZE);
            if (num_sent < 0) {
               /* Error! */
               return FALSE;
            } /* Endif */                          
            break;
            
         default:
            break;
      } /* Endswitch */ 
   }/* Endif */     

   return TRUE;

} /* Endbody */                


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name  : EDS_Serial_check_errors
* Returned Value : void
* Comments       : Determines if the client request is valid.
*
*END--------------------------------------------------------------------------*/ 

void EDS_Serial_check_errors
   (
      EDS_OP_STRUCT_PTR op_ptr
   )
{ /* Body */

   /* Valid operation? */
   switch(ntohl(op_ptr->OPERATION)) {
      case EDS_IDENTIFY:
      case EDS_LITTLE_ENDIAN_IDENTIFY:
      case EDS_READ:
      case EDS_WRITE:
         htonl(op_ptr->EDS_ERROR,EDS_OK);
         break;
      default:
         htonl(op_ptr->EDS_ERROR,EDS_INVALID_OP);
         return;
   } /* Endswitch */
   
   /* Valid size? */
   if ((ntohl(op_ptr->OPERATION) == EDS_READ) || (ntohl(op_ptr->OPERATION) == EDS_WRITE)) {
      /* if ((ntohl(op_ptr->SIZE) < (uint_32)0L) || */
      if (((int_32)ntohl(op_ptr->SIZE) < (uint_32)0L) ||
          (ntohl(op_ptr->SIZE) > (uint_32)EDS_BUFFER_SIZE)) {
         htonl(op_ptr->EDS_ERROR,EDS_INVALID_SIZE);
      } else {
         htonl(op_ptr->EDS_ERROR,EDS_OK);   
      } /* Endif */
   } /* Endif */

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name  : EDS_Serial_identify
* Returned Value : void
* Comments       : Fills in an OP_STRUCT containing the processor type, endian, 
*                  start and end of kernel memory of this server, and the result 
*                  status.
*
*END--------------------------------------------------------------------------*/ 

void EDS_Serial_identify
   (
      EDS_OP_STRUCT_PTR op_ptr
   ) 
{ /* Body */

   htonl(op_ptr->PROCESSOR,_mqx_get_cpu_type());
#if (PSP_MEMORY_ADDRESSING_CAPABILITY == 32)
   htonl(op_ptr->ENDIAN, MQX_BIG_ENDIAN);
#else
   htonl(op_ptr->ENDIAN, MSG_HDR_ENDIAN);
#endif
   htonl(op_ptr->ADDRESS,(uint_32)_mqx_get_initialization()->START_OF_KERNEL_MEMORY);
   htonl(op_ptr->ADDRESS2,(uint_32)_mqx_get_initialization()->END_OF_KERNEL_MEMORY);

} /* Endbody */ 


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : EDS_Serial_send
* Returned Value   : 
* Comments         : Package the data and send it out
*
*END*-------------------------------------------------------------------------*/

int_32 EDS_Serial_send
   (
      FILE_PTR  io_stream,
      char_ptr  buffer_ptr,
      int_32    bytes_to_send
   )
{ /* Body */
#ifdef EDS_USE_DEBUG_SPACES
   char   out_str[4];
#else
   char   out_str[3];
#endif
   int_32 err;
   uchar  the_char;
   int_32 i;

   /* Send the Start of Frame character */
   err = fputc(STX, io_stream);
   if (err == IO_EOF) {
      return -1;
   } /* Endif */

   for (i = 0; i < bytes_to_send; i++) {
      /* Convert each byte to a string */
      the_char = (uchar)buffer_ptr[i];
#ifdef EDS_USE_DEBUG_SPACES
      sprintf(out_str, "%02x ", the_char);
#else
      sprintf(out_str, "%02x", the_char);
#endif      
      /* Send the byte */
      err = fputs(out_str, io_stream);
      if (err != MQX_OK) {
         return -1;
      } /* Endif */
   } /* EndFor */

   /* Send the End of Frame character */
   err = fputc(ETX, io_stream);
   if (err == IO_EOF) {
      return -1;
   } /* Endif */
   
   return bytes_to_send;

} /* Endbody */


/*FUNCTION*---------------------------------------------------------------------
*
* Function Name    : EDS_Serial_recv
* Returned Value   : 
* Comments         : Receive data and unpack it
*
*END*-------------------------------------------------------------------------*/

int_32 EDS_Serial_recv
   (
      FILE_PTR io_stream,
      char_ptr buffer_ptr,
      int_32   buff_size
   )
{ /* Body */
   int_32   state = STATE_WAIT_STX;
   boolean  done = FALSE;
   int_32   the_char;
   char_ptr pos_ptr;
   int_32   count;
   char     byte_arr[3];
   int_32   index = 0;
   uint_32  convert;
   uchar    byte_char;

   while (!done) {
      switch (state) {
         case STATE_WAIT_STX:
            count = 0;
            the_char = fgetc(io_stream);
            if (the_char == IO_EOF) {
               /* Read error */
               return -1;
            } /* Endif */
            if (the_char == STX) {
               state = STATE_WAIT_NON_STX;
               break;
            } /* Endif */
            DEBUGM(printf("%c ", the_char);)
            break;

         case STATE_WAIT_NON_STX:
            the_char = fgetc(io_stream);
            if (the_char == IO_EOF) {
               return -1;
            } /* Endif */
            if (the_char == ETX) {
               DEBUGM(printf("Bad ETX\n\r");)
               state = STATE_WAIT_STX;
               break;
            } /* Endif */
            if (the_char != STX) { /*and !ETX */
               pos_ptr = buffer_ptr;
               state = STATE_GETTING_DATA;
               break;
            } /* Endif */                 
            break;

         case STATE_GETTING_DATA:
            /* the_char is read in previous state */
            
            /* Store the character */
            byte_arr[index] = (char)the_char;
            if (index == 0) {
               index = 1;
            } else {
               /* Combine two characters into a byte and store to buffer */
               byte_arr[2] = '\0';
               sscanf(byte_arr, "%02x", &convert);
               byte_char = convert & 0xFF;
               *pos_ptr++ = byte_char;
               count++;
               index = 0;
            } /* Endif */

            /* Read a new character */
            the_char = fgetc(io_stream);
            if (the_char == IO_EOF) {
               return -1;
            } /* Endif */

#ifdef EDS_USE_DEBUG_SPACES
            /* Discard spaces and try again */
            if (the_char == ' ') {
               the_char = fgetc(io_stream);
               if (the_char == IO_EOF) {
                  return -1;
               } /* Endif */               
            } /* Endif */
#endif
            if (the_char == STX) {
               DEBUGM(printf("Bad STX\n\r");)
               state = STATE_WAIT_NON_STX;
               break;
            } /* Endif */

            if (the_char == ETX && count == buff_size) {
               done = TRUE;
               break;
            } /* Endif */

            if (the_char == ETX) { /* && we don't have enough characters yet */
               state = STATE_WAIT_STX;
               DEBUGM(printf("Early ETX\n\r");)
               break;
            } /* Endif */

            break;
      } /* Endswitch */
   } /* Endwhile */   

   return count;

} /* Endbody */

/* EOF */

/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: lwmsgq.c
***
*** Comments:
***   This file provides a simple light weight message queue.
*** A message in the message pool has a fixed size, a multiple of 32 bits.
*** Blocking reads, and Blocking writes are provided.
***
***************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"
#include "lwmsgq.h"
#include "lwmsgqpr.h"

#if MQX_USE_LWMSGQ
/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _lwmsgq_init
* Returned Value   : error code
* Comments         :
*   This function initializes the light weight message queue
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwmsgq_init
   (
      /* The starting location of the message pool */
      pointer   location,

      /* Number of messages created after the header */
      _mqx_uint num_messages,

      /* The size of the messages in _mqx_max_type's */
      _mqx_uint msg_size
   )
{/* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   LWMSGQ_STRUCT_PTR q_ptr = (LWMSGQ_STRUCT_PTR)location;   


/* START CR 2365 */
#if MQX_CHECK_ERRORS
   LWMSGQ_STRUCT_PTR lwmsg_chk_ptr;
#endif
/* END CR 2365 */
   
   
   _GET_KERNEL_DATA(kernel_data);
   _KLOGE4(KLOG_lwmsgq_init, location, num_messages, msg_size);

   _QUEUE_INIT(&q_ptr->WAITING_WRITERS,0);
   _QUEUE_INIT(&q_ptr->WAITING_READERS,0);
   q_ptr->MSG_SIZE      = msg_size;
   q_ptr->MAX_SIZE      = num_messages;
   q_ptr->CURRENT_SIZE  = 0;
   q_ptr->MSG_START_LOC = (_mqx_max_type_ptr)
      ((uchar_ptr)q_ptr + sizeof(LWMSGQ_STRUCT));
   q_ptr->MSG_END_LOC   = q_ptr->MSG_START_LOC + msg_size * num_messages;
   q_ptr->MSG_WRITE_LOC = q_ptr->MSG_START_LOC;
   q_ptr->MSG_READ_LOC  = q_ptr->MSG_START_LOC;
   if (kernel_data->LWMSGQS.NEXT == NULL) {
      /* Initialize the light weight message queue */
      _QUEUE_INIT(&kernel_data->LWMSGQS, 0);
   } /* Endif */
   q_ptr->VALID         = LWMSGQ_VALID;
   
#if MQX_CHECK_ERRORS
   /* Check if lwmsgq is already initialized */
   lwmsg_chk_ptr = (LWMSGQ_STRUCT_PTR)((pointer)kernel_data->LWMSGQS.NEXT);
   while (lwmsg_chk_ptr != (LWMSGQ_STRUCT_PTR)((pointer)&kernel_data->LWMSGQS)) {
      if (lwmsg_chk_ptr == q_ptr) {
         _KLOGX2(KLOG_lwmsgq_init, MQX_EINVAL);
         return(MQX_EINVAL);
      } /* Endif */
      lwmsg_chk_ptr = (LWMSGQ_STRUCT_PTR)((pointer)lwmsg_chk_ptr->LINK.NEXT);
   } /* Endwhile */
#endif

   _QUEUE_ENQUEUE(&kernel_data->LWMSGQS, &q_ptr->LINK);
   _KLOGX2(KLOG_lwmsgq_init, MQX_OK);
   return MQX_OK;

}/* Endbody */


/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _lwmsgq_send
* Returned Value   : error code
* Comments         :
*   This function puts the word into the queue if possible
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwmsgq_send
   (
      /* Handle to the queue */
      pointer           handle,

      /* location of message to copy in */
      _mqx_max_type_ptr message,

      /* flags for blocking on full, blocking on send */
      _mqx_uint         flags
   )
{/* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   LWMSGQ_STRUCT_PTR      q_ptr = (LWMSGQ_STRUCT_PTR)handle;
   _mqx_uint              i;
   _mqx_max_type_ptr      from_ptr;
   _mqx_max_type_ptr      to_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
   _KLOGE4(KLOG_lwmsgq_send, handle, message, flags);
   
   _int_disable();
#if MQX_CHECK_VALIDITY
   if (q_ptr->VALID != LWMSGQ_VALID){
      _int_enable();
      _KLOGX2(KLOG_lwmsgq_send, LWMSGQ_INVALID);
      return LWMSGQ_INVALID;
   } /* Endif */
#endif
   if (LWMSGQ_IS_FULL(q_ptr)) {
      if (flags & LWMSGQ_SEND_BLOCK_ON_FULL) {
         td_ptr = kernel_data->ACTIVE_PTR;
         while (LWMSGQ_IS_FULL(q_ptr)) {
            td_ptr->STATE = LWMSGQ_WRITE_BLOCKED;
            td_ptr->INFO  = (_mqx_uint)&q_ptr->WAITING_WRITERS;
            _QUEUE_UNLINK(td_ptr);
            _QUEUE_ENQUEUE(&q_ptr->WAITING_WRITERS, &td_ptr->AUX_QUEUE);
            _sched_execute_scheduler_internal(); /* Let other tasks run */
         } /* Endwhile */
      } else {
         _int_enable();
         _KLOGX2(KLOG_lwmsgq_send, LWMSGQ_FULL);
         return LWMSGQ_FULL;
      } /* Endif */
   }/* Endif */
   to_ptr = q_ptr->MSG_WRITE_LOC;
   from_ptr = message;
   i = q_ptr->MSG_SIZE+1;
   while (--i) {
      *to_ptr++ = *from_ptr++;
   } /* Endwhile */
   q_ptr->MSG_WRITE_LOC += q_ptr->MSG_SIZE;
   if (q_ptr->MSG_WRITE_LOC >= q_ptr->MSG_END_LOC) {
      q_ptr->MSG_WRITE_LOC = q_ptr->MSG_START_LOC;
   } /* Endif */
   q_ptr->CURRENT_SIZE++;
   if (! _QUEUE_IS_EMPTY(&q_ptr->WAITING_READERS)) {
      _QUEUE_DEQUEUE(&q_ptr->WAITING_READERS, td_ptr);
      _BACKUP_POINTER(td_ptr, TD_STRUCT, AUX_QUEUE);
      _TIME_DEQUEUE(td_ptr, kernel_data);
      td_ptr->INFO = 0;  /* Signal that post is activating the task */
      _TASK_READY(td_ptr, kernel_data);
      if (flags & LWMSGQ_SEND_BLOCK_ON_SEND) {
         _task_block();
      } else {
         _CHECK_RUN_SCHEDULER(); /* Let higher priority task run */
      }/* Endif */
   } else {
      if (flags & LWMSGQ_SEND_BLOCK_ON_SEND) {
         _task_block();
      }/* Endif */
   } /* Endif */
   _int_enable();
   _KLOGX2(KLOG_lwmsgq_send, MQX_OK);
   return MQX_OK;

}/* Endbody */


/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _lwmsgq_receive
* Returned Value   : error code
* Comments         :
*   This function receives a message from the light weight message queue.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwmsgq_receive
   (
      /* Handle to the queue */
      pointer             handle,

      /* location of message to copy to */
      _mqx_max_type_ptr   message,

      /* flags for blocking on empty */
      _mqx_uint           flags,

      /* Timeout for receive if using ticks if 0, ignored */
      _mqx_uint           ticks,

      /* Timeout if receive timout using tick struct must have flags set */
      MQX_TICK_STRUCT_PTR tick_ptr
   )
{/* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   TD_STRUCT_PTR          td_ptr;
   LWMSGQ_STRUCT_PTR      q_ptr = (LWMSGQ_STRUCT_PTR)handle;
   _mqx_uint              i;
   _mqx_max_type_ptr      from_ptr;
   _mqx_max_type_ptr      to_ptr;
   
   _GET_KERNEL_DATA(kernel_data);
   _KLOGE6(KLOG_lwmsgq_receive, handle, message, flags, ticks, tick_ptr);
   
   _int_disable();
#if MQX_CHECK_VALIDITY
   if (q_ptr->VALID != LWMSGQ_VALID){
      _int_enable();
      _KLOGX2(KLOG_lwmsgq_send, LWMSGQ_INVALID);
      return LWMSGQ_INVALID;
   } /* Endif */
#endif
   if (LWMSGQ_IS_EMPTY(q_ptr)) {
      if (flags & LWMSGQ_RECEIVE_BLOCK_ON_EMPTY) {
         td_ptr = kernel_data->ACTIVE_PTR;
         while (LWMSGQ_IS_EMPTY(q_ptr)) {
            td_ptr->STATE = LWMSGQ_READ_BLOCKED;
            td_ptr->INFO  = (_mqx_uint)&q_ptr->WAITING_READERS;
            _QUEUE_UNLINK(td_ptr);
            _QUEUE_ENQUEUE(&q_ptr->WAITING_READERS, &td_ptr->AUX_QUEUE);
            if (ticks || (flags & (LWMSGQ_TIMEOUT_UNTIL | LWMSGQ_TIMEOUT_FOR))){
               if (ticks) {
                  PSP_ADD_TICKS_TO_TICK_STRUCT(&kernel_data->TIME, ticks,
                     &td_ptr->TIMEOUT);
               } else if (flags & LWMSGQ_TIMEOUT_UNTIL){
                  td_ptr->TIMEOUT = *tick_ptr;
               } else {
                  PSP_ADD_TICKS(tick_ptr, &kernel_data->TIME, &td_ptr->TIMEOUT);
               } /* Endif */
               _time_delay_internal(td_ptr);
               if (td_ptr->INFO != 0) {
                  _int_enable();
                  _KLOGX2(KLOG_lwmsgq_receive, LWMSGQ_TIMEOUT);
                  return LWMSGQ_TIMEOUT;
               } /* Endif */
            } else {
               _sched_execute_scheduler_internal(); /* Let other tasks run */
            } /* Endif */
         } /* Endwhile */
      } else {
         _int_enable();
         _KLOGX2(KLOG_lwmsgq_receive, LWMSGQ_EMPTY);
         return LWMSGQ_EMPTY;
      } /* Endif */
   }/* Endif */
   from_ptr = q_ptr->MSG_READ_LOC;
   to_ptr = message;
   i = q_ptr->MSG_SIZE+1;
   while (--i) {
      *to_ptr++ = *from_ptr++;
   } /* Endwhile */
   q_ptr->MSG_READ_LOC += q_ptr->MSG_SIZE;
   if (q_ptr->MSG_READ_LOC >= q_ptr->MSG_END_LOC) {
      q_ptr->MSG_READ_LOC = q_ptr->MSG_START_LOC;
   } /* Endif */
   q_ptr->CURRENT_SIZE--;
   if (! _QUEUE_IS_EMPTY(&q_ptr->WAITING_WRITERS)) {
      _QUEUE_DEQUEUE(&q_ptr->WAITING_WRITERS, td_ptr);
      _BACKUP_POINTER(td_ptr, TD_STRUCT, AUX_QUEUE);
      td_ptr->INFO = 0;  /* Signal that post is activating the task */
      _TASK_READY(td_ptr, kernel_data);
      _CHECK_RUN_SCHEDULER(); /* Let higher priority task run */
   } /* Endif */
   _int_enable();
   _KLOGX2(KLOG_lwmsgq_receive, MQX_OK);
   return MQX_OK;

}/* Endbody */
#endif /* MQX_USE_LWMSGQ */

/* EOF */

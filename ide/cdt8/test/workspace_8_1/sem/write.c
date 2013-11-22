/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2003 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: write.c
***
*** Comments: 
*** This file contains the code for the write task.
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <sem.h>
#include "main.h"

/*TASK*----------------------------------------------------------
*
* Task Name : write_task
* Comments  : 
*     This task opens a connection to all three semaphores then 
*     waits for sem.write and sem.index. It writes one entry
*     in the data array and posts sem.index and sem.read.
*END*-----------------------------------------------------------*/
     
void write_task 
   (
      uint_32 initial_data
   )
{
   pointer write_sem;
   pointer read_sem;
   pointer index_sem;

   /* open connections to all the semaphores */
   if (_sem_open("sem.write", &write_sem) != MQX_OK) {
      printf("\nOpening write semaphore failed.");
      _mqx_exit(0);
   }                  
   if (_sem_open("sem.index", &index_sem) != MQX_OK) {
      printf("\nOpening index semaphore failed.");
      _mqx_exit(0);
   }
   if (_sem_open("sem.read", &read_sem) != MQX_OK) {
      printf("\nOpening read semaphore failed.");
      _mqx_exit(0);
   }

   while (TRUE) {
      /* wait for the semphores */
      if (_sem_wait(write_sem, 0) != MQX_OK) {
         printf("\nWwaiting for Write semaphore failed");
         _mqx_exit(0);
      }                  
      if (_sem_wait(index_sem, 0) != MQX_OK) {
         printf("\nWaiting for index semaphore failed");
         _mqx_exit(0);
      }
      
      fifo.DATA[fifo.WRITE_INDEX++] = _task_get_id();
      if (fifo.WRITE_INDEX >= ARRAY_SIZE) {
         fifo.WRITE_INDEX = 0;
      }
      /* Post the semaphores */
      _sem_post(index_sem);
      _sem_post(read_sem);
   }

}

/* EOF */

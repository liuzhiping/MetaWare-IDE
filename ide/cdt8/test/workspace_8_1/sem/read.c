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
*** File: read.c
***
*** Comments: 
*** This file contains the code for the read task.
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <sem.h>
#include "main.h"

/*TASK*----------------------------------------------------------
*
* Task Name : read_task
* Comments  : 
*     This task opens a connection to all three semaphores, then 
*     waits for sem.read and sem.index. One element in the data
*     array is displayed. sem.index and sem.write are then posted.
*END*----------------------------------------------------------*/
                
void read_task 
   (
      uint_32 initial_data
   )
{
   pointer write_sem;
   pointer read_sem;
   pointer index_sem;

   /* open connections to all the sempahores */
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
      /* wait for the semaphores */
      if (_sem_wait(read_sem, 0) != MQX_OK) {
         printf("\nWaiting for read semaphore failed.");
         _mqx_exit(0);
      }
      if (_sem_wait(index_sem,0) != MQX_OK) {
         printf("\nWaiting for index semaphore failed.");
         _mqx_exit(0);
      }

      printf("\n 0x%lx", fifo.DATA[fifo.READ_INDEX++]);
      if (fifo.READ_INDEX >= ARRAY_SIZE) {
         fifo.READ_INDEX = 0;
      } 
      /* Post the semaphores */
      _sem_post(index_sem);
      _sem_post(write_sem);
   }

}

/* EOF */

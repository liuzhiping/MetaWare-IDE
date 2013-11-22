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
*** File: main.c
***
*** Comments: 
*** Comments: 
***    This example demonstrates semaphore usage. The read task creates 
***    2 write tasks. It also creates a read and write semaphore.
***    Each write task waits for the write semaphore. When aquired,
***    it places its task ID into a buffer and posts the read semaphore 
***    to signal the read task.
***
***    The read task waits for the read semaphore and then prints the
***    task ID. It then posts the write semaphore.
***
*** Expected Output:
*** write_task created. id 0x10003
*** write_task created. id 0x10004
*** write_task created. id 0x10005
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10004
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10004
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10003
*** 0x10004
*** 0x10003
*** 
*** Pattern continues until terminated
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <sem.h>
#include "main.h"

SW_FIFO      fifo;

/*TASK*----------------------------------------------------------
*
* Task Name : main_task
* Comments  : 
*     This task initializes three semaphores, creates NUM_WRITERS 
*     write_task's, and creates one read_task.
*END*----------------------------------------------------------*/

void main_task 
   (
      uint_32 initial_data
   )
{
   _task_id   task_id;
   _mqx_uint  i;

   fifo.READ_INDEX  = 0;
   fifo.WRITE_INDEX = 0;

   /* Create the semaphores */
   if (_sem_create_component(3,1,6) != MQX_OK) {
      printf("\nCreate semaphore component failed");
      _mqx_exit(0);
   }
   if (_sem_create("sem.write", ARRAY_SIZE, 0) != MQX_OK) {
      printf("\nCreating write semaphore failed");
      _mqx_exit(0);
   }
   if (_sem_create("sem.read", 0, 0) != MQX_OK) {
      printf("\nCreating read semaphore failed");
      _mqx_exit(0);
   }
   if (_sem_create("sem.index", 1, 0) != MQX_OK) {
      printf("\nCreating index semaphore failed");
      _mqx_exit(0);
   }

   /* Create the tasks */
   for (i = 0; i < NUM_WRITERS; i++) {
      task_id = _task_create(0, WRITE_TASK, (uint_32)i);
      printf("\nwrite_task created, id 0x%lx", task_id);
   }
   
   task_id = _task_create(0,READ_TASK, 0);
   printf("\nread_task created, id 0x%lX", task_id);

   _task_block();

}

/* EOF */

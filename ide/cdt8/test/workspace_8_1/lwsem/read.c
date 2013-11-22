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
*** File: write.c
***
*** Comments: 
***    This example demonstrates lightweight semaphore usage.
***    The read task creates 3 write tasks and assigns each a 
***    unique identifier. It also creates a read and write semaphore.
***    Each write task waits for the write semaphore. When aqired,
***    it places it's identifier in a buffer and then posts to the
***    read task..
***
***    The read task waits for the read semaphore and then prints the
***    identifier. It then posts the write semaphore.
***
*** Expected Output:
*** write_task created. id ox10003
*** write_task created. id ox10004
*** write_task created. id ox10005
*** write_task created. 0x41
*** write_task created. 0x42
*** write_task created. 0x43
***
*** A
*** A
*** B
*** C
*** A
*** B
*** C
***
*** Patterns continue until terminated
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include "read.h"

SW_FIFO     fifo;

/*TASK*--------------------------------------------------------
*
* Task Name : read_task
* Comments  : This task creates two semaphores and 
*             NUM_WRITER write_tasks. It waits
*             on the read_sem and finally outputs the 
*             "data" variable.
*END*--------------------------------------------------------*/

void read_task 
   (
      uint_32 initial_data
   )
{
   _task_id  task_id;
   _mqx_uint result;
   _mqx_uint i;

   /* Create the lightweight semaphores */
   result = _lwsem_create(&fifo.READ_SEM, 0);
   if (result != MQX_OK) {
      printf("\nCreating read_sem failed: 0x%X", result);
      _mqx_exit(0);
   }

   result = _lwsem_create(&fifo.WRITE_SEM, 1);
   if (result != MQX_OK) {
      printf("\nCreating write_sem failed: 0x%X", result);
      _mqx_exit(0);
   }

   /* Create the write tasks */
   for (i = 0; i < NUM_WRITERS; i++) {
      task_id = _task_create(0, WRITE_TASK, (uint_32)('A' + i));
      printf("\nwrite_task created, id 0x%lX", task_id);
   }

   while(TRUE) {
      result = _lwsem_wait(&fifo.READ_SEM);
      if (result != MQX_OK) {
         printf("\n_lwsem_wait failed: 0x%X", result);
         _mqx_exit(0);
      }
      putchar('\n');
      putchar(fifo.DATA);
      _lwsem_post(&fifo.WRITE_SEM);
   }

}

/* EOF */

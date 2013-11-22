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
***    This file contains the write task code.
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include "read.h"

/*TASK*--------------------------------------------------------
*
* Task Name : write_task
* Comments  : This task waits for the write semaphore, 
*             then writes a character to "data" and posts a
*             read semaphore. 
*END*--------------------------------------------------------*/

void write_task 
   (
      uint_32 initial_data
   )
{

   printf("\nWrite task created: 0x%lX", initial_data);
   while (TRUE) {
      if (_lwsem_wait(&fifo.WRITE_SEM) != MQX_OK) {
         printf("\n_lwsem_wait failed");
         _mqx_exit(0);
      }
      fifo.DATA = (uchar)initial_data;
      _lwsem_post(&fifo.READ_SEM);
   }

}

/* EOF */

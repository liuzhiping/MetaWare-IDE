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
*** File: taskq.c
***
*** Comments: 
***    This example creates a task queue and the simulated_ISR
***    task. Then it enters an infinite loop, printing "Tick" and
***    suspending the task queue.
*** 
*** 
*** Expected Output:
*** Tick
*** Tick
*** Tick
*** Tick
*** Tick
*** Tick
*** 
*** Repeats until terminated
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>

/* Task IDs */
#define SERVICE_TASK 5
#define ISR_TASK     6

extern void simulated_ISR_task(uint_32);
extern void service_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   {SERVICE_TASK, service_task,       800, 5, "service",
      MQX_AUTO_START_TASK, 0,0},
   {ISR_TASK,     simulated_ISR_task, 800, 5, "simulated_ISR",
      0,                   0, 0},
   {0,            0,                  0,   0, 0,
      0,                   0, 0}
};

pointer   my_task_queue;

/*TASK*-----------------------------------------------------
* 
* Task Name : simulated_ISR_task
* Comments  :
*   This task pauses and then resumes the task queue.
*END*-----------------------------------------------------*/

void simulated_ISR_task 
   (
      uint_32 initial_data
   )
{

   while (TRUE) {
      _time_delay_ticks(10);
      _taskq_resume(my_task_queue, FALSE);
   }
}


/*TASK*-----------------------------------------------------
* 
* Task Name : service_task
* Comments  :
*    This task creates a task queue and the simulated_ISR_task
*    task. Then it enters an infinite loop, printing "Tick" and
*    suspending the task queue.
*END*-----------------------------------------------------*/

void service_task
   (
      uint_32 initial_data
   )
{
   _task_id second_task_id;

   /* create a task queue */
   my_task_queue = _taskq_create(MQX_TASK_QUEUE_FIFO);
   if (my_task_queue == NULL) {
      _mqx_exit(0);
   }

   /* create the ISR task */
   second_task_id = _task_create(0, ISR_TASK, 0);
   if (second_task_id == MQX_NULL_TASK_ID) {
      printf ("\n Could not create simulated_ISR_task\n");
      _mqx_exit(0);
   }
   
   while (TRUE) {
      printf(" Tick \n");
      _taskq_suspend(my_task_queue);
   }

}

/* EOF */

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
*** File: event.c
***
*** Comments: 
***    This file contains the source for the event example program.
*** 
***    A Simulated_tick ISR sets an event bit each time it runs. 
***    The Service task performs a certain action each time a tick 
***    occurs and waits for the event bit that Simulated_tick sets.
***
*** 
*** Expected Output:
*** Tick
*** Tick
*** Tick
*** Tick
***
*** Coninues until halted
*** 
***************************************************************************
*END**********************************************************************/

/* For hardware, increase ISR_TASK_DELAY_TICKS to 200 */
/* Reduced on the simulator so the delay times are equivelent */
#ifndef _ISR_TASK_DELAY_TICKS
#define _ISR_TASK_DELAY_TICKS 10
#endif

#include <mqx.h>
#include <bsp.h>
#include <event.h>

/* Task IDs */
#define SERVICE_TASK 5
#define ISR_TASK     6

/* Function prototypes */
extern void simulated_ISR_task(uint_32);
extern void service_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   {SERVICE_TASK, service_task,       800, 5, "service",       
      MQX_AUTO_START_TASK, 0, 0},
   {ISR_TASK,     simulated_ISR_task, 800, 5, "simulated_ISR", 
      0,                   0, 0},
   {0,            0,                  0,   0, 0, 
      0,                   0, 0}
};

/*TASK*-----------------------------------------------------
* 
* Task Name    : simulated_ISR_task
* Comments     :
*    This task opens a connection to the event group. After
*    delaying, it sets the event bits.   
*END*-----------------------------------------------------*/

void simulated_ISR_task 
   (
      uint_32 initial_data
   )
{
   pointer event_ptr;

   /* open event connection */
   if (_event_open("event.global",&event_ptr) != MQX_OK)  {
      printf("\nOpen Event failed");
      _mqx_exit(0);
   }

   while (TRUE) {
      _time_delay_ticks(_ISR_TASK_DELAY_TICKS);
      if (_event_set(event_ptr,0x01) != MQX_OK) {
         printf("\nSet Event failed");
         _mqx_exit(0);
      }
   }
}

/*TASK*-----------------------------------------------------
* 
* Task Name    : service_task
* Comments     :
*    This task creates an event group and the simulated_ISR_task 
*    task. It opens a connection to the event group and waits.
*    After the appropriate event bit has been set, it clears 
*    the event bit and prints "Tick."
*END*-----------------------------------------------------*/

void service_task 
   (
      uint_32 initial_data
   )
{
   pointer  event_ptr;
   _task_id second_task_id;

   /* Set up an event group */
   if (_event_create("event.global") != MQX_OK) {
      printf("\nMake event failed");
      _mqx_exit(0);
   }
   if (_event_open("event.global", &event_ptr) != MQX_OK) {
      printf("\nOpen event failed");
      _mqx_exit(0);
   }

   /* Create the ISR task */
   second_task_id = _task_create(0, ISR_TASK, 0);
   if (second_task_id == MQX_NULL_TASK_ID) {
      printf("Could not create simulated_ISR_task \n");
      _mqx_exit(0);
   }

   while (TRUE) {
      if (_event_wait_all_ticks(event_ptr, 0x01, 0) != MQX_OK) {
         printf("\nEvent Wait failed");
         _mqx_exit(0);
      }

      if (_event_clear(event_ptr,0x01) != MQX_OK) {
         printf("\nEvent Clear failed");
         _mqx_exit(0);
      }
      printf(" Tick \n");
   }
}  

/* EOF */
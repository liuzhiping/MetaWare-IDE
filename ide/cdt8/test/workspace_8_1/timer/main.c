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
***    This example creates a timer and uses it to control
***    a blinking LED simulation.
***
*** Expected Output:
*** ON
*** OFF
*** ON
***
*** The task is finished!
***
*** Can be slow running on the simulator.
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>
#include <timer.h>

/*  This example calls _timer_create_component before it activates the
**  timers so that the application can specify the timer task stack size.
**  The stack size may need to be larger than the TIMER_DEFAULT_STACK_SIZE 
**  that is defined in timer.h, since the application's handler function uses 
**  this stack. In this example, the handlers are LED_on and LED_off, 
**  which have large stack requirements. You may need to increase the stack size
**  for your target hardware.
*/
#define TIMER_TASK_PRIORITY  2
#define TIMER_STACK_SIZE     1000

#define MAIN_TASK      10

extern void main_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   {MAIN_TASK, main_task, 2000, 8, "Main", MQX_AUTO_START_TASK,
      0, 0},
   {0L,        0,         0,    0, 0,      0,
      0, 0}
};

/*FUNCTION*------------------------------------------------------
*
* Function Name  : LED_on
* Returned Value : none
* Comments       :
*     This timer function prints out "ON"
*END*-----------------------------------------------------------*/

void LED_on
   (
      _timer_id id,
      pointer data_ptr,
      MQX_TICK_STRUCT_PTR tick_ptr
   )
{
   printf("ON\n");
}

/*FUNCTION*------------------------------------------------------
*
* Function Name  : LED_off
* Returned Value : none
* Comments       :
*     This timer function prints out "OFF"
*END*-----------------------------------------------------------*/

void LED_off
   (
      _timer_id id,
      pointer data_ptr,
      MQX_TICK_STRUCT_PTR tick_ptr
   )
{
   printf("OFF\n");
}

/*TASK*----------------------------------------------------------
*
* Task Name : main_task
* Comments  : 
*     This task creates two timers, each of a period of 2 seconds,
*     the second timer offset by 1 second from the first.
*END*-----------------------------------------------------------*/

void main_task
   (
      uint_32 initial_data
   )
{
   MQX_TICK_STRUCT ticks;
   MQX_TICK_STRUCT dticks;
   _timer_id       on_timer;
   _timer_id       off_timer;

   /* 
   ** Create the timer component with more stack than the default
   ** in order to handle printf() requirements: 
   */
   _timer_create_component(TIMER_TASK_PRIORITY, TIMER_STACK_SIZE);

   _time_init_ticks(&dticks, 0);
   _time_add_sec_to_ticks(&dticks, 2);

   _time_get_ticks(&ticks);
   _time_add_sec_to_ticks(&ticks, 1);
   on_timer = _timer_start_periodic_at_ticks(LED_on, 0, 
      TIMER_ELAPSED_TIME_MODE, &ticks, &dticks);
   _time_add_sec_to_ticks(&ticks, 1);
   off_timer = _timer_start_periodic_at_ticks(LED_off, 0, 
      TIMER_ELAPSED_TIME_MODE, &ticks, &dticks);

   _time_delay_ticks(20);
   printf("\nThe task is finished!");

   _timer_cancel(on_timer);
   _timer_cancel(off_timer);

   _mqx_exit(0);

}

/* EOF */

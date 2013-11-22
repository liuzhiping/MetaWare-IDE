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
*** File: watchdog.c
***
*** Comments: 
***    This example creates a watchdog timer. It then sets the watchdog
***    and creates delays of greater periods of time until the timer
***    fires.
*** 
*** Expected Output:
*** 
*** 1000
*** 10000
*** 100000
*** 1000000
*** Watchdog expired for task: 0xE17B8
*** 1000
*** 10000
*** 100000
*** 1000000
*** Watchdog expired for task: 0xE17B8
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <watchdog.h>

#define MAIN_TASK       10

extern void main_task(uint_32);
extern void handle_watchdog_expiry(pointer);


TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   { MAIN_TASK, main_task, 4000, 8, "Main",
      MQX_AUTO_START_TASK, 0, 0},
   { 0,         0,         0,    0, 0,
      0,                   0, 0}
};


/*FUNCTION*------------------------------------------------------
*
* Function Name  : handle_watchdog_expiry
* Returned Value : none
* Comments       :
*     This function is called when a watchdog has expired.
*END*-----------------------------------------------------------*/

void handle_watchdog_expiry
   (
      pointer td_ptr
   )
{
   printf("\nWatchdog expired for task: 0x%P", td_ptr);
   _mqx_exit(1);
}

/*FUNCTION*------------------------------------------------------
*
* Function Name  : waste_time
* Returned Value : input value times 10
* Comments       :
*     This function loops the specified number of times,
*     essentially wasting time.
*END*-----------------------------------------------------------*/

_mqx_uint waste_time
   (
      _mqx_uint n
   )
{
   _mqx_uint        i;
   volatile uint_32 result;

   result = 0;
   for (i = 0; i < n; i++) {
      result += 1;
   }
   return result * 10;
}


/*TASK*----------------------------------------------------------
*
* Task Name : main_task
* Comments  : 
*   This task creates a watchdog, then loops, performing
*   work for longer and longer periods until the watchdog fires.
*END*-----------------------------------------------------------*/

void main_task
   (
      uint_32 initial_data
   )
{
   MQX_TICK_STRUCT ticks;
   _mqx_uint       result;
   _mqx_uint       n;

   _time_init_ticks(&ticks, 10);

   result = _watchdog_create_component(BSP_TIMER_INTERRUPT_VECTOR, 
      handle_watchdog_expiry);
   if (result != MQX_OK) {
      printf("\nError creating watchdog component.");
      _mqx_exit(0);
   }

   n = 100;
   while (TRUE) {
      result = _watchdog_start_ticks(&ticks);
      n = waste_time(n);
      _watchdog_stop();
      printf("\n %d", n);
   }

}

/* EOF */

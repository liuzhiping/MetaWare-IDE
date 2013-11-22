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
*** File: isr.c
***
*** Comments: 
***    This example installs a new Interrupt Service Routine in place
***    of the existing Timer ISR. The new ISR calls the timer ISR. The
***    example then delays for 200 ticks and then prints the tick count.
***
*** Expected Output:
*** Tick count = 20
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>

#define MAIN_TASK 10
extern void main_task(uint_32);
extern void new_tick_isr(pointer);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   { MAIN_TASK, main_task, 2000, 8, "Main",
      MQX_AUTO_START_TASK, 0, 0 },
   { 0L,        0,         0,    0, 0,
      0,                   0, 0}
};

typedef struct my_isr_struct
{
   pointer               OLD_ISR_DATA;
   void      (_CODE_PTR_ OLD_ISR)(pointer);
   _mqx_uint             TICK_COUNT;
} MY_ISR_STRUCT, _PTR_ MY_ISR_STRUCT_PTR;

/*ISR*-----------------------------------------------------------
*
* ISR Name : new_tick_isr
* Comments :
*   This ISR replaces the existing timer ISR, then calls the 
*   old timer ISR.
*END*-----------------------------------------------------------*/

void new_tick_isr
   (
      pointer user_isr_ptr
   )
{
   MY_ISR_STRUCT_PTR  isr_ptr;

   isr_ptr = (MY_ISR_STRUCT_PTR)user_isr_ptr;
   isr_ptr->TICK_COUNT++;

   /* Chain to the previous notifier */
   (*isr_ptr->OLD_ISR)(isr_ptr->OLD_ISR_DATA);
}

/*TASK*----------------------------------------------------------
*
* Task Name : main_task
* Comments  : 
*   This task installs a new ISR to replace the timer ISR.
*   It then waits for some time, finally printing out the
*   number of times the ISR ran.
*END*-----------------------------------------------------------*/

void main_task
   (
      uint_32 initial_data
   )
{
   MY_ISR_STRUCT_PTR  isr_ptr;

   isr_ptr = _mem_alloc_zero((_mem_size)sizeof(MY_ISR_STRUCT));

   isr_ptr->TICK_COUNT   = 0;
   isr_ptr->OLD_ISR_DATA =
      _int_get_isr_data(BSP_TIMER_INTERRUPT_VECTOR);
   isr_ptr->OLD_ISR      =
      _int_get_isr(BSP_TIMER_INTERRUPT_VECTOR);

   _int_install_isr(BSP_TIMER_INTERRUPT_VECTOR, new_tick_isr,
      isr_ptr);

   _time_delay_ticks(20);

   printf("\nTick count = %d\n", isr_ptr->TICK_COUNT);

   _mqx_exit(0);

}

/* EOF */

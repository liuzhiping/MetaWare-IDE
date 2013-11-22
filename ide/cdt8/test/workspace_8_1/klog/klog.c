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
*** File: klog.c
***
*** Comments: 
***    This example enables kernel logging, waits for a long period of 
***    time and then displays the log results.
***
*** Expected Output:
***
*** Kernel log contains:
*** 1. 0x00000000:1BD4B -> FUN       _time_delay_ticks 0x0 0x0 0x0 0x0 0xE64 
*** 2. 0x00000000:1C08C -> XFUN      _time_delay_ticks 0x0 0x0 0x0 0x0 0x0 
*** 3. 0x00000000:1C2D8 -> FUN       _time_delay_ticks 0x5 0x0 0x0 0x0 0xE64 
*** 4. 0x00000000:1C713 -> NEW TASK TD 0xE128C ID 0x10001 STATE 0x2 STACK 0xE1830
*** 5. 0x00000001:03B4 -> INT   0x3
*** 6. 0x00000001:07F2 -> INT   0x3 END
*** 7. 0x00000002:018E -> INT   0x3
*** 8. 0x00000002:03EB -> INT   0x3 END
*** 9. 0x00000003:0188 -> INT   0x3
*** 10. 0x00000003:03E5 -> INT   0x3 END
*** 11. 0x00000004:0188 -> INT   0x3
*** 12. 0x00000004:03E5 -> INT   0x3 END
*** 13. 0x00000005:0188 -> INT   0x3
*** 14. 0x00000005:0527 -> INT   0x3 END
*** 15. 0x00000005:0733 -> NEW TASK TD 0xE18F8 ID 0x10002 STATE 0x2 STACK 0xE2884
*** 16. 0x00000005:09CB -> XFUN      _time_delay_ticks 0x0 0x0 0x0 0x0 0x0 
*** 17. 0x00000005:0BE0 -> FUN       _time_delay_ticks 0xA 0x0 0x0 0x0 0xE64 
*** 18. 0x00000005:0E3B -> NEW TASK TD 0xE128C ID 0x10001 STATE 0x2 STACK 0xE182C
*** 19. 0x00000006:0188 -> INT   0x3
*** 20. 0x00000006:03F7 -> INT   0x3 END
*** 21. 0x00000007:0188 -> INT   0x3
*** 22. 0x00000007:03EC -> INT   0x3 END
*** 23. 0x00000008:0188 -> INT   0x3
*** 24. 0x00000008:03E5 -> INT   0x3 END
*** 25. 0x00000009:0188 -> INT   0x3
*** 26. 0x00000009:03E5 -> INT   0x3 END
*** 27. 0x0000000A:0188 -> INT   0x3
*** 28. 0x0000000A:03E5 -> INT   0x3 END
*** 29. 0x0000000B:0188 -> INT   0x3
*** 30. 0x0000000B:03E5 -> INT   0x3 END
*** 31. 0x0000000C:0189 -> INT   0x3
*** 32. 0x0000000C:03E6 -> INT   0x3 END
*** 33. 0x0000000D:0188 -> INT   0x3
*** 34. 0x0000000D:03E5 -> INT   0x3 END
*** 35. 0x0000000E:0189 -> INT   0x3
*** 36. 0x0000000E:03E6 -> INT   0x3 END
*** 37. 0x0000000F:0188 -> INT   0x3
*** 38. 0x0000000F:04AD -> INT   0x3 END
***
*** Repeats with log data for several hundred lines
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <log.h>
#include <klog.h>

extern void main_task(uint_32 initial_data);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{
   { 10, main_task,  2000,  8, "Main",
      MQX_AUTO_START_TASK, 0, 0},
   { 0,         0,   0,     0, 0,
      0,                   0, 0}
};

/*TASK*----------------------------------------------------------
*
* Task Name : main_task
* Comments  :
*   This task logs timer interrupts to the kernel log,
*   then prints out the log.
*END*-----------------------------------------------------------*/

void main_task
   (
      uint_32 initial_data
   )
{
   _mqx_uint result;
   _mqx_uint i;

   /* Create the kernel log */
   result = _klog_create(4096, 0);
   if (result != MQX_OK) {
      printf("Main task: _klog_create failed");
      _mqx_exit(0);
   }

   /* Enable kernel logging */
   _klog_control(KLOG_ENABLED | KLOG_CONTEXT_ENABLED |
      KLOG_INTERRUPTS_ENABLED| KLOG_SYSTEM_CLOCK_INT_ENABLED |
      KLOG_FUNCTIONS_ENABLED | KLOG_TIME_FUNCTIONS |
      KLOG_INTERRUPT_FUNCTIONS, TRUE);

   /* Write data into kernel log */   
   for (i = 0; i < 10; i++) {
      _time_delay_ticks(5 * i);
   }

   /* Disable kernel logging */
   _klog_control(0xFFFFFFFF, FALSE);

   /* Read data from the kernel log */
   printf("\nKernel log contains:\n");
   while (_klog_display()){
   }

   _mqx_exit(0);

}

/* EOF */

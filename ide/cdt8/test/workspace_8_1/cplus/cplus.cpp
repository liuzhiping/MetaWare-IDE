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
*** File: cplus.cc
***
*** Comments: 
***    This file contains the source for the cplus example program.
***
***    This program constructs and destructs a global, local and
***    heap instance of an object.
***
*** Expected Output:
*** global: Constructed OK
*** local: Constructed OK
*** heap: Constructed OK
*** heap: deallocation  
*** local: deallocation  
*** global: deallocation   
*** 
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>

/* Task IDs */
#define CPLUS_TASK 5

extern void cplus_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] = 
{ 
    {CPLUS_TASK, cplus_task, 2000, 5, "cplus", MQX_AUTO_START_TASK, 0, 0},
    {0,          0,          0,    0, 0,       0,                   0, 0}
};

#ifdef __CODEWARRIOR__ /* Override Codewarrior C++ runtime function */
void __throw_bad_alloc(void){_mqx_fatal_error(MQX_OUT_OF_MEMORY);}
#endif

class HelloWorld {
private:
   int check_init;
   const char *id;
public:
   HelloWorld() {
      check_init = 0x1234567;
   }
   ~HelloWorld() {
      _io_printf("%s: deallocation\n",id);
   }
   void print(const char *x) {
      id = x;
      if (check_init == 0x1234567) {
	 _io_printf("%s: Constructed OK\n",id);
      } else {
	 _io_printf("%s: Constructor not called\n",id);
      }
   }
};

HelloWorld global;

/*TASK*-----------------------------------------------------
* 
* Task Name    : cplus_task
* Comments     :
*    This task prints the state of each object
*
*END*-----------------------------------------------------*/

void cplus_task
   (
      uint_32 initial_data
   )
{
   { // Scope for local to destruct
      HelloWorld local;
      HelloWorld *heap;

      global.print("global");
      local.print("local");
      heap = new HelloWorld;
      if (heap != 0) {
         heap->print("heap");
         delete heap;
      } else {
         _io_printf("heap: new failed\n");
      } /* Endif */
   } // local should destruct

   _io_fflush(stdout);
   _mqx_exit(0);
}

/* EOF */

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
*** File: cplus2.cpp
***
*** Comments:
***    This file contains the source for a simple C++ program that
*** allocates extra variable storage space in each task control block
*** (td_struct).  You can think of it as a way for each task to have
*** it's own local storage for a var (or many vars).  It can get very
*** expensive if the space is large and there are many tasks.
***
***    This program also verifies that C++ constructors get called after
*** the MQX kernel gets initialized.  Usually C++ constructors are
*** called from _bsp_enable_card().  If _task_reserve_space() is called
*** too early then it will return an error code without allocating any
*** space for the storage.
***
*** Expected Output:
***
*** reserved base: 0x000e1870
***       offset1:          0
***       offset1:          2
***       offset1:          4
***       offset1:          8
*** PASEED
***
***************************************************************************
*END**********************************************************************/

#include <mqx.h>
#include <bsp.h>
#include <fio.h>

/* Task IDs */
#define CPLUS2_TASK 5

extern void cplus2_task(uint_32);

TASK_TEMPLATE_STRUCT  MQX_template_list[] =
{
    {CPLUS2_TASK, cplus2_task, 800, 5, "cplus2", MQX_AUTO_START_TASK, 0, 0},
    {0,           0,           0,   0, 0,        0,                   0, 0}
};

class ReserveBeforeTaskCreation {
public:
   _mqx_uint offset1, offset2, offset3, offset4;
   ReserveBeforeTaskCreation() {
      // Calls to _task_reserve_space MUST be made
      // before the 1st task is created.
      offset1 = _task_reserve_space(1);
      offset2 = _task_reserve_space(2);
      offset3 = _task_reserve_space(4);
      offset4 = _task_reserve_space(8);
   }
};

ReserveBeforeTaskCreation reserved_space;

#undef  assert
#define assert(b)       \
  if(!(b)){_io_printf(__FILE__":%d failed\n",__LINE__); \
  _mqx_fatal_error(1);}

/*TASK*-----------------------------------------------------
*
* Task Name    : cplus2_task
* Comments     :
*    This task prints the state of each object
*
*END*-----------------------------------------------------*/

void cplus2_task
   (
      uint_32 initial_data
   )
{
   uchar_ptr  base;

   // This address is the beginning of memory reserved for this
   // unique task.  Each task will get its own reserved memory.
   base = (uchar_ptr) _task_get_reserved_base();

   _io_printf("Reserved base: 0x%08x\n", base);

   // Verify the constructor executed correctly
   assert(reserved_space.offset1 < 64);
   assert(reserved_space.offset2 > reserved_space.offset1);
   assert(reserved_space.offset3 > reserved_space.offset2);
   assert(reserved_space.offset4 > reserved_space.offset3);
   assert(reserved_space.offset4 < 64);

   // Here's how you calculate the var addresses
   volatile uchar   _PTR_ var1 = (volatile uchar   _PTR_)(base+reserved_space.offset1);
   volatile uint_16 _PTR_ var2 = (volatile uint_16 _PTR_)(base+reserved_space.offset2);
   volatile uint_32 _PTR_ var3 = (volatile uint_32 _PTR_)(base+reserved_space.offset3);
   volatile uint_64 _PTR_ var4 = (volatile uint_64 _PTR_)(base+reserved_space.offset4);

   // The storage for these vars should be valid and initialized to zero
   assert(*var1 == 0);
   assert(*var2 == 0);
   assert(*var3 == 0);
   assert(*var4 == 0);

   // Note that offsets should be aligned similar to malloc
   _io_printf("      offset1: %10d\n", reserved_space.offset1);
   _io_printf("      offset2: %10d\n", reserved_space.offset2);
   _io_printf("      offset3: %10d\n", reserved_space.offset3);
   _io_printf("      offset4: %10d\n", reserved_space.offset4);
   _io_printf("PASSED\n");

   _mqx_exit(0);
}

/* EOF */

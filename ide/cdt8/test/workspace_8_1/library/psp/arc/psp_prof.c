/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: arc.c
***
*** Comments:      
***   This file contains utiltity functions for use with an ARC cpu.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

uint_32 _mwprof_countdown_ticks = 0;
uint_32 _mwprof_countdown_freq  = 0;

uint_32 _psp_prof_enabled = 0;
uint_32 _psp_prof_counter = 0;
uint_32 _psp_prof_countdown_ticks = 0;
uint_32 _psp_prof_timer_wrap      = 0;
uint_32 _psp_prof_timer_tcontrol  = 0;
uint_32 _psp_prof_timer_tcount    = 0;
uint_32 _psp_prof_timer_tlimit    = 0;
uint_32 _psp_prof_vector_number   = 0;
void  (_CODE_PTR_ _psp_prof_old_isr)(void);

extern void _psp_prof_isr1(void);
extern void _psp_prof_isr2(void);


/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _psp_profile_init
* Returned Value   : none
* Comments         :
*  Initilize the support functions for the metaware profiler.
*
*END*------------------------------------------------------------------------*/

void _psp_profile_init
   (
      /* [IN] what speed is the CPU running at */
      uint_32 system_clock,
      
      /* [IN] how many microseconds for the profiler ISR */
      uint_32 profile_usec,
      
      /* [IN] how many milliseconds for the BSP timer ISR */
      uint_32 bsp_msec,
      
      /* [IN] at how many ticks does the timer wrap */
      uint_32 timer_wrap,
      
      /* [IN] the timer tcontrol register */
      uint_32 timer_tcontrol,
      
      /* [IN] the timer tcount register */
      uint_32 timer_tcount,
      
      /* [IN] if non-zero, the tlimit register */
      uint_32 timer_tlimit,

      /* [IN] the timer interrupt vector number */
      uint_32 timer_vector
   )
{ /* Body */
    
   _psp_prof_enabled = 0;
   _psp_prof_counter = 0;
   _psp_prof_timer_wrap      = timer_wrap;
   _psp_prof_timer_tcontrol  = timer_tcontrol;
   _psp_prof_timer_tcount    = timer_tcount;
   _psp_prof_timer_tlimit    = timer_tlimit;
   _psp_prof_vector_number   = timer_vector;
   _psp_prof_countdown_ticks = 
      system_clock / (1000L / bsp_msec);

   _mwprof_countdown_freq = system_clock;
   _mwprof_countdown_ticks = 
      system_clock / (1000000L / profile_usec);

   _psp_prof_old_isr = _int_install_kernel_isr(
      _psp_prof_vector_number, _psp_prof_isr1);

   _psp_prof_enabled = 0;
   _psp_prof_counter = 0;

   /* Stop timer */
   _psp_set_aux(timer_tcount,0);
   _psp_set_aux(timer_tcontrol,0);
   _psp_set_aux(_psp_prof_timer_tcount,
         _psp_prof_timer_wrap - _mwprof_countdown_ticks);
   _psp_set_aux(_psp_prof_timer_tcontrol, 3);

   _psp_prof_old_isr = _int_install_kernel_isr(
      _psp_prof_vector_number, _psp_prof_isr1);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _mwprofile_clock_freq
* Returned Value   : uint_32 clock frequency
* Comments         :
*  Support function used by MW profiling library
*
*END*------------------------------------------------------------------------*/

uint_32 _mwprofile_clock_freq
   (
      void
   )
{ /* Body */

    return(_mwprof_countdown_freq/_mwprof_countdown_ticks);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _mwstop_profile_clock
* Returned Value   : none
* Comments         :
*   This function used by application to stop profiling
*
*END*------------------------------------------------------------------------*/

void _mwstop_profile_clock
   (
      void
   )
{ /* Body */

   _int_disable();
   if (!_psp_prof_enabled)  {
      _psp_prof_enabled = 0;
      _psp_prof_counter = 0;
      /* Stop timer */
      _psp_set_aux(_psp_prof_timer_tcount,0);
      _psp_set_aux(_psp_prof_timer_tcontrol,0);
   } /* Endif */
   _int_enable();    

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _mwstart_profile_clock
* Returned Value   : none
* Comments         :
*   This function used by application to start profiling
*
*END*------------------------------------------------------------------------*/

void _mwstart_profile_clock
   (
      void
   )
{ /* Body */

   _int_disable();
   if (!_psp_prof_enabled)  {
      _psp_prof_counter = 0;
      /* Stop timer */
      _psp_set_aux(_psp_prof_timer_tcount,0);
      _psp_set_aux(_psp_prof_timer_tcontrol,0);
      _psp_set_aux(_psp_prof_timer_tcount,
         _psp_prof_timer_wrap - _mwprof_countdown_ticks);
      _psp_set_aux(_psp_prof_timer_tcontrol, 3);
      _psp_prof_enabled = 1;
   } /* Endif */
   _int_enable();    

} /* Endbody */

/* EOF */

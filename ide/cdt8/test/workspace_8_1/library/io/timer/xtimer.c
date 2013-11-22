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
*** File: xtimer.c
***
*** Comments:      
***   This file contains the functions for dealing with the xtimer device.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "mem_prv.h"
#include "mqx_prv.h"

/*FUNCTION****************************************************************
* 
* Function Name    : _xtimer_init
* Returned Value   : uint_32 value to pass to _xtimer_get_usec
* Comments         :
*    This routine initializes the timer.
* 
*END**********************************************************************/

uint_32 _xtimer_init
   (
      /* [IN] the address of the timer device */
      volatile XTIMER_STRUCT _PTR_  timer_ptr,

      /* [IN] the interrupt vector to install the device onto */
      uint_32                       int_vector,

      /* [IN] the tick rate desired (in ms) */
      uint_32                       tick_rate

   )
{ /* Body */
   uint_32 ref = 0;

   ref = tick_rate * 1000;
   timer_ptr->CONTROL  = 0; /* Stop the timer */
   timer_ptr->PRESCALE = 16;
   timer_ptr->PRELOAD  = (uint_16)ref;
   timer_ptr->IRQVECT  = (uchar)int_vector;
   timer_ptr->CONTROL  = XTIMER_CONTROL_RESET | XTIMER_CONTROL_ENABLE |
      XTIMER_CONTROL_RESTART | XTIMER_CONTROL_SOFTWARE_ACK | 1;

   return ref;

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _xtimer_int_available
* Returned Value   : boolean TRUE or FALSE
* Comments         :
*    This routine initializes the timer.
* 
*END**********************************************************************/

boolean _xtimer_int_available
   (
      /* [IN] the address of the timer device */
      volatile XTIMER_STRUCT _PTR_  timer_ptr
   )
{ /* Body */

   return (timer_ptr->STATUS & 0x80);

} /* Endbody */


/*FUNCTION****************************************************************
* 
* Function Name    : _xtimer_get_microseconds
* Returned Value   : uint_32 microseconds
* Comments         :
*    This routine returns the number of microseconds that the hardware
* timer has ticked since the last interrupt.
* 
*END**********************************************************************/

uint_32 _xtimer_get_microseconds
   (
      /* [IN] the address of the timer device */
      volatile XTIMER_STRUCT _PTR_  timer_ptr,

      /* [IN] the value returned by _xtimer_init */
      uint_32                       ref_value,

      /* [IN] the programming tick range of the timer in ms */
      uint_32                       alarm_resolution
   )
{ /* Body */
   uint_32 result;
   uint_32 count;
#if MQX_KERNEL_LOGGING
   uint_32 status;
   KERNEL_DATA_STRUCT_PTR kernel_data;
#endif

   count = (uint_32)timer_ptr->COUNT;
   if (timer_ptr->STATUS & 0x80) {
      /* the timer MUST be read here again to avoid a hw race */
      count = (uint_32)timer_ptr->COUNT;

      result = alarm_resolution * 1000;
      /* For this hw, reading the status turns off the interrupt,
      ** thus we need to service the interrupt here
      */

#if MQX_KERNEL_LOGGING
      _GET_KERNEL_DATA(kernel_data);
      status = kernel_data->LOG_CONTROL;
      kernel_data->LOG_CONTROL = 0;
#endif

      _time_notify_kernel();

#if MQX_KERNEL_LOGGING
      kernel_data->LOG_CONTROL = status;
#endif

   } else {
      result = 0;
   } /* Endif */

   result += (ref_value - count);

   return result;

} /* Endbody */

/* EOF */

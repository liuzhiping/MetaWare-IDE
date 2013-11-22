/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: _timer_isr.c
***
*** Comments:      
***   This file contains the interrupt service routine for timer 1. 
***    This file is added to address CR 2396.
***                                                                
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*ISR*********************************************************************
* 
* Function Name    : _timer1_isr
* Returned Value   : void
* Comments         :
*    The timer ISR is the interrupt handler for the clock tick.
* 
*END**********************************************************************/

void _timer1_isr
   (
      pointer dummy
   )
{ /* Body */

   /* Clear the interrupt */
   _psp_set_aux(PSP_AUX_TCONTROL1, 3);


} /* Endbody */
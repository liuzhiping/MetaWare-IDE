/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International.
***
*** File: initide.c
***
*** Comments:
***    This file contains the IDE initialization functions. This file is 
***    added to address CR 2283.
***
**************************************************************************
*END*********************************************************************/

#include "mqx.h"
#include "bsp.h"
#include "bsp_prv.h"
#include <io_ideprv.h>

#if BSP_USE_IDE

/* 
** Timing parameters. These are the figures taken from 
** "Aurora IDE Interface Implementation Specification".
** This is arranged as an array of 7 parameters for each of the
** 5 modes ARC supports. The individual timing parameters are
** in turn:
**  - T0  Minimum PIO cycle time
**  - T1  Minimum addr setup before nDIOR/W
**  - T2  Minimum nDIOR/W pulse width
**  - T2L Minimum nDIOR/W recovery time (modes 3&4 only)
**  - T3  Minimum write data setup time
**  - T4  Minimum write data hold time
**  - T5  Minimum read data setup time
**  - T6  Minimum read data hold data
**
** Note: All figures are in nanoseconds. We are only interstead in first 4.
*/
static uint_32 timings[5][8] = 
{	
    /* T0, T1,  T2, T2L, T3, T4, T5, T6 */	
	{  600, 70, 165,   0, 60, 30, 50, 5 },
	{  383, 50, 125,   0, 45, 20, 35, 5 },
	{  240, 30, 100,   0, 30, 15, 20, 5 },
	{  180, 30,  80,  70, 30, 10, 20, 5 },
	{  120, 25,  70,  25, 20, 10, 20, 5 }
}; 

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_ide_pio_mode
* Returned Value   : none
* Comments         :
*    Routine to tune the timing of PIO mode for IDE devices.
*
*END*----------------------------------------------------------------------*/

void _bsp_ide_pio_mode
(
   /* [IN] PIO mode */
   uint_16 pio_mode
)
{ /* Body */

   IDE_CONTROL_STRUCT_PTR ide_ptr = (IDE_CONTROL_STRUCT_PTR)BSP_IDE_CONTROLLER_BASE;
   volatile uint_32       t0,t1,t2,t2l, clk_period;
   volatile double        cycles_per_nanosec;

   cycles_per_nanosec = ((double)BSP_SYSTEM_CLOCK)/1000000000.0;
   
   /* Calculate T0, T1, T2 and TE (T2L) */	   
   t0  = (uint_32) (cycles_per_nanosec * ((double)(timings[pio_mode][IDE_TIMING_T0])));
   t1  = (uint_32) (cycles_per_nanosec * ((double)(timings[pio_mode][IDE_TIMING_T1])));
   t2  = (uint_32) (cycles_per_nanosec * ((double)(timings[pio_mode][IDE_TIMING_T2])));
   t2l = (uint_32) (cycles_per_nanosec * ((double)(timings[pio_mode][IDE_TIMING_T2L])));

   /*
   ** T1 + T2 + T2L must be more than the T0 minimum value 
   ** specified for the particular PIO mode. 
   */
   
   while ((t1+t2+t2l) <= t0) { 
      t2++;
      if ((t1+t2+t2l) <= t0) 
         t2l++; 
      if ((t1+t2+t2l) <= t0) 
         t1++; 
   } /* Endwhile */

   /* Set the controller PIO mode read/write timing register */
   ide_ptr->PIO_SETUP = ((((t2l) & 0xff) << 16) | (((t2) & 0xff) << 8) | ((t1) & 0xff));

} /* Endbody */ 

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_ide_reset
* Returned Value   : none
* Comments         :
*     Reset the IDE controller before a disk reset.
*
*END*----------------------------------------------------------------------*/

void _bsp_ide_reset(void)
{ /* Body */

   IDE_CONTROL_STRUCT_PTR  ide_ptr;
   uint_16                 us, us_start;

   ide_ptr = (IDE_CONTROL_STRUCT_PTR)BSP_IDE_CONTROLLER_BASE;

   /* Set the PIO timing settings to a dummy number */
   ide_ptr->PIO_SETUP = 0x00505050;

   /* Assert the controller */
   ide_ptr->STAT_CTRL &= ~BSP_IDE_STATCTRL_RS;

   /* Wait for 20 millisecond */   
   us_start = _time_get_microseconds();
   do {
      us = _time_get_microseconds();
   } while (((uint_32)(us - us_start) * 1000) < 20000);

   /* Take the controller out off reset */
   ide_ptr->STAT_CTRL = BSP_IDE_STATCTRL_RS;

   /* Wait for 20 millisecond */
   us_start = _time_get_microseconds();
   do {
      us = _time_get_microseconds();
   } while (((uint_32)(us - us_start) * 1000) < 20000);

   /* Clear interrupt for the controller */
   if(ide_ptr->STAT_CTRL & BSP_IDE_STATCTRL_IS) {
      ide_ptr->STAT_CTRL |= BSP_IDE_STATCTRL_IC; 
   } /* Endif */

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_ide_enable_int
* Returned Value   : none
* Comments         :
*    Enable interrupt for the IDE controller.
*
*END*----------------------------------------------------------------------*/

void _bsp_ide_enable_int
(
      /* [IN] Interrupt vector to use */
      uint_32  vector
)
{ /* Body */

   uint_32 reg;
   IDE_CONTROL_STRUCT_PTR  ide_ptr;

   ide_ptr = (IDE_CONTROL_STRUCT_PTR)BSP_IDE_CONTROLLER_BASE;

   /* Set controller's interrupt trigger register */
   reg = _psp_get_aux(PSP_AUX_ITRIGGER);
   reg |= (1 << vector);
   _psp_set_aux(PSP_AUX_ITRIGGER, reg);

   /* Enable controller interrupt to the CPU */
   reg = _psp_get_aux(PSP_AUX_IENABLE);
   reg |= (1 << vector);
   _psp_set_aux(PSP_AUX_IENABLE, reg);

   /* Enable controller's interrupt */
   ide_ptr->STAT_CTRL |= BSP_IDE_STATCTRL_IE;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_ide_disable_int
* Returned Value   : none
* Comments         :
*    Disable interrupt for the IDE controller.
*
*END*----------------------------------------------------------------------*/

void _bsp_ide_disable_int
(
      /* [IN] Interrupt vector to use */
      uint_32  vector
)
{ /* Body */

   uint_32 reg;
   IDE_CONTROL_STRUCT_PTR  ide_ptr;

   ide_ptr = (IDE_CONTROL_STRUCT_PTR)BSP_IDE_CONTROLLER_BASE;

   /* Disable controller interrupt to the CPU */
   reg = _psp_get_aux(PSP_AUX_IENABLE);
   reg &= ~(1 << vector);
   _psp_set_aux(PSP_AUX_IENABLE, reg);

   /* Disable controller's interrupt */
   ide_ptr->STAT_CTRL &= ~BSP_IDE_STATCTRL_IE;

} /* Endbody */

/*ISR*-----------------------------------------------------------------------
* 
* Function Name    : _bsp_ide_isr
* Returned Value   : MQX_OK or error code
* Comments         :
*    Interrupt handler routin for the IDE.
*
*END*----------------------------------------------------------------------*/

void _bsp_ide_isr
(
      /* [IN] The Notifier Data for this interrupt */
      pointer notifier_data
)
{ /* Body */

   IDE_CONTROL_STRUCT_PTR  ide_ptr = (IDE_CONTROL_STRUCT_PTR)BSP_IDE_CONTROLLER_BASE;

   /* Clear the interrupt */
   if(ide_ptr->STAT_CTRL & BSP_IDE_STATCTRL_IS) {
      ide_ptr->STAT_CTRL |= BSP_IDE_STATCTRL_IC; 
   } /* Endif */

} /* Endbody */ 


#endif /* BSP_USE_IDE */

/* EOF */
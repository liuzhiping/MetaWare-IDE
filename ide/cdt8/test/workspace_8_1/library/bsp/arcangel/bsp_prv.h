#ifndef __bsp_prv_h__
#define __bsp_prv_h__ 1
/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: bsp_prv.h
***
*** Comments:      
***   This file contains the definitions of constants and structures
***   required for initialization of the card, private to the bsp.
***
**************************************************************************
*END*********************************************************************/

#if BSP_LEGACY_TIMER
#define BSP_TIMER_WRAP      (0x01000000)
#define BSP_TIMER_WRAP_MASK (0x00FFFFFF)
#define BSP_TCOUNT     PSP_AUX_TCOUNT0
#define BSP_TCONTROL   PSP_AUX_TCONTROL0
#define BSP_TLIMIT     0

#else
#define BSP_TIMER_WRAP      (0xFFFFFFFF)
#define BSP_TIMER_WRAP_MASK (0xFFFFFFFF)
#if BSP_TIMER==0
#define BSP_TCOUNT   PSP_AUX_TCOUNT0
#define BSP_TCONTROL PSP_AUX_TCONTROL0
#define BSP_TLIMIT   PSP_AUX_TLIMIT0
#else
#define BSP_TCOUNT   PSP_AUX_TCOUNT1
#define BSP_TCONTROL PSP_AUX_TCONTROL1
#define BSP_TLIMIT   PSP_AUX_TLIMIT1
#endif

#endif


#define BSP_HW_TICKS_PER_INTERRUPT \
   (BSP_TIMER_FREQUENCY / BSP_ALARM_FREQUENCY)

#define BSP_STARTING_COUNTUP_TIMER_VALUE \
   (BSP_TIMER_WRAP - BSP_HW_TICKS_PER_INTERRUPT)

#ifdef BSP_USE_FIXED_CLOCK_RATE
#define BSP_HW_TICKS_PER_TICK(x)    BSP_HW_TICKS_PER_INTERRUPT
#define BSP_TIMER_REFERENCE(x)      BSP_STARTING_COUNTUP_TIMER_VALUE
#define BSP_TIMER_DRIFT_CORRECTION  3
#else
#define BSP_HW_TICKS_PER_TICK(x)    (x)->HW_TICKS_PER_TICK
#define BSP_TIMER_REFERENCE(x)      (x)->TIMER_HW_REFERENCE
#define BSP_TIMER_DRIFT_CORRECTION  5
#endif

#ifdef __cplusplus
extern "C" {

extern void _init(void);
extern void _fini(void);
#endif

extern uint_32 _bsp_get_hwticks(pointer);

extern void _bsp_exit_handler(void);
extern void _bsp_stop_all_ints(void);
extern void _bsp_timer_isr(pointer);
extern void _bsp_soft_int_clear(void);

/* Call back to MW second stage initialization of run-time */
extern void _mwrtl_init(void);

/* Start CR 2283 */
/* IDE interface */
extern void _bsp_ide_enable_int(uint_32);
extern void _bsp_ide_disable_int(uint_32);
extern void _bsp_ide_reset(void);
extern void _bsp_ide_pio_mode(uint_16);
extern void _bsp_ide_isr(pointer);
/* End CR 2283 */

/* Start CR 2396 */
/* PMU */
#if MQX_USE_PMU
extern uint_32 _bsp_init_pmu(void);
extern uint_32 _bsp_init_dvfs(void);
extern void _timer1_isr(pointer);
#endif
/* End CR 2396 */

/* 
** I/O initialization controlled by initialization structures for each
** channel
*/
extern MW_SIMUART_INIT_STRUCT    _bsp_uart_init;
extern VUART_SERIAL_INIT_STRUCT  _bsp_vuart1_init;
extern VUART_SERIAL_INIT_STRUCT  _bsp_vuart2_init;
extern VUART_SERIAL_INIT_STRUCT  _bsp_vuart3_init;
extern VUART_SERIAL_INIT_STRUCT  _bsp_vuart4_init;
extern VUART_SERIAL_INIT_STRUCT  _bsp_vuart5_init;
extern VUART_SERIAL_INIT_STRUCT  _bsp_vuart6_init;
extern VUART_SERIAL_INIT_STRUCT  _bsp_vuart7_init;
extern VUART_SERIAL_INIT_STRUCT  _bsp_vuart8_init;

extern FLASHX_INIT_STRUCT        _bsp_flashx_init;
#ifdef __cplusplus
}
#endif

#endif
/* EOF */

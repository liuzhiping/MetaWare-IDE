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
*** File: init_bsp.c
***
*** Comments:      
***   This file contains the functions for initializing the Board
*** Support Package.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "bsp.h"
#include "bsp_prv.h"

#include "io_rev.h"
#include "bsp_rev.h"

/* Start CR 2403 */
/* #define BSP_TRAP_EXCEPTIONS	1 */
/* End CR 2403 */

const char _PTR_ _mqx_bsp_revision = REAL_NUM_TO_STR(BSP_REVISION);
const char _PTR_ _mqx_io_revision  = REAL_NUM_TO_STR(IO_REVISION);

#if BSP_ARC4
volatile uint_32 _bsp_shm_int_reg = 0xFFFFFFFF;
#endif

/* Start CR 2396 */
#if MQX_USE_PMU
#include "pmu.h"
static uint_32 pmu_ticks_per_tick = 0;
#endif
/* End CR 2396 */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_enable_card
* Returned Value   : _mqx_uint result
* Comments         :
*   This function sets up operation of the card.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _bsp_enable_card
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   PSP_SUPPORT_STRUCT_PTR support_ptr;
   _mqx_uint              result;

   _bsp_stop_all_ints();
#if BSP_ARC4
   _bsp_soft_int_clear();
#endif

   _int_set_vector_table(0);

   kernel_data = _mqx_get_kernel_data();

   /* Set the CPU type */
   kernel_data->CPU_TYPE = BSP_CPU_TYPE;

   /* Set the bsp exit handler, called by _mqx_exit */
   kernel_data->EXIT_HANDLER = _bsp_exit_handler;

   /* Initialize support services */
   _arc_initialize_support();

   /* Initialize the interrupt handling */
   result = _int_init(BSP_FIRST_INTERRUPT_VECTOR_USED,
      BSP_LAST_INTERRUPT_VECTOR_USED);
   if (result != MQX_OK) {
      return result;
   } /* Endif */

   _psp_int_install();

   /* when debugging it's nice to display exceptions */
/* Start CR 2403 */
/*
#if BSP_TRAP_EXCEPTIONS
   _int_install_verbose_unexpected_isr();
#endif
*/
/* End CR 2403 */

   /* Initialize the timer interrupt */
   kernel_data->SYSTEM_CLOCK_INT_NUMBER = BSP_TIMER_INTERRUPT_VECTOR;

   kernel_data->TIMER_HW_REFERENCE = BSP_STARTING_COUNTUP_TIMER_VALUE;
   kernel_data->TICKS_PER_SECOND   = BSP_ALARM_FREQUENCY;
   kernel_data->HW_TICKS_PER_TICK  = BSP_HW_TICKS_PER_INTERRUPT;
   kernel_data->GET_HWTICKS        = _bsp_get_hwticks;
   kernel_data->GET_HWTICKS_PARAM  = NULL;

   /* Install the MQX timer interrupt handler */
   if (_int_install_isr(BSP_TIMER_INTERRUPT_VECTOR, _bsp_timer_isr,
      (pointer)0) == NULL)
   {
      return MQX_TIMER_ISR_INSTALL_FAIL;
   } /* Endif */

   /* Initialize the timer */
#ifdef BSP_TIMER
   _psp_set_int_level(BSP_TIMER_INTERRUPT_VECTOR, BSP_TIMER_INTERRUPT_LEVEL);
   _psp_set_aux(BSP_TCONTROL,0);
#ifndef BSP_LEGACY_TIMER
   _psp_set_aux(BSP_TLIMIT,BSP_TIMER_WRAP);
   _psp_set_aux(PSP_AUX_TCONTROL0,0);
   _psp_set_aux(PSP_AUX_TCONTROL1,0);
#endif
   _psp_set_aux(BSP_TCOUNT,BSP_TIMER_REFERENCE(kernel_data));
   _psp_set_aux(BSP_TCONTROL,3);
#endif

#ifdef __PROFILE__
   _psp_profile_init(BSP_SYSTEM_CLOCK, BSP_PROFILE_RESOLUTION, 
      BSP_ALARM_RESOLUTION, BSP_TIMER_WRAP, BSP_TCONTROL,
/* Start CR 1814 */	  
      /*BSP_TCOUNT, 3, BSP_TIMER_INTERRUPT_VECTOR);*/
      BSP_TCOUNT, BSP_TLIMIT, BSP_TIMER_INTERRUPT_VECTOR);
/* End CR 1814 */
#endif

   support_ptr = (PSP_SUPPORT_STRUCT_PTR)kernel_data->PSP_SUPPORT_PTR;
   support_ptr->MS_PER_TICK = 1000 / BSP_ALARM_FREQUENCY;
   support_ptr->MS_PER_TICK_IS_INT = 
      (support_ptr->MS_PER_TICK * BSP_ALARM_FREQUENCY) == 1000;

   _time_set_ticks_per_sec(BSP_ALARM_FREQUENCY);
      
   /* Initialize the I/O Sub-system */
   result = _io_init();
   if (result != MQX_OK) {
      return result;
   } /* Endif */

   /* Install flash device driver */
#if BSP_USE_FLASH
   result = _io_flashx_install(&_bsp_flashx_init);
#endif

   /* Install device driver for console */
#ifdef BSP_VUART1_BASE   
   _vuart_serial_polled_install(BSP_VUART1_CHANNEL_NAME, &_bsp_vuart1_init,
       _bsp_vuart1_init.QUEUE_SIZE);
   _vuart_serial_int_install(BSP_VUART1_ICHANNEL_NAME, &_bsp_vuart1_init,
      _bsp_vuart1_init.QUEUE_SIZE);
#endif
#ifdef BSP_VUART2_BASE   
   _vuart_serial_polled_install(BSP_VUART2_CHANNEL_NAME, &_bsp_vuart2_init,
      _bsp_vuart2_init.QUEUE_SIZE);
   _vuart_serial_int_install(BSP_VUART2_ICHANNEL_NAME, &_bsp_vuart2_init,
      _bsp_vuart2_init.QUEUE_SIZE);
#endif
#ifdef BSP_VUART3_BASE   
   _vuart_serial_polled_install(BSP_VUART3_CHANNEL_NAME, &_bsp_vuart3_init,
      _bsp_vuart3_init.QUEUE_SIZE);
   _vuart_serial_int_install(BSP_VUART3_ICHANNEL_NAME, &_bsp_vuart3_init,
      _bsp_vuart3_init.QUEUE_SIZE);
#endif
#ifdef BSP_VUART4_BASE   
   _vuart_serial_polled_install(BSP_VUART4_CHANNEL_NAME, &_bsp_vuart4_init,
      _bsp_vuart4_init.QUEUE_SIZE);
   _vuart_serial_int_install(BSP_VUART4_ICHANNEL_NAME, &_bsp_vuart4_init,
      _bsp_vuart4_init.QUEUE_SIZE);
#endif
#ifdef BSP_VUART5_BASE   
   _vuart_serial_polled_install(BSP_VUART5_CHANNEL_NAME, &_bsp_vuart5_init,
      _bsp_vuart5_init.QUEUE_SIZE);
   _vuart_serial_int_install(BSP_VUART5_ICHANNEL_NAME, &_bsp_vuart5_init,
      _bsp_vuart5_init.QUEUE_SIZE);
#endif
#ifdef BSP_VUART6_BASE   
   _vuart_serial_polled_install(BSP_VUART6_CHANNEL_NAME, &_bsp_vuart6_init,
      _bsp_vuart6_init.QUEUE_SIZE);
   _vuart_serial_int_install(BSP_VUART6_ICHANNEL_NAME, &_bsp_vuart6_init,
      _bsp_vuart6_init.QUEUE_SIZE);
#endif
#ifdef BSP_VUART7_BASE   
   _vuart_serial_polled_install(BSP_VUART7_CHANNEL_NAME, &_bsp_vuart7_init,
      _bsp_vuart7_init.QUEUE_SIZE);
   _vuart_serial_int_install(BSP_VUART7_ICHANNEL_NAME, &_bsp_vuart7_init,
      _bsp_vuart7_init.QUEUE_SIZE);
#endif
#ifdef BSP_VUART8_BASE   
   _vuart_serial_polled_install(BSP_VUART8_CHANNEL_NAME, &_bsp_vuart8_init,
      _bsp_vuart8_init.QUEUE_SIZE);
   _vuart_serial_int_install(BSP_VUART8_ICHANNEL_NAME, &_bsp_vuart8_init,
      _bsp_vuart8_init.QUEUE_SIZE);
#endif

   /* Install device driver for console */
#ifndef MQX_NO_C_COMPILER_RUNTIME  
   _mw_simuart_serial_polled_install(BSP_SIMUART_NAME, &_bsp_uart_init,
      _bsp_uart_init.QUEUE_SIZE);
#endif

   /* Initialize the default serial I/O */
#if defined(BSP_VUART1_BASE) || defined(BSP_VUART2_BASE) || \
    defined(BSP_VUART3_BASE) || defined(BSP_VUART4_BASE) || \
    defined(BSP_VUART5_BASE) || defined(BSP_VUART6_BASE) || \
    defined(BSP_VUART7_BASE) || defined(BSP_VUART8_BASE) || \
    ! defined(MQX_NO_C_COMPILER_RUNTIME)
    /* Must have at least one serial device to call this function */
   _io_serial_default_init();
#endif

/* Start CR 2283 */
   /* Install IDE controller */
#if BSP_USE_IDE
   result = _io_ide_install("ide:", BSP_IDE_DEFAULT_DEVICE_NUM, 
                            BSP_IDE_INTERRUPT_VECTOR, BSP_IDE_INTERRUPT_LEVEL);
   if(result != MQX_OK) {
      return result;
   } /* Endif */
#endif
/* End CR 2283 */

/* Start CR 2396 */
#if MQX_USE_PMU
   result = _bsp_init_pmu();

   if(result != MQX_OK) {
      return result;
   } /* Endif */

   result = _bsp_init_dvfs();

   if(result != MQX_OK) {
      return result;
   } /* Endif */

   pmu_ticks_per_tick = kernel_data->HW_TICKS_PER_TICK;
#endif
/* End CR 2396 */

#ifdef MQX_NO_C_COMPILER_RUNTIME  
#ifdef __cplusplus
   _init();
#endif
#else
   _mwrtl_init();
#endif

   _ICACHE_ENABLE(BSP_ICACHE_MODE);
#if (BSP_USE_CODERAM)
   /* Check the Icache build config register to see if code ram is supported */
   result = _psp_get_aux(PSP_AUX_I_CACHE_BUILD) & 
      PSP_AUX_I_CACHE_BUILD_CONFIG_MASK;
   if (result == PSP_AUX_I_CACHE_BUILD_CONFIG_1_WAY_CR) {
      _psp_set_aux(PSP_AUX_CODE_RAM, BSP_CODERAM_ADDR);
   } /* Endif */
#endif

#if (BSP_USE_INT_LDST_RAM)
   result = _psp_get_aux(PSP_AUX_LDSTRAM_BUILD);
   if (result & PSP_AUX_GENERAL_BUILD_VERSION_MASK) {
      pointer loc;

      /* Calculate the size of the LD/ST RAM */
      result = (result >> 8) & 0x7;
      result = (1 << (result + 1)) * 1024;

      /* Determine current location of LD/ST RAM */
      loc = (pointer)_psp_get_aux(PSP_AUX_LOCAL_RAM);

      /* Copy existing data to LD/ST RAM */
      _mem_copy((pointer)BSP_INT_LDST_ADDR, loc, (_mem_size)result);

      /* Now overlay LD/ST RAM on top of main memory */
      _psp_set_aux(PSP_AUX_LOCAL_RAM, BSP_INT_LDST_ADDR);
   } /* Endif */
#endif
   
   _DCACHE_ENABLE(BSP_DCACHE_MODE);

   return MQX_OK;

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_exit_handler
* Returned Value   : none
* Comments         :
*    This function is called when mqx exits.  Any hardware programming
* required due to MQX exiting, should be done here.
*
*END*----------------------------------------------------------------------*/

void _bsp_exit_handler
   (
      void
   )
{ /* Body */

#if MQX_EXIT_ENABLED
   _ICACHE_DISABLE();
   _DCACHE_FLUSH();
   _DCACHE_DISABLE();

   _bsp_stop_all_ints();

#ifdef __cplusplus
   _fini();
#endif
#endif

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_get_hwticks
* Returned Value   : none
* Comments         :
*    This function returns the number of hw ticks that have elapsed
* since the last interrupt
*
*END*----------------------------------------------------------------------*/

uint_32 _bsp_get_hwticks
   (
      pointer param
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   uint_32                result = 0;
   uint_32                ticks;

   kernel_data = _mqx_get_kernel_data();

   ticks = _psp_get_aux(BSP_TCOUNT);
/* Start CR 2396 */
#if MQX_USE_PMU
   while (ticks < (kernel_data->TIMER_HW_REFERENCE)) { /* Int pending */
     result += (kernel_data->HW_TICKS_PER_TICK);
     ticks  -= (kernel_data->HW_TICKS_PER_TICK);
     ticks  &= BSP_TIMER_WRAP_MASK;
   } /* Endwhile */
   ticks -= (kernel_data->TIMER_HW_REFERENCE);
#else
   while (ticks < BSP_TIMER_REFERENCE(kernel_data)) { /* Int pending */
     result += BSP_HW_TICKS_PER_TICK(kernel_data);
     ticks  -= BSP_HW_TICKS_PER_TICK(kernel_data);
     ticks  &= BSP_TIMER_WRAP_MASK;
   } /* Endwhile */
   ticks -= BSP_TIMER_REFERENCE(kernel_data);
#endif
/* End CR 2396 */

   return (ticks + result);
} /* Endbody */


/*ISR*********************************************************************
* 
* Function Name    : _bsp_timer_isr
* Returned Value   : void
* Comments         :
*    The timer ISR is the interrupt handler for the clock tick.
* 
*END**********************************************************************/

void _bsp_timer_isr
   (
      pointer dummy
   )
{ /* Body */
#ifndef __PROFILE__
   uint_32                count;
   KERNEL_DATA_STRUCT_PTR kernel_data;
/* Start CR 2396 */
#if MQX_USE_PMU
   PMU_STRUCT_PTR         pmu_ptr;
   uint_32                tmp_threshold;
#endif
/* End CR 2396 */

   _GET_KERNEL_DATA(kernel_data);

   count = _psp_get_aux(BSP_TCOUNT);
   _psp_set_aux(BSP_TCOUNT, 0);
  
   while (count > (kernel_data->HW_TICKS_PER_TICK))
   {
     count -= (kernel_data->HW_TICKS_PER_TICK);
     PSP_INC_TICKS(&kernel_data->TIME);
   } /* Endwhile */

/* Start CR 2396 */
#if MQX_USE_PMU
   pmu_ptr = kernel_data->PMU_STRUCT_PTR;

   /* Set back timer count reg. */
   _psp_set_aux(BSP_TLIMIT, BSP_TIMER_WRAP);

   if (pmu_ptr->PMU_MODE > 0) {

      /* Set clock interval to normal (default) */
      if (!pmu_ptr->SLOW_CLOCK_INTERVAL_FLAG)
      {
         kernel_data->TIMER_HW_REFERENCE = BSP_STARTING_COUNTUP_TIMER_VALUE;
         pmu_ptr->TICKS_COUNT = 1;
      } /* Endif */

      /* Slow clock interval */
      if (pmu_ptr->SLOW_CLOCK_INTERVAL_FLAG &&  
          (pmu_ptr->MAX_SLOW_DOWN_NUM > pmu_ptr->CURRENT_NUM_SLOW_DOWN) ) {
         pmu_ticks_per_tick = pmu_ticks_per_tick * MQX_PMU_SLOW_DOWN_FACTOR;
         kernel_data->TIMER_HW_REFERENCE = (BSP_TIMER_WRAP - pmu_ticks_per_tick);
         pmu_ptr->TICKS_COUNT = pmu_ptr->TICKS_COUNT * MQX_PMU_SLOW_DOWN_FACTOR;
         pmu_ptr->CURRENT_NUM_SLOW_DOWN++;
      } /* Endif */
   } /* Endif */

   /* Check if Auto DVFS is enabled */
   if (pmu_ptr->AUTO_DVFS_ENABLE == MQX_PMU_DVFS_AUTO_ENABLE) {

      /* Count each s/w tick for auto DVFS mode */
      pmu_ptr->AUTO_DVFS_TICKS++; 

      /* Exceeded the period, start over */
      if (pmu_ptr->AUTO_DVFS_TICKS > pmu_ptr->AUTO_DVFS_PERIOD) {         
         pmu_ptr->AUTO_DVFS_TICKS = 1;
         pmu_ptr->AUTO_DVFS_IDLE_CNT = 0;

      } else {
         /* If we have had 3 s/w ticks with no idle time, switch to full power */
         if ((pmu_ptr->AUTO_DVFS_TICKS > 2) && (pmu_ptr->AUTO_DVFS_IDLE_CNT == 0)) {
            pmu_ptr->GLOBAL_DVFS_MODE = MQX_PMU_DVFS_PER_MODE_1;

         /* 
         ** If we have had 5 or more s/w ticks, we can start calculating precentage of 
         ** idle time -- but not if it is 0 
         */
         } else if ((pmu_ptr->AUTO_DVFS_TICKS > 4) && (pmu_ptr->AUTO_DVFS_IDLE_CNT != 0)) {
            /* percent of idle time */
            tmp_threshold = (pmu_ptr->AUTO_DVFS_IDLE_CNT * 100) / pmu_ptr->AUTO_DVFS_TICKS; 
            /* Test against user thresholds */
            if (tmp_threshold > pmu_ptr->AUTO_THRESHOLD_2) {
               pmu_ptr->GLOBAL_DVFS_MODE = MQX_PMU_DVFS_PER_MODE_4;
            } else if (tmp_threshold > pmu_ptr->AUTO_THRESHOLD_1) {
               pmu_ptr->GLOBAL_DVFS_MODE = MQX_PMU_DVFS_PER_MODE_3;
            } else if (tmp_threshold > pmu_ptr->AUTO_THRESHOLD_0) {
               pmu_ptr->GLOBAL_DVFS_MODE = MQX_PMU_DVFS_PER_MODE_2;
            } else {
                pmu_ptr->GLOBAL_DVFS_MODE = MQX_PMU_DVFS_PER_MODE_1;
            } /* Endif */
         } else if (pmu_ptr->AUTO_DVFS_IDLE_CNT == 0) {   
            /* 0 percent idle time after 5 ticks, go to full power*/
            pmu_ptr->GLOBAL_DVFS_MODE = MQX_PMU_DVFS_PER_MODE_1;
         } /* Endif */
      } /* Endif */ 
   } /* Endif */

   count += (kernel_data->TIMER_HW_REFERENCE);

#else
   count += BSP_TIMER_REFERENCE(kernel_data);
#endif
/* End CR 2396 */

   _psp_set_aux(BSP_TCONTROL, 3);
   _psp_set_aux(BSP_TCOUNT, count);

#endif
  
   _time_notify_kernel(); // Calls PSP_INC_TICKS

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_stop_all_ints
* Returned Value   : none
* Comments         :
*    This function stops all devices from interrupting
*
*END*----------------------------------------------------------------------*/

#ifdef BSP_SIM_MODE

#define VMACS_EXIST 0

#else

#define VMACS_EXIST  (BSP_VMAC1_ENET_BASE | BSP_VMAC2_ENET_BASE |    \
   BSP_VMAC3_ENET_BASE | BSP_VMAC4_ENET_BASE | BSP_VMAC5_ENET_BASE | \
   BSP_VMAC6_ENET_BASE | BSP_VMAC7_ENET_BASE | BSP_VMAC8_ENET_BASE)

#endif

void _bsp_stop_all_ints
   (
      void
   )
{ /* Body */
#if VMACS_EXIST
   VMAC_REG_STRUCT_PTR edev_ptr;
#endif
#if defined(BSP_VUART1_BASE) || defined(BSP_VUART2_BASE)    \
   || defined(BSP_VUART3_BASE) || defined(BSP_VUART4_BASE)  \
   || defined(BSP_VUART5_BASE) || defined(BSP_VUART6_BASE)  \
   || defined(BSP_VUART7_BASE) || defined(BSP_VUART8_BASE)
   VUART_DEVICE_STRUCT_PTR  udev_ptr;
   uchar                    tmp = 0;
#endif

   /*
   ** Disable interrupts on all possible VMACs
   */
#if VMACS_EXIST
#if BSP_VMAC1_ENET_BASE
   edev_ptr = (VMAC_REG_STRUCT_PTR)BSP_VMAC1_ENET_BASE;
   _BSP_WRITE_VMAC(&edev_ptr->INT_STATUS,0xFFFFFFFF);
   _BSP_WRITE_VMAC(&edev_ptr->INT_ENABLE,0x0);
#endif

#if BSP_VMAC2_ENET_BASE
   edev_ptr = (VMAC_REG_STRUCT_PTR)BSP_VMAC2_ENET_BASE;
   _BSP_WRITE_VMAC(&edev_ptr->INT_STATUS,0xFFFFFFFF);
   _BSP_WRITE_VMAC(&edev_ptr->INT_ENABLE,0x0);
#endif

#if BSP_VMAC3_ENET_BASE
   edev_ptr = (VMAC_REG_STRUCT_PTR)BSP_VMAC3_ENET_BASE;
   _BSP_WRITE_VMAC(&edev_ptr->INT_STATUS,0xFFFFFFFF);
   _BSP_WRITE_VMAC(&edev_ptr->INT_ENABLE,0x0);
#endif

#if BSP_VMAC4_ENET_BASE
   edev_ptr = (VMAC_REG_STRUCT_PTR)BSP_VMAC4_ENET_BASE;
   _BSP_WRITE_VMAC(&edev_ptr->INT_STATUS,0xFFFFFFFF);
   _BSP_WRITE_VMAC(&edev_ptr->INT_ENABLE,0x0);
#endif

#if BSP_VMAC5_ENET_BASE
   edev_ptr = (VMAC_REG_STRUCT_PTR)BSP_VMAC5_ENET_BASE;
   _BSP_WRITE_VMAC(&edev_ptr->INT_STATUS,0xFFFFFFFF);
   _BSP_WRITE_VMAC(&edev_ptr->INT_ENABLE,0x0);
#endif

#if BSP_VMAC6_ENET_BASE
   edev_ptr = (VMAC_REG_STRUCT_PTR)BSP_VMAC6_ENET_BASE;
   _BSP_WRITE_VMAC(&edev_ptr->INT_STATUS,0xFFFFFFFF);
   _BSP_WRITE_VMAC(&edev_ptr->INT_ENABLE,0x0);
#endif

#if BSP_VMAC7_ENET_BASE
   edev_ptr = (VMAC_REG_STRUCT_PTR)BSP_VMAC7_ENET_BASE;
   _BSP_WRITE_VMAC(&edev_ptr->INT_STATUS,0xFFFFFFFF);
   _BSP_WRITE_VMAC(&edev_ptr->INT_ENABLE,0x0);
#endif

#if BSP_VMAC8_ENET_BASE
   edev_ptr = (VMAC_REG_STRUCT_PTR)BSP_VMAC8_ENET_BASE;
   _BSP_WRITE_VMAC(&edev_ptr->INT_STATUS,0xFFFFFFFF);
   _BSP_WRITE_VMAC(&edev_ptr->INT_ENABLE,0x0);
#endif
#endif

   /*
   ** Disable interrupts on all possible VUARTs
   */
#ifdef BSP_VUART1_BASE
   udev_ptr = (VUART_DEVICE_STRUCT_PTR)BSP_VUART1_BASE;
   _BSP_WRITE_VUART(&udev_ptr->STATUS,tmp);
#endif

#ifdef BSP_VUART2_BASE
   udev_ptr = (VUART_DEVICE_STRUCT_PTR)BSP_VUART2_BASE;
   _BSP_WRITE_VUART(&udev_ptr->STATUS,tmp);
#endif

#ifdef BSP_VUART3_BASE
   udev_ptr = (VUART_DEVICE_STRUCT_PTR)BSP_VUART3_BASE;
   _BSP_WRITE_VUART(&udev_ptr->STATUS,tmp);
#endif

#ifdef BSP_VUART4_BASE
   udev_ptr = (VUART_DEVICE_STRUCT_PTR)BSP_VUART4_BASE;
   _BSP_WRITE_VUART(&udev_ptr->STATUS,tmp);
#endif

#ifdef BSP_VUART5_BASE
   udev_ptr = (VUART_DEVICE_STRUCT_PTR)BSP_VUART5_BASE;
   _BSP_WRITE_VUART(&udev_ptr->STATUS,tmp);
#endif

#ifdef BSP_VUART6_BASE
   udev_ptr = (VUART_DEVICE_STRUCT_PTR)BSP_VUART6_BASE;
   _BSP_WRITE_VUART(&udev_ptr->STATUS,tmp);
#endif

#ifdef BSP_VUART7_BASE
   udev_ptr = (VUART_DEVICE_STRUCT_PTR)BSP_VUART7_BASE;
   _BSP_WRITE_VUART(&udev_ptr->STATUS,tmp);
#endif

#ifdef BSP_VUART8_BASE
   udev_ptr = (VUART_DEVICE_STRUCT_PTR)BSP_VUART8_BASE;
   _BSP_WRITE_VUART(&udev_ptr->STATUS,tmp);
#endif

   /* Clear level status register */
   _psp_set_aux(PSP_AUX_IRQ_LV12, 0x3);

} /* Endbody */

#if BSP_ARC4
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_soft_int_trigger
* Returned Value   : none
* Comments         :
*    This function triggers a software interrupt
*
*END*----------------------------------------------------------------------*/

void _bsp_soft_int_trigger
   (
      uint_32  int_vector
   )
{ /* Body */
   uint_32    old_status;
   uint_32    status;
   uint_32    i;
   uint_32    bit = ~(0x1 << int_vector);
   
   /* Read status */
   _psp_set_aux(0xb0, bit);
   _bsp_shm_int_reg = bit;
   for(i=0; i<100; i++){ 
      _ASM("  NOP");
   } /* Endfor */
   bit = 0xFFFFFFFF;
   _psp_set_aux(0xb0, bit);

} /* Endbody */

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _bsp_soft_int_clear
* Returned Value   : none
* Comments         :
*    This function clears a software interrupt
*
*END*----------------------------------------------------------------------*/

void _bsp_soft_int_clear
   (
      void
   )
{ /* Body */
   
   _psp_set_aux(0xb0, 0xFFFFFFFF);

} /* Endbody */
#endif

/* EOF */

#ifndef __acaa700_h__
#define __acaa700_h__ 1
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
*** File: acaa700.h
***
*** Comments:
***   This include file is used to provide information needed by
***   a Precise application program using the kernel running on a
***   ARC 700 target CPU.
***
**************************************************************************
*END*********************************************************************/

/* Start CR 2282 */
/* SYSCLKM is defined in the linker command file */
extern unsigned int __SYSCLKM[];
/* End CR 2282 */

/*----------------------------------------------------------------------
**                  HARDWARE INITIALIZATION DEFINITIONS
*/

/*
** Define the board type
*/
#define BSP_ACAA700   	TRUE
#define BSP_CPU_TYPE 	PSP_CPU_TYPE_ARC_700
#define BSP_ARC4 	0  /*Not an arc 4 */

/*
** PROCESSOR MEMORY BOUNDS
*/
#define BSP_SRAM_BASE               ((pointer)(0x00000000))
#define BSP_SRAM_SIZE                         (0x00200000)
#define BSP_FLASH_BASE              ((pointer)(0x00800000))

/* peripheral base address */
#define BSP_PERIPHERAL_BASE         (0xC0000000)

/*
** Flash details
*/
/* Each flash is 2 bytes or 16 bits wide   */
#define BSP_FLASH_WIDTH                                (2)
/*
** There are two flash devices in parallel. When flash devices are in
** parallel they are treated as one device but with larger block sizes.
*/
#define BSP_FLASH_DEVICES                              (2)
/*
** The board has the Intel 28F128 part installed but not all of it maybe
** accessible depending on the memory configuration of the XBF.
*/
#define BSP_NUM_FLASH_BLOCKS                           (8)
#define BSP_FLASH_BLOCK_SIZE                           (128*1024)

#define BSP_FLASH_SIZE  \
   (BSP_FLASH_DEVICES * BSP_NUM_FLASH_BLOCKS * BSP_FLASH_BLOCK_SIZE)

/*
** By default flash is enabled. Set this define to 0 to remove flash support
** from the BSP
*/
#ifndef BSP_USE_FLASH
#define BSP_USE_FLASH  1
#endif

/* Start CR 2283 */
/*
** IDE interface define
*/
 
/* By default IDE is enabled. Set this define to 0 to remove IDE support from the BSP */
#ifndef BSP_USE_IDE
#define BSP_USE_IDE  1
#endif

/* Aurora-defined base address */
#define BSP_IDE_CONTROLLER_BASE                (BSP_PERIPHERAL_BASE + 0xFC9000)

/* Drive's registers map in at 0x80 above the controller base address */
#define BSP_IDE_REGISTER_OFFSET                 (0xA0)
#define BSP_IDE_DEFAULT_PIO_MODE                (4)
#define BSP_IDE_DEFAULT_DEVICE_NUM              (0) 

/* statctrl bits */
#define BSP_IDE_STATCTRL_IS                     0x20
#define BSP_IDE_STATCTRL_RS                     0x04
#define BSP_IDE_STATCTRL_IC                     0x02
#define BSP_IDE_STATCTRL_IE                     0x01

/* We always use the new IDE port registering, so these are fixed here. */
#define BSP_IDE_DEFAULT_IO_BASE                 (0)
#define BSP_IDE_DEFAULT_IRQ                     (10)

/* Is interrupt mode supported */
/* Interrupt mode is not supported yet */
#define BSP_IDE_INTERRUPT_SUPPORTED             (0)
/* End CR 2283 */

/*
** Define the addresses of the support modules on the board
*/

/* register base for the first uart */
#define BSP_VUART1_BASE                         (BSP_PERIPHERAL_BASE + 0xFC1000)
#define BSP_VUART1_QUEUE_SIZE                   128
#define BSP_VUART1_BAUD                 	    9600
#define BSP_VUART1_CHANNEL_NAME                 "ttya:"
#define BSP_VUART1_ICHANNEL_NAME                "ittya:"
#define BSP_VUART_IN_AUX_SPACE                  (0)
#define BSP_VUART_STRIDE_SIZE                   (3)

/* Start CR 2280 */
/* register base for the second uart */
#define BSP_VUART2_BASE                         (BSP_PERIPHERAL_BASE + 0xFC1100)
#define BSP_VUART2_QUEUE_SIZE                   128
#define BSP_VUART2_BAUD                 	    9600
#define BSP_VUART2_CHANNEL_NAME                 "ttyb:"
#define BSP_VUART2_ICHANNEL_NAME                "ittyb:"
#define BSP_VUART_IN_AUX_SPACE                  (0)
#define BSP_VUART_STRIDE_SIZE                   (3)
/* End CR 2280 */

/*
** Register base for the VMACs
*/
/* The VMAC driver allows for up to 8 VMACs to exist.
** AA3 board can only have one though
*/
/* Actual count: */
#define BSP_ENET_COUNT 1
#define BSP_VMAC1_ENET_BASE                        (BSP_PERIPHERAL_BASE + 0xFC2000)
#define BSP_VMAC2_ENET_BASE                        (0x0)
#define BSP_VMAC3_ENET_BASE                        (0x0)
#define BSP_VMAC4_ENET_BASE                        (0x0)
#define BSP_VMAC5_ENET_BASE                        (0x0)
#define BSP_VMAC6_ENET_BASE                        (0x0)
#define BSP_VMAC7_ENET_BASE                        (0x0)
#define BSP_VMAC8_ENET_BASE                        (0x0)

/*
** Device numbers for the VMACs
*/
#define BSP_VMAC1_PHY_ID                           (0x3)
#define BSP_VMAC2_PHY_ID                           (0x0)
#define BSP_VMAC3_PHY_ID                           (0x0)
#define BSP_VMAC4_PHY_ID                           (0x0)
#define BSP_VMAC5_PHY_ID                           (0x0)
#define BSP_VMAC6_PHY_ID                           (0x0)
#define BSP_VMAC7_PHY_ID                           (0x0)
#define BSP_VMAC8_PHY_ID                           (0x0)

/*
** Enable duplex polling code for VMACs. If all VMACs do not poll then the
** polling code is removed altogether.
** Note: You must still initialize the VMAC with ENET_POLL_PEER_PERIODICALLY
** flag.
*/
#define BSP_ENABLE_VMAC1_POLLING                   (1)
#define BSP_ENABLE_VMAC2_POLLING                   (0)
#define BSP_ENABLE_VMAC3_POLLING                   (0)
#define BSP_ENABLE_VMAC4_POLLING                   (0)
#define BSP_ENABLE_VMAC5_POLLING                   (0)
#define BSP_ENABLE_VMAC6_POLLING                   (0)
#define BSP_ENABLE_VMAC7_POLLING                   (0)
#define BSP_ENABLE_VMAC8_POLLING                   (0)

#define BSP_ENABLE_VMAC_POLLING \
   (BSP_ENABLE_VMAC1_POLLING || BSP_ENABLE_VMAC2_POLLING || \
    BSP_ENABLE_VMAC3_POLLING || BSP_ENABLE_VMAC4_POLLING || \
    BSP_ENABLE_VMAC5_POLLING || BSP_ENABLE_VMAC6_POLLING || \
    BSP_ENABLE_VMAC7_POLLING || BSP_ENABLE_VMAC8_POLLING)

/*
** Polling period for VMACs duplex mode
*/
#define BSP_VMAC1_POLL_PERIOD                      (1000)
#define BSP_VMAC2_POLL_PERIOD                      (0)
#define BSP_VMAC3_POLL_PERIOD                      (0)
#define BSP_VMAC4_POLL_PERIOD                      (0)
#define BSP_VMAC5_POLL_PERIOD                      (0)
#define BSP_VMAC6_POLL_PERIOD                      (0)
#define BSP_VMAC7_POLL_PERIOD                      (0)
#define BSP_VMAC8_POLL_PERIOD                      (0)

/*
** Priority of the Polling task for VMACs duplex mode
*/
#define BSP_VMAC1_POLL_PRIORITY                      (1)
#define BSP_VMAC2_POLL_PRIORITY                      (0)
#define BSP_VMAC3_POLL_PRIORITY                      (0)
#define BSP_VMAC4_POLL_PRIORITY                      (0)
#define BSP_VMAC5_POLL_PRIORITY                      (0)
#define BSP_VMAC6_POLL_PRIORITY                      (0)
#define BSP_VMAC7_POLL_PRIORITY                      (0)
#define BSP_VMAC8_POLL_PRIORITY                      (0)

/* Start CR 2249 */
/* VMAC error code */
#define BSP_VMAC_ERR_BASE               0x100000
#define BSP_VMAC_ERR_INVALID_DEVICE     (BSP_VMAC_ERR_BASE | 0x10)
/* End CR 2249 */

/*
** Define this macro to non-zero if the VMAC is able to
** handle high bandwidth flooding with the BVCI memory
** bus.  Can it handle ping -f from a Unix host?
*/
#ifndef VMAC_ENABLE_TX_CHAINING
#define VMAC_ENABLE_TX_CHAINING                       1
#endif

/*
** Indicate the VMAC supports the ENET_get_speed function necessary for
** the MQX SNMP MIB in SNMP
*/
#define BSP_ENET_GET_SPEED_SUPPORT                    1

#ifdef BSP_SIM_MODE
#define BSP_SIM_ENET_BASE                          (0x200000)
#endif

/* Set this define to install simulated I/O */
/*#define BSP_INSTALL_SIM_IO*/

/*
** The clock tick rate in ticks per second
*/
#ifdef BSP_ALARM_FREQUENCY
#undef BSP_ALARM_FREQUENCY
#endif
#define BSP_ALARM_FREQUENCY     (200)

/*
** Old clock rate definition in MS per tick, kept for compatibility
*/
#define BSP_ALARM_RESOLUTION  (1000 / BSP_ALARM_FREQUENCY)

/*
** Define the timer to use
*/
#define BSP_TIMER                            (0)

/*
** The profiler clock resolution in ticks per second
*/
#define BSP_PROFILE_RESOLUTION               (100)

/*
** Uncomment this define to use a fixed clock rate. This will produce slightly
** smaller code but at the expense of flexibility. When this define is set, MQX
** will always assume a clock speed of BSP_CRYSTAL_FREQUENCY despite the fact
** it may have been divided down by the dip switches on the back of the AA3.
*/

#define BSP_USE_FIXED_CLOCK_RATE

/*
** The processor clock speed
*/
/* Start CR 2282 */
#define BSP_CRYSTAL_FREQUENCY                ((unsigned int)__SYSCLKM)
/* End CR 2282 */

#ifdef BSP_USE_FIXED_CLOCK_RATE
#define BSP_SYSTEM_CLOCK                     BSP_CRYSTAL_FREQUENCY
#else
#warning CUR A7 XBF doesnt have AUX 0x56 needed by _bsp_get_system_clock
#define BSP_SYSTEM_CLOCK                     (_bsp_get_system_clock())
#endif

/* Config info for data cache */
#define BSP_DCACHE_MODE \
   (PSP_AUX_DC_CTRL_ENABLE_BYPASS | PSP_AUX_DC_CTRL_INVALIDATE_FLUSHES)

/* Set this define to 1 to enable and use the code ram */
#define BSP_USE_CODERAM       1
/*
** Set this define to the area of memory to load into code ram
** This define is ignored if BSP_USE_CODERAM is 0
*/
#define BSP_CODERAM_ADDR  _task_block

/* Config info for instruction cache */
#if BSP_USE_CODERAM
#define BSP_ICACHE_MODE   (PSP_AUX_IC_CTRL_ENABLE_BYPASS | \
   PSP_AUX_IC_CTRL_ENABLE_CODERAM)
#else
#define BSP_ICACHE_MODE   (PSP_AUX_IC_CTRL_ENABLE_BYPASS)
#endif

/* Set this define to 1 to enable and use the internal LD/ST RAM */
#define BSP_USE_INT_LDST_RAM 0
/*
** Set this define to the area of memory to be overlayed by the internal
** LD/ST RAM.
** This define is ignored if BSP_USE_INT_LDST_RAM is 0
*/
#define BSP_INT_LDST_ADDR    BSP_DEFAULT_START_OF_KERNEL_MEMORY

/*
** INTERRUPT DEFINITIONS
*/

/*
** Define the interrupt vectors used
*/
/* Start CR 2283 */
#if BSP_USE_IDE
#define BSP_IDE_INTERRUPT_VECTOR           PSP_EXCPT_13_VECTOR
#define BSP_IDE_INTERRUPT_LEVEL            (1)
#endif
/* End CR 2283 */

#if BSP_TIMER == 0
#define BSP_TIMER_INTERRUPT_VECTOR          PSP_EXCPT_3_VECTOR
#else
#define BSP_TIMER_INTERRUPT_VECTOR          PSP_EXCPT_4_VECTOR
#endif
#define BSP_TIMER_INTERRUPT_LEVEL           (1)

/* Start CR 2281 */
#define BSP_VUART1_INTERRUPT_VECTOR         PSP_EXCPT_5_VECTOR
#define BSP_VUART1_INTERRUPT_LEVEL          (1)
#define BSP_VUART2_INTERRUPT_VECTOR         PSP_EXCPT_10_VECTOR
#define BSP_VUART2_INTERRUPT_LEVEL          (1)
#define BSP_VUART3_INTERRUPT_VECTOR         PSP_EXCPT_11_VECTOR
#define BSP_VUART3_INTERRUPT_LEVEL          (1)
#define BSP_VUART4_INTERRUPT_VECTOR         (0)
#define BSP_VUART4_INTERRUPT_LEVEL          (1)
#define BSP_VUART5_INTERRUPT_VECTOR         (0)
#define BSP_VUART5_INTERRUPT_LEVEL          (1)
#define BSP_VUART6_INTERRUPT_VECTOR         (0)
#define BSP_VUART6_INTERRUPT_LEVEL          (1)
#define BSP_VUART7_INTERRUPT_VECTOR         (0)
#define BSP_VUART7_INTERRUPT_LEVEL          (1)
#define BSP_VUART8_INTERRUPT_VECTOR         (0)
#define BSP_VUART8_INTERRUPT_LEVEL          (1)
/* End CR 2281 */

#define BSP_VMAC1_INTERRUPT_VECTOR          PSP_EXCPT_6_VECTOR
#define BSP_VMAC2_INTERRUPT_VECTOR          (0)
#define BSP_VMAC3_INTERRUPT_VECTOR          (0)
#define BSP_VMAC4_INTERRUPT_VECTOR          (0)
#define BSP_VMAC5_INTERRUPT_VECTOR          (0)
#define BSP_VMAC6_INTERRUPT_VECTOR          (0)
#define BSP_VMAC7_INTERRUPT_VECTOR          (0)
#define BSP_VMAC8_INTERRUPT_VECTOR          (0)

#define BSP_VMAC_TX_POLLRATE             (BSP_SYSTEM_CLOCK/(1024 * 1000))

/*
** Define the range of interrupts for which the application can install
** isrs.
*/
#define BSP_FIRST_INTERRUPT_VECTOR_USED      PSP_EXCPT_RESET_VECTOR
#define BSP_LAST_INTERRUPT_VECTOR_USED       PSP_EXCPT_EXTENSION_INTRUCTION_EXCEPTION_VECTOR

/*
** Define the timer frequency
*/
#define BSP_TIMER_FREQUENCY                  BSP_SYSTEM_CLOCK

#define _BSP_READ_VUART(x,y)  y = (uchar)*((volatile _Uncached uchar _PTR_)((pointer)(x)))
#define _BSP_WRITE_VUART(x,y) *((volatile _Uncached uchar _PTR_)((pointer)(x))) = (uchar)(y)

#define _BSP_READ_VMAC(x)    (*((volatile _Uncached uint_32 _PTR_)((pointer)(x))))
#define _BSP_WRITE_VMAC(x,y) *((volatile _Uncached uint_32 _PTR_)((pointer)(x))) = (uint_32)(y)

/*----------------------------------------------------------------------
**                  DEFAULT MQX INITIALIZATION DEFINITIONS
*/
#define BSP_VUART_OPEN_MODE   (pointer) \
   (IO_SERIAL_XON_XOFF | IO_SERIAL_TRANSLATION | IO_SERIAL_ECHO)
#define BSP_MWUART_OPEN_MODE  (pointer) \
   (IO_SERIAL_TRANSLATION | IO_SERIAL_ECHO)

/* The following defined in linker command file */
extern uint_32 __KERNEL_DATA_START[];
extern uint_32 __KERNEL_DATA_END[];

#define BSP_DEFAULT_PROCESSOR_NUMBER                 (1)
#define BSP_DEFAULT_START_OF_KERNEL_MEMORY           (pointer)__KERNEL_DATA_START
#define BSP_DEFAULT_END_OF_KERNEL_MEMORY             (pointer)__KERNEL_DATA_END
#define BSP_DEFAULT_INTERRUPT_STACK_SIZE             (1024)
#define BSP_DEFAULT_MQX_HARDWARE_INTERRUPT_LEVEL_MAX (2)
#define BSP_DEFAULT_MAX_MSGPOOLS                     (16)
#define BSP_DEFAULT_MAX_MSGQS                        (128)

#ifdef BSP_DEFAULT_HOSTLINK
# define BSP_DEFAULT_IO_CHANNEL                      BSP_SIMUART_NAME
# define BSP_DEFAULT_IO_OPEN_MODE                    BSP_MWUART_OPEN_MODE
#else
# define BSP_DEFAULT_IO_CHANNEL                      "ttya:"
# ifdef BSP_VUART1_BASE
#  define BSP_DEFAULT_IO_OPEN_MODE                   BSP_VUART_OPEN_MODE
# else
#  define BSP_DEFAULT_IO_OPEN_MODE                   BSP_MWUART_OPEN_MODE
# endif
#endif

#endif /* __acaa700_h__ */
/* EOF */

#ifndef __mqx_h__
#define __mqx_h__
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: mqx.h
***
*** Comments:
***    This file contains the structure definitions and constants for an
*** application which will be using MQX.
***    All compiler provided header files must be included before mqx.h.
***
*** $Header:mqx.h, 14, 3/31/2004 3:07:31 PM, $
***
*** $NoKeywords$
***************************************************************************
*END**********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                         MQX VERSION NUMBER
*/
#define MQX_VERSION (252)
#define __MQX__

/*--------------------------------------------------------------------------*/
/*
**                   INCLUDE MQX CONFIGURATION CONSTANTS
*/


#include "arc_cnfg.h"

#include "mqx_cnfg.h"         /* See also psp_cnfg.hs in each PSP directory */

/*--------------------------------------------------------------------------*/
/*
**                 INCLUDE MQX STANDARD TYPES FROM THE PSP
*/

#include "psptypes.h"

/*--------------------------------------------------------------------------*/
/*
**                 INCLUDE MQX IO COMPONENT CONSTANTS
*/

#include "mqx_ioc.h"


/*--------------------------------------------------------------------------*/
/*
**                          STANDARD CONSTANTS
**
**  Note that if standard 'C' library files are included after mqx.h,
**  the defines of TRUE, FALSE and NULL may sometimes conflict, as most
**  standard library files do not check for previous definitions.
*/

#ifdef  FALSE
   #undef  FALSE
#endif
#define FALSE ((boolean)0)

#ifdef  TRUE
   #undef  TRUE
#endif
#define TRUE ((boolean)1)

#ifdef  NULL
   #undef  NULL
#endif
#ifdef __cplusplus /* CR1508 */
#define NULL 0
#else
#define NULL ((pointer)0)
#endif


/*--------------------------------------------------------------------------*/
/*
**                    TASK IDS AND PROCESSOR NUMBERS
**
** These definitions are for MQX task IDs and processor numbers.
*/

typedef uint_32 _task_id;           /* what a task_id looks like  */

#define MQX_NULL_TASK_ID    ((_task_id)0)
#define MQX_DEFAULT_TASK_ID ((_task_id)-1)

typedef uint_16 _processor_number;  /* what a processor number is */

/*
** Valid processor numbers run from 1 to 255 for 16 bit queue IDs or
** 1 to 65535 for 32 bit queue IDs
*/
#if MQX_USE_32BIT_MESSAGE_QIDS == 0
   #define MQX_MAX_PROCESSOR_NUMBER  ((_processor_number)255)
#else
   #define MQX_MAX_PROCESSOR_NUMBER  ((_processor_number)65535)
#endif

/*--------------------------------------------------------------------------*/
/*
**                       MEMORY ENDIAN DEFINITIONS
**
**  A PSP will define in its header file PSP_ENDIAN to be one of these
*/
#define MQX_BIG_ENDIAN    (0)
#define MQX_LITTLE_ENDIAN (1)


/*--------------------------------------------------------------------------*/
/*
**                          ERROR CODE BASES
**
**  These are the base values of various MQX component error codes
*/
#define MQX_ERROR_BASE         (0x1000)

#define SEM_ERROR_BASE         (MQX_ERROR_BASE | 0x100)
#define EDS_SERIAL_ERROR_BASE  (MQX_ERROR_BASE | 0x200)
#define EVENT_ERROR_BASE       (MQX_ERROR_BASE | 0x300)
#define RESERVED1_ERROR_BASE   (MQX_ERROR_BASE | 0x400)
#define LOG_ERROR_BASE         (MQX_ERROR_BASE | 0x500)
#define WATCHDOG_ERROR_BASE    (MQX_ERROR_BASE | 0x600)
#define MSG_ERROR_BASE         (MQX_ERROR_BASE | 0x700)
#define PART_ERROR_BASE        (MQX_ERROR_BASE | 0x800)
#define RESERVED2_ERROR_BASE   (MQX_ERROR_BASE | 0x900)
#define IO_ERROR_BASE          (MQX_ERROR_BASE | 0xA00)
#define RESERVED3_ERROR_BASE   (MQX_ERROR_BASE | 0xB00)
#define RESERVED4_ERROR_BASE   (MQX_ERROR_BASE | 0xC00)
#define RESERVED5_ERROR_BASE   (MQX_ERROR_BASE | 0xD00)
#define RESERVED6_ERROR_BASE   (MQX_ERROR_BASE | 0xE00)
#define NAME_ERROR_BASE        (MQX_ERROR_BASE | 0xF00)

/*--------------------------------------------------------------------------*/
/*
**                          TASK ERROR CODES
**
**  These are the values of the task error codes used by Kernel
*/
#define MQX_OK                              (0)

#define MQX_INVALID_POINTER                 (MQX_ERROR_BASE|0x01)
#define MQX_INVALID_SIZE                    (MQX_ERROR_BASE|0x02)
#define MQX_NOT_RESOURCE_OWNER              (MQX_ERROR_BASE|0x03)
#define MQX_OUT_OF_MEMORY                   (MQX_ERROR_BASE|0x04)
#define MQX_CORRUPT_MEMORY_SYSTEM           (MQX_ERROR_BASE|0x05)
#define MQX_CORRUPT_STORAGE_POOL            (MQX_ERROR_BASE|0x06)
#define MQX_CORRUPT_STORAGE_POOL_FREE_LIST  (MQX_ERROR_BASE|0x07)
#define MQX_CORRUPT_STORAGE_POOL_POINTERS   (MQX_ERROR_BASE|0x08)
#define MQX_INVALID_CHECKSUM                (MQX_ERROR_BASE|0x09)
#define MQX_OUT_OF_TASK_DESCRIPTORS         (MQX_ERROR_BASE|0x0A)
#define MQX_INVALID_MEMORY_BLOCK            (MQX_ERROR_BASE|0x0B)
#define MQX_INVALID_PARAMETER               (MQX_ERROR_BASE|0x0C)
#define MQX_CANNOT_CALL_FUNCTION_FROM_ISR   (MQX_ERROR_BASE|0x0D)
#define MQX_INVALID_TASK_PRIORITY           (MQX_ERROR_BASE|0x0E)
#define MQX_TASK_QUEUE_EMPTY                (MQX_ERROR_BASE|0x0F)
#define MQX_NO_TASK_TEMPLATE                (MQX_ERROR_BASE|0x10)
#define MQX_INVALID_TASK_STATE              (MQX_ERROR_BASE|0x11)
#define MQX_INVALID_TASK_ID                 (MQX_ERROR_BASE|0x12)
#define MQX_INVALID_PROCESSOR_NUMBER        (MQX_ERROR_BASE|0x13)
#define MQX_INVALID_VECTORED_INTERRUPT      (MQX_ERROR_BASE|0x14)
#define MQX_INVALID_TEMPLATE_INDEX          (MQX_ERROR_BASE|0x15)
#define MQX_INVALID_CONFIGURATION           (MQX_ERROR_BASE|0x16)

/* Kernel component error codes */
#define MQX_COMPONENT_EXISTS                (MQX_ERROR_BASE|0x17)
#define MQX_COMPONENT_DOES_NOT_EXIST        (MQX_ERROR_BASE|0x18)
#define MQX_INVALID_COMPONENT_HANDLE        (MQX_ERROR_BASE|0x19)
#define MQX_INVALID_COMPONENT_BASE          (MQX_ERROR_BASE|0x1A)
#define MQX_INVALID_COMPONENT_NAME          (MQX_ERROR_BASE|0x1B)
#define MQX_INVALID_HANDLE                  (MQX_ERROR_BASE|0x1C)

/* Test error codes */
#define MQX_CORRUPT_QUEUE                   (MQX_ERROR_BASE|0x1D)
#define MQX_INVALID_TASK_QUEUE              (MQX_ERROR_BASE|0x1E)
#define MQX_INVALID_LWSEM                   (MQX_ERROR_BASE|0x1F)
#define MQX_CORRUPT_INTERRUPT_STACK         (MQX_ERROR_BASE|0x20)

/* Initialization error return codes */
#define MQX_KERNEL_MEMORY_TOO_SMALL         (MQX_ERROR_BASE|0x21)
#define MQX_COULD_NOT_CREATE_IPC_TASK       (MQX_ERROR_BASE|0x22)
#define MQX_TOO_MANY_PRIORITY_LEVELS        (MQX_ERROR_BASE|0x23)
#define MQX_TOO_MANY_INTERRUPTS             (MQX_ERROR_BASE|0x24)
#define MQX_DUPLICATE_TASK_TEMPLATE_INDEX   (MQX_ERROR_BASE|0x25)
#define MQX_TIMER_ISR_INSTALL_FAIL          (MQX_ERROR_BASE|0x26)

/* Scheduler error codes */
#define MQX_SCHED_INVALID_POLICY            (MQX_ERROR_BASE|0x27)
#define MQX_SCHED_INVALID_PARAMETER_PTR     (MQX_ERROR_BASE|0x28)
#define MQX_SCHED_INVALID_PARAMETER         (MQX_ERROR_BASE|0x29)
#define MQX_SCHED_INVALID_TASK_ID           (MQX_ERROR_BASE|0x2A)

/* I/O error codes */
#define MQX_INVALID_IO_CHANNEL              (MQX_ERROR_BASE|0x2B)
#define MQX_IO_OPERATION_NOT_AVAILABLE      (MQX_ERROR_BASE|0x2C)

/* IPC error codes */
#define MQX_INTER_PROCESSOR_INIT_FAILED     (MQX_ERROR_BASE|0x2D)
#define MQX_IPC_INVALID_MESSAGE             (MQX_ERROR_BASE|0x2E)
#define MQX_IPC_SERVICE_NOT_AVAILABLE       (MQX_ERROR_BASE|0x2F)
#define MQX_IPC_ROUTE_EXISTS                (MQX_ERROR_BASE|0x30)

/* User memory error codes */
#define MQX_MEM_POOL_TOO_SMALL              (MQX_ERROR_BASE|0x31)
#define MQX_MEM_POOL_INVALID                (MQX_ERROR_BASE|0x32)

/* MMU error codes */
#define MQX_OUT_OF_MMU_PAGE_TABLES          (MQX_ERROR_BASE|0x33)
#define MQX_MMU_CONTEXT_EXISTS              (MQX_ERROR_BASE|0x34)
#define MQX_MMU_CONTEXT_DOES_NOT_EXIST      (MQX_ERROR_BASE|0x35)
#define MQX_MMU_PARENT_TASK_CANNOT_BE_MMU   (MQX_ERROR_BASE|0x36)

/* LWSEM wait timeout error codes */
#define MQX_LWSEM_WAIT_TIMEOUT              (MQX_ERROR_BASE|0x37)

/* LWMEM error codes              */
#define MQX_LWMEM_POOL_INVALID              (MQX_ERROR_BASE|0x38)

/* LWEVENT error codes            */
#define MQX_LWEVENT_INVALID                 (MQX_ERROR_BASE|0x39)

/* LWTIMER error codes            */
#define MQX_LWTIMER_INVALID                 (MQX_ERROR_BASE|0x40)

/*--------------------------------------------------------------------------*/
/*
**                          POSIX ERROR CODES
**
** These are the POSIX error codes used by POSIX compliant functions
** in MQX.
*/

#define _task_errno  (*_task_get_error_ptr())

#define MQX_EOK             (0x0000)
#define MQX_E2BIG           (0x0801)
#define MQX_EACCES          (0x0802)
#define MQX_EAGAIN          (0x0803)
#define MQX_EBADF           (0x0804)
#define MQX_EBADMSG         (0x0805)
#define MQX_EBUSY           (0x0806)
#define MQX_ECANCELED       (0x0807)
#define MQX_ECHILD          (0x0808)
#define MQX_EDEADLK         (0x0809)
#define MQX_EDOM            (0x080a)
#define MQX_EEXIST          (0x080b)
#define MQX_EFAULT          (0x080c)
#define MQX_EFBIG           (0x080d)
#define MQX_EINPROGRESS     (0x080e)
#define MQX_EINTR           (0x080f)
#define MQX_EINVAL          (0x0810)
#define MQX_EIO             (0x0811)
#define MQX_EISDIR          (0x0812)
#define MQX_EMFILE          (0x0813)
#define MQX_EMLINK          (0x0814)
#define MQX_EMSGSIZE        (0x0815)
#define MQX_ENAMETOOLONG    (0x0816)
#define MQX_ENFILE          (0x0817)
#define MQX_ENODEV          (0x0818)
#define MQX_ENOENT          (0x0819)
#define MQX_ENOEXEC         (0x081a)
#define MQX_ENOLCK          (0x081b)
#define MQX_ENOMEM          (0x081c)
#define MQX_ENOSPC          (0x081d)
#define MQX_ENOSYS          (0x081e)
#define MQX_ENOTDIR         (0x081f)
#define MQX_ENOTEMPTY       (0x0820)
#define MQX_ENOTSUP         (0x0821)
#define MQX_ENOTTY          (0x0822)
#define MQX_ENXIO           (0x0823)
#define MQX_EPERM           (0x0824)
#define MQX_EPIPE           (0x0825)
#define MQX_ERANGE          (0x0826)
#define MQX_EROFS           (0x0827)
#define MQX_ESPIPE          (0x0828)
#define MQX_ESRCH           (0x0829)
#define MQX_ETIMEDOUT       (0x082a)
#define MQX_EXDEV           (0x082b)


/*--------------------------------------------------------------------------*/
/*
**                 TASK TEMPLATE ATTRIBUTES FLAGS
**
** These are bit flags or'd together to form the creation attributes for
** a task.
*/

/*
** Tasks with this attribute will be created automatically at
** system initialization time.
*/
#define MQX_AUTO_START_TASK            (0x01)

/*
** Tasks with this attribute will save the floating point co-processor
** registers during context switching.
** If the floating point registers are separate from the normal registers,
** their context is managed independently during task switching. The registers
** are saved/restored only when a new floating point task is scheduled to run.
*/
#define MQX_FLOATING_POINT_TASK        (0x02)

/*
** Tasks with this attribute will use the time slice
** scheduler within the kernel
*/
#define MQX_TIME_SLICE_TASK            (0x04)

/*
** Tasks with this attribute will save the DSP co-processor
** registers during context switching.
** If the DSP registers are separate from the normal registers,
** their context is managed independently during task switching. The registers
** are saved/restored only when a new DSP task is scheduled to run.
*/
#define MQX_DSP_TASK                   (0x08)

/*--------------------------------------------------------------------------*/
/*
**                        MONITOR DEFINITIONS
**
** These definitions are used by the MQX BSPs to determine which type of
** monitor is currently running, and thus to modify their initialization
** behaviour (Handling of caches, interrupts etc)
** The variable _mqx_monitor_type is set to one of these by the application
** program.  The default value is MONITOR_TYPE_NONE
*/

#define MQX_MONITOR_TYPE_NONE      (0)

/* Polled serial I/O monitor debugger */
#define MQX_MONITOR_TYPE_MON       (0x01)

/* Interrupt driven serial I/O monitor debugger */
#define MQX_MONITOR_TYPE_MON_INT   (0x02)

/* Background mode debugger being used */
#define MQX_MONITOR_TYPE_BDM       (0x03)

/* JTAG debugger being used */
#define MQX_MONITOR_TYPE_JTAG      (0x04)

/* Some other monitor in use */
#define MQX_MONITOR_TYPE_OTHER     (0x10)


/*--------------------------------------------------------------------------*/
/*
**                       KERNEL SCHEDULER CONSTANTS
*/

/* Scheduling policy constants */
#define MQX_SCHED_FIFO                 (1)
#define MQX_SCHED_RR                   (2)

/* Explicit scheduler task queueing policy */
#define MQX_TASK_QUEUE_FIFO           (0x00)
#define MQX_TASK_QUEUE_BY_PRIORITY    (0x08)
#define MQX_TASK_QUEUE_RESUME_ONE     FALSE
#define MQX_TASK_QUEUE_RESUME_ALL     TRUE

/*--------------------------------------------------------------------------*/
/*
**                       TEST AND SET CONSTANTS
**
** These are the possible values returned by the MQX test and set function.
** It is also used by the mutex functions as the value used when locking
** a mutex.
*/
#define MQX_TEST_AND_SET_VALUE        (0x80)
#define MQX_TEST_AND_SET_NOT_ACQUIRED MQX_TEST_AND_SET_VALUE
#define MQX_TEST_AND_SET_ACQUIRED     (0x00)


/*--------------------------------------------------------------------------*/
/*
**                       GENERIC CONSTANTS
**
*/
/*
** The maximum number of characters in a task's name. Includes terminating
** /0 character.
*/
#define MQX_MAX_TASK_NAME_SIZE        (50)

/*--------------------------------------------------------------------------*/
/*
**                       MEMORY CONSTANTS AND TYPE DEFINITIONS
*/
/* what a memory pool ID looks like  */
typedef pointer _mem_pool_id;

#define MQX_NULL_MEM_POOL_ID    (NULL)

/* The minimum amount of memory needed by the memory management routines. */
#define MQX_MIN_MEMORY_POOL_SIZE (256)

/*--------------------------------------------------------------------------*/
/*
**                          DATA STRUCTURES
*/

/*--------------------------------------------------------------------------*/
/*
**                            TIME CONSTANTS
*/
#define PICOSECS_IN_NANOSECOND   (1000)
#define NANOSECS_IN_MICROSECOND  (1000)
#define MICROSECS_IN_MILLISECOND (1000)
#define MILLISECS_IN_SECOND      (1000)
#define MICROSECS_IN_SECOND      (MILLISECS_IN_SECOND*MICROSECS_IN_MILLISECOND)
#define NANOSECS_IN_SECOND       (MICROSECS_IN_SECOND*NANOSECS_IN_MICROSECOND)
#define SECS_IN_MINUTE           (60UL)
#define MINUTES_IN_HOUR          (60UL)
#define HOURS_IN_DAY             (24UL)
#define MAX_DAYS_IN_MONTH        (31UL)
#define DAYS_IN_YEAR             (365UL)
#define MAX_DAYS_IN_YEAR         (366UL)
#define MONTHS_IN_YEAR           (12UL)
#define SECS_IN_HOUR             (SECS_IN_MINUTE*MINUTES_IN_HOUR)
#define SECS_IN_DAY              (SECS_IN_HOUR*HOURS_IN_DAY)
#define SECS_IN_YEAR             (SECS_IN_DAY*DAYS_IN_YEAR)
#define CLK_FIRST_YEAR           (1970UL)
#define XCLK_FIRST_YEAR          (1970)
#define CLK_LAST_YEAR            (2099UL)
#define XCLK_LAST_YEAR           (2481)
#define XCLK_ENTRIES             (512)
#define MAXIMUM_SECONDS_IN_TIME  (4102444800UL)
#define MAXIMUM_MILLISECONDS     (999UL)
#define MAXIMUM_DAYS_IN_XDATE    (187004UL)

/* This defines the mininum number of bits in the tick structure */
#define MQX_MIN_BITS_IN_TICK_STRUCT (64)

/* This defines the number of _mqx_uints in the tick structure */
#define MQX_NUM_TICK_FIELDS  ((MQX_MIN_BITS_IN_TICK_STRUCT+ \
   MQX_INT_SIZE_IN_BITS-1) / MQX_INT_SIZE_IN_BITS)

/*
** Define for alarm frequency. This is for pre-2.50 BSPs
** that do not have the define. New BSPs overide this define
*/
#ifndef BSP_ALARM_FREQUENCY
#define BSP_ALARM_FREQUENCY     (MILLISECS_IN_SECOND / BSP_ALARM_RESOLUTION)
#endif

/*--------------------------------------------------------------------------*/
/*
** TIME STRUCTURE
**
** A structure to represent time in seconds and milliseconds
*/

typedef struct time_struct
{

   /* The number of seconds in the time.  */
   uint_32     SECONDS;

   /* The number of milliseconds in the time. */
   uint_32     MILLISECONDS;

} TIME_STRUCT, _PTR_ TIME_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
** DATE STRUCT
**
** This structure is used to hold the time in a simple "date" format.
** Conversion functions _time_to_date, _time_from_date convert between
** this date struct and the time struct.
*/
typedef struct date_struct
{
   /* 1970 - 2099 */
   uint_16 YEAR;

   /* 1 - 12 */
   uint_16 MONTH;

   /* 1 - 31 (depending on month) */
   uint_16 DAY;

   /* 0 - 23 */
   uint_16 HOUR;

   /* 0 - 59 */
   uint_16 MINUTE;

   /* 0 - 59 */
   uint_16 SECOND;

   /* 0 - 999 */
   uint_16 MILLISEC;

} DATE_STRUCT, _PTR_ DATE_STRUCT_PTR;

/*
** This structure defines how time is maintained in the system.
** Time is kept internally in the form of ticks. This is a 64 bit
** field which is maintained in an array whose size is
** dependent upon the PSP. HW_TICKS is used to track time between
** ticks (timer interrupts).
*/
typedef struct mqx_tick_struct
{
   _mqx_uint TICKS[MQX_NUM_TICK_FIELDS];
   uint_32   HW_TICKS;
} MQX_TICK_STRUCT, _PTR_ MQX_TICK_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
** EXTENDED DATE STRUCT
**
** This structure is used to hold the time in a "date" format more detailed
** than the MQX_DATE_STRUCT.
** Conversion functions _time_ticks_to_xdate, _time_xdate_to_ticks
** convert between this extended date struct and the tick struct.
*/
typedef struct mqx_xdate_struct
{
   uint_16 YEAR;     /* Since 1970 */
   uint_16 MONTH;    /* 1 - 12 (since January)   */
   uint_16 MDAY;     /* 1 - 31 (of the month)    */
   uint_16 HOUR;     /* 0 - 23 (since midnight   */
   uint_16 MIN;      /* 0 - 59 (since last hour) */
   uint_16 SEC;      /* 0 - 59 (since last min)  */
   uint_16 MSEC;     /* 0 - 999                  */
   uint_16 USEC;     /* 0 - 999                  */
   uint_16 NSEC;     /* 0 - 999                  */
   uint_16 PSEC;     /* 0 - 999                  */
/* START CR 302 */
#if 0
   uint_16 WDAY;     /* 1 - 7 (Sunday is day 1)  */
#endif
   uint_16 WDAY;     /* 0 - 6 (Sunday is day 0)  */
/* END CR 302 */
   uint_16 YDAY;     /* 0 - 365                  */
} MQX_XDATE_STRUCT, _PTR_ MQX_XDATE_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
** TASK TEMPLATE STRUCTURE
**
** This structure defines task templates. A pointer to an array of these
** structures, terminated by an element with zero field values, is placed
** into the MQX initialization structure.
*/

typedef struct   task_template_struct
{
   /* The local unique number identifying this task template. */
   _mqx_uint            TASK_TEMPLATE_INDEX;

   /*
   ** The start address of the function which represents this task.
   ** This function will be called when a task is created with the
   ** task template index above. The task is deleted when this function
   ** returns.
   */
   void    (_CODE_PTR_  TASK_ADDRESS)(uint_32);

   /* The amount of stack space required by this task. */
   _mem_size            TASK_STACKSIZE;

   /*
   ** The software priority level of this task.
   ** These priorities start at 0, which is the highest priority.
   */
   _mqx_uint            TASK_PRIORITY;

   /* pointer to the string name of the task. */
   char           _PTR_ TASK_NAME;

   /*
   ** possible attributes for the task.
   ** Possible bit values are:
   ** MQX_AUTO_START_TASK     - create 1 instance of task at initialization time
   ** MQX_FLOATING_POINT_TASK - task uses the floating point co-processor
   ** MQX_TIME_SLICE_TASK     - task uses the time-slice scheduler
   */
   _mqx_uint             TASK_ATTRIBUTES;

   /*
   ** The value stored in this field is the default value passed as the
   ** parameter to a Task when it is created.
   */
   uint_32               CREATION_PARAMETER;

   /*
   ** This field is the default time slice to use for the task
   ** Time slice in ticks or milliseconds depending on config. bit
   ** in mqx_cnfg.h
   */
   _mqx_uint             DEFAULT_TIME_SLICE;

} TASK_TEMPLATE_STRUCT, _PTR_ TASK_TEMPLATE_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
** MQX INITIALIZATION STRUCTURE
**
** This structure defines the information required to be passed to
** MQX at initialization time.
** The structure is filled in by the user, and a pointer to it
** is passed to the function '_mqx'
**
*/

typedef  struct mqx_initialization_struct
{

   /* The processor number of this processor */
   _mqx_uint       PROCESSOR_NUMBER;

   /* The address of the start RAM for use by the local kernel. */
   pointer        START_OF_KERNEL_MEMORY;

   /*
   ** The address of the end of RAM for use by the local kernel.
   ** The user must ensure that sufficient ram has been allocated for
   ** all of the tasks.
   */
   pointer        END_OF_KERNEL_MEMORY;

   /*
   ** The size of the interrupt stack.
   ** This is the maximum amount of stack space used
   ** by all interrupt handlers.
   */
   _mqx_uint       INTERRUPT_STACK_SIZE;

   /* The address of the task template list for this processor. */
   TASK_TEMPLATE_STRUCT_PTR TASK_TEMPLATE_LIST;

   /*
   ** The maximum hardware interrupt priority level of MQX.
   ** ie, an MQX _int_disable call will only disable to this level.
   ** (Applicable to CPUs with multiple interrupt levels only)
   */
   _mqx_uint       MQX_HARDWARE_INTERRUPT_LEVEL_MAX;

   /* The maximum allowed number of message pools */
   _mqx_uint       MAX_MSGPOOLS;

   /* The maximum allowed message queue number on this processor */
   _mqx_uint       MAX_MSGQS;

   /*
   ** A string indicating which device is to be used as
   ** the default (console) I/O for the processor.
   */
   char     _PTR_ IO_CHANNEL;

   /* The open flags to pass to the default I/O device */
   char     _PTR_ IO_OPEN_MODE;

/* START CR 897 */
   /* reserved fields for later use */
/*   _mqx_uint       RESERVED[2];*/

   /* If not NULL, the location of the interrupt stack */
   pointer        INTERRUPT_STACK_LOCATION;

   _mqx_uint       RESERVED[1];

/* END CR 897 */

} MQX_INITIALIZATION_STRUCT, _PTR_ MQX_INITIALIZATION_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
** QUEUE ELEMENT STRUCTURE
**
** This structure is required in each queue element.
** The address of this structure is used to enqueue,
** dequeue elements
*/
typedef struct queue_element_struct
{

   /* next element in queue, MUST BE FIRST FIELD */
   struct queue_element_struct _PTR_ NEXT;

   /* previous element in queue, MUST BE SECOND FIELD */
   struct queue_element_struct _PTR_ PREV;

} QUEUE_ELEMENT_STRUCT, _PTR_ QUEUE_ELEMENT_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
** QUEUE STRUCTURE
**
** This structure represents a generic queue head structure.  Each queue
** element is made up of a data structure consisting of a NEXT pointer
** followed by a PREV pointer.  Thus any type of element may be queued
** onto this queue.
*/

typedef struct queue_struct
{

   /* next element in queue, MUST BE FIRST FIELD */
   struct queue_element_struct _PTR_ NEXT;

   /* previous element in queue, MUST BE SECOND FIELD */
   struct queue_element_struct _PTR_ PREV;

   /* current number of elements in the queue */
   uint_16                           SIZE;

   /* Maximum number of elements in the queue */
   uint_16                           MAX;

} QUEUE_STRUCT, _PTR_ QUEUE_STRUCT_PTR;



/*--------------------------------------------------------------------------*/
/*
**                         LWSEM STRUCTURE
**
** This structure defines a light weight semaphore.
** These sempahores implement a simple counting semaphore.
** Tasks wait on these semaphores in a FIFO manner.
** Priority inheritance is NOT implemented for these semaphores.
** The semaphores can be embedded into data structures similarly to
** mutexes.
*/
typedef struct lwsem_struct
{

   /* The next two fields are used to maintain a list of all LWSEMS */

   /* The next lwsem */
   struct lwsem_struct _PTR_  NEXT;

   /* The previous lwsem */
   struct lwsem_struct _PTR_  PREV;

   /* The queue of tasks waiting for this lwsem */
   QUEUE_STRUCT               TD_QUEUE;

   /* A validation stamp */
   _mqx_uint                  VALID;

   /* the semaphore value */
   _mqx_int                   VALUE;

} LWSEM_STRUCT, _PTR_ LWSEM_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
**                  PROTOTYPES OF KERNEL FUNCTIONS
*/

#ifdef __cplusplus
extern "C" {
#endif
#ifndef __TAD_COMPILE__
extern void             _int_default_isr(pointer);
extern void             _int_disable(void);
extern void             _int_enable(void);
extern void             _int_exception_isr(pointer);
extern void (_CODE_PTR_ _int_get_default_isr(void))(pointer);
extern void (_CODE_PTR_ _int_get_exception_handler(_mqx_uint))(_mqx_uint, _mqx_uint,
   pointer, pointer);
extern void (_CODE_PTR_ _int_get_isr(_mqx_uint))(pointer);
extern pointer          _int_get_isr_data(_mqx_uint);
extern _mqx_uint        _int_get_isr_depth(void);
extern void (_CODE_PTR_ _int_get_kernel_isr(_mqx_uint))(void);
extern _mqx_max_type    _int_get_previous_vector_table(void);
extern _mqx_max_type    _int_get_vector_table(void);
extern _mqx_uint        _int_init(_mqx_uint, _mqx_uint);
extern void (_CODE_PTR_ _int_install_default_isr(
   void (_CODE_PTR_)(pointer)))(pointer);
extern void (_CODE_PTR_ _int_install_kernel_isr(_mqx_uint,
   void (_CODE_PTR_)(void)))(void);
extern void (_CODE_PTR_ _int_install_isr(_mqx_uint,
   void (_CODE_PTR_)(pointer), pointer))(pointer);
extern void (_CODE_PTR_ _int_install_exception_isr(void))(pointer);
extern void (_CODE_PTR_ _int_install_unexpected_isr(void))(pointer);
extern void             _int_kernel_isr(void);
extern void (_CODE_PTR_ _int_set_exception_handler(_mqx_uint,
   void (_CODE_PTR_)(_mqx_uint, _mqx_uint,pointer,pointer)))(_mqx_uint, _mqx_uint,
   pointer, pointer);
extern pointer          _int_set_isr_data(_mqx_uint, pointer);
extern _mqx_max_type    _int_set_vector_table(_mqx_max_type);
extern void             _int_unexpected_isr(pointer);

extern _mqx_uint        _klog_create(_mqx_uint, _mqx_uint);
extern _mqx_uint        _klog_create_at(_mqx_uint, _mqx_uint, pointer);
extern _mqx_uint        _klog_get_interrupt_stack_usage(_mem_size_ptr, _mem_size_ptr);
extern _mqx_uint        _klog_get_task_stack_usage(_task_id, _mem_size_ptr,
   _mem_size_ptr);
extern void             _klog_control(uint_32, boolean);
extern void             _klog_disable_logging_task(_task_id);
extern boolean          _klog_display(void);
extern void             _klog_enable_logging_task(_task_id);
extern void             _klog_show_stack_usage(void);


extern _mqx_uint        _lwsem_create(LWSEM_STRUCT_PTR, _mqx_int);
extern _mqx_uint        _lwsem_destroy(LWSEM_STRUCT_PTR);
extern _mqx_uint        _lwsem_poll(LWSEM_STRUCT_PTR);
extern _mqx_uint        _lwsem_post(LWSEM_STRUCT_PTR);
extern _mqx_uint        _lwsem_test(pointer _PTR_, pointer _PTR_);
extern _mqx_uint        _lwsem_wait(LWSEM_STRUCT_PTR);
extern _mqx_uint        _lwsem_wait_ticks(LWSEM_STRUCT_PTR, _mqx_uint);
extern _mqx_uint        _lwsem_wait_for(LWSEM_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR);
extern _mqx_uint        _lwsem_wait_until(LWSEM_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR);

extern void _PTR_       _mem_alloc(_mem_size);
extern void _PTR_       _mem_alloc_from(_mem_pool_id, _mem_size);
extern void _PTR_       _mem_alloc_zero(_mem_size);
extern void _PTR_       _mem_alloc_zero_from(_mem_pool_id, _mem_size);
extern pointer          _mem_alloc_system(_mem_size);
extern pointer          _mem_alloc_system_from(_mem_pool_id, _mem_size);
extern pointer          _mem_alloc_system_zero(_mem_size);
extern pointer          _mem_alloc_system_zero_from(_mem_pool_id, _mem_size);
extern void             _mem_copy(pointer, pointer, _mem_size);
extern _mem_pool_id     _mem_create_pool(pointer, _mem_size);
extern _mqx_uint        _mem_extend(pointer, _mem_size);
extern _mqx_uint        _mem_extend_pool(_mem_pool_id, pointer, _mem_size);
extern _mqx_uint        _mem_free(pointer);
extern _mqx_uint        _mem_free_part(pointer, _mem_size);
extern pointer          _mem_get_error(void);
extern pointer          _mem_get_error_pool(_mem_pool_id);
extern void _PTR_       _mem_get_highwater(void);
extern void _PTR_       _mem_get_highwater_pool(_mem_pool_id);
extern _mem_size        _mem_get_size(pointer);
extern uint_32          _mem_sum_ip(uint_32, _mem_size, pointer);
extern void             _mem_swap_endian(uchar _PTR_, pointer);
/* Start CR 855 */
extern void             _mem_swap_endian_len(uchar _PTR_, pointer, _mqx_uint);
/* End   CR 855 */
extern _mqx_uint        _mem_test(void);
extern _mqx_uint        _mem_test_all(_mem_pool_id _PTR_);
extern _mqx_uint        _mem_test_pool(_mem_pool_id);
extern _mqx_uint        _mem_test_and_set(uchar_ptr);
extern _mqx_uint        _mem_transfer(pointer, _task_id, _task_id);
extern void             _mem_zero(pointer, _mem_size);

extern _mqx_uint        _mem_verify(pointer base, pointer extent); // Added 10/22/2003

extern _mqx_uint         _mqx(MQX_INITIALIZATION_STRUCT_PTR);
extern const MQX_TICK_STRUCT  _mqx_zero_tick_struct;
extern const char _PTR_  _mqx_copyright;
extern const char _PTR_  _mqx_date;
extern const char _PTR_  _mqx_generic_revision;
extern const char _PTR_  _mqx_psp_revision;
extern const char _PTR_  _mqx_bsp_revision;
extern const char _PTR_  _mqx_io_revision;
extern void              _mqx_exit(_mqx_uint);
extern void              _mqx_fatal_error(_mqx_uint);
extern _mqx_uint         _mqx_get_counter(void);
extern _mqx_uint         _mqx_get_cpu_type(void);
extern MQX_INITIALIZATION_STRUCT_PTR _mqx_get_initialization(void);
extern pointer           _mqx_get_kernel_data(void);
extern void (_CODE_PTR_  _mqx_get_exit_handler(void))(void);
extern _task_id          _mqx_get_system_task_id(void);
extern pointer           _mqx_get_tad_data(pointer);
extern void              _mqx_idle_task(uint_32);
extern volatile _mqx_uint _mqx_monitor_type;
extern void              _mqx_set_cpu_type(_mqx_uint);
extern void              _mqx_set_exit_handler(void (_CODE_PTR_)(void));
extern void              _mqx_set_tad_data(pointer, pointer);
extern const char _PTR_  _mqx_version;


extern QUEUE_ELEMENT_STRUCT_PTR _queue_dequeue(QUEUE_STRUCT_PTR);
extern boolean                  _queue_enqueue(QUEUE_STRUCT_PTR,
   QUEUE_ELEMENT_STRUCT_PTR);
extern _mqx_uint                _queue_get_size(QUEUE_STRUCT_PTR);
extern void                     _queue_init(QUEUE_STRUCT_PTR, uint_16);
extern boolean                  _queue_insert(QUEUE_STRUCT_PTR, QUEUE_ELEMENT_STRUCT_PTR,
   QUEUE_ELEMENT_STRUCT_PTR);
extern boolean                  _queue_is_empty(QUEUE_STRUCT_PTR);
extern QUEUE_ELEMENT_STRUCT_PTR _queue_head(QUEUE_STRUCT_PTR);
extern QUEUE_ELEMENT_STRUCT_PTR _queue_next(QUEUE_STRUCT_PTR, QUEUE_ELEMENT_STRUCT_PTR);
extern void                     _queue_unlink(QUEUE_STRUCT_PTR,
   QUEUE_ELEMENT_STRUCT_PTR);
extern _mqx_uint                _queue_test(QUEUE_STRUCT_PTR, pointer _PTR_);


extern _mqx_uint         _sched_get_max_priority(_mqx_uint);
extern _mqx_uint         _sched_get_min_priority(_mqx_uint);
extern uint_32           _sched_get_rr_interval(_task_id, uint_32_ptr);
extern _mqx_uint         _sched_get_rr_interval_ticks(_task_id, MQX_TICK_STRUCT_PTR);
extern _mqx_uint         _sched_get_policy(_task_id, _mqx_uint_ptr);
extern _mqx_uint         _sched_set_policy(_task_id, _mqx_uint);
extern uint_32           _sched_set_rr_interval(_task_id, uint_32);
extern _mqx_uint         _sched_set_rr_interval_ticks(_task_id,
   MQX_TICK_STRUCT_PTR, MQX_TICK_STRUCT_PTR);
extern void              _sched_yield(void);


extern _mqx_uint         _task_abort(_task_id);
extern void              _task_block(void);
extern _mqx_uint         _task_check_stack(void);
extern _task_id          _task_create(_processor_number, _mqx_uint, uint_32);
extern _task_id          _task_create_blocked(_processor_number, _mqx_uint, uint_32);
/* START CR 897 */
extern _task_id          _task_create_at(_processor_number, _mqx_uint,
   uint_32, pointer, _mem_size);
/* END CR 897 */
extern _mqx_uint         _task_destroy(_task_id);
extern void              _task_disable_fp(void);
extern void              _task_enable_fp(void);
extern _task_id          _task_get_creator(void);
extern pointer           _task_get_environment(_task_id);
extern void (_CODE_PTR_  _task_get_exception_handler(_task_id))(_mqx_uint,pointer);
extern _mqx_uint         _task_get_error(void);
extern _mqx_uint   _PTR_ _task_get_error_ptr(void);
extern void (_CODE_PTR_  _task_get_exit_handler(_task_id))(void);
extern _task_id          _task_get_id(void);
extern _task_id          _task_get_id_from_name(char_ptr);
extern _mqx_uint         _task_get_index_from_id(_task_id);
extern uint_32           _task_get_parameter(void);
extern uint_32           _task_get_parameter_for(_task_id);
extern _processor_number _task_get_processor(_task_id);
extern _mqx_uint         _task_get_priority(_task_id, _mqx_uint_ptr);
extern pointer           _task_get_td(_task_id);
extern _mqx_uint         _task_get_template_index(char_ptr);
extern TASK_TEMPLATE_STRUCT_PTR _task_get_template_ptr(_task_id);
extern void              _task_ready(pointer);
extern _mqx_uint         _task_restart(_task_id, uint_32_ptr, boolean);
extern pointer           _task_set_environment(_task_id, pointer);
extern _mqx_uint         _task_set_error(_mqx_uint);
extern void (_CODE_PTR_  _task_set_exception_handler(_task_id,
   void (_CODE_PTR_)(_mqx_uint,pointer)))(_mqx_uint,pointer);
extern void (_CODE_PTR_  _task_set_exit_handler(_task_id,
   void (_CODE_PTR_)(void)))(void);
extern uint_32           _task_set_parameter(uint_32);
extern uint_32           _task_set_parameter_for(uint_32, _task_id);
extern _mqx_uint         _task_set_priority(_task_id, _mqx_uint, _mqx_uint_ptr);
extern void              _task_stop_preemption(void);
extern void              _task_start_preemption(void);

/* Start CR 1124: C runtime thread local storage */
extern _mqx_uint         _task_reserve_space(_mqx_uint);
extern pointer           _task_get_reserved_base(void);
/* End   CR 1124 */


extern pointer           _taskq_create(_mqx_uint);
extern _mqx_uint         _taskq_destroy(pointer);
extern _mqx_uint         _taskq_resume(pointer, boolean);
extern _mqx_uint         _taskq_suspend(pointer);
extern _mqx_uint         _taskq_suspend_task(_task_id, pointer);
extern _mqx_uint         _taskq_test(pointer _PTR_, pointer _PTR_);
extern _mqx_uint         _taskq_get_value(pointer);

extern MQX_TICK_STRUCT_PTR  _time_add_day_to_ticks(MQX_TICK_STRUCT_PTR,
   _mqx_uint);
extern MQX_TICK_STRUCT_PTR  _time_add_hour_to_ticks(MQX_TICK_STRUCT_PTR,
   _mqx_uint);
extern MQX_TICK_STRUCT_PTR  _time_add_min_to_ticks(MQX_TICK_STRUCT_PTR,
   _mqx_uint);
extern MQX_TICK_STRUCT_PTR  _time_add_msec_to_ticks(MQX_TICK_STRUCT_PTR,
   _mqx_uint);
extern MQX_TICK_STRUCT_PTR  _time_add_nsec_to_ticks(MQX_TICK_STRUCT_PTR,
   _mqx_uint);
extern MQX_TICK_STRUCT_PTR  _time_add_sec_to_ticks(MQX_TICK_STRUCT_PTR,
   _mqx_uint);
extern MQX_TICK_STRUCT_PTR  _time_add_usec_to_ticks(MQX_TICK_STRUCT_PTR,
   _mqx_uint);
extern MQX_TICK_STRUCT_PTR  _time_add_psec_to_ticks(MQX_TICK_STRUCT_PTR,
   _mqx_uint);

extern void                 _time_delay(uint_32);
extern void                 _time_delay_ticks(_mqx_uint);
extern void                 _time_delay_for(MQX_TICK_STRUCT_PTR);
extern void                 _time_delay_until(MQX_TICK_STRUCT_PTR);
extern void                 _time_dequeue(_task_id);
extern void                 _time_dequeue_td(pointer);
extern void                 _time_diff(TIME_STRUCT_PTR, TIME_STRUCT_PTR,
   TIME_STRUCT_PTR);
extern int_32               _time_diff_days(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, boolean _PTR_);
extern int_32               _time_diff_hours(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, boolean _PTR_);
extern int_32               _time_diff_minutes(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, boolean _PTR_);
extern int_32               _time_diff_milliseconds(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, boolean _PTR_);
extern int_32               _time_diff_nanoseconds(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, boolean _PTR_);
extern int_32               _time_diff_picoseconds(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, boolean _PTR_);
extern int_32               _time_diff_seconds(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, boolean _PTR_);
extern int_32               _time_diff_microseconds(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, boolean _PTR_);
extern _mqx_uint            _time_diff_ticks(MQX_TICK_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR, MQX_TICK_STRUCT_PTR);
extern boolean              _time_from_date(DATE_STRUCT_PTR, TIME_STRUCT_PTR);
extern void                 _time_get(TIME_STRUCT_PTR);
extern void                 _time_get_elapsed(TIME_STRUCT_PTR);
extern void                 _time_get_elapsed_ticks(MQX_TICK_STRUCT_PTR);
extern uint_32              _time_get_hwticks(void);
extern uint_32              _time_get_hwticks_per_tick(void);
extern uint_16              _time_get_microseconds(void);
extern uint_32              _time_get_nanoseconds(void);
extern _mqx_uint            _time_get_resolution(void);
extern void                 _time_get_ticks(MQX_TICK_STRUCT_PTR);
extern _mqx_uint            _time_get_ticks_per_sec(void);
extern _mqx_uint            _time_init_ticks(MQX_TICK_STRUCT_PTR, _mqx_uint);
extern boolean              _time_normalize_xdate(MQX_XDATE_STRUCT_PTR);
extern void                 _time_notify_kernel(void);
extern void                 _time_set(TIME_STRUCT_PTR);
extern void                 _time_set_hwtick_function(
   uint_32 (_CODE_PTR_)(pointer), pointer);
extern void                 _time_set_hwticks_per_tick(uint_32);
extern _mqx_uint            _time_set_resolution(_mqx_uint);
extern void                 _time_set_ticks(MQX_TICK_STRUCT_PTR);
extern void                 _time_set_ticks_per_sec(_mqx_uint);
extern void                 _time_set_timer_vector(_mqx_uint);
extern boolean              _time_ticks_to_xdate(MQX_TICK_STRUCT_PTR,
   MQX_XDATE_STRUCT_PTR);
extern boolean              _time_to_date(TIME_STRUCT_PTR, DATE_STRUCT_PTR);
extern boolean              _time_xdate_to_ticks(MQX_XDATE_STRUCT_PTR,
   MQX_TICK_STRUCT_PTR);
extern boolean              _time_to_ticks(TIME_STRUCT_PTR, MQX_TICK_STRUCT_PTR);
extern boolean              _ticks_to_time(MQX_TICK_STRUCT_PTR, TIME_STRUCT_PTR);

/* Start CR 1124: C runtime thread local storage */
#define MQX_TLS_SUPPORTED   1 /* Thread local storage supported */
extern pointer              _crt_tls_reference(void);
/* End   CR 1124 */

#endif

#ifdef __cplusplus
}
#endif

/*--------------------------------------------------------------------------*/
/*
**                  SPECIAL MEMORY ALLOCATOR OPTION
*/

#if MQX_USE_LWMEM_ALLOCATOR
#ifndef __MEMORY_MANAGER_COMPILE__
#include "lwmem.h"
#define _mem_alloc _lwmem_alloc
#define _mem_alloc_from _lwmem_alloc_from
#define _mem_alloc_zero _lwmem_alloc_zero
#define _mem_alloc_zero_from _lwmem_alloc_zero_from
#define _mem_alloc_system _lwmem_alloc_system
#define _mem_alloc_system_from _lwmem_alloc_system_from
#define _mem_alloc_system_zero _lwmem_alloc_system_zero
#define _mem_alloc_system_zero_from _lwmem_alloc_system_zero_from
#define _mem_free _lwmem_free
#define _mem_transfer _lwmem_transfer
#endif
#endif

#endif
/* EOF */

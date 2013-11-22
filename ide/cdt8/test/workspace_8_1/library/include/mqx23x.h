#ifndef __mqx23x_h__
#define __mqx23x_h__ 1
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
*** File: mqx23x.h
***
*** Comments: 
*** 
***    This include file provides defines to allow for MQX2.3x code to
***  be recompiled so that it will run on MQX2.40.
***
***    BSP writers should include bsp23x.h
***    PSP writers should include psp23x.h
*** 
***************************************************************************
*END**********************************************************************/

/* New header files required */
#include "message.h"
#include "fio.h"
#include "io.h"

/* Changes to mqx error codes */
#define OK                                MQX_OK

#define NULL_TASK_ID                      MQX_NULL_TASK_ID
#define DEFAULT_TASK_ID                   MQX_DEFAULT_TASK_ID
#define NULL_POOL_ID                      MSGPOOL_NULL_POOL_ID
#define MQX_NOT_A_NOTIFIER_FUNCTION       MQX_CANNOT_CALL_FUNCTION_FROM_ISR

#define MQX_OUT_OF_MSG_BUFFERS            MSGPOOL_OUT_OF_MESSAGES
#define MQX_INVALID_QUEUE_ID              MSGQ_INVALID_QUEUE_ID
#define MQX_QUEUE_IN_USE                  MSGQ_QUEUE_IN_USE
#define MQX_NOT_QUEUE_OWNER               MSGQ_NOT_QUEUE_OWNER
#define MQX_QUEUE_IS_NOT_OPEN             MSGQ_QUEUE_IS_NOT_OPEN
#define MQX_EXCEEDED_MAXIMUM_NUMBER_OF_BUFFER_POOLS MSGPOOL_OUT_OF_POOLS
#define MQX_INVALID_POOL_ID               MSGPOOL_INVALID_POOL_ID
#define MQX_ALL_POOL_BUFFERS_NOT_FREE     MSGPOOL_ALL_MESSAGES_NOT_FREE
#define MQX_POOL_BUFFER_SIZE_TOO_SMALL    MSGPOOL_MESSAGE_SIZE_TOO_SMALL
#define MQX_NO_MESSAGE_AVAILABLE          MSGQ_MESSAGE_NOT_AVAILABLE

#define MQX_INVALID_SEND_BUFFER           MSGQ_INVALID_MESSAGE
#define MQX_INACTIVE_QUEUE                MSGQ_INVALID_QUEUE_ID
#define MQX_QUEUE_FULL                    MSGQ_QUEUE_FULL
/* Start CR 2290 
#define MQX_RECEIVE_TIMEOUT               MSGQ_RECEIVE_TIMEOUT
   End CR 2290 */

#define MQX_COULD_NOT_CREATE_BUFFER_POOLS MSGPOOL_POOL_NOT_CREATED
#define MQX_NUMBER_OF_QUEUES_TOO_LARGE    MSGQ_TOO_MANY_QUEUES
#define MQX_TIMER_NOTIFIER_INSTALL_FAIL   MQX_TIMER_ISR_INSTALL_FAIL

#define SCHED_INVALID_POLICY              MQX_SCHED_INVALID_POLICY
#define SCHED_INVALID_PARAMETER_PTR       MQX_SCHED_INVALID_PARAMETER_PTR
#define SCHED_INVALID_PARAMETER           MQX_SCHED_INVALID_PARAMETER
#define SCHED_INVALID_TASK_ID             MQX_SCHED_INVALID_TASK_ID

/* Posix error codes */
#define _errno          _mqx_errno  

#define EOK             MQX_EOK             
#define E2BIG           MQX_E2BIG           
#define EACCES          MQX_EACCES          
#define EAGAIN          MQX_EAGAIN          
#define EBADF           MQX_EBADF           
#define EBADMSG         MQX_EBADMSG         
#define EBUSY           MQX_EBUSY           
#define ECANCELED       MQX_ECANCELED       
#define ECHILD          MQX_ECHILD          
#define EDEADLK         MQX_EDEADLK         
#define EDOM            MQX_EDOM            
#define EEXIST          MQX_EEXIST          
#define EFAULT          MQX_EFAULT          
#define EFBIG           MQX_EFBIG           
#define EINPROGRESS     MQX_EINPROGRESS     
#define EINTR           MQX_EINTR           
#define EINVAL          MQX_EINVAL          
#define EIO             MQX_EIO             
#define EISDIR          MQX_EISDIR          
#define EMFILE          MQX_EMFILE          
#define EMLINK          MQX_EMLINK          
#define EMSGSIZE        MQX_EMSGSIZE        
#define ENAMETOOLONG    MQX_ENAMETOOLONG    
#define ENFILE          MQX_ENFILE          
#define ENODEV          MQX_ENODEV          
#define ENOENT          MQX_ENOENT          
#define ENOEXEC         MQX_ENOEXEC         
#define ENOLCK          MQX_ENOLCK          
#define ENOMEM          MQX_ENOMEM          
#define ENOSPC          MQX_ENOSPC          
#define ENOSYS          MQX_ENOSYS          
#define ENOTDIR         MQX_ENOTDIR         
#define ENOTEMPTY       MQX_ENOTEMPTY       
#define ENOTSUP         MQX_ENOTSUP         
#define ENOTTY          MQX_ENOTTY          
#define ENXIO           MQX_ENXIO           
#define EPERM           MQX_EPERM           
#define EPIPE           MQX_EPIPE           
#define ERANGE          MQX_ERANGE          
#define EROFS           MQX_EROFS           
#define ESPIPE          MQX_ESPIPE          
#define ESRCH           MQX_ESRCH           
#define ETIMEDOUT       MQX_ETIMEDOUT       
#define EXDEV           MQX_EXDEV           

/* Task template initializers */
#define AUTO_START_TASK      MQX_AUTO_START_TASK
#define FLOATING_POINT_TASK  MQX_FLOATING_POINT_TASK
#define TIME_SLICE_TASK      MQX_TIME_SLICE_TASK

#define MONITOR_TYPE_NONE    MQX_MONITOR_TYPE_NONE
#define MONITOR_TYPE_MON     MQX_MONITOR_TYPE_MON
#define MONITOR_TYPE_MON_INT MQX_MONITOR_TYPE_MON_INT
#define MONITOR_TYPE_BDM     MQX_MONITOR_TYPE_BDM
#define MONITOR_TYPE_OTHER   MQX_MONITOR_TYPE_OTHER

/* Message definitions */
#define ANY_QUEUE            MSGQ_ANY_QUEUE
#define FREE_QUEUE           MSGQ_FREE_QUEUE
#define MAX_MESSAGE_SIZE     (MAX_UINT_16 - sizeof(MESSAGE_HEADER_STRUCT))  
#define MSG_HDR_INTEL        MSG_HDR_LITTLE_ENDIAN
#define MSG_DATA_INTEL       MSG_DATA_LITTLE_ENDIAN
#define MSG_HDR_MOTOROLA     MSG_HDR_BIG_ENDIAN
#define MSG_DATA_MOTOROLA    MSG_DATA_BIG_ENDIAN
#define MSG_IS_HDR_INTEL     MSG_IS_HDR_LITTLE_ENDIAN
#define MSG_IS_DATA_INTEL    MSG_IS_DATA_LITTLE_ENDIAN
#define MSG_IS_HDR_MOTOROLA  MSG_IS_HDR_BIG_ENDIAN
#define MSG_IS_DATA_MOTOROLA MSG_IS_DATA_BIG_ENDIAN
#define NULL_QUEUE_ID        MSGO_NULL_QUEUE_ID


/* I/O Definitions */
#define STDIN                IO_STDIN
#define STDOUT               IO_STDOUT
#define STDERR               IO_STDERR
#define PROC_STDIN           IO_PROC_STDIN
#define PROC_STDOUT          IO_PROC_STDOUT
#define PROC_STDERR          IO_PROC_STDERR

/* Scheduler defines */
#define SCHED_FIFO            MQX_SCHED_FIFO
#define SCHED_RR              MQX_SCHED_RR
#define SCHED_TASK_QUEUE_FIFO MQX_TASK_QUEUE_FIFO
#define SCHED_TASK_QUEUE_BY_PRIORITY MQX_TASK_QUEUE_BY_PRIORITY
                             
/* CPU types */
#define CPU_TYPE_MC68000     PSP_CPU_TYPE_MC68000     
#define CPU_TYPE_MC68010     PSP_CPU_TYPE_MC68010     
#define CPU_TYPE_MC68020     PSP_CPU_TYPE_MC68020     
#define CPU_TYPE_MC68030     PSP_CPU_TYPE_MC68030     
#define CPU_TYPE_MC68040     PSP_CPU_TYPE_MC68040     
#define CPU_TYPE_MC68302     PSP_CPU_TYPE_MC68302     
#define CPU_TYPE_MC68332     PSP_CPU_TYPE_MC68332     
#define CPU_TYPE_MC68331     PSP_CPU_TYPE_MC68331     
#define CPU_TYPE_MCCPU32     PSP_CPU_TYPE_MCCPU32     
#define CPU_TYPE_MC68330     PSP_CPU_TYPE_MC68330     
#define CPU_TYPE_MC68333     PSP_CPU_TYPE_MC68333     
#define CPU_TYPE_MC68334     PSP_CPU_TYPE_MC68334     
#define CPU_TYPE_MC68340     PSP_CPU_TYPE_MC68340     
#define CPU_TYPE_MC68341     PSP_CPU_TYPE_MC68341     
#define CPU_TYPE_MC68306     PSP_CPU_TYPE_MC68306     
#define CPU_TYPE_MC68307     PSP_CPU_TYPE_MC68307     
#define CPU_TYPE_MC68356     PSP_CPU_TYPE_MC68356     
#define CPU_TYPE_MC68322     PSP_CPU_TYPE_MC68322     
#define CPU_TYPE_MC68060     PSP_CPU_TYPE_MC68060     
#define CPU_TYPE_MC68360     PSP_CPU_TYPE_MC68360     
#define CPU_TYPE_MC68349     PSP_CPU_TYPE_MC68349     
#define CPU_TYPE_MC88000     PSP_CPU_TYPE_MC88000     
#define CPU_TYPE_MC88110     PSP_CPU_TYPE_MC88110     
#define CPU_TYPE_MC96002     PSP_CPU_TYPE_MC96002     
#define CPU_TYPE_I8088       PSP_CPU_TYPE_I8088       
#define CPU_TYPE_I8086       PSP_CPU_TYPE_I8086       
#define CPU_TYPE_I80186      PSP_CPU_TYPE_I80186      
#define CPU_TYPE_I80286      PSP_CPU_TYPE_I80286      
#define CPU_TYPE_I80386      PSP_CPU_TYPE_I80386      
#define CPU_TYPE_I80486      PSP_CPU_TYPE_I80486      
#define CPU_TYPE_IPENTIUM    PSP_CPU_TYPE_IPENTIUM    
#define CPU_TYPE_I860        PSP_CPU_TYPE_I860        
#define CPU_TYPE_I960        PSP_CPU_TYPE_I960        
#define CPU_TYPE_POWERPC_403 PSP_CPU_TYPE_POWERPC_403 
#define CPU_TYPE_POWERPC_505 PSP_CPU_TYPE_POWERPC_505 
#define CPU_TYPE_POWERPC_601 PSP_CPU_TYPE_POWERPC_601 
#define CPU_TYPE_POWERPC_602 PSP_CPU_TYPE_POWERPC_602 
#define CPU_TYPE_POWERPC_603 PSP_CPU_TYPE_POWERPC_603 
#define CPU_TYPE_POWERPC_604 PSP_CPU_TYPE_POWERPC_604 
#define CPU_TYPE_POWERPC_821 PSP_CPU_TYPE_POWERPC_821 
#define CPU_TYPE_POWERPC_860 PSP_CPU_TYPE_POWERPC_860 
#define CPU_TYPE_TMS320C30   PSP_CPU_TYPE_TMS320C30   
#define CPU_TYPE_TMS320C31   PSP_CPU_TYPE_TMS320C31   
#define CPU_TYPE_TMS320C40   PSP_CPU_TYPE_TMS320C40   
#define CPU_TYPE_TMS320C44   PSP_CPU_TYPE_TMS320C44   
#define CPU_TYPE_SHARC       PSP_CPU_TYPE_SHARC       
#define CPU_TYPE_R3000       PSP_CPU_TYPE_R3000       
#define CPU_TYPE_R4000       PSP_CPU_TYPE_R4000       
#define CPU_TYPE_CW4010      PSP_CPU_TYPE_CW4010      
#define CPU_TYPE_CW4001      PSP_CPU_TYPE_CW4001      
#define CPU_TYPE_CW4002      PSP_CPU_TYPE_CW4002      
#define CPU_TYPE_CW4003      PSP_CPU_TYPE_CW4003      
#define CPU_TYPE_MCF5102     PSP_CPU_TYPE_MCF5102     
#define CPU_TYPE_MCF5202     PSP_CPU_TYPE_MCF5202     
#define CPU_TYPE_MCF5203     PSP_CPU_TYPE_MCF5203     
#define CPU_TYPE_MCF5204     PSP_CPU_TYPE_MCF5204     
#define CPU_TYPE_MCF5206     PSP_CPU_TYPE_MCF5206     
#define CPU_TYPE_PTHREADS    PSP_CPU_TYPE_PTHREADS    
#define CPU_TYPE_MSTHREADS   PSP_CPU_TYPE_MSTHREADS 

/* Changes to mqx function names from mqx.h */
#define _Add_ready                        _task_ready
#define _Block                            _task_block
#define _Close_queue                      _msgq_close
#define _Convert_to_qid                   _msgq_get_id
#define _Convert_to_td                    _task_get_td
#define _Copy                             _mem_copy
#define _Copyright                        _mqx_copyright
#define _Create(proc,index,param) \
   _task_create(proc, index, param); _sched_yield();
#define _Create_pool                      _msgpool_create_system
#define _Create_named_pool                _msgpool_create
#define _Date                             _mqx_date
#define _Destroy                          _task_destroy
#define _Destroy_named_pool               _msgpool_destroy
#define _Disable                          _int_disable
#define _Enable                           _int_enable
#define _Father_id                        _task_get_creator
#define _Flint                            _int_kernel_isr
#define _Free_buffer                      _msg_free
#define _Freevec                          _mem_free
#define _Get_alarm_resolution             _time_get_resolution
#define _Get_buffer                       _msg_alloc_system
#define _Get_counter                      _mqx_get_counter
#define _Get_MQX_init_struct              _mqx_get_initialization
#define _Get_kernel_data                  _mqx_get_kernel_data
#define _Get_kernel_time                  _time_get_elapsed
#define _Get_named_buffer                 _msg_alloc
#define _Get_stdio                        _io_get_handle
#define _Get_time                         _time_get
#define _Getvec                           _mem_alloc
#define _Getzerovec                       _mem_alloc_zero
#define _Highestmem                       _mem_get_highwater
#define _Home_processor                   _task_get_processor
#define _Idle_loop_task                   _mqx_idle_task
#define _Install_first_level_notifier     _int_install_kernel_isr
#define _Install_notifier                 _int_install_isr
#define _Install_unexpected_interrupt_handlers _int_install_unexpected_isr
#define _Local_task_manager_task          _ipc_task
#define _IP_Routing_table                 _ipc_routing_table
#define _Memory_block_in_error            _mem_get_error
#define _Message_pending                  _msgq_get_count
#define _MEM_Add_kernel_memory            _mem_extend
#define _MQX                              _mqx
#define _MQX_Exit()                       _mqx_exit(0)
#define _MQX_Get_first_level_notifier     _int_get_kernel_isr
#define _MQX_Get_microseconds             _time_get_microseconds
#define _MQX_Get_notifier                 _int_get_isr
#define _MQX_Get_notifier_data            _int_get_isr_data
#define _MQX_Get_task_parameter           _task_get_parameter
#define _MQX_Klog_display                 _klog_display
#define _MQX_Log_control                  _klog_control
#define _MQX_Set_task_parameter           _task_set_parameter
#define _MQX_Show_stack_usage             _klog_show_stack_usage
#define _MQX_Task_enable_logging          _klog_enable_logging_task
#define _MQX_Task_disable_logging         _klog_disable_logging_task
#define _My_id                            _task_get_id
#define _Open_queue                       _msgq_open
#define _Open_notifier_queue              _msgq_open_system
#define _Receive_message                  _msgq_receive
#define _Receive_message_queue            _msgq_poll
#define _Remove_time_queue                _time_dequeue
#define _Reschedule                       _sched_yield
#define _sched_destroy_task_queue         _taskq_destroy
#define _sched_get_priority_max           _sched_get_max_priority
#define _sched_get_priority_min           _sched_get_min_priority
#define _sched_init_task_queue            _taskq_create
#define _sched_resume                     _taskq_resume
#define _sched_size_task_queue            _taskq_get_value
#define _sched_suspend                    _taskq_suspend
#define _Send_message                     _msgq_send
#define _Send_message_blocked             _msgq_send_block
#define _Set_alarm_resolution             _time_set_resolution
#define _Set_kernel_monitor_entry         _mqx_set_exit_handler
#define _Set_task_error_code              _task_set_error
#define _Set_time                         _time_set
#define _Set_stdio                        _io_set_handle
#define _Sizevec                          _mem_get_size
#define _Stackoverflow                    _task_check_stack
#define _Swap_endian                      _mem_swap_endian
#define _Swap_endian_header               _msg_swap_endian_header
#define _Swap_endian_data                 _msg_swap_endian_data
#define _Task_error_code                  _task_get_error
#define _Task_error_code_ptr              _task_get_error_ptr
#define _Task_fp_enable                   _task_enable_fp
#define _Task_fp_disable                  _task_disable_fp
#define _Task_stop_preemption             _task_stop_preemption
#define _Task_restore_preemption          _task_start_preemption
#define _Testmem                          _mem_test
#define _Test_and_set                     _mem_test_and_set
#define _Timeout                          _time_delay
#define _Trimvec                          _mem_free_part
#define _Transfer_memory_block            _mem_transfer
#define _Version                          _mqx_version
#define _Zeromem                          _mem_zero

/* Changes from IO portion of mqx.h */
#define _BWinit                _io_poll_init
#define _BWputc                _io_poll_putc
#define _BWgetc                _io_poll_getc
#define _BWstatus              _io_poll_status
#define _BWgetline             _io_poll_getline
#define _BWprintf              _io_poll_printf

#define _INTio_init            _io_int_init
#define _INTio_putc            _io_int_putc
#define _INTio_getc            _io_int_getc
#define _INTio_status          _io_int_status
#define _INTio_getline         _io_int_getline

#define _Doprint               _io_doprint
#define _Fopen                 _io_fopen
#define _Printf                _io_printf
#define _Scanf                 _io_scanf
#define _Sprintf               _io_sprintf
#define _Sscanf                _io_sscanf
#define _Vprintf               _io_vprintf
#define _Vfprintf              _io_vfprintf
#define _Vsprintf              _io_vsprintf

#define _Fgetc                 _io_fgetc
#define _Fgetline              _io_fgetline
#define _Fgets                 _io_fgets
#define _Fprintf               _io_fprintf
#define _Fputc                 _io_fputc
#define _Fputs                 _io_fputs
#define _Fscanf                _io_fscanf
#define _Fstatus               _io_fstatus
#define _Fungetc               _io_fungetc
#define _Scanline              _io_scanline
#define _Getchar               _io_getchar
#define _Getline               _io_getline
#define _Gets                  _io_gets
#define _Putchar               _io_putchar
#define _Putc                  _io_putc
#define _Puts                  _io_puts
#define _Status                _io_status
#define _Ungetc                _io_ungetc

/* Serial I/O definitions */
#define MQX_DO_XON_XOFF        IO_SERIAL_XON_XOFF
#define MQX_DO_TRANSLATION     IO_SERIAL_TRANSLATION
#define MQX_DO_ECHO            IO_SERIAL_ECHO

#define MQX_STDIN              IO_STDIN
#define MQX_STDOUT             IO_STDOUT 
#define MQX_STDERR             IO_STDERR

/* changes from clock.h */
#define CLK_Time_from_struct   _time_from_date
#define CLK_Time_to_struct     _time_to_date
#define CLK_TIME_STRUCT        DATE_STRUCT
#define CLK_TIME_STRUCT_PTR    DATE_STRUCT_PTR

/* Changes to pthread.h (file no longer exists) */
#define pthread_mutex_create_component     _mutex_create_component
#define pthread_mutex_destroy              _mutex_destroy
#define pthread_mutex_getprioceiling       _mutex_get_priority_ceiling
#define pthread_mutex_setprioceiling       _mutex_set_priority_ceiling
#define pthread_mutex_init                 _mutex_init
#define pthread_mutex_lock                 _mutex_lock
#define pthread_mutex_try_lock             _mutex_try_lock
#define pthread_mutex_unlock               _mutex_unlock
#define pthread_mutex_task_wait_count      _mutex_get_wait_count
#define pthread_mutexattr_destroy          _mutatr_destroy
#define pthread_mutexattr_getlimitspin     _mutatr_get_spin_limit
#define pthread_mutexattr_setlimitspin     _mutatr_set_spin_limit
#define pthread_mutexattr_getprioceiling   _mutatr_get_priority_ceiling
#define pthread_mutexattr_setprioceiling   _mutatr_set_priority_ceiling
#define pthread_mutexattr_getprotocol      _mutatr_get_sched_protocol
#define pthread_mutexattr_setprotocol      _mutatr_set_sched_protocol
#define pthread_mutexattr_getwaitingpolicy _mutatr_get_wait_protocol
#define pthread_mutexattr_setwaitingpolicy _mutatr_set_wait_protocol
#define pthread_mutexattr_init             _mutatr_init
#define pthread_mutex_attr_t               MUTEX_ATTR_STRUCT
#define pthread_mutex_t                    MUTEX_STRUCT
#define PRIO_INHERIT                       MUTEX_PRIO_INHERIT
#define PRIO_PROTECT                       MUTEX_PRIO_PROTECT

/* Changes to event.h */
#define _event_mkevent         _event_create
#define _event_task_wait_count _event_get_wait_count
#define _event_value           _event_get_value

/* Changes to sem.h */
#define _sem_mksem             _sem_create
#define _sem_task_wait_count   _sem_get_wait_count
#define _sem_value             _sem_get_value

/* Changes to name.h */
#define _name_add_name         _name_add
#define _name_find_name        _name_find
#define _name_delete_name      _name_delete

/* Changes to log.h */
#define LOG_ENTRY_HEADER_STRUCT     LOG_ENTRY_STRUCT
#define LOG_ENTRY_HEADER_STRUCT_PTR LOG_ENTRY_STRUCT_PTR

/* Changes from the PSP file proc.h */
#define _Disable_dc               _dcache_disable
#define _Disable_ic               _icache_disable
#define _Enable_dc                _dcache_enable
#define _Enable_ic                _icache_enable
#define _Flsh_dc_line             _dcache_flush_line
#define _Flsh_dc_and_disable      _dcache_flush_and_disable
#define _Flsh_dc_mlines           _dcache_flush_mlines
#define _Flush_dc                 _dcache_flush
#define _Flush_ic                 _icache_flush
#define _Get_int_vector_table     _int_get_vector_table
#define _Get_trap_vector_table    _int_get_trap_vector_table
#define _Get_users_vbr            _int_get_previous_vector_table
#define _Get_vbr                  _int_get_vector_table
#define _Invl_dc_line             _dcache_invalidate_line
#define _Invl_dc_mlines           _dcache_invalidate_mlines
#define _Invl_ic_line             _icache_invalidate_line
#define _Invl_ic_mlines           _icache_invalidate_mlines
#define _I_cache                  _cache_init
#define _KRNL_Kernel_data         _mqx_kernel_data
#define _Set_cacr                 _cache_init
#define _Set_int_vector_table     _int_set_vector_table
#define _Set_trap_vector_table    _int_set_trap_vector_table
#define _Set_vbr                  _int_set_vector_table
#define _Set_kernel_data          _mqx_set_kernel_data
#define __Kernel_data             _mqx_kernel_data


/* Changes to the BSP file <bsp_name>.h */
#define _MONITOR                                  _mqx_monitor_type
#define DEFAULT_PROCESSOR_NUMBER                  BSP_DEFAULT_PROCESSOR_NUMBER
#define DEFAULT_START_OF_KERNEL_MEMORY            BSP_DEFAULT_START_OF_KERNEL_MEMORY
#define DEFAULT_END_OF_KERNEL_MEMORY              BSP_DEFAULT_END_OF_KERNEL_MEMORY
#define DEFAULT_MQX_HARDWARE_INTERRUPT_LEVEL_MAX  BSP_DEFAULT_MQX_HARDWARE_INTERRUPT_LEVEL_MAX
#define DEFAULT_MAX_BUFFER_POOLS                  BSP_DEFAULT_MAX_MSGPOOLS
#define DEFAULT_IO_CHANNEL                        BSP_DEFAULT_IO_CHANNEL
#define DEFAULT_BW_OPEN_MODE                      BSP_DEFAULT_IO_OPEN_MODE
#define DEFAULT_INTERRUPT_STACK_SIZE              BSP_DEFAULT_INTERRUPT_STACK_SIZE
#define DEFAULT_MAX_QUEUE_NUMBER                  BSP_DEFAULT_MAX_MSGQS


/* Changes to Watchdog definitions */
#define TIMER_INTERRUPT_VECTOR    BSP_TIMER_INTERRUPT_VECTOR

/*--------------------------------------------------------------------------*/
/*                   OBSOLETE FUNCTION DEFINITIONS 
*/

/* To be used in the parameter structure to indicate that the field is
** to be ignored, or as the policy to be applied.
*/
#define SCHED_NOT_CHANGED (_mqx_uint)(-1)

/*--------------------------------------------------------------------------*/
/* 
** SCHEDULER PARAMETERS STRUCTURE
**
** This structure defines the parameters structure use to 
** set/modify/get the scheduling method for a task
*/
typedef struct sched_param_struct {

   /* The software priority level the task is to run at */
   _mqx_uint     PRIORITY;
   
   /* The time slice interval (milliseconds) the task is to use */
   _mqx_uint     ROUND_ROBIN_INTERVAL;

   /* The policy of the requested item (used in get parameters) */
   _mqx_uint     POLICY;

} SCHED_PARAM_STRUCT, _PTR_ SCHED_PARAM_STRUCT_PTR;

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern _mqx_uint          _sched_getparam(_task_id, SCHED_PARAM_STRUCT_PTR);
extern _mqx_uint          _sched_setparam(_task_id, SCHED_PARAM_STRUCT_PTR);
extern _mqx_uint          _sched_setparam_td(pointer, SCHED_PARAM_STRUCT_PTR);
extern _mqx_uint          _sched_setscheduler(_task_id, _mqx_uint, 
   SCHED_PARAM_STRUCT_PTR);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

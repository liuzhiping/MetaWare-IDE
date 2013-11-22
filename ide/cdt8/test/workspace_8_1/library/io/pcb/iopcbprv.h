#ifndef __iopcbprv_h__
#define __iopcbprv_h__ 1
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
*** File: iopcbprv.h
***
*** Comments:      
***   This include file is used to provide private definitions for the
***   io pcb package.
***
**************************************************************************
*END*********************************************************************/

#include <part.h>

/*--------------------------------------------------------------------------*/
/*
**                            CONSTANT DEFINITIONS
*/

/* PCB Definitions */
#define IO_PCB_VALID ((_mqx_uint)(0x69706362))   /* "ipcb" */


/*--------------------------------------------------------------------------*/
/*
**                            MACRO DEFINITIONS
*/

#define IO_PCB_CREATE_POOL_ID(ptr) (_io_pcb_pool_id)((uchar_ptr)(ptr)-1)

#define IO_PCB_GET_POOL_PTR(id) (IO_PCB_POOL_STRUCT_PTR)((uchar_ptr)(id)+1)

/*--------------------------------------------------------------------------*/
/*
**                            DATATYPES
*/

/* 
** IO PCB POOL STRUCT
** This structure defines what a pcb pool structure contains.
*/
typedef struct io_pcb_pool_struct
{
   /* queue element for queueing */
   QUEUE_ELEMENT_STRUCT          QUEUE;

   /* Validity field */
   _mqx_uint                     VALID;

   /* Number of fragments */
   _mqx_uint                     NUM_FRAGS;
   
   /* Size of extra data */
   _mem_size                     EXTRA_DATA_SIZE;

   /* The function to call when a pcb is allocated */
   IO_PCB_STRUCT_PTR (_CODE_PTR_ PCB_ALLOC_FUNCTION_PTR)
      (IO_PCB_STRUCT_PTR, pointer);
   
   /* An additional pointer to pass to the allocation function */
   pointer                       ALLOC_FUNCTION_DATA_PTR;
   
   /* The function to call when a pcb is freed */
   IO_PCB_STRUCT_PTR (_CODE_PTR_ PCB_FREE_FUNCTION_PTR)
      (IO_PCB_STRUCT_PTR, pointer);

   /* An additional pointer to pass to the free function */
   pointer                       FREE_FUNCTION_DATA_PTR;

   /* Partition ID */
   _partition_id                 PARTITION;

   /* Blocking wait semaphore */
   LWSEM_STRUCT                  PCB_LWSEM;
   
} IO_PCB_POOL_STRUCT, _PTR_ IO_PCB_POOL_STRUCT_PTR;


/*
**
** IO PCB DEVICE STRUCT
**
** This is the structure used to store device information for an
** installed PCB I/O driver
*/
typedef struct io_pcb_device_struct
{

   /* Used to link io_device_structs together */
   QUEUE_ELEMENT_STRUCT QUEUE_ELEMENT;

   /*
   ** A string that identifies the device.  This string is matched
   ** by fopen, then the other information is used to initialize a
   ** FILE struct for standard I/O.  This string is also provided in
   ** the kernel initialization record for the default I/O channel
   */
   char_ptr             IDENTIFIER;
  
   /* The I/O init function */
   _mqx_int (_CODE_PTR_ IO_OPEN)(FILE_PTR, char _PTR_, char _PTR_);

   /* The I/O deinit function */
   _mqx_int (_CODE_PTR_ IO_CLOSE)(FILE_PTR);

   /* The I/O read function */
   _mqx_int (_CODE_PTR_ IO_READ)(FILE_PTR, IO_PCB_STRUCT_PTR _PTR_);

   /* The I/O write function */
   _mqx_int (_CODE_PTR_ IO_WRITE)(FILE_PTR, IO_PCB_STRUCT_PTR);

   /* The I/O ioctl function */
   _mqx_int (_CODE_PTR_ IO_IOCTL)(FILE_PTR, _mqx_uint, pointer);

   /* The I/O uninstall function */
   _mqx_int (_CODE_PTR_ IO_UNINSTALL)(struct io_pcb_device_struct _PTR_);

   /* The I/O channel specific initialization data */
   pointer              DRIVER_INIT_PTR;

   /* 
   ** Used to keep track of the number of times the pcb device 
   ** has been opened
   */

   _mqx_uint            NUMBER_OF_OPENS;

} IO_PCB_DEVICE_STRUCT, _PTR_ IO_PCB_DEVICE_STRUCT_PTR;

/*--------------------------------------------------------------------------*/
/*
**                      FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
/* IO PCB Device functions */
extern _mqx_uint    _io_pcb_free_internal(IO_PCB_STRUCT _PTR_);
extern _mqx_uint _io_pcb_dev_install(
      char_ptr, 
      _mqx_int (_CODE_PTR_)(FILE_DEVICE_STRUCT_PTR, char _PTR_, char _PTR_),
      _mqx_int (_CODE_PTR_)(FILE_DEVICE_STRUCT_PTR),
      _mqx_int (_CODE_PTR_)(FILE_DEVICE_STRUCT_PTR, IO_PCB_STRUCT _PTR_ _PTR_),
      _mqx_int (_CODE_PTR_)(FILE_DEVICE_STRUCT_PTR, IO_PCB_STRUCT _PTR_),
      _mqx_int (_CODE_PTR_)(FILE_DEVICE_STRUCT_PTR, _mqx_uint, pointer),
      _mqx_int (_CODE_PTR_)(IO_PCB_DEVICE_STRUCT_PTR),
      pointer);
extern _mqx_uint _io_pcb_dev_uninstall(char_ptr);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

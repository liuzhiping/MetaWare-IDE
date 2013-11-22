#ifndef __io_pcb_h__
#define __io_pcb_h__
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
*** File: io_pcb.h
***
*** Comments:      
***   This file is the header file for the I/O subsystem interface.                        
***
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*
**                            CONSTANT DEFINITIONS
*/

/* An invalid pool id */
#define IO_PCB_NULL_POOL_ID (_io_pcb_pool_id)(0)

/* Error codes */
#define IO_PCB_POOL_INVALID            (0x2010)
#define IO_PCB_INVALID                 (0x2011)
#define IO_PCB_NOT_A_PCB               (0x2012)
#define IO_PCB_NOT_A_PCB_DEVICE        (0x2013)
#define IO_PCB_READ_NOT_AVAILABLE      (0x2014)
#define IO_PCB_WRITE_NOT_AVAILABLE     (0x2015)
#define IO_PCB_DEVICE_DOES_NOT_EXIST   (0x2016)
#define IO_PCB_ALLOC_CALLBACK_FAILED   (0x2017)

/* PCB IOCTL Commands */
#define IO_PCB_IOCTL_READ_CALLBACK_SET (0x1001)
#define IO_PCB_IOCTL_SET_INPUT_POOL    (0x1002)
#define IO_PCB_IOCTL_ENQUEUE_READQ     (0x1003)
#define IO_PCB_IOCTL_START             (0x1004)
#define IO_PCB_IOCTL_UNPACKED_ONLY     (0x1005)

/*--------------------------------------------------------------------------*/
/*
**                            MACRO DEFINITIONS
*/
#define IO_PCB_FREE(pcb_ptr) \
   (*(pcb_ptr)->FREE_PCB_FUNCTION_PTR)(pcb_ptr)

/*--------------------------------------------------------------------------*/
/*
**                            DATATYPES
*/

/* The type of an IO PCB Pool ID (the address of the pool - 4) */
typedef pointer _io_pcb_pool_id;

typedef struct pcb_queue_element_struct {

   /* next element in queue, MUST BE FIRST FIELD */
   struct pcb_queue_element_struct _PTR_ NEXT;

   /* previous element in queue, MUST BE SECOND FIELD */
   struct pcb_queue_element_struct _PTR_ PREV;
   
} PCB_QUEUE_ELEMENT_STRUCT, _PTR_ PCB_QUEUE_ELEMENT_STRUCT_PTR;

/* 
** IO PCB FRAGMENT STRUCT
** This structure defines the location and size of a memory fragment, used by
** the IO PCB structure.
*/
typedef struct io_pcb_fragment_struct {

   /* The length of the data in bytes */
   _mqx_uint  LENGTH;

   /* The starting address of the data */
   uchar_ptr  FRAGMENT;

} IO_PCB_FRAGMENT_STRUCT, _PTR_ IO_PCB_FRAGMENT_STRUCT_PTR;

/*
** IO PCB STRUCT
** This structure defines what a Packet Control Block (PCB) looks like.
** The PCB is used to define the format of a data packet.  The data packet
** consists of any number of fragments of data located in various memory locations.
** The meaning of each fragment is protocol and application dependent.
*/
typedef struct io_pcb_struct {

   /* MQX queue utility pointers for queueing up PCBs */
   PCB_QUEUE_ELEMENT_STRUCT  QUEUE;

   /* The function to call when freeing this PCB */
   _mqx_uint     (_CODE_PTR_ FREE_PCB_FUNCTION_PTR)(struct io_pcb_struct _PTR_);

   /* PCB Validity field used for validity checking*/
   _mqx_uint                 VALID;

   /* The PCB pool that the PCB was allocated from */
   _io_pcb_pool_id           POOL_ID;

   /* Addresss of private information for used by the protocol/application */
   pointer                   OWNER_PRIVATE;
   pointer                   INSTANTIATOR_PRIVATE;

   /* protocol/application specific data */
   uint_16                   PRIVATE;

   /* The number of fragments in the variable length array */
   uint_16                   NUMBER_OF_FRAGMENTS;

   /* A variable length array of data fragments */
   IO_PCB_FRAGMENT_STRUCT  FRAGMENTS[1];

} IO_PCB_STRUCT, _PTR_ IO_PCB_STRUCT_PTR;


/*--------------------------------------------------------------------------*/
/*
**                      FUNCTION PROTOTYPES
*/

#ifdef __cplusplus
extern "C" {
#endif

/* IO PCB functions */
#ifndef __TAD_COMPILE__
extern _io_pcb_pool_id   _io_pcb_create_pool(_mqx_uint, _mem_size, _mqx_uint, 
   _mqx_uint, _mqx_uint, 
   IO_PCB_STRUCT_PTR (_CODE_PTR_) (IO_PCB_STRUCT_PTR, pointer), pointer,
   IO_PCB_STRUCT_PTR (_CODE_PTR_) (IO_PCB_STRUCT_PTR, pointer), pointer);
extern IO_PCB_STRUCT_PTR _io_pcb_alloc(_io_pcb_pool_id, boolean);
extern _mqx_int  _io_pcb_read(FILE _PTR_, IO_PCB_STRUCT_PTR _PTR_);
extern _mqx_int  _io_pcb_write(FILE _PTR_, IO_PCB_STRUCT _PTR_);
extern _mqx_uint _io_pcb_free(IO_PCB_STRUCT _PTR_);
extern _mqx_uint _io_pcb_free_internal(IO_PCB_STRUCT _PTR_);
extern _mqx_uint _io_pcb_destroy_pool(_io_pcb_pool_id);
extern _mqx_uint _io_pcb_test(pointer _PTR_, pointer _PTR_);
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

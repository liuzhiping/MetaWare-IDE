#ifndef __edserprv_h__
#define __edserprv_h__
/*HEADER************************************************************************
********************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: edserprv.h
***
*** Comments: 
***   Serial Embedded Debug Server private header file
***
********************************************************************************
*END***************************************************************************/

#include "message.h"
#include "msg_prv.h"
#include "fio.h"
#include "serial.h"
#include "pcb.h"

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */

/* Define the following to send a space character after each byte */
/* #define EDS_USE_DEBUG_SPACES */

/* Define the following for extra debug information */
/* #define DO_DEBUG */
#ifdef DO_DEBUG
#define DEBUGM(x) x
#define EDS_USE_DEBUG_SPACES 
#else
#define DEBUGM(x)
#undef EDS_USE_DEBUG_SPACES 
#endif

/* Private defines */

/* The eds task message queue number for ipc operation */
#define EDS_IPC_MESSAGE_QUEUE_NUMBER  (2)

/* ED Serial Task info */
#define EDS_SERIAL_STACK_SIZE            1500L
#define EDS_SERIAL_TASK_NAME             "EDS_Serial_task"

/* server operations */
#define EDS_IDENTIFY               1
#define EDS_LITTLE_ENDIAN_IDENTIFY 0x80000000 /* PRIVATE */
#define EDS_READ                   2
#define EDS_WRITE                  3
#define EDS_CHANGE_PROCESSOR       4
#define EDS_CONFIG_MULTIPROC       5

#define EDS_BIG_ENDIAN      0
#define EDS_LITTLE_ENDIAN   0xFFFFFFFF

/* Start and end of frame characters */
#define STX                 0x02
#define ETX                 0x03

/* Receive states */
#define STATE_WAIT_STX      0x0001
#define STATE_WAIT_NON_STX  0x0002
#define STATE_GETTING_DATA  0x0003

/* error codes */
#define EDS_OK              0
#define EDS_INVALID_OP      1
#define EDS_INVALID_SIZE    2

#define EDS_OP_STRUCT_SIZE  (sizeof(EDS_OP_STRUCT))
#define EDS_BUFFER_SIZE     (128)

#define EDS_PROC_DEF_STRUCT_SIZE  (sizeof(EDS_PROC_DEF_STRUCT))

#define EDS_SERIAL_VALID    ((_mqx_uint)0x65647373)   /* "edss" */

/* Macros */
#if (PSP_MEMORY_ADDRESSING_CAPABILITY != 8)
   #if (PSP_MEMORY_ADDRESSING_CAPABILITY == 32)
      #define UNPACK(src,dest,size) \
         { \
            uint_32 i; \
            uint_32 _PTR_ src_lp;\
            uchar   _PTR_ dest_lp;\
            src_lp = (src);\
            dest_lp = (dest);\
            for(i = 0; i < (size); i+=4) { \
               htonl(dest_lp,*src_lp);\
               src_lp++;\
               dest_lp+=4;\
            }\
         }
      #define PACK(src,dest,size) \
         { \
            uint_32 i; \
            uchar  _PTR_ src_lp;\
            uchar  _PTR_ dest_lp;\
            src_lp = (src);\
            dest_lp = (dest);\
            for(i = 0; i < (size); i+=4) { \
               *dest_lp = ntohl(src_lp);\
               dest_lp++;\
               src_lp +=4;\
            }\
         }
   #elif (PSP_MEMORY_ADDRESSING_CAPABILITY == 24)
      #define UNPACK(src,dest,size) \
         { \
            uint_24 i; \
            uint_24 _PTR_ src_lp;\
            uchar   _PTR_ dest_lp;\
            src_lp = (src);\
            dest_lp = (dest);\
            for(i = 0; i < (size); i+=3) { \
               htonls(dest_lp,*src_lp);\
               src_lp++;\
               dest_lp+=3;\
            }\
         }
      #define PACK(src,dest,size) \
         { \
            uint_24 i; \
            uchar _PTR_  src_lp;\
            uchar  _PTR_ dest_lp;\
            src_lp = (src);\
            dest_lp = (dest);\
            for(i = 0; i < (size); i+=3) { \
               *dest_lp = ntohls(src_lp);\
               dest_lp++;\
               src_lp +=3;\
            }\
         }
   #elif (PSP_MEMORY_ADDRESSING_CAPABILITY == 16)
      #define UNPACK(src,dest,size) \
         { \
            uint_16 i; \
            uint_16 _PTR_ src_lp;\
            uchar   _PTR_ dest_lp;\
            src_lp = (src);\
            dest_lp = (dest);\
            for(i = 0; i < (size); i+=2) { \
               htons(dest_lp,*src_lp);\
               src_lp++;\
               dest_lp+=2;\
            }\
         }
      #define PACK(src,dest,size) \
         { \
            uint_16 i; \
            uchar _PTR_  src_lp;\
            uchar  _PTR_ dest_lp;\
            src_lp = (src);\
            dest_lp = (dest);\
            for(i = 0; i < (size); i+=2) { \
               *dest_lp = ntohs(src_lp);\
               dest_lp++;\
               src_lp +=2;\
            }\
         }
   #else
      #error "Memory type not understood"
   #endif

   #define READ_BYTES(src,dest,size)   UNPACK(src,dest,size)
   #define WRITE_BYTES(src,dest,size)  PACK(src,dest,size)

#else 
   #define READ_BYTES(dest,src,size)   _mem_copy(dest,src,size)
   #define WRITE_BYTES(dest,src,size)  _mem_copy(dest,src,size)
#endif

/*--------------------------------------------------------------------------*/
/*                      DATA STRUCTURE DEFINITIONS                          */

/* Structures */
typedef struct op_struct {
   uchar OPERATION[4];   /* server operation          */
   uchar ADDRESS[4];     /* read/write memory address */
   uchar ADDRESS2[4];    /* extra address field       */
   uchar SIZE[4];        /* size of buffer            */
   uchar PROCESSOR[4];   /* processor type            */
   uchar ENDIAN[4];      /* endian of processor       */
   uchar EDS_ERROR[4];   /* error code                */
} EDS_OP_STRUCT, _PTR_ EDS_OP_STRUCT_PTR;

typedef struct eds_data_struct {
   EDS_OP_STRUCT  EDS_OP_DATA;
   uchar          BUFFER[EDS_BUFFER_SIZE];
} EDS_DATA_STRUCT, _PTR_ EDS_DATA_STRUCT_PTR;

/* structure used to define a processor */
typedef struct eds_proc_def_struct {
   uchar        ENDIAN[4];
   uchar        KD_START[4];
   uchar        KD_END[4];
   uchar        PROC_NUM[4];
   uchar        CPU_TYPE[4];
} EDS_PROC_DEF_STRUCT, _PTR_ EDS_PROC_DEF_STRUCT_PTR;

typedef struct eds_proc_reg_struct {
   EDS_PROC_DEF_STRUCT   PROC_DATA;
   QUEUE_ELEMENT_STRUCT  LIST;
} EDS_PROC_REG_STRUCT, _PTR_ EDS_PROC_REG_STRUCT_PTR;

typedef struct eds_component_struct
{
   LWSEM_STRUCT SEM;
   _mqx_uint     STATE;
   _mqx_uint     VALID;
} EDS_COMPONENT_STRUCT, _PTR_ EDS_COMPONENT_STRUCT_PTR;


/* function prototypes */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __TAD_COMPILE__
extern void    _eds_serial_master_task(uint_32);
extern void    _eds_serial_slave_task(uint_32);

extern boolean _eds_serial_process_master(EDS_SERIAL_COMPONENT_STRUCT_PTR);
extern boolean _eds_serial_process_slave(EDS_SERIAL_COMPONENT_STRUCT_PTR);
extern void    _eds_serial_check_errors(EDS_OP_STRUCT_PTR);
extern void    _eds_serial_identify(EDS_OP_STRUCT_PTR);
extern int_32  _eds_serial_send(FILE_PTR, char_ptr, int_32);
extern int_32  _eds_serial_recv(FILE_PTR, char_ptr, int_32);
#endif
 
#ifdef __cplusplus
}
#endif

#endif

/* EOF */

#ifndef __pcb_h__
#define __pcb_h__
/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: pcb.h
***
*** Comments:  This file contains the definitions of the generic
***            PCB (Packet Control Block) structure.  
***            Since this structure is common to
***            various protocols, this file is distributed with
***            several products.
***
**************************************************************************
*END*********************************************************************/

/*--------------------------------------------------------------------------*/
/*                        CONSTANT DEFINITIONS                              */


#define __PCB__

#define PCB_free(pcb_ptr)  ((pcb_ptr)->FREE(pcb_ptr))

/* Macros for byte exchanges between host and network */
#if (PSP_MEMORY_ADDRESSING_CAPABILITY == 8)
   #if (PSP_ENDIAN == MQX_BIG_ENDIAN) && (PSP_MEMORY_ACCESSING_CAPABILITY <= 8)

      #define _HOST_TO_BEOCT_32(p,x) (*(uint_32_ptr)(p) = (x))
      #define _HOST_TO_BEOCT_16(p,x) (*(uint_16_ptr)(p) = (x))
      #define _HOST_TO_BEOCT_8(p,x)  (*(  uchar_ptr)(p) = (x))

      #define _BEOCT_TO_HOST_32(p)   (*(uint_32_ptr)(p))
      #define _BEOCT_TO_HOST_16(p)   (*(uint_16_ptr)(p))
      #define _BEOCT_TO_HOST_8(p)    (*(  uchar_ptr)(p))

   #elif (PSP_ENDIAN == MQX_BIG_ENDIAN) && (PSP_MEMORY_ACCESSING_CAPABILITY <= 16)

      #define _HOST_TO_BEOCT_32(p,x) (  (((uint_32)(p) & 1) == 0) ?              \
                                             (*(uint_32_ptr)&(p)[0] = (x))       \
                                      :                                          \
                                             (*(  uchar_ptr)&(p)[0] = (x) >> 24, \
                                              *(uint_16_ptr)&(p)[1] = (x) >>  8, \
                                              *(  uchar_ptr)&(p)[3] = (x))       )
      #define _HOST_TO_BEOCT_16(p,x) (  (((uint_32)(p) & 1) == 0) ?              \
                                             (*(uint_16_ptr)&(p)[0] = (x))       \
                                      :                                          \
                                             (*(  uchar_ptr)&(p)[0] = (x) >>  8, \
                                              *(  uchar_ptr)&(p)[1] = (x))       )
      #define _HOST_TO_BEOCT_8(p,x)          (*(  uchar_ptr)&(p)[0] = (x))

      #define _BEOCT_TO_HOST_32(p) (  (((uint_32)(p) & 1) == 0) ?                    \
                                                     (*(uint_32_ptr)&(p)[0])         \
                                    :                                                \
                                           (((uint_32)*(  uchar_ptr)&(p)[0] << 24) | \
                                            ((uint_32)*(uint_16_ptr)&(p)[1] <<  8) | \
                                            ((uint_32)*(  uchar_ptr)&(p)[3]      ))  )
      #define _BEOCT_TO_HOST_16(p) (  (((uint_32)(p) & 1) == 0) ?                    \
                                                     (*(uint_16_ptr)&(p)[0])         \
                                    :                                                \
                                           (((uint_16)*(  uchar_ptr)&(p)[0] <<  8) | \
                                ((uint_16)*(  uchar_ptr)&(p)[1]      ))  )
      #define _BEOCT_TO_HOST_8(p)                    (*(  uchar_ptr)&(p)[0])

   #elif (PSP_ENDIAN == MQX_BIG_ENDIAN)

      #define _HOST_TO_BEOCT_32(p,x) (  (((uint_32)(p) & 3) == 0) ?            \
                                           (*(uint_32_ptr)&(p)[0] = (x))       \
                                      : (((uint_32)(p) & 1) == 0) ?            \
                                           (*(uint_16_ptr)&(p)[0] = (x) >> 16, \
                                            *(uint_16_ptr)&(p)[2] = (x))       \
                                      :                                        \
                                           (*(  uchar_ptr)&(p)[0] = (x) >> 24, \
                                            *(uint_16_ptr)&(p)[1] = (x) >>  8, \
                                            *(  uchar_ptr)&(p)[3] = (x))       )
      #define _HOST_TO_BEOCT_16(p,x) (  (((uint_32)(p) & 1) == 0) ?            \
                                           (*(uint_16_ptr)&(p)[0] = (x))       \
                                      :                                        \
                                           (*(  uchar_ptr)&(p)[0] = (x) >>  8, \
                                            *(  uchar_ptr)&(p)[1] = (x))       )
      #define _HOST_TO_BEOCT_8(p,x)        (*(  uchar_ptr)&(p)[0] = (x))

      #define _BEOCT_TO_HOST_32(p) (  (((uint_32)(p) & 3) == 0) ?                    \
                                                     (*(uint_32_ptr)&(p)[0])         \
                                    : (((uint_32)(p) & 1) == 0) ?                    \
                                           (((uint_32)*(uint_16_ptr)&(p)[0] << 16) | \
                                            ((uint_32)*(uint_16_ptr)&(p)[2]      ))  \
                                    :                                                \
                                           (((uint_32)*(  uchar_ptr)&(p)[0] << 24) | \
                                            ((uint_32)*(uint_16_ptr)&(p)[1] <<  8) | \
                                            ((uint_32)*(  uchar_ptr)&(p)[3]      ))  )
      #define _BEOCT_TO_HOST_16(p) (  (((uint_32)(p) & 1) == 0) ?                    \
                                                     (*(uint_16_ptr)&(p)[0])         \
                                    :                                                \
                                           (((uint_16)*(  uchar_ptr)&(p)[0] <<  8) | \
                                            ((uint_16)*(  uchar_ptr)&(p)[1]      ))  )
      #define _BEOCT_TO_HOST_8(p)                    (*(  uchar_ptr)&(p)[0])

   #else

/* START CR 2362 */
/* New method does not generate warnings */
#if 0
      #define _HOST_TO_BEOCT_32(p,x) (*p = (x << 24) | ((x & 0xff00) << 8) | \
                                     ((x & 0xff0000) >> 8) | (x >> 24))



      #define _HOST_TO_BEOCT_16(p,x) (*p = (x & 0xff00) >>  8 | \
                                      (x & 0xff) << 8)


/* Old method generates better code but produces warnings */
#else

      #define _HOST_TO_BEOCT_32(p,x) (((uchar_ptr)(p))[0] = (x) >> 24, \
                                      ((uchar_ptr)(p))[1] = (x) >> 16, \
                                      ((uchar_ptr)(p))[2] = (x) >> 8, \
                                      ((uchar_ptr)(p))[3] = (x))

      #define _HOST_TO_BEOCT_16(p,x) (((uchar_ptr)(p))[0] = (x) >> 8, \
                                      ((uchar_ptr)(p))[1] = (x))
#endif
/* END CR 2362 */

      #define _HOST_TO_BEOCT_8(p,x)  (((uchar_ptr)(p))[0] = (x))

      #define _BEOCT_TO_HOST_32(p)   ((((uint_32)(p)[0]) << 24) | \
                                      (((uint_32)(p)[1]) << 16) | \
                                      (((uint_32)(p)[2]) <<  8) | \
                                      (((uint_32)(p)[3])      ))

      #define _BEOCT_TO_HOST_16(p)   ((((uint_16)(p)[0]) <<  8) | \
                                      (((uint_16)(p)[1])      ))

      #define _BEOCT_TO_HOST_8(p)    (((  uchar)(p)[0]        ))


   #endif

#else

   /*
   ** This set of macros will always work on all processors.
   ** The sets of macros above are just optimizations for
   ** specific architectures.
   */

   #define _HOST_TO_BEOCT_32(p,x) ((p)[0] = ((uint_32)(x)) >> 24 & 0xFF, \
                                   (p)[1] = ((uint_32)(x)) >> 16 & 0xFF, \
                                   (p)[2] = ((uint_32)(x)) >>  8 & 0xFF, \
                                   (p)[3] = ((uint_32)(x))       & 0xFF)

   #define _HOST_TO_BEOCT_16(p,x) ((p)[0] = ((uint_16)(x)) >>  8 & 0xFF, \
                                   (p)[1] = ((uint_16)(x))       & 0xFF)

   #define _HOST_TO_BEOCT_8(p,x)  ((p)[0] = ((uint_8)(x))       & 0xFF)

   #define _BEOCT_TO_HOST_32(p)   ((((uint_32)(p)[0] & 0xFF) << 24) | \
                                   (((uint_32)(p)[1] & 0xFF) << 16) | \
                                   (((uint_32)(p)[2] & 0xFF) <<  8) | \
                                   (((uint_32)(p)[3] & 0xFF)      ))

   #define _BEOCT_TO_HOST_16(p)   ((((uint_16)(p)[0] & 0xFF) <<  8) | \
                                   (((uint_16)(p)[1] & 0xFF)      ))

   #define _BEOCT_TO_HOST_8(p)    ((((  uchar)(p)[0] & 0xFF)      ))

#endif

/* Backwards compatibility macros */
#define htonl _HOST_TO_BEOCT_32
#define htons _HOST_TO_BEOCT_16
#define htonc _HOST_TO_BEOCT_8 
#define ntohl _BEOCT_TO_HOST_32  
#define ntohs _BEOCT_TO_HOST_16  
#define ntohc _BEOCT_TO_HOST_8   

/*--------------------------------------------------------------------------*/
/*
**                          DATA STRUCTURES
*/

/*
** The PCB (Packet Control Block)
** 
** One PCB contains exactly one packet, possibly split over several areas of
** memory.
** 
** The PCB structure consists of two pointers (FREE and PRIVATE) followed by a
** variable-length array of PCB_FRAGMENT structures.  The array is terminated
** by an entry with LENGTH=0 and FRAGMENT=NULL.
**
** The PCB's owner (i.e. the module that allocated it) must initialize the
** FREE field to point to a function to free the PCB.  PRIVATE can be used by
** the PCB's owner to store any information it wishes.
*/

typedef struct pcb_fragment 
{
   uint_32           LENGTH;
   uchar_ptr         FRAGMENT;
} PCB_FRAGMENT, _PTR_ PCB_FRAGMENT_PTR;

typedef struct pcb 
{
   void (_CODE_PTR_  FREE)(struct pcb _PTR_);
   pointer           PRIVATE;
   PCB_FRAGMENT      FRAG[1];
} PCB, _PTR_ PCB_PTR;


/*--------------------------------------------------------------------------*/
/*                           EXTERNAL DECLARATIONS                          */

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
}
#endif

#endif
/* EOF */

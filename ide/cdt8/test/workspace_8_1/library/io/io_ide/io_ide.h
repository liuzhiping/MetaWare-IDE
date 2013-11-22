#ifndef __io_ide_h__
#define __io_ide_h__
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
*** File: io_ide.h
***
*** Comments:
*** Comments: The file contains functions prototype, defines, structure 
***           definitions to the IDE controller. This file is added to 
***           address CR 2283
***
**************************************************************************
*END*********************************************************************/

/*-----------------------------------------------------------------------*/
/*
**                          CONSTANT DECLARATIONS
*/

/* 2 IDE drives maximum */
#define IDE_MAX_DRIVE                         (2)

/* PIO timing parameters */
#define IDE_TIMING_T0                         (0)
#define IDE_TIMING_T1                         (1)
#define IDE_TIMING_T2                         (2)
#define IDE_TIMING_T2L                        (3)

/* The maximum time to wait for reset to complete in seconds */
#define IDE_RST_TIMEOUT                       (31)

/* Default timeout in seconds */
#define IDE_BSY_DEFAULT_TIMEOUT               (5)

/* Error */
#define IDE_ERR_BASE                          (0x11000)
#define IDE_ERR_DEVICE_NOT_VALID              (IDE_ERR_BASE | 0x01)

/*
** IO_IDE IOCTL calls
*/
#define IO_IDE_IOCTL_GET_BASE_ADDRESS         (0x0101)
#define IO_IDE_IOCTL_GET_ID                   (0x0102)
#define IO_IDE_IOCTL_GET_PIO_TIMING           (0x0103)
#define IO_IDE_IOCTL_GET_PIO_MODE             (0x0104)
#define IO_IDE_IOCTL_SET_PIO_MODE             (0x0105)


/*-----------------------------------------------------------------------*/
/*
**                          DATATYPE DECLARATIONS
*/


/* IDE initialization structure */
typedef struct io_ide_init_struct
{

   /* The drive number ( 0, 1 ) */
   uint_32                DRIVE;

   /* pointer to IDE controller */
   volatile   pointer     IDE_CTRL_STRUCT_PTR;

   /* The interrupt vector to use */
   uint_32                INTERRUPT_VECTOR;

   /* PIO mode (1, 2, 3, 4) */
   uint_32                IDE_PIO_MODE;
   
   /* Interrupt Service Routin */   
   void    (_CODE_PTR_ OLD_ISR)(void);
   void    (_CODE_PTR_ OLD_ISR_EXCEPTION_HANDLER)(void);
   pointer             OLD_ISR_DATA;

} IO_IDE_INIT_STRUCT, _PTR_ IO_IDE_INIT_STRUCT_PTR;


/*-----------------------------------------------------------------------*/
/*
**                          PROTOTYPE DECLARATIONS
*/

#ifdef __cplusplus
extern "C" {
#endif

/* Interface functions */
extern int_32 _io_ide_install(char_ptr, uint_32, uint_32, uint_32);
extern int_32 _io_ide_open(FILE _PTR_, char_ptr, char_ptr);
extern int_32 _io_ide_close(FILE _PTR_);
extern int_32 _io_ide_ioctl(FILE _PTR_, uint_32, uint_32_ptr);


#ifdef __cplusplus
}
#endif

#endif

/* EOF */
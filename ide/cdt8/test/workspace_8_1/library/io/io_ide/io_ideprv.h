#ifndef __io_ideprv_h__
#define __io_ideprv_h__
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
*** File: io_ideprv.h
***
*** Comments:
*** Comments: The file contains functions prototype, defines, structure 
***           definitions private to the IDE controller. This file is 
***           added to address CR 2283
***
**************************************************************************
*END*********************************************************************/

/*-----------------------------------------------------------------------*/
/*
**                          DATATYPE DECLARATIONS
*/

/* ARC's IDE controller registers */
typedef volatile struct ide_control_struct
{
   
   /* Identity register */
   uint_32  ID;

   /* PIO Timing Setup register */   
   uint_32  PIO_SETUP;

   /* Status and Control register */
   uint_32  STAT_CTRL;

   /* not mapped */
   uchar    RESERVED[116];
   
   /* pointer to IDE device registers */
   pointer  IDE_DEVICE_REG;

} IDE_CONTROL_STRUCT, _PTR_ IDE_CONTROL_STRUCT_PTR;

#endif

/* EOF */
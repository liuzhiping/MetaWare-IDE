/************************************************************************/
/*                                                                      */
/* Copyright (C) ARC International, Inc. 2007.                          */
/*                                                                      */
/* All Rights Reserved.                                                 */
/*                                                                      */
/* This software is the property of ARC International, Inc.  It is      */
/* furnished under a specific licensing agreement.  It may be used or   */
/* copied only under terms of the licensing agreement.                  */
/*                                                                      */
/* For more information, contact support@arc.com                        */
/*                                                                      */
/************************************************************************/

#ifndef _CONFIG_H 
#define _CONFIG_H 1

#include "csp/generic/vr_generic.h"
#include "csp/arc/vr_arc.h"



#if 0
#define USE_ARC_CHANNEL 1   /* define this to use ARCChannels */
#endif


#if defined(VR_RAW) || defined(VR_RAW_ARC)
#define USE_IMAGE_VIEWER 1 /* define this if want to use image viewer */
#endif


#define NUMBER_OF_RENDER_CPUS  2
        /* change this to alter the number of render CPUs */

#define NUMBER_OF_CPU_ISLANDS  (2 + NUMBER_OF_RENDER_CPUS)


#define TOTAL_DRAM_SIZE   (32*1024*1024) /* 32Mb */

#define BOARD_NAME "board"


extern void
target_specific_configuration(void);


#define GR_THD      0
#define DISPLAY_THD 1
#define RENDER_THD  2 /* render threads start at 2 */



#endif /* _CONFIG_H */


/************************************************************************/
/************************************************************************/
/**                                                                     */
/** Copyright (c) ARC International 2008.                               */
/** All rights reserved                                                 */
/**                                                                     */
/** This software embodies materials and concepts which are             */
/** confidential to ARC International and is made                       */
/** available solely pursuant to the terms of a written license         */
/** agreement with ARC International                                    */
/**                                                                     */
/** For more information, contact support@arc.com                       */
/**                                                                     */
/**                                                                     */
/************************************************************************/
/************************************************************************/



#ifndef _CONFIG_H 
#define _CONFIG_H 1


#if 0
#define USE_ARC_CHANNEL 1   /* define this to use ARCChannels */
#endif


#ifndef VR_MQX
#define USE_IMAGE_VIEWER 1 /* define this if want to use image viewer */
#endif



extern void
target_specific_configuration(void);


#define GR_THD      0
#define DISPLAY_THD 1
#define RENDER_THD  2 /* render threads start at 2 */



#endif /* _CONFIG_H */


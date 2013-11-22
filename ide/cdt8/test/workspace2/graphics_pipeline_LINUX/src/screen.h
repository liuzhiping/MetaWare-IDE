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



#ifndef _SCREEN_H
#define _SCREEN_H 1


/* vt100 screen functions */

extern void
screen_clear(void);

extern void
screen_coord(int x, int y);

extern void
eh(char *file, int line, char *error);

#define EH(err) eh(__FILE__,__LINE__, (err))

#endif /* _SCREEN_H  */


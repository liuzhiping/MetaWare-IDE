/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: arc_main.c
***
*** Comments:      
***   This include file is used to provide information needed by
***   a Precise application program using the kernel running on a
***   Arc Cores target CPU.
***                                                               
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include <stdlib.h>
#include <mqx.h>

extern char _fbss[], _ebss[];   /* defined by the linker */
/* Start CR 2409 */
#pragma weak _fbss
#pragma weak _ebss
/* End CR 2409 */

extern char _fsbss[], _esbss[]; /* defined by the linker, or weak */
extern unsigned __MEMSIZE__;
extern void __arc_main(void);
extern int main(void);

unsigned char __enable_prof_interrupt = 0;
int __INITBSS__ = 1;
int __main_at = 0; 

#if _DSPC
/*
 * Linker generates these symbols to mark the bounds of X/Y memories
 * I assume I can make them int* because the linker options file should
 * also say ALIGN(4) BLOCK(4).
 */
extern int _fXdata[], _eXdata[], _fYdata[], _eYdata[];
#pragma weak _fXdata
#pragma weak _eXdata
#pragma weak _fYdata
#pragma weak _eYdata

#define XYCONFIG	0x98
#define BURSTSYS	0x99
#define BURSTXYM	0x9a
#define BURSTSZ		0x9b
#define BURST_LOAD	(1<<30)

    // Transfers in 32 bit mode so halve xy address
#define dsp_blmem2xy(bank,xy,dest,src,size) _sr((unsigned long)src,BURSTSYS); \
    _sr(dest,BURSTXYM); \
    _sr((size | (bank<<24) | (xy<<29) | BURST_LOAD),BURSTSZ)

#define dsp_bwait() while((_lr(XYCONFIG) & 0x10) != 0)

/*
 * This routine copies the contents of the .Xdata section to X memory
 * and the contents of the .Ydata section to Y memory.
 *
 * NOTE:  We have to be carefull not to touch any X/Y aux registers
 * unless we absolutely need them.  Otherwise this code will screw
 * up on an ARC that wasn't configured with X/Y memory.
 */
static void copy_xdata() {
    int elements = ((char*)_eXdata - (char*)_fXdata) - 1;
    dsp_blmem2xy(0,0,0,_fXdata, elements);
    dsp_bwait();
    }
#pragma off_inline(copy_xdata);
static void copy_ydata() {
    int elements = ((char*)_eYdata - (char*)_fYdata) - 1;
    dsp_blmem2xy(0,1,0,_fYdata, elements);
    dsp_bwait();
    }
#pragma off_inline(copy_ydata);
static void init_xy_memory_bank_0() {
    if (_fXdata != 0 && _fXdata != _eXdata) copy_xdata();
    if (_fYdata != 0 && _fYdata != _eYdata) copy_ydata();
    }
#endif

/*
** This is the first C entry point of the ARC runtime library initialization.
*/
void __arc_main
   (
      void
   ) 
{ /* Body */

   /* 
   ** Support for linker's INITDATA feature to copy ROM data to RAM.  This can
   ** also be used to zero BSS. We'll ignore the unlikely error result (see
   ** declaration and comments in stdlib.h).
   */
#ifdef MQX_COMPRESS_INITDATA
   _initcopy();
#endif

   /* 
   ** Zero the BSS before first access unless we've been instructed not to do so
   ** by the debugger.
   */
   if (__INITBSS__) {
      memset( _fbss, 0, _ebss - _fbss );
      if (_fsbss != NULL) {
         memset( _fsbss, 0, _esbss - _fsbss );
      } /* Endif */
   } /* Endif */

#if _DSPC
    init_xy_memory_bank_0();
#endif

   __enable_prof_interrupt = 0;

#ifdef __PROFILE__
   if (__enable_prof_interrupt) {
      _enable();  // from stdlib.h
   } /* Endif */
#endif

   __main_at = __LINE__ + 1;
   main();

} /* Endbody */

/* EOF */

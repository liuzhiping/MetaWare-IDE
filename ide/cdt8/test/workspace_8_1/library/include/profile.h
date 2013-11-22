#ifndef __profile_h__
#define __profile_h__ 1
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
*** File: profile.h
***
*** Comments:
***   This file is intended to be used by applications when including profiling
***   Any profiler-specific information required can be added here
***
********************************************************************************
*END***************************************************************************/

/* Profiling information specific to compiler */
#if defined(__DCC__) || defined(__TIC54__)
   /* Nothing required */
#else
   #error "Compiler not recognized"
#endif

#endif
/* EOF */

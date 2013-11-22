#ifndef __psp_comp_h__
#define __psp_comp_h__
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
*** File: psp_comp.h
***            
*** Comments:      
***   This file determines which compiler is running, then includes
***   the compiler specific header file
***
**************************************************************************
*END*********************************************************************/

#if defined(__MET__)
#include "met_comp.h"
#else
#error "COMPILER TYPE NOT DEFINED"
#endif

#endif
/* EOF */

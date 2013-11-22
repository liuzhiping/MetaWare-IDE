/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: cplus.h
***
*** Comments:
***    This file contains source for the C++ exception test.
***
*** $Header:cplus.h, 1, 5/14/2004 12:15:00 PM, $
***
*** $NoKeywords$
***************************************************************************
*END**********************************************************************/

#ifndef __cplus_h__
#define __cplus_h__

class eclass1 {
public:
   int a;
};
class eclass2 {
public:
   int a;
};
class eclass3 {
public:
   int a;
};

extern void throw_func(int);

#endif /* __cplus_h__ */

/* EOF */


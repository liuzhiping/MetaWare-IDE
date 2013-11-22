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
*** File: thrower.cpp
***
*** Comments:
***    This file contains source for the C++ exception test.
***
*** $Header$
***
*** $NoKeywords$
***************************************************************************
*END**********************************************************************/

#include "cplus.h"

static eclass1 e1;
static eclass2 e2;
static eclass3 e3;

void throw_func
   (
      int pass
   )
{
   switch (pass) {
   case 1: throw &e1;
   case 2: throw &e2;
   case 3: throw &e3;
   }
}

/* EOF */


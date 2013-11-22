/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: ti_rom.c
***
*** Comments:
***   This file contains the function database for the time conversion
*** functions.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"

const uchar _time_days_in_month_internal[2][13] =
{
  { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 },
  { 0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }
};

const uint_32 _time_secs_before_month_internal[2][13] =
{
   {
      0,
      31UL*SECS_IN_DAY,
      59UL*SECS_IN_DAY,
      90UL*SECS_IN_DAY,
      120UL*SECS_IN_DAY,
      151UL*SECS_IN_DAY,
      181UL*SECS_IN_DAY,
      212UL*SECS_IN_DAY,
      243UL*SECS_IN_DAY,
      273UL*SECS_IN_DAY,
      304UL*SECS_IN_DAY,
      334UL*SECS_IN_DAY,
      ~0
   },
   {
      0,
      31UL*SECS_IN_DAY,
      60UL*SECS_IN_DAY,
      91UL*SECS_IN_DAY,
      121UL*SECS_IN_DAY,
      152UL*SECS_IN_DAY,
      182UL*SECS_IN_DAY,
      213UL*SECS_IN_DAY,
      244UL*SECS_IN_DAY,
      274UL*SECS_IN_DAY,
      305UL*SECS_IN_DAY,
      335UL*SECS_IN_DAY,
      ~0
   }
};

/*
** The following array contains the number of seconds between Jan 1, 1970
** and a given year. For example, to determine the number of seconds 
** between Jan 1, 1970 and Jan 1, 1988, the subscript (1988-1970) would
** be used to index into this table. Note that the first entry is equal to 
** zero (0).
**
** NOTE:  If this table is expanded to include the year 2100, then it must
** be noted that the year 2100 is NOT a leap year.  Only century years 
** divisible by 400 are leap years.
*/

const uint_32 _time_secs_before_year_internal[] =
{
   ((1970UL-1970UL) * SECS_IN_YEAR) + ( 0UL * SECS_IN_DAY),
   ((1971UL-1970UL) * SECS_IN_YEAR) + ( 0UL * SECS_IN_DAY),
   ((1972UL-1970UL) * SECS_IN_YEAR) + ( 0UL * SECS_IN_DAY),
   ((1973UL-1970UL) * SECS_IN_YEAR) + ( 1UL * SECS_IN_DAY),
   ((1974UL-1970UL) * SECS_IN_YEAR) + ( 1UL * SECS_IN_DAY),
   ((1975UL-1970UL) * SECS_IN_YEAR) + ( 1UL * SECS_IN_DAY),
   ((1976UL-1970UL) * SECS_IN_YEAR) + ( 1UL * SECS_IN_DAY),
   ((1977UL-1970UL) * SECS_IN_YEAR) + ( 2UL * SECS_IN_DAY),
   ((1978UL-1970UL) * SECS_IN_YEAR) + ( 2UL * SECS_IN_DAY),
   ((1979UL-1970UL) * SECS_IN_YEAR) + ( 2UL * SECS_IN_DAY),
   ((1980UL-1970UL) * SECS_IN_YEAR) + ( 2UL * SECS_IN_DAY),
   ((1981UL-1970UL) * SECS_IN_YEAR) + ( 3UL * SECS_IN_DAY),
   ((1982UL-1970UL) * SECS_IN_YEAR) + ( 3UL * SECS_IN_DAY),
   ((1983UL-1970UL) * SECS_IN_YEAR) + ( 3UL * SECS_IN_DAY),
   ((1984UL-1970UL) * SECS_IN_YEAR) + ( 3UL * SECS_IN_DAY),
   ((1985UL-1970UL) * SECS_IN_YEAR) + ( 4UL * SECS_IN_DAY),
   ((1986UL-1970UL) * SECS_IN_YEAR) + ( 4UL * SECS_IN_DAY),
   ((1987UL-1970UL) * SECS_IN_YEAR) + ( 4UL * SECS_IN_DAY),
   ((1988UL-1970UL) * SECS_IN_YEAR) + ( 4UL * SECS_IN_DAY),
   ((1989UL-1970UL) * SECS_IN_YEAR) + ( 5UL * SECS_IN_DAY),
   ((1990UL-1970UL) * SECS_IN_YEAR) + ( 5UL * SECS_IN_DAY),
   ((1991UL-1970UL) * SECS_IN_YEAR) + ( 5UL * SECS_IN_DAY),
   ((1992UL-1970UL) * SECS_IN_YEAR) + ( 5UL * SECS_IN_DAY),
   ((1993UL-1970UL) * SECS_IN_YEAR) + ( 6UL * SECS_IN_DAY),
   ((1994UL-1970UL) * SECS_IN_YEAR) + ( 6UL * SECS_IN_DAY),
   ((1996UL-1970UL) * SECS_IN_YEAR) + ( 6UL * SECS_IN_DAY),
   ((1996UL-1970UL) * SECS_IN_YEAR) + ( 6UL * SECS_IN_DAY),
   ((1997UL-1970UL) * SECS_IN_YEAR) + ( 7UL * SECS_IN_DAY),
   ((1998UL-1970UL) * SECS_IN_YEAR) + ( 7UL * SECS_IN_DAY),
   ((1999UL-1970UL) * SECS_IN_YEAR) + ( 7UL * SECS_IN_DAY),
   ((2000UL-1970UL) * SECS_IN_YEAR) + ( 7UL * SECS_IN_DAY),
   ((2001UL-1970UL) * SECS_IN_YEAR) + ( 8UL * SECS_IN_DAY),
   ((2002UL-1970UL) * SECS_IN_YEAR) + ( 8UL * SECS_IN_DAY),
   ((2003UL-1970UL) * SECS_IN_YEAR) + ( 8UL * SECS_IN_DAY),
   ((2004UL-1970UL) * SECS_IN_YEAR) + ( 8UL * SECS_IN_DAY),
   ((2005UL-1970UL) * SECS_IN_YEAR) + ( 9UL * SECS_IN_DAY),
   ((2006UL-1970UL) * SECS_IN_YEAR) + ( 9UL * SECS_IN_DAY),
   ((2007UL-1970UL) * SECS_IN_YEAR) + ( 9UL * SECS_IN_DAY),
   ((2008UL-1970UL) * SECS_IN_YEAR) + ( 9UL * SECS_IN_DAY),
   ((2009UL-1970UL) * SECS_IN_YEAR) + (10UL * SECS_IN_DAY),
   ((2010UL-1970UL) * SECS_IN_YEAR) + (10UL * SECS_IN_DAY),
   ((2011UL-1970UL) * SECS_IN_YEAR) + (10UL * SECS_IN_DAY),
   ((2012UL-1970UL) * SECS_IN_YEAR) + (10UL * SECS_IN_DAY),
   ((2013UL-1970UL) * SECS_IN_YEAR) + (11UL * SECS_IN_DAY),
   ((2014UL-1970UL) * SECS_IN_YEAR) + (11UL * SECS_IN_DAY),
   ((2015UL-1970UL) * SECS_IN_YEAR) + (11UL * SECS_IN_DAY),
   ((2016UL-1970UL) * SECS_IN_YEAR) + (11UL * SECS_IN_DAY),
   ((2017UL-1970UL) * SECS_IN_YEAR) + (12UL * SECS_IN_DAY),
   ((2018UL-1970UL) * SECS_IN_YEAR) + (12UL * SECS_IN_DAY),
   ((2019UL-1970UL) * SECS_IN_YEAR) + (12UL * SECS_IN_DAY),
   ((2020UL-1970UL) * SECS_IN_YEAR) + (12UL * SECS_IN_DAY),
   ((2021UL-1970UL) * SECS_IN_YEAR) + (13UL * SECS_IN_DAY),
   ((2022UL-1970UL) * SECS_IN_YEAR) + (13UL * SECS_IN_DAY),
   ((2023UL-1970UL) * SECS_IN_YEAR) + (13UL * SECS_IN_DAY),
   ((2024UL-1970UL) * SECS_IN_YEAR) + (13UL * SECS_IN_DAY),
   ((2025UL-1970UL) * SECS_IN_YEAR) + (14UL * SECS_IN_DAY),
   ((2026UL-1970UL) * SECS_IN_YEAR) + (14UL * SECS_IN_DAY),
   ((2027UL-1970UL) * SECS_IN_YEAR) + (14UL * SECS_IN_DAY),
   ((2028UL-1970UL) * SECS_IN_YEAR) + (14UL * SECS_IN_DAY),
   ((2029UL-1970UL) * SECS_IN_YEAR) + (15UL * SECS_IN_DAY),
   ((2030UL-1970UL) * SECS_IN_YEAR) + (15UL * SECS_IN_DAY),
   ((2031UL-1970UL) * SECS_IN_YEAR) + (15UL * SECS_IN_DAY),
   ((2032UL-1970UL) * SECS_IN_YEAR) + (15UL * SECS_IN_DAY),
   ((2033UL-1970UL) * SECS_IN_YEAR) + (16UL * SECS_IN_DAY),
   ((2034UL-1970UL) * SECS_IN_YEAR) + (16UL * SECS_IN_DAY),
   ((2035UL-1970UL) * SECS_IN_YEAR) + (16UL * SECS_IN_DAY),
   ((2036UL-1970UL) * SECS_IN_YEAR) + (16UL * SECS_IN_DAY),
   ((2037UL-1970UL) * SECS_IN_YEAR) + (17UL * SECS_IN_DAY),
   ((2038UL-1970UL) * SECS_IN_YEAR) + (17UL * SECS_IN_DAY),
   ((2039UL-1970UL) * SECS_IN_YEAR) + (17UL * SECS_IN_DAY),
   ((2040UL-1970UL) * SECS_IN_YEAR) + (17UL * SECS_IN_DAY),
   ((2041UL-1970UL) * SECS_IN_YEAR) + (18UL * SECS_IN_DAY),
   ((2042UL-1970UL) * SECS_IN_YEAR) + (18UL * SECS_IN_DAY),
   ((2043UL-1970UL) * SECS_IN_YEAR) + (18UL * SECS_IN_DAY),
   ((2044UL-1970UL) * SECS_IN_YEAR) + (18UL * SECS_IN_DAY),
   ((2045UL-1970UL) * SECS_IN_YEAR) + (19UL * SECS_IN_DAY),
   ((2046UL-1970UL) * SECS_IN_YEAR) + (19UL * SECS_IN_DAY),
   ((2047UL-1970UL) * SECS_IN_YEAR) + (19UL * SECS_IN_DAY),
   ((2048UL-1970UL) * SECS_IN_YEAR) + (19UL * SECS_IN_DAY),
   ((2049UL-1970UL) * SECS_IN_YEAR) + (20UL * SECS_IN_DAY),
   ((2050UL-1970UL) * SECS_IN_YEAR) + (20UL * SECS_IN_DAY),
   ((2051UL-1970UL) * SECS_IN_YEAR) + (20UL * SECS_IN_DAY),
   ((2052UL-1970UL) * SECS_IN_YEAR) + (20UL * SECS_IN_DAY),
   ((2053UL-1970UL) * SECS_IN_YEAR) + (21UL * SECS_IN_DAY),
   ((2054UL-1970UL) * SECS_IN_YEAR) + (21UL * SECS_IN_DAY),
   ((2055UL-1970UL) * SECS_IN_YEAR) + (21UL * SECS_IN_DAY),
   ((2056UL-1970UL) * SECS_IN_YEAR) + (21UL * SECS_IN_DAY),
   ((2057UL-1970UL) * SECS_IN_YEAR) + (22UL * SECS_IN_DAY),
   ((2058UL-1970UL) * SECS_IN_YEAR) + (22UL * SECS_IN_DAY),
   ((2059UL-1970UL) * SECS_IN_YEAR) + (22UL * SECS_IN_DAY),
   ((2060UL-1970UL) * SECS_IN_YEAR) + (22UL * SECS_IN_DAY),
   ((2061UL-1970UL) * SECS_IN_YEAR) + (23UL * SECS_IN_DAY),
   ((2062UL-1970UL) * SECS_IN_YEAR) + (23UL * SECS_IN_DAY),
   ((2063UL-1970UL) * SECS_IN_YEAR) + (23UL * SECS_IN_DAY),
   ((2064UL-1970UL) * SECS_IN_YEAR) + (23UL * SECS_IN_DAY),
   ((2065UL-1970UL) * SECS_IN_YEAR) + (24UL * SECS_IN_DAY),
   ((2066UL-1970UL) * SECS_IN_YEAR) + (24UL * SECS_IN_DAY),
   ((2067UL-1970UL) * SECS_IN_YEAR) + (24UL * SECS_IN_DAY),
   ((2068UL-1970UL) * SECS_IN_YEAR) + (24UL * SECS_IN_DAY),
   ((2069UL-1970UL) * SECS_IN_YEAR) + (25UL * SECS_IN_DAY),
   ((2070UL-1970UL) * SECS_IN_YEAR) + (25UL * SECS_IN_DAY),
   ((2071UL-1970UL) * SECS_IN_YEAR) + (25UL * SECS_IN_DAY),
   ((2072UL-1970UL) * SECS_IN_YEAR) + (25UL * SECS_IN_DAY),
   ((2073UL-1970UL) * SECS_IN_YEAR) + (26UL * SECS_IN_DAY),
   ((2074UL-1970UL) * SECS_IN_YEAR) + (26UL * SECS_IN_DAY),
   ((2075UL-1970UL) * SECS_IN_YEAR) + (26UL * SECS_IN_DAY),
   ((2076UL-1970UL) * SECS_IN_YEAR) + (26UL * SECS_IN_DAY),
   ((2077UL-1970UL) * SECS_IN_YEAR) + (27UL * SECS_IN_DAY),
   ((2078UL-1970UL) * SECS_IN_YEAR) + (27UL * SECS_IN_DAY),
   ((2079UL-1970UL) * SECS_IN_YEAR) + (27UL * SECS_IN_DAY),
   ((2080UL-1970UL) * SECS_IN_YEAR) + (27UL * SECS_IN_DAY),
   ((2081UL-1970UL) * SECS_IN_YEAR) + (28UL * SECS_IN_DAY),
   ((2082UL-1970UL) * SECS_IN_YEAR) + (28UL * SECS_IN_DAY),
   ((2083UL-1970UL) * SECS_IN_YEAR) + (28UL * SECS_IN_DAY),
   ((2084UL-1970UL) * SECS_IN_YEAR) + (28UL * SECS_IN_DAY),
   ((2085UL-1970UL) * SECS_IN_YEAR) + (29UL * SECS_IN_DAY),
   ((2086UL-1970UL) * SECS_IN_YEAR) + (29UL * SECS_IN_DAY),
   ((2087UL-1970UL) * SECS_IN_YEAR) + (29UL * SECS_IN_DAY),
   ((2088UL-1970UL) * SECS_IN_YEAR) + (29UL * SECS_IN_DAY),
   ((2089UL-1970UL) * SECS_IN_YEAR) + (30UL * SECS_IN_DAY),
   ((2090UL-1970UL) * SECS_IN_YEAR) + (30UL * SECS_IN_DAY),
   ((2091UL-1970UL) * SECS_IN_YEAR) + (30UL * SECS_IN_DAY),
   ((2092UL-1970UL) * SECS_IN_YEAR) + (30UL * SECS_IN_DAY),
   ((2093UL-1970UL) * SECS_IN_YEAR) + (31UL * SECS_IN_DAY),
   ((2094UL-1970UL) * SECS_IN_YEAR) + (31UL * SECS_IN_DAY),
   ((2095UL-1970UL) * SECS_IN_YEAR) + (31UL * SECS_IN_DAY),
   ((2096UL-1970UL) * SECS_IN_YEAR) + (31UL * SECS_IN_DAY),
   ((2097UL-1970UL) * SECS_IN_YEAR) + (32UL * SECS_IN_DAY),
   ((2098UL-1970UL) * SECS_IN_YEAR) + (32UL * SECS_IN_DAY),
   ((2099UL-1970UL) * SECS_IN_YEAR) + (32UL * SECS_IN_DAY),
   ~0
};

/* EOF */

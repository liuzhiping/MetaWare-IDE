/* --- g_util.c --- 2007 Nov 22, by Prem Sobel
 *
 * This modules implements utility functions needed by gdraw
 */

#include <stdio.h>
#include <stdlib.h>
#include "g_util.h"
// ------------------------------------------------------------------------
int iabs(int i) {
    return i<0 ? -i : i;
} // iabs
// ------------------------------------------------------------------------
void swap2int(int *a, int *b) { int s;
     s = *a;
    *a = *b;
    *b =  s;
} // swap2int
// ------------------------------------------------------------------------
void swap3int(int *a, int *b, int *c) { int s;
     s = *a;
    *a = *b;
    *b = *c;
    *c =  s;
} // swap3int
// ------------------------------------------------------------------------
// Transforms distance d into multiples of unit distance,
// and removing unit offset.
int unit_dist(int ud, int d) {
    return d/ud-1;
} // unit_dist
// ------------------------------------------------------------------------
void *zalloc(int nb) {
void *p;
    p = malloc(nb);
    if(!p) {
        printf("malloc %d failed\n", nb);
        exit(1);
    }
    return p;
} // zalloc
// ------------------------------------------------------------------------
static int seed32 = 29;

void srand16(int seed) {
    seed32 = seed;
} // srand16
// ------------------------------------------------------------------------
int rand16(void) {
    seed32 += 2711;
    seed32 *= 229;
    seed32 = ((seed32>> 8)^seed32)&0x000000FF |
             ((seed32>> 8)^seed32)&0x0000FF00 |
             ((seed32>> 8)^seed32)&0x00FF0000 |
             ((seed32<<24)^seed32)&0xFF000000;
    return (seed32>>8)&0xFFFF;
} // rand16
// ========================================================================

/* --- g_arc.c --- 2008 Jan 02, by Prem Sobel
 *
 * This modules implements the (currently nondirected) arcs in a graph.
 */
#include <stdio.h>
#include <stdlib.h>
#include "g_util.h"
#include "g_arc.h"
#include "g_node.h"
#include "vr_dbg.h"
// ========================================================================
arc_t *arc_alloc(void) {
arc_t *a;
    a = zalloc(sizeof(arc_t));
    a->a_node = 0;
    a->a_next = 0;
    return a;
} // arc_alloc
// ------------------------------------------------------------------------
void arc_free(arc_t *a) {
    if(a) {
        free(a);
    }
} // arc_free
// ------------------------------------------------------------------------
void arc_draw(arc_t *a) {
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
        fprintf(fp_gdraw, " %d", a->a_node->n_id);
} // arc_alloc
// ========================================================================


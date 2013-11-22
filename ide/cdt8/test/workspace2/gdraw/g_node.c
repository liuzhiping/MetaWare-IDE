/* --- g_node.c --- 2008 Jan 04, by Prem Sobel
 *
 * This modules implements nodes for gdraw.
 */

#include <stdio.h>
#include <stdlib.h>
#include "g_util.h"
#include "g_node.h"
#include "g_arc.h"
#include "vr_dbg.h"

// ========================================================================
node_t *node_alloc(int id, int empty, int words) {
node_t *n;
int k;
    n = zalloc(sizeof(node_t));
    n->n_id = id;
    n->n_wr = n->n_hr = 0;
    n->n_xc = n->n_yc = 0;
    n->n_fx = n->n_fy = 0.0;
    n->n_empty = empty;
    if(!empty) {
        n->n_words_in_node_set = words;
        n->n_node_set = zalloc(words*sizeof(unsigned));
        for(k=0; k<words; k++)
            n->n_node_set[k] = 0;
    } else {
        n->n_words_in_node_set = 0;
        n->n_node_set = 0;
    }
    n->n_num_arcs = 0;
    n->n_arc = 0;
    n->n_next = 0;
    return n;
} // node_alloc
// ------------------------------------------------------------------------
void node_free(node_t *n) {
arc_t *a, *an;
    if(n) {
        if(n->n_node_set)
            free(n->n_node_set);
        for(a=n->n_arc; a; a=an) {
            an = a->a_next;
            arc_free(a);
        }
        free(n);
    }
} // node_free
//------------------------------------------------------------------------
void node_draw(node_t *n, int unit, int ud) {
int xc, yc;
arc_t *a;
    xc = n->n_xc;
    yc = n->n_yc;
    if(unit) {
        xc = unit_dist(ud, xc);
        yc = unit_dist(ud, yc);
    }
    if(!n->n_empty) {
        if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW) {
            fprintf(fp_gdraw, "%2d : xc=%02d yc=%02d", n->n_id, xc, yc);
            fprintf(fp_gdraw, " wr=%02d hr=%02d :", n->n_wr, n->n_hr);
 	    }
        for(a=n->n_arc; a; a=a->a_next) {
            arc_draw(a);
        }
    } else {
        if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
            fprintf(fp_gdraw, "%2d : xc=%02d yc=%02d", n->n_id, xc, yc);
    }
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
        fprintf(fp_gdraw, "\n");
} // node_draw
//------------------------------------------------------------------------
void node_draw_d(node_t *n, int x, int y) {
double xdc, ydc;
arc_t *a;
    xdc = n->n_xdc+x;
    ydc = n->n_ydc+y;
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW) {
        fprintf(fp_gdraw, "%2d : xc=%+07.2f yc=%+07.2f", n->n_id, xdc, ydc);
        fprintf(fp_gdraw, " wr=%02d hr=%02d fx=%+07.2f fy=%+07.2f :",
            n->n_wr, n->n_hr, n->n_fx, n->n_fy, n->n_num_arcs);
    }
    for(a=n->n_arc; a; a=a->a_next) {
        arc_draw(a);
    }
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
        fprintf(fp_gdraw, "\n");
} // node_draw_d
// ------------------------------------------------------------------------
#define dabs(d) ((d<0.0) ? -d : d)
#define DAMPING 0.4

int node_move(node_t *n) {
// Returns 0 if no change, 1 if any change.
// If not an empty node resets force to zero.
int chg=0;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_NODE, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    if((0.4<dabs(n->n_fx)) || (0.4<dabs(n->n_fy)))
        chg = 1;
    n->n_xdc += n->n_fx*DAMPING;
    n->n_ydc += n->n_fy*DAMPING;
    n->n_fx = n->n_fy = 0;
    vr_dbg_exit(VR_DBG_NODE, "@- {%u} chg=%d\n", fid, chg); // VR_DBG
    return chg;
} // node_move
// ------------------------------------------------------------------------
node_t *node_find(node_t *n, int id) {
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_NODE, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    for(; n; n=n->n_next) {
        if(id == n->n_id) {
            vr_dbg_exit(VR_DBG_NODE, "@- {%u} n=%d\n", fid, n->n_id); // VR_DBG
            return n;
        }
    }
    printf("Error: could not find node %s\n", id);
    exit(1);
    return 0;
} // node_find
// ------------------------------------------------------------------------
// Add node n2 via an arc to n1

void node_add_arc(node_t *n1, node_t *n2) {
arc_t *a;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_NODE, "@+ {%u} n1=%d n2=%d\n",
        fid=++*vr_dbg_fid, n1->n_id, n2->n_id); // VR_DBG
    a = arc_alloc();
    a->a_node = n2;
    a->a_next = n1->n_arc;
    n1->n_arc = a;
    n1->n_num_arcs++;
    vr_dbg_exit(VR_DBG_NODE, "@- {%u}\n", fid); // VR_DBG
} // node_add_arc
// ------------------------------------------------------------------------
void node_fill_set(unsigned *set, node_t *n) {
int w, b;
arc_t *a;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_NODE, "@+ {%u} n=%d\n", fid=++*vr_dbg_fid, n->n_id); // VR_DBG
    w = n->n_id/BITS_PER_UNSIGNED;
    b = n->n_id%BITS_PER_UNSIGNED;
    if(set[w] & (1<<b))
        return; // stop recursion when this n_id is seen again
    set[w] |= (1<<b);
    for(a=n->n_arc; a; a=a->a_next) {
        node_fill_set(set, a->a_node);
    }
    vr_dbg_exit(VR_DBG_NODE, "@- {%u}\n", fid); // VR_DBG
} // node_fill_set
// ------------------------------------------------------------------------
// returns 0 if 
int node_in_set(unsigned *set, node_t *n) {
int w, b, in;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_NODE, "@+ {%u} n=%d\n", fid=++*vr_dbg_fid, n->n_id); // VR_DBG
    w = n->n_id/BITS_PER_UNSIGNED;
    b = n->n_id%BITS_PER_UNSIGNED;
    in = set[w] & (1<<b);
    vr_dbg_exit(VR_DBG_NODE, "@- {%u} n=%d\n", fid, in); // VR_DBG
    return in;
} // node_fill_set
// ------------------------------------------------------------------------
// returns 1 if set s1 and s2 have at least one common element,
// returns 0 if s1 and s2 are disjoint

int node_set_overlap(int nw, unsigned *s1, unsigned *s2) { int k;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_NODE, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    for(k=0; k<nw; k++) {
        if(s1[k] & s2[k]) {
            vr_dbg_exit(VR_DBG_NODE, "@- {%u} 1\n", fid); // VR_DBG
            return 1;
        }
    }
    vr_dbg_exit(VR_DBG_NODE, "@- {%u} 0\n", fid); // VR_DBG
    return 0;
} // node_set_overlap
// ========================================================================


/* --- g_sys.c --- 2008 Jan 16, by Prem Sobel
 *
 * This modules implements a system consisting of disjoint nodes, dsets,
 * and the functions to lay them out.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "g_util.h"
#include "g_arc.h"
#include "g_node.h"
#include "g_dset.h"
#include "g_sys.h"
#include "vr_dbg.h"

#define SEED 12521

#define MIN_SEP 8
// ========================================================================
// Yields a random integer in the range 0..r-1
// (assumes r<=RAND_MAX)
#define rand_range(r) (rand16()%r)
// ========================================================================
sys_t *sys_alloc(int total_nodes) {
sys_t *s;
    s = zalloc(sizeof(sys_t));
    s->s_dset = 0;
    s->s_total_nodes = total_nodes;
    return s;
} // sys_alloc
// ------------------------------------------------------------------------
void sys_free(sys_t *s) {
dset_t *ds, *dsn;
    if(s) {
        for(ds=s->s_dset; ds; ds=dsn) {
            dsn = ds->ds_next;
            dset_free(ds);
        }
        free(s);
    }
} // sys_free
//------------------------------------------------------------------------
void sys_draw(sys_t *s, int unit, char *msg) {
dset_t *ds;
    for(ds=s->s_dset; ds; ds=ds->ds_next) {
        dset_draw(ds, unit, msg);
    }
} // sys_draw
// ------------------------------------------------------------------------
// split a list of nodes into disjoint sets and put them in 's'

void sys_n2dset(sys_t *s, node_t *n_sys) {
dset_t *ds;
node_t *n, *nn;
int ds_id=0, words, k;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_SYS, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    words = WORDS(s->s_total_nodes);
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
        fprintf(fp_gdraw, "n2dset (words=%d) =>\n", words);
    // fill in nodes sets recursively
    for(n=n_sys; n; n=n->n_next) {
        node_fill_set(n->n_node_set, n);
        if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW) {
            fprintf(fp_gdraw, "Node %d:", n->n_id);
            for(k=0; k<words; k++)
                fprintf(fp_gdraw, " %08X", n->n_node_set[k]);
            fprintf(fp_gdraw, "\n");
        }
    }
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
        fprintf(fp_gdraw, "%s", sep);
    // split system into disjoint sets of connected nodes
    for(n=n_sys; n; n=nn) {
        // Remember n->n_next before it is lost when 'n' is added to a dset
        nn = n->n_next;
        // is node 'n' in an existing dset?
        for(ds=s->s_dset; ds; ds=ds->ds_next) {
            if(node_in_set(ds->ds_node->n_node_set, n)) { // yes
                dset_add_node(ds, n); // add
                goto next_n;
            }
        }
        // no, create new dset
        ds = dset_alloc(ds_id++);
        dset_add_node(ds, n);
        // add to sys
        ds->ds_next = s->s_dset;
        s->s_dset = ds;
        next_n: ;
    }
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW) {
        fprintf(fp_gdraw, "%s", sep);
        sys_draw(s, 0, "n2dset");
    }
    vr_dbg_exit(VR_DBG_SYS, "@- {%u}\n", fid); // VR_DBG
} // sys_n2dset
// ------------------------------------------------------------------------
// Generate a random system of nodes and arcs subject to constraints

void sys_gen_rand(sys_t *s) {
node_t *n_sys=0, *n, *n1, *n2;
arc_t  *a;
int k, id1, id2, words;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_SYS, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    words = WORDS(NUM_NODES);
    srand16(SEED); // for repeatability
    // allocate nodes and fill in random node sizes
    for(k=0; k<NUM_NODES; k++) {
        n = node_alloc(k, 0, words);
        n->n_wr = SIZE_MIN+rand_range(SIZE_STEP);
        n->n_hr = SIZE_MIN+rand_range(SIZE_STEP);
        n->n_next = n_sys;
        n_sys = n;
    }
    // generate NUM_ARCS random graph arcs subject to ARCS_MAX limit per node
    for(k=0; k<NUM_ARCS; k++) {
        again_a:
        // choose n1
        do {
            id1 = rand_range(NUM_NODES);
            n1 = node_find(n_sys, id1);
        } while(n1->n_num_arcs >= ARCS_MAX);
        // choose n2
        do {
            id2 = rand_range(NUM_NODES);
            if(id2 == id1)
                goto again_a;
            n2 = node_find(n_sys, id2);
        } while(n2->n_num_arcs >= ARCS_MAX);
        // no duplicate arcs
        for(a=n1->n_arc; a; a=a->a_next) {
            if(n2 == a->a_node)
                goto again_a;
        }
        // ok, add arc to both nodes (will add direction later - FIXME)
        node_add_arc(n1, n2);
        node_add_arc(n2, n1);
    }
    // show random graph
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW) {
        fprintf(fp_gdraw, "Random system => SEED=%d NUM_NODES=%d NUM_ARCS=%d ARCS_MAX=%d\n",
            SEED, NUM_NODES, NUM_ARCS, ARCS_MAX);
	    for(n=n_sys; n; n=n->n_next) {
	        node_draw(n, 0, 0);
	    }
        fprintf(fp_gdraw, "%s", sep);
    }
    sys_n2dset(s, n_sys);
    vr_dbg_exit(VR_DBG_SYS, "@- {%u}\n", fid); // VR_DBG
} // sys_gen_rand
// ------------------------------------------------------------------------
void sys_layout(sys_t *s) {
dset_t *ds;
int max, ud, n_id=0;
node_t *n;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_SYS, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    for(ds=s->s_dset; ds; ds=ds->ds_next) {
        // calc ds->ds_calc_unit_dist
        max = 0;
        for(n=ds->ds_node; n; n=n->n_next) {
            n_id++; // count the number of nodes
            if(n->n_wr > max)
                max = n->n_wr;
            if(n->n_hr > max)
                max = n->n_hr;
        }
        ds->ds_unit_dist = ud = max+MIN_SEP;
        if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
            fprintf(fp_gdraw, "@ ds=%d: ds_unit_dist=%d\n", ds->ds_id, ud);
    }
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW) {
        fprintf(fp_gdraw, "%s", sep);
        sys_draw(s, 0, "init");
    }
    for(ds=s->s_dset; ds; ds=ds->ds_next) {
        dset_layout(ds, &n_id, s->s_total_nodes);
    }
    vr_dbg_exit(VR_DBG_SYS, "@- {%u}\n", fid); // VR_DBG
} // sys_layout
// ========================================================================

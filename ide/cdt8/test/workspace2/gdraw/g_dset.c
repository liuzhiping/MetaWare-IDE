/* --- g_dset.c --- 2008 Jan 15, by Prem Sobel
 *
 * This module implements dset_t, a disjoint set of connected nodes.
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "g_util.h"
#include "g_dset.h"
#include "g_node.h"
#include "g_arc.h"
#include "vr_dbg.h"

#define ATTRACT_SCALE 0.25
#define REPEL_SCALE   0.50
#define Y_POSITION_GAP 16.0

char *sep="---------------------------------------------------\n";
// ========================================================================
static void add_d(int ud, int *d, int dx, int dy) {
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_DSET, "@+ {%u} dx=%d dy=%d\n", fid=++*vr_dbg_fid, dx, dy); // VR_DBG
    if(!dx&&(ud==dy) || !dy&&(ud==dx)) {
        *d += 0;
    } else if((ud==dx) && (ud==dy)) {
        *d += ud+1;
    } else {
        *d += dx+dy;
    }
    vr_dbg_exit(VR_DBG_DSET, "@- {%u}\n", fid); // VR_DBG
} // add_d
// ========================================================================
dset_t *dset_alloc(int id) {
dset_t *ds;
    ds = zalloc(sizeof(dset_t));
    ds->ds_id = id;
    ds->ds_nc = 0;
    ds->ds_num_n = 0;
    ds->ds_num_n2d = 0;
    ds->ds_unit_dist = 0; // means not yet calculated
    ds->ds_x = ds->ds_y = 0;
    ds->ds_node = 0;
    ds->ds_next = 0;
    return ds;
} // dset_alloc
// ------------------------------------------------------------------------
void dset_free(dset_t *ds) {
node_t *n, *nn;
    if(ds) {
        for(n=ds->ds_node; n; n=nn) {
            nn = n->n_next;
            node_free(n);
        }
        free(ds);
    }
} // dset_free
//------------------------------------------------------------------------
void dset_draw(dset_t *ds, int unit, char *msg) {
node_t *n;
    if(!(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW))
         return;
    fprintf(fp_gdraw, "%s ds=%d =>\n", msg, ds->ds_id);
    for(n=ds->ds_node; n; n=n->n_next) {
        if(unit>=0)
            node_draw(n, unit, ds->ds_unit_dist);
        else
            node_draw_d(n, ds->ds_x, ds->ds_y);
    }
    fprintf(fp_gdraw, "%s", sep);
} // dset_draw
// ------------------------------------------------------------------------
void dset_add_node(dset_t *ds, node_t *n) {
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_DSET, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
        fprintf(fp_gdraw, "@ add %snode %d to dset %d\n",
            n->n_empty ? "empty " : "", n->n_id, ds->ds_id);
    n->n_next = ds->ds_node;
    ds->ds_node = n; // assumes not a duplicate!
    if(!n->n_empty)
        ds->ds_num_n++;
    ds->ds_num_n2d++;
    vr_dbg_exit(VR_DBG_NODE, "@- {%u}\n", fid); // VR_DBG
} // dset_add_node
// ------------------------------------------------------------------------
static void dset_arc_pull(dset_t *ds) {
double xdc1, ydc1, xd, yd;
node_t *n1, *n2;
arc_t *a;
int w, h;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_DSET, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
        for(n1=ds->ds_node; n1; n1=n1->n_next) {
            if(n1->n_empty)
                continue;
            xdc1 = n1->n_xdc;
            ydc1 = n1->n_ydc;
            for(a=n1->n_arc; a; a=a->a_next) {
                n2  = a->a_node;
                // prevent node overlap in x direction
                w = n1->n_wr + n2->n_wr;
                xd = n2->n_xdc - xdc1;
                if(xd < 0.0) {
                    if(-xd < w) {
                        xd = 0.0;
                    } else {
                        xd += w;
                    }
                } else {
                    if(xd < w) {
                        xd = 0.0;
                    } else {
                        xd -= w;
                    }
                }
                // prevent node overlap in y direction
                h = n1->n_hr + n2->n_hr;
                yd = n2->n_ydc - ydc1;
                if(yd < 0.0) {
                    if(-yd < h) {
                        yd = 0.0;
                    } else {
                        yd += h;
                    }
                } else {
                    if(yd < h) {
                        yd = 0.0;
                    } else {
                        yd -= h;
                    }
                }
                // attractive force proportional to distance (after overlap removed)
                n1->n_fx += xd*ATTRACT_SCALE;
                n1->n_fy += yd*ATTRACT_SCALE;
                n2->n_fx -= xd*ATTRACT_SCALE;
                n2->n_fy -= yd*ATTRACT_SCALE;
            }
        }
    vr_dbg_exit(VR_DBG_NODE, "@- {%u}\n", fid); // VR_DBG
} // dset_arc_pull
// ------------------------------------------------------------------------
static void dset_node_repel(dset_t *ds) {
double xdc1, ydc1, xdc2, ydc2, dx, dy /*, min_x, max_y, max_hr, k */;
struct node_s *n1, *n2;
double d;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_DSET, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    for(n1=ds->ds_node; n1; n1=n1->n_next) {
        xdc1 = n1->n_xdc;
        ydc1 = n1->n_ydc;
        for(n2=n1->n_next; n2; n2=n2->n_next) {
            xdc2 = n2->n_xdc;
            ydc2 = n2->n_ydc;
            dx = xdc1 - xdc2;
            dy = ydc1 - ydc2;
            d = 0.001+sqrt(dx*dx+dy*dy)*REPEL_SCALE;
            // repelling force inversely proportional to distance
            n1->n_fx += dx/d;
            n1->n_fy += dy/d;
            n2->n_fx += -dx/d;
            n2->n_fy += -dy/d;
        }
    }
    vr_dbg_exit(VR_DBG_NODE, "@- {%u}\n", fid); // VR_DBG
} // dset_node_repel
// ------------------------------------------------------------------------
static int dset_move(dset_t *ds) {
// returns 0 if no change, 1 if any change
int chg=0;
node_t *n;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_DSET, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    for(n=ds->ds_node; n; n=n->n_next) {
        chg |= node_move(n);
    }
    vr_dbg_exit(VR_DBG_NODE, "@- {%u} chg=%d\n", fid, chg); // VR_DBG
    return chg;
} // dset_move
// ------------------------------------------------------------------------
// Scale position of nodes to avoid node overlap.
// Return vertical size of resulting scaled DSet plus gap.

int dset_scale_pos(dset_t *ds) {
  node_t *n1, *n2;
  double ovlap;
  double ovlap_x=0.0, ovlap_y=0.0; // overlap factor per dimension
  double max_x = -9999999.0;
  double min_x = +9999999.0;
  double max_y = -9999999.0;
  double min_y = +9999999.0;
  int h;
  unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_DSET, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    // find overlap of nodes
    for(n1=ds->ds_node; n1; n1=n1->n_next) {
        if(n1->n_empty)
            continue;
        for(n2=ds->ds_node; n2; n2=n2->n_next) {
            if(n2 == n1)
                continue;
            if(n2->n_empty)
                continue;
            // do nodes overlap (even if not connected)?
            if((n1->n_xdc+n1->n_wr < n2->n_xdc-n2->n_wr) ||
               (n1->n_xdc-n1->n_wr > n2->n_xdc+n2->n_wr) ||
               (n1->n_ydc+n1->n_hr < n2->n_ydc-n2->n_hr) ||
               (n1->n_ydc-n1->n_hr > n2->n_ydc+n2->n_hr)) { // no
                continue;
            }
            // calculate overlap factor, ovlap, in x dimension (if any)
            if(n1->n_xdc < n2->n_xdc) {
                /*      n1
                 *   +-----+ (ignore relative vertical displacement)
                 *   |     |
                 *   |  * +|...+
                 *   |    !|   !
                 *   +-----+   !
                 *        !  * ! n2
                 *        !    !
                 *        +....+
                 */
                ovlap = n1->n_xdc+n1->n_wr-(n2->n_xdc-n2->n_wr);
                if(ovlap > ovlap_x)
                    ovlap_x = ovlap;
            } else if(n1->n_xdc > n2->n_xdc) {
                /*      n2
                 *   +-----+ (ignore relative vertical displacement)
                 *   |     |
                 *   |  * +|...+
                 *   |    !|   !
                 *   +-----+   !
                 *        !  * ! n1
                 *        !    !
                 *        +....+
                 */
                ovlap = (n2->n_xdc+n2->n_wr-(n1->n_xdc-n1->n_wr))/(n1->n_xdc-n2->n_xdc);
                if(ovlap > ovlap_x)
                    ovlap_x = ovlap;
            }
            // calculate overlap factor, ovlap, in y dimension (if any)
            if(n1->n_ydc < n2->n_ydc) {
                /*      n1
                 *   +-----+ (ignore relative horizontal displacement)
                 *   |     |
                 *   |     |
                 *   |  *  |
                 *   |    +|...+
                 *   |    !|   !
                 *   +-----+   !
                 *        !  * ! n2
                 *        !    !
                 *        +....+
                 */
                ovlap = n1->n_ydc+n1->n_hr-(n2->n_ydc-n2->n_hr);
                if(ovlap > ovlap_y)
                    ovlap_y = ovlap;
            } else if(n1->n_ydc > n2->n_ydc) {
                /*      n2
                 *   +-----+ (ignore relative horizontal displacement)
                 *   |     |
                 *   |     |
                 *   |  *  |
                 *   |    +|...+
                 *   |    !|   !
                 *   +-----+   !
                 *        !  * ! n1
                 *        !    !
                 *        +....+
                 */
                ovlap = n2->n_ydc+n2->n_hr-(n1->n_ydc-n1->n_hr);
                if(ovlap > ovlap_y)
                    ovlap_y = ovlap;
            }
        }
    }
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
        fprintf(fp_gdraw, "ds=%d ovlap_x=%.2f ovlap_y=%.2f\n",
            ds->ds_id, ovlap_x, ovlap_y);
    // find min_x, max_x and min_y, max_y
    for(n1=ds->ds_node; n1; n1=n1->n_next) {
        if(n1->n_empty)
            continue;
        // find min_x
        if(min_x > n1->n_xdc-n1->n_wr)
            min_x = n1->n_xdc-n1->n_wr;
        // find max_x
        if(max_x < n1->n_xdc+n1->n_wr)
            max_x = n1->n_xdc+n1->n_wr;
        // find min_y
        if(min_y > n1->n_ydc-n1->n_hr)
            min_y = n1->n_ydc-n1->n_hr;
        // find max_y
        if(max_y < n1->n_ydc+n1->n_hr)
            max_y = n1->n_ydc+n1->n_hr;
    }
    // scale position of Nodes to eliminate all overlap,
    for(n1=ds->ds_node; n1; n1=n1->n_next) {
        if(n1->n_empty)
            continue;
        // scale position
        n1->n_xdc += (n1->n_xdc-min_x)*ovlap_x;
        n1->n_ydc += (n1->n_ydc-min_y)*ovlap_y;
        if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
            fprintf(fp_gdraw, "n=%d xdc=%.2f ydc=%.2f\n",
                n1->n_id, n1->n_xdc, n1->n_ydc);
    }
    // return vertical size of DSet plus gap
    h = (int)(max_y-min_y+Y_POSITION_GAP);
    vr_dbg_exit(VR_DBG_NODE, "@- {%u} h=%d\n", fid, h); // VR_DBG
    return h;
} // dset_scale_pos
// ------------------------------------------------------------------------
void dset_layout(dset_t *ds,
                 int *n_id, // ptr to next available unused node id value
                 int words) {
int num, c, r, k, kk, any, d1, d2, d3, dx, dy, ud;
node_t *n, *n1, *n2, *n3, *nn, *nm, *np, *nq;
arc_t *a;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_DSET, "@+ {%u}\n", fid=++*vr_dbg_fid); // VR_DBG
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
        fprintf(fp_gdraw, "dset layout ds=%d\n", ds->ds_id);
    ud = ds->ds_unit_dist;
    // count number of nodes in dset
    for(num=0,n=ds->ds_node; n; n=n->n_next)
        num++;
    // initialize layout
    ds->ds_num_n = num;
    c = (int)sqrt((double)num);
    ds->ds_nc = (c*c < num) ? c+1 : c;
    ds->ds_num_n2d = c * ds->ds_nc;
    if(ds->ds_num_n2d < num)
        ds->ds_num_n2d = ds->ds_nc * ds->ds_nc;
    // add empty nodes to dset to fill 2d array (if needed)
    kk = ds->ds_num_n2d;
    for(k=ds->ds_num_n; k<kk; k++) {
        n = node_alloc(*n_id, 1, words);
        (*n_id)++;
        dset_add_node(ds, n);
    }
    // do initial layout on regular grid
    r = c = 0;
    for(n=ds->ds_node; n; n=n->n_next) {
        n->n_xc = 2*SIZE+c*ud;
        n->n_yc = 2*SIZE+r*ud;
        // calc next position in visual 2D array
        if(++c >= ds->ds_nc) {
            c = 0;
            r++;
        }
    }
    dset_draw(ds, 1, "layout");
    // swap two positions to minimize total length
    swap2:
    any = 0;
    for(n1=ds->ds_node; n1; n1=n1->n_next) {
        for(n2=n1->n_next; n2; n2=n2->n_next) {
            d1 = d2 = 0;
            // calculate length for: swapped (ba) and unswapped (ab)
            for(nn=ds->ds_node; nn; nn=nn->n_next) {
                for(a=nn->n_arc; a; a=a->a_next) {
                    nq = a->a_node;
                    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW2)
                        fprintf(fp_gdraw, "@@ n1=%d n2=%d nn=%d nq=%d\n",
                            n1->n_id, n2->n_id, nn->n_id, nq->n_id);
                    // add city block distance of connected nodes for: ab
                    dx = iabs(nn->n_xc - nq->n_xc);
                    dy = iabs(nn->n_yc - nq->n_yc);
                    add_d(ud, &d1, dx, dy);
                    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW2)
                        fprintf(fp_gdraw, "@@   dx=%d dy=%d d1=%d\n", dx, dy, d1);
                    // add city block distance of connected nodes for: ba
                    np = nn;
                    if(np == n1)
                        np = n2;
                    else if(np == n2)
                        np = n1;
                    if(nq == n2)
                        nq = n1;
                    else if(nq == n1)
                        nq = n2;
                    dx = iabs(np->n_xc - nq->n_xc);
                    dy = iabs(np->n_yc - nq->n_yc);
                    add_d(ud, &d2, dx, dy);
                    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW2)
                         fprintf(fp_gdraw, "@@   dx=%d dy=%d d2=%d np=%d nq=%d\n",
                            dx, dy, d2, np->n_id, nq->n_id);
                }
            }
            if(d2 < d1) { // swap position of n1 and n2
                any = 1;
                swap2int(&n1->n_xc, &n2->n_xc);
                swap2int(&n1->n_yc, &n2->n_yc);
 	            if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
                    fprintf(fp_gdraw, "@swap2 n1=%d(d1=%d) n2=%d(d2=%d)\n",
                        n1->n_id, d1, n2->n_id, d2);
                dset_draw(ds, 1, "swap2");
            }
        }
    }
    if(any) {
        goto swap2;
    }
    /* ----------------------------------------------------------
     * Swap three positions to minimize total length,
     * there are 6 permuations (only 3 matter):
     *    abc - d1
     *    acb - ignore (equiv to swap b-c)
     *    bac - ignore (equiv to swap a-b)
     *    bca - d2 (rotate left)
     *    cab - d3 (rotate right)
     *    cba - ignore (equiv to swap a-c)
     * choose min(d1,d2,d3): if d1 is min it means no change,
     * id d2 is min then rotate left, if d3 is min rotate right.
     * ----------------------------------------------------------
     * For permuations of four positions there are 8 cases,
     * rather than two, so that is not currently supported
     * since swap two and swap three get us to optimal for
     * well over 90% of the time (at least for small cases).
     * ----------------------------------------------------------
     */
    //swap3:
    any = 0;
    for(n1=ds->ds_node; n1; n1=n1->n_next) {
        for(n2=n1->n_next; n2; n2=n2->n_next) {
            for(n3=n2->n_next; n3; n3=n3->n_next) {
                d1 = d2 = d3 = 0;
                for(nn=ds->ds_node; nn; nn=nn->n_next) {
                    for(a=nn->n_arc; a; a=a->a_next) {
                        nq = nm = a->a_node;
                        // calc city block distance d1 of connected nodes for: abc
                        dx = iabs(nn->n_xc - nq->n_xc);
                        dy = iabs(nn->n_yc - nq->n_yc);
                        add_d(ud, &d1, dx, dy);
                        // calc city block distance d2 of connected nodes for: bca
                        np = nn;
                        if(np == n1)
                            np = n3;
                        else if(np == n2)
                            np = n1;
                        else if(np == n3)
                            np = n2;
                        nq = nm;
                        if(nq == n1)
                            nq = n3;
                        else if(nq == n2)
                            nq = n1;
                        else if(nq == n3)
                            nq = n2;
                        dx = iabs(np->n_xc - nq->n_xc);
                        dy = iabs(np->n_yc - nq->n_yc);
                        add_d(ud, &d2, dx, dy);
                        // calc city block distance d3 of connected nodes for: cab
                        np = nn;
                        if(np == n1)
                            np = n2;
                        else if(np == n2)
                            np = n3;
                        else if(np == n3)
                            np = n1;
                        nq = nm;
                        if(nq == n1)
                            nq = n2;
                        else if(nq == n2)
                            nq = n3;
                        else if(nq == n3)
                            nq = n1;
                        dx = iabs(np->n_xc - nq->n_xc);
                        dy = iabs(np->n_yc - nq->n_yc);
                        add_d(ud, &d3, dx, dy);
                    }
                }
                // find min(d1,d2,d3) and swap if d1 is not min
                if(d2 < d1) {
                    if(d3 < d2) { // d3 is min
                        d3:
                        swap3int(&n3->n_xc, &n1->n_xc, &n2->n_xc);
                        swap3int(&n3->n_yc, &n1->n_yc, &n2->n_yc);
 	                    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
                            fprintf(fp_gdraw, "@swap3 n3=%d(d3=%d) n1=%d(d1=%d) n2=%d(d2=%d)\n",
                                n3->n_id, d3, n1->n_id, d1, n2->n_id, d2); 
                    } else { // d2 is min
                        swap3int(&n2->n_xc, &n3->n_xc, &n1->n_xc);
                        swap3int(&n2->n_yc, &n3->n_yc, &n1->n_yc);
 	                    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
                            fprintf(fp_gdraw, "@swap3 n2=%d(d2=%d) n3=%d(d3=%d) n1=%d(d1=%d)\n",
                                n2->n_id, d2, n3->n_id, d3, n1->n_id, d1); 
                    }
                    dset_draw(ds, 1, "swap3");
                    any = 1;
                } else if(d3 < d1) {
                    goto d3;
                }
            }
        }
    }
    if(any) {
        goto swap2;
    }
    // eliminate empty nodes (no longer needed)
    for(np=0,n=ds->ds_node; n; n=nn) {
        nn = n->n_next;
        if(n->n_empty) {
            if(!np) {
                ds->ds_node = nn;
            } else {
                np->n_next = nn;
            }
            node_free(n);
        } else
            np = n;
    }
    // use force to arrange nodes (if more than 1 node)
    if(ds->ds_node && ds->ds_node->n_next) {
        // copy int position to double
        for(n=ds->ds_node; n; n=n->n_next) {
            n->n_xdc = n->n_xc;
            n->n_ydc = n->n_yc;
        }
        // apply forces
        for(k=0; k<70; k++) {
            dset_arc_pull(ds);
            dset_node_repel(ds);
            dset_draw(ds, -1, "arc_force");
            if(!dset_move(ds))
                break;
        }
        if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW)
            fprintf(fp_gdraw, "@ k=%d ds=%d\n", k, ds->ds_id);
    }
    vr_dbg_exit(VR_DBG_NODE, "@- {%u}\n", fid); // VR_DBG
} // dset_layout
// ========================================================================

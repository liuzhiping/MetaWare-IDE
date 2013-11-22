/* --- gdraw.c --- 2007 Dec 17, by Prem Sobel
 *
 * This program generates a random system of nodes and its layout.
 */

#include <stdio.h>
#include "g_node.h"
#include "g_arc.h"
#include "g_dset.h"
#include "g_sys.h"
#include "vr_dbg.h"
// ========================================================================
int main(int argc, char **argv) {
sys_t *s;
    vr_dbg_init(0);
    if(vr_dbg_fa[VR_DBG_SYS]&VR_DBG_SYS_SHOW) {
        fp_gdraw = fopen("gdraw.log", "w");
        if(!fp_gdraw)
            fp_gdraw = stdout;
    } else {
    	fp_gdraw = stdout;
    }
    // generate a system of random graph nodes and arcs
    s = sys_alloc(NUM_NODES);
    sys_gen_rand(s);
    // layout
    sys_layout(s);
    // cleanup
    sys_free(s);
    vr_dbg_done();
    if(fp_gdraw != stdout)
        fclose(fp_gdraw);
    return(0);
} // main
// ========================================================================

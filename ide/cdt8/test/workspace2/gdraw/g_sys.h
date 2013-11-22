/* --- g_sys.h --- 2008 Jan 16, by Prem Sobel
 *
 * Header for module: g_sys.c
 */

// 9, 7, 3
#define NUM_NODES  18
#define NUM_ARCS   13
#define ARCS_MAX   4

extern FILE *fp_sys;
// ========================================================================
typedef struct sys_s {
    struct dset_s *s_dset; // linked list of disjoint sets in system
    int            s_total_nodes;
} sys_t;
// ------------------------------------------------------------------------
sys_t *sys_alloc(int total_nodes);
void   sys_free(sys_t *s);
void   sys_draw(sys_t *s, int unit, char *msg);
void   sys_gen_rand(sys_t *s);
void   sys_n2dset(sys_t *s, struct node_s *n_sys);
void   sys_layout(sys_t *s);
// ========================================================================

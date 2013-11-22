/* --- g_dset.h --- 2008 Jan 15, by Prem Sobel
 *
 * Header for module: g_dset.c
 */

// Parameters for random size
#define SIZE_MIN   4
#define SIZE_STEP  10
#define SIZE       (SIZE_MIN+SIZE_STEP)

extern char *sep;
// ========================================================================
typedef struct dset_s {
    int       ds_id; // unique id number for dset
    int       ds_nc; // num cols in 2d layout of disjoint graph
    int       ds_num_n; // number of nodes in disjoint set
    int       ds_num_n2d; // calculated based on ds_num_n; to make 2D array
                          // extra nodes have: g_empty=1
    int       ds_unit_dist; // calculated from max g_wr and g_hr
    int       ds_x, ds_y; // origin of dset
    struct node_s *ds_node; // linked list of nodes in disjoint set
    struct dset_s *ds_next; // next dset in list
} dset_t;
// ------------------------------------------------------------------------
dset_t *dset_alloc(int id);
void    dset_free(dset_t *ds);
void    dset_draw(dset_t *ds, int unit, char *msg);
void    dset_add_node(dset_t *ds, struct node_s *n);
int     dset_scale_pos(dset_t *ds);
void    dset_layout(dset_t *ds, int *n_id, int words);
// ========================================================================

/* --- g_node.h --- 2007 Nov 26, by Prem Sobel
 *
 * header for module: g_node.c
 */

#define BITS_PER_UNSIGNED (8*sizeof(unsigned))
#define WORDS(num_nodes)  (1+(num_nodes-1)/BITS_PER_UNSIGNED)
// ========================================================================
typedef struct node_s {
    int n_id; // unique node number in graph
    int n_empty; // 0 if actual node, 1 if empty place holder
    int n_wr, n_hr; // "radius" of rectangle
    int n_xc, n_yc; // center of rectangle (relative to dset origin)
    double n_xdc, n_ydc; // center of rectangle (relative to dset origin)
    double n_fx, n_fy; // force
    int       n_words_in_node_set;
    unsigned *n_node_set; // allocated array
    int n_num_arcs; // number of arcs in following list
    struct arc_s  *n_arc; // linked list of arcs
    struct node_s *n_next; // linked list of nodes
} node_t;
// ------------------------------------------------------------------------
node_t *node_alloc(int id, int empty, int words);
void    node_free(node_t *n);
void    node_draw(node_t *n, int unit, int ud);
void    node_draw_d(node_t *n, int x, int y);
int     node_move(node_t *n);
node_t *node_find(node_t *n, int id);
void    node_add_arc(node_t *n1, node_t *n2);
int     node_in_set(unsigned *set, node_t *n);
void    node_fill_set(unsigned *set, node_t *n);
int     node_set_overlap(int nw, unsigned *s1, unsigned *s2);
// ========================================================================

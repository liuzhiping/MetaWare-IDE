/* --- g_arc.h --- 2007 Nov 22, by Prem Sobel
 *
 * Header file for: g_arc.c
 */

// ========================================================================
typedef struct arc_s {
    struct node_s *a_node; // ptr to node (ignore direction)
    struct arc_s  *a_next; // linked list of nodes
} arc_t;
// ------------------------------------------------------------------------
arc_t *arc_alloc(void);
void   arc_free(arc_t *a);
void   arc_draw(arc_t *a);
// ========================================================================

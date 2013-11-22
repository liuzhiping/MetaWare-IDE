/* $Header:$
 * Purpose: header for vr_dbg.c
 */

#ifndef _VR_DBG_H_
#define _VR_DBG_H_

extern FILE *fp_gdraw;
// ---------------------------------------------------------------------------
// === Define configuration: exactly one of these must be true ===
#define LINUX 0
#define WIN32 0
// following is also used for CGYWIN for now
#define LIB   1
// ---------------------------------------------------------------------------
// Must be in same order and value as vr_dbg_fa_name[ ] in vr_dbg.c,
// all but VR_DBG_ZZZ (which must be last and highest index)
// are project specific and need to be edited.
#define VR_DBG_NODE 0
#define VR_DBG_DSET (VR_DBG_NODE+1)
#define VR_DBG_SYS  (VR_DBG_DSET+1)
#define    VR_DBG_SYS_SHOW  (1<<3)
#define    VR_DBG_SYS_SHOW2 (1<<4)
#define VR_DBG_ZZZ  (VR_DBG_SYS+1)

// The above last array index is reserved for a developer to use
// in a sandbox and is subject to change. It is not guaranteed to
// be available in production code.
//
// The following is one more than the last debug index,
// it is the array size:
#define VR_DBG_NUM_FA  (VR_DBG_ZZZ +1)
// ---------------------------------------------------------------------------
// Category bit definitions, assumes sizeof(unsigned int)>=4:

#define VR_DBG_L1     (1<<0)
#define VR_DBG_L2     (1<<1)
#define VR_DBG_L3     (1<<2)
#define VR_DBG_EXIT   (1<<30)
#define VR_DBG_CALL   (1<<31)

// Level as 3 bit number mask
#define VR_DBG_L_MASK (VR_DBG_L3|VR_DBG_L2|VR_DBG_L1)
// ---------------------------------------------------------------------------
extern unsigned int *vr_dbg_fid; // function id;

extern int          *vr_dbg_mode; // one of following
#define VR_DBGM_DEBUG   0
#define VR_DBGM_INFO    1
#define VR_DBGM_WARN    2
#define VR_DBGM_ERROR   3

#define NUM_VR_DBGM     4
extern char         *vr_dbg_mode_str[NUM_VR_DBGM];

extern FILE        **vr_dbg_fp;

extern unsigned int *vr_dbg_fa; // vr_dbg_fa[VR_DBG_NUM_FA]
extern char         *vr_dbg_fa_name[VR_DBG_NUM_FA];

#define VR_DBG_FLINE_SIZE 1000
extern char vr_dbg_fline[VR_DBG_FLINE_SIZE];
// ---------------------------------------------------------------------------
void vr_dbg_init(char *vr_dbg_log_fn);
void vr_dbg_done(void);

void vr_dbg_cli(void);

void vr_dbg_zero(void);
void vr_dbg_signature(void *p); // customize as needed in vr_dbg.c
// ---------------------------------------------------------------------------
// Macro API
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
#if LINUX
#define vr_dbg_log(mode, format, args...) \
    if(*vr_dbg_mode <= mode) { \
        snprintf(vr_dbg_fline, VR_DBG_FLINE_SIZE, "(%s: file=%s line=%d) %s", \
            vr_dbg_mode_str[mode], __FILE__, __LINE__, format); \
        fprintf(*vr_dbg_fp, vr_dbg_fline, ##args); \
    }
#else
#define vr_dbg_log(mode, format, args...) \
    if(*vr_dbg_mode <= mode) { \
        sprintf(vr_dbg_fline, "(%s: file=%s line=%d) %s", \
            vr_dbg_mode_str[mode], __FILE__, __LINE__, format); \
        fprintf(*vr_dbg_fp, vr_dbg_fline, ##args); \
    }
#endif
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
#define vr_dbg_level(fa, dlevel, format, args...) \
    if(vr_dbg_fa && ((vr_dbg_fa[fa]&VR_DBG_L_MASK) >= dlevel)) { \
        vr_dbg_log(VR_DBGM_DEBUG, format, ##args) \
    }

#define vr_dbg_and(fa, mask, format, args...) \
    if(vr_dbg_fa && (vr_dbg_fa[fa] & mask)) { \
        vr_dbg_log(VR_DBGM_DEBUG, format, ##args); \
    }
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
#define vr_dbg_call(fa, format, args...) \
    { vr_dbg_and(fa, VR_DBG_CALL, format, ##args); }

#define vr_dbg_exit(fa, format, args...) \
    { vr_dbg_and(fa, VR_DBG_EXIT, format, ##args); }
// ---------------------------------------------------------------------------
#endif // _VR_DBG_H_

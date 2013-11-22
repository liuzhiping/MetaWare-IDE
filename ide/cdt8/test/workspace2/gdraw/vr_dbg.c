/* $HeaderURL:$
 * $Id:$
 * Purpose: Provide functions and state for system debug.
 */

#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#if LINUX
#include <sys/shm.h>
#include <sys/ipc.h>
#else
#define strcasecmp strcmp
#endif
#include "vr_dbg.h"

static char *_id__="$Id:$";
FILE *fp_gdraw;
// ---------------------------------------------------------------------------
#define VR_DBG_LOG_FN "vr_dbg.log"
#define CONFIG_FN  "vr_dbg.conf"
#define CFG_LINE_SIZE 200
// ---------------------------------------------------------------------------
unsigned int *vr_dbg_fid; // global function id sequence number;
         int *vr_dbg_mode;
FILE        **vr_dbg_fp;
unsigned int *vr_dbg_fa; // array of values
char          vr_dbg_fline[VR_DBG_FLINE_SIZE]; // scratch buffer for vr_dbg_log()
int           vr_dbg_init_depth;
// ---------------------------------------------------------------------------
char *vr_dbg_mode_str[NUM_VR_DBGM] = {
    "DEBUG", "INFO", "WARN", "ERROR",
};
// ---------------------------------------------------------------------------
char *vr_dbg_fa_name[VR_DBG_NUM_FA] = {
    // array of func area names
    "NODE",
    "DSET",
    "SYS",
    "ZZZ"
};
// ---------------------------------------------------------------------------
#define DEFAULT_SHM_KEY 79497

static int vr_dbg_key = DEFAULT_SHM_KEY;

#if (LINUX && WIN32) || (LINUX && LIB) || (WIN32 && LIB)
@ // this will cause a compile error because invalid multiple configuration!
#elif LINUX
#elif WIN32
@ // this will cause a compile error because not yet supported!
#elif LIB
static unsigned int  vr_dbg_fid_;
static          int  vr_dbg_mode_;
static FILE         *vr_dbg_fp_;
#else
@ // this will cause a compile error because invalid no configuration!
#endif
// ---------------------------------------------------------------------------
// Skip spaces, tabs, CR, and NL in 'line' starting with *k.
// *k index of first non white space or NULL char in 'line'.

static void skip_white_space(char *line, unsigned int *k) {
    for(; line[*k]; (*k)++) {
        if((' ' != line[*k]) && ('\t' != line[*k]) &&
           ('\n' != line[*k]) && ('\r' != line[*k]))
            break;
    }
} // skip_white_space
// ---------------------------------------------------------------------------
// following must be at least 2 larger than longest possible token length
#define TOK_SIZE 40

char *vr_dbg_get_token(char *line, unsigned int *k) {
int kk;
char static tok[TOK_SIZE];
    skip_white_space(line, k);
    tok[kk=0] = 0;
    for(; line[*k]; (*k)++) {
        if((' ' != line[*k])  && ('\t' != line[*k]) &&
           ('\n' != line[*k]) && ('\r' != line[*k])) {
            tok[kk++] = line[*k];
            if(kk >= TOK_SIZE) { // token longer than expected length!
                kk = TOK_SIZE-1;
                break;
            }
        } else
            break;
    }
    tok[kk] = 0;
    return tok;
} // vr_dbg_get_token
// ---------------------------------------------------------------------------
static void vr_dbg_log_open(char *fn) {
    *vr_dbg_fp = fopen(fn ? fn : VR_DBG_LOG_FN, "w");
    if(!*vr_dbg_fp) {
         printf("@@@ error, unable to open file %s for write (using stdout)"
                " because %s\n",
             fn ? fn : VR_DBG_LOG_FN, strerror(errno));
         *vr_dbg_fp = stdout;
    }
} // vr_dbg_log_open
// ---------------------------------------------------------------------------
#if LIB
static void vr_dbg_catch_ctrl_c(int i) { int c;
    again:
    printf("Caught CTRL-C, i.e. SIGINT;\n"
           "do you want to modify debug settings (type: 'D' or 'd')\n"
           "or do you want exit (type: 'X' or 'x')\n");
    c = getchar();
    if(('x' == c) || ('X' == c)) {
        vr_dbg_done();
        exit(1);
    } else if(('d' == c) || ('D' == c)) {
        vr_dbg_cli();
        //printf("@@@ catch ctrl_c: exited ctrl_c vr_dbg_cli()\n");
    } else {
        printf("\nI don't understand!\n");
        goto again;
    }
} // vr_dbg_catch_ctrl_c
#endif
// ---------------------------------------------------------------------------
static void vr_dbg_use_cnfg_file_values(void) {
char cfg_line[CFG_LINE_SIZE], *tok;
unsigned int k, kk, level, any;
unsigned int hex;
FILE *fp_cfg;
    // If config file exists: open, read and parse each line.
    // Only change values for which there is an entry.
    fp_cfg = fopen(CONFIG_FN, "r");
    if(fp_cfg) {
        while(fgets(cfg_line, CFG_LINE_SIZE, fp_cfg)) {
            k = 0;
            tok = vr_dbg_get_token(cfg_line, &k);
            if(!tok[0])
                continue; // ignore blank lines
            if('#' == tok[0])
                continue; // ignore comments
            // Is it a functional area name to be initialized?
            any = 0;
            for(kk=0; kk<VR_DBG_NUM_FA; kk++) {
                if(!strcmp(vr_dbg_fa_name[kk], tok)) { // yes
                    any = 1;
                    skip_white_space(cfg_line, &k);
                    if(!cfg_line[k])
                        goto saw_eol;
                    // Is there a value?
                    if(('0' == cfg_line[k]) &&
                       (('x' == cfg_line[k+1]) ||
                        ('X' == cfg_line[k+1]))) { // yes hex
                        for(k+=2,hex=0; cfg_line[k]; k++) { // hex
                            if(('0' <= cfg_line[k]) &&
                               ('9' >= cfg_line[k])) {
                                hex = hex*16 + cfg_line[k]-'0';
                                any = 2;
                            } else if(('a' <= cfg_line[k]) &&
                                      ('f' >= cfg_line[k])) {
                                hex = hex*16 + cfg_line[k]+10-'a';
                                any = 2;
                            } else if(('A' <= cfg_line[k]) &&
                                      ('F' >= cfg_line[k])) {
                                hex = hex*16 + cfg_line[k]+10-'A';
                                any = 2;
                            } else
                                break;
                        }
                        if(2 == any) {
                            vr_dbg_fa[kk] = hex;
                        }
                    } else { // decimal (range: 0..7), default is 0
                        for(level=0; cfg_line[k]; k++) {
                            if(('0' <= cfg_line[k]) &&
                               ('9' >= cfg_line[k])) {
                                level = level*10 + cfg_line[k]-'0'; // yes dec
                                any = 2;
                            } else
                                break;
                        }
                        if(2 == any) {
                            vr_dbg_fa[kk] &= ~7;
                            vr_dbg_fa[kk] |= (level>7) ? 7 : level;
                        }
                    }
                    if(1 == any) {
                        saw_eol:
                        vr_dbg_log(VR_DBGM_INFO,
                            "vr_dbg_fa[%d:%s] (unchanged no value)\n", kk, tok);
                    } else if(2 == any) {
                        vr_dbg_log(VR_DBGM_INFO, "vr_dbg_fa[%d:%s] = 0x%08X\n",
                            kk, tok, vr_dbg_fa[kk]);
                    }
                    break;
                }
           }
       }
       fclose(fp_cfg);
    } else {
    	vr_dbg_log(VR_DBGM_WARN, "Config file: %s does not exist\n", CONFIG_FN);
    }
} // vr_dbg_use_cnfg_file_values
// ---------------------------------------------------------------------------
// Create and initialize shared memory area for debug parameters,
// if it does not yet exist. Attach to the shared memory segment.
// Exits on error.
//
// This function must be called in main() at the beginning for 
// every process using the debug infrastructure.
/*
 * Possible CONFIG_FN contents (no order of the lines is imposed or required):
 *     SHMKEY key
 *     MODE mode
 *     fn1 v1
 *     fn2 v2
 *     ...
 *     ----------------------------------------------------------------
 *     Notes:
 *         Items shown above in lower case are parameters, e.g. key or v1
 *         (an int >0); other parameters are names (strings).  The case
 *         of strings (inluding SHMKEY or MODE) does not matter.
 *         The numbers v1, v2, ... the initial value for the debug word
 *         for the corresponding named functional area: fn1, fn2, ...
 *         when the shared memory is created.
 *
 *         These debug levels can be either decimal in the range 0 to 7,
 *         or hexadecimal in the range 0x0 to 0xFFFFFFFF, but
 *         if an initial value is not given for a funcarea debug level
 *         then the default value of 0 is used.
 *
 *         "mode" is one of: DEBUG | INFO | WARN | ERROR
 *         (again, this field is not case sensitive).
 */

void vr_dbg_init(char *vr_dbg_log_fn) {
char cfg_line[CFG_LINE_SIZE], *tok;
int size, kk, mode_init=VR_DBGM_WARN;
unsigned k, key;
#if LINUX
struct shmid_ds buf;
#endif
FILE *fp_cfg;
    if(!vr_dbg_init_depth)
        vr_dbg_init_depth = 1;
    // If config file exists: open, read look only for:
    //     SHMKEY ...
    //     MODE ...
    fp_cfg = fopen(CONFIG_FN, "r");
    if(fp_cfg) {
        while(fgets(cfg_line, CFG_LINE_SIZE, fp_cfg)) {
            k = 0;
            tok = vr_dbg_get_token(cfg_line, &k);
            if(!tok[0])
                continue; // ignore blank lines
            if('#' == tok[0])
                continue; // ignore comments
            if(!strcasecmp("SHMKEY", tok)) {
                skip_white_space(cfg_line, &k);
                for(key=0; cfg_line[k]; k++) {
                    if((cfg_line[k]<'0') || (cfg_line[k]>'9'))
                        break;
                    key = key*10 + cfg_line[k]-'0';
                }
                if(key > 0) {
                    vr_dbg_key = key;
                }
            } else if(!strcasecmp("MODE", tok)) {
                tok = vr_dbg_get_token(cfg_line, &k);
                for(kk=0; kk<NUM_VR_DBGM; kk++) {
                    if(!strcasecmp(vr_dbg_mode_str[kk], tok)) {
                        mode_init = kk;
                        goto mode_ok;
                    }
                }
                printf("@@ unknown mode %s (ignored)\n", tok);
                mode_ok: ;
            }
        }
        fclose(fp_cfg);
    }
    // Find shared memory id (create if non existing yet)
    size = 2*sizeof(unsigned int) + // for vr_dbg_fid, vr_dbg_mode
         + sizeof(FILE **) + sizeof(FILE *) +
        sizeof(unsigned int)*VR_DBG_NUM_FA; // for array vr_dbg_fa
#if LINUX
    // does debug shared memory segment exist?
    shmid = shmget( vr_dbg_key, size, 0666 );
    if(-1 == shmid) { // no, try and create it
        shmid = shmget( vr_dbg_key, size, 0666|IPC_CREAT );
        if(-1 == shmid) { // failed
            printf("@@@ shmget failed for key=%d: size=%d %s "
                   "(may need to reboot)\n",
                vr_dbg_key, size, strerror(errno));
            exit(1);
        }
    }
    // Find out status of shared memory (for attachment count)
    if(-1 == shmctl(shmid, IPC_STAT, &buf)) {
        printf("@@@ shmctl failed: %s\n", strerror(errno));
        exit(1);
    }
    // If we are first destroy it and recreate it in case size changed.
    if(0 == buf.shm_nattch) {
        if(-1 == shmctl(shmid, IPC_RMID, &buf)) {
            printf("@@@ unable to remove vr_dbg shared memory: %s\n",
                strerror(errno));
        }
        shmid = shmget(vr_dbg_key, size, 0666|IPC_CREAT);
        if(-1 == shmid) { // failed
            printf("@@@ shmget failed for key=%d: size=%d %s "
                "(tried to recreate)\n",
                vr_dbg_key, size, strerror(errno));
            exit(1);
        }
    }
    // Attach to that shared memory, and assign ptrs
    vr_dbg_fid = (unsigned int *) shmat(shmid, 0, 0);
    if(-1 == (int)vr_dbg_fid) {
        printf("@@@ shmat failed: %s\n", strerror(errno));
        exit(1);
    }
    // Find out status of shared memory (for attachment count)
    if(-1 == shmctl(shmid, IPC_STAT, &buf)) {
        printf("@@@ shmctl failed: %s\n", strerror(errno));
        exit(1);
    }
    vr_dbg_mode = vr_dbg_fid + sizeof(unsigned int);
    vr_dbg_fp = (FILE **)(vr_dbg_mode + sizeof(unsigned int));
    *vr_dbg_fp = (FILE *)(vr_dbg_fp + sizeof(FILE **));
    vr_dbg_fa = (unsigned int *)(vr_dbg_fp + sizeof(FILE *) + sizeof(FILE **));
    // If first attached see if config file has initialization.
    if(1 == buf.shm_nattch) {
        *vr_dbg_fid = 1;
        vr_dbg_zero();
        *vr_dbg_mode = mode_init;
        vr_dbg_log_open(vr_dbg_log_fn);
        if(DEFAULT_SHM_KEY != vr_dbg_key)
            vr_dbg_log(VR_DBGM_WARN, "Changed vr_dbg_key to %d\n", vr_dbg_key);   
        if(VR_DBGM_WARN != mode_init)
            vr_dbg_log(VR_DBGM_WARN, "Changed mode to %s\n",
                vr_dbg_mode_str[mode_init]);   
        vr_dbg_use_cnfg_file_values();
    }
#elif WIN32
    // TBD - FIXME
    { printf("@@@ vr_dbg_init() not yet implemented fo WIN32\n"); exit(1); }
#elif LIB
    vr_dbg_fid = &vr_dbg_fid_;
    vr_dbg_mode = &vr_dbg_mode_;
    *vr_dbg_mode = mode_init;
    vr_dbg_fp = &vr_dbg_fp_;
    if(!*vr_dbg_fp)
        vr_dbg_log_open(vr_dbg_log_fn);
    vr_dbg_fa = malloc(sizeof(unsigned int)*VR_DBG_NUM_FA);
    if(!vr_dbg_fa) {
        vr_dbg_log(VR_DBGM_ERROR, "malloc failed in vr_dbg_init()", 1);
        exit(1);
    }
    vr_dbg_zero();
    if(VR_DBGM_WARN != mode_init)
        vr_dbg_log(VR_DBGM_WARN, "Changed mode to %s\n", vr_dbg_mode_str[mode_init]);   
    vr_dbg_use_cnfg_file_values();
    if(DEFAULT_SHM_KEY != vr_dbg_key)
        vr_dbg_log(VR_DBGM_WARN, "Changed vr_dbg_key to %d\n", vr_dbg_key);   
    signal(SIGINT, vr_dbg_catch_ctrl_c);
#endif
} // vr_dbg_init
// ---------------------------------------------------------------------------
void vr_dbg_done(void) {
    fflush(*vr_dbg_fp);
    #if LINUX
    shmdt(vr_dbg_fid); // Detach from shared memory.
    #endif
} // vr_dbg_done
// ---------------------------------------------------------------------------
// Zero the debug array.

void vr_dbg_zero(void) {
int n;
    for(n=0; n<VR_DBG_NUM_FA; n++)
        vr_dbg_fa[n] = 0;
} // vr_dbg_zero
// ---------------------------------------------------------------------------
// Example debug signature analysis function.
// Calls to this function must exist somewhere in code.
// Usage in this example: put call to this function in ZZZ code.
// If need be the prototype can be modified in: vr_dbg.h
// to have more arguments.
//
// Edit this function as needed to match a failure signature.

void vr_dbg_signature(void *p) {
    if(vr_dbg_fa && (*vr_dbg_mode==VR_DBGM_DEBUG) &&
       (vr_dbg_fa[VR_DBG_ZZZ] & (1<<4))) {
        // this signature test is enabled
        if(95 == (int)p) {
            vr_dbg_fa[VR_DBG_ZZZ] |= 1;
        } else if(99 == (int)p) {
            vr_dbg_fa[VR_DBG_ZZZ] &= ~1;
        }
    }
} // vr_dbg_signature
// ---------------------------------------------------------------------------
/* Example usage, added debug lines end with: // VR_DBG

#include "vr_dbg.h"

int func1(int p1, ..., int indent) {
int err=0;
int status;
int p2;
    unsigned int fid=++*vr_dbg_fid; // VR_DBG
    vr_dbg_call(VR_DBG_ZZZ, "@+ {%u} p1=%d\n", fid=++*vr_dbg_fid, p1); // VR_DBG
    ...
    status = ... ;
    dbf_level(VR_DBG_ZZZ, 3, "@@ {%u} status=%d\n", fid, status); // VR_DBG
    ...
    p2 = ... ;
    if(1 == p2)
        err = 1;
    else if(p2>2)
        err |= func1(p2-1, ... ,indent+1); // is a recursive call
    ...
    vr_dbg_and(VR_DBG_ZZZ, (1<<4), "@@ {%u} p2=%d", fid, p2); // VR_DBG
    ...
    vr_dbg_signature(p2); // VR_DBG
    ...
    vr_dbg_exit(VR_DBG_ZZZ, "@- {%u} err=%d \n", fid, err); // VR_DBG
    return err;
} // func1

*/
// ---------------------------------------------------------------------------

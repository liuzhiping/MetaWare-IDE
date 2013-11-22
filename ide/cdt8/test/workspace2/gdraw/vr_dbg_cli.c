/* $HeaderURL:$
 * $Id:$
 * Purpose: Provide functions and state for system debug cli or gui.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "vr_dbg.h"
#if LINUX
#include <sys/shm.h>
#include <ncurses.h>
#endif
//#include <signal.h>
//#include <errno.h>
//#include <sys/types.h>
//#include <sys/ipc.h>

static char *_id__="$Id:$";
static unsigned int vr_dbg_select;
// ---------------------------------------------------------------------------
static int getchar_(void) { int c;
    do {
        c = getchar();
    } while(('\r' == c) || ('\n' == c));
    return c;
} // getchar_
// ---------------------------------------------------------------------------
static unsigned vr_dbg_hex(void) {
unsigned n=0;
int c;
    while(1) {
        #if LINUX
        c = getch();
        #elif WIN32
        c = getchar();
        #elif LIB
        c = getchar();
        #endif
        if((c>='0')&&(c<='9')) {
            n = n*16 + c-'0';
        } else if((c>='a')&&(c<='f')) {
            n = n*16 + c+10-'a';
        } else if((c>='A')&&(c<='F')) {
            n = n*16 + c+10-'A';
        } else
            return n;
    }
} // vr_dbg_hex
// ---------------------------------------------------------------------------
// Fuction to parse and return number between 0 and max.
// If number exceeds max it is set to max.

static unsigned vr_dbg_unsigned(unsigned max) {
unsigned n=0;
int c;
    while(1) {
        #if LINUX
        c = getch();
        #elif WIN32
        c = getchar();
        #elif LIB
        c = getchar();
        #endif
        if((c>='0') && (c<='9')) {
            n = n*10 + c-'0';
            if(n >= max)
                return max;
        } else
            return n;
    }
} // vr_dbg_unsigned
// ---------------------------------------------------------------------------
#if LIB
static void vr_dbg_clear(void) { int k;
    for(k=0; k<8; k++) // 8 => 40
        printf("\n");
}

static char vr_dbg_fa_selected(int n) {
    return (n==vr_dbg_select) ? '@' : ' ';
} // vr_dbg_fa_selected
#endif
// ---------------------------------------------------------------------------
// Parse CLI input for debug shared memory variables.
// Returns selected index if continuing to debug,
// else returns -1 if quiting.

static int vr_dbg_parse(void) {
int c;
unsigned k, r=0;
    #if LINUX
    c = getch();
    #elif WIN32
    c = getchar_();
    #elif LIB
    c = getchar_();
    #endif
    switch(c) {
        case '.':
            #if LINUX
            c = getch();
            #elif WIN32
            c = getchar_();
            #elif LIB
            c = getchar_();
            #endif
            switch(c) {
                case 'd':
                case 'D':
                    *vr_dbg_mode = VR_DBGM_DEBUG;
                    break;
                case 'i':
                case 'I':
                    *vr_dbg_mode = VR_DBGM_INFO; 
                    break;
                case 'w':
                case 'W':
                    *vr_dbg_mode = VR_DBGM_WARN;
                    break;
                case 'e':
                case 'E':
                    *vr_dbg_mode = VR_DBGM_ERROR;
                    break;
            }
            break;
        case 'H':
        case 'h': // show help
            #if LINUX
            clear();
            move(++r, 1);
            printw("q) quit");
            move(++r, 1);
            printw("z) zero all");
            move(++r, 1);
            printw("t) set all,");
            move(++r, 1);
            printw("L0..7) Change all levels to 0..7");
            move(r+=2, 1);
            printw("i0..%d_) select by index, selected is ", VR_DBG_NUM_FA-1);
            attron(A_REVERSE);
            printw("video reversed");
            attroff(A_REVERSE);
            move(r+=2, 1);
            printw("+) increment level of selected");
            move(++r, 1);
            printw("-) decrement level of selected");
            move(++r, 1);
            printw("0..7) Change level of selected to 0..7");
            move(r+=2, 1);
            printw("s0..31_) set bit 0..31 in selected "
                   "(terminated by non dec char)");
            move(++r, 1);
            printw("r0..31_) reset bit 0..31 in selected "
                   "(terminated by non dec char)");
            move(++r, 1);
            printw("xHHHHH_) assign selected hex value: "
                   "HHHHH terminated by non hex char");
            move(++r, 1);
            printw("dDDDDD_) assign selected decimal value: "
                   "DDDDD terminated by non dec char");
            move(r+=2, 1);
            printw(".D) set MTASK_LOG_LEVEL_DEBUG");
            move(++r, 1);
            printw(".I) set MTASK_LOG_LEVEL_INFO");
            move(++r, 1);
            printw(".N) set MTASK_LOG_LEVEL_NOTICE");
            move(++r, 1);
            printw(".E) set MTASK_LOG_LEVEL_ERROR");
            move(r+=2, 1);
            printw("c) toggle call bit in selected");
            move(++r, 1);
            printw("e) toggle exit bit in selected");
            move(r+=2, 1);
            printw("Note:");
            move(++r, 1);
            printw("   All letters are case insensitive.");
            move(++r, 1);
            printw("   _  Means any non digit character or key.");
            move(r+=2, 1);
            printw("Type any key to continue...");
            getch();
            #elif WIN32
            #elif LIB
            vr_dbg_clear();
            printf("q) quit\n"
                   "z) zero all\n"  
                   "t) set all,\n"  
                   "L0..7) Change all levels to 0..7\n\n"
                   "i0..%d) select by index (selected is marked with: @)\n"
                   "+) increment level of selected\n"
                   "-) decrement level of selected\n"
                   "0..7) Change level of selected to 0..7\n"
                   "s0..31) set bit 0..31 in selected "
                   "(terminated by non dec char)\n"
                   "r0..31) reset bit 0..31 in selected"
                   "(terminated by non dec char)\n"
                   "xHHHHH) assign selected hex value: "
                   "HHHHH terminated by non hex char\n"
                   "dDDDDD) assign selected decimal value: "
                   "DDDDD terminated by non dec char\n\n"
                   ".D) set mode to: DEBUG\n"
                   ".I) set mode to: INFO\n"
                   ".W) set mode to: WARN\n"
                   ".E) set mode to: ERROR\n\n"
                   "c) toggle call bit in selected\n"
                   "e) toggle exit bit in selected\n\n"
                   "Note:\n"
                   "   All letters are case insensitive.\n"
                   //"   _  Means any non digit character or key.\n\n"
                   "\nAll input must end with the ENTER key\n"
                   "Type ENTER to continue...\n"
                   , VR_DBG_NUM_FA-1);
            getchar();
            getchar();
            #endif
            break;
        case 'I':
        case 'i': // index
            vr_dbg_select = vr_dbg_unsigned(VR_DBG_NUM_FA-1);
            break;
        case 'Z':
        case 'z': // clear vr_dbg_fa[]
            vr_dbg_zero();
            break;
        case 'T':
        case 't': // set vr_dbg_fa[]
            for(c=0; c<VR_DBG_NUM_FA; c++)
                vr_dbg_fa[c] = 0xFFFFFFFF;
            break;
        case 'S':
        case 's': // set entry bit
            vr_dbg_fa[vr_dbg_select] |= (1<<vr_dbg_unsigned(31));
        case 'R':
            break;
        case 'r': // reset entry bit
            vr_dbg_fa[vr_dbg_select] &= ~(1<<vr_dbg_unsigned(31));
            break;
        case 'D':
        case 'd':
            vr_dbg_fa[vr_dbg_select] = vr_dbg_unsigned(0xFFFFFFFF);
            break;
        case 'X':
        case 'x':
            vr_dbg_fa[vr_dbg_select] = vr_dbg_hex();
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
            vr_dbg_fa[vr_dbg_select] &= ~VR_DBG_L_MASK;
            vr_dbg_fa[vr_dbg_select] |= c-'0';
            break;
        case 'L':
        case 'l':
            c=getchar_();
            if(('0' <= c) && ('7' >= c)) {
                for(k=0; k<VR_DBG_NUM_FA; k++) {
                    vr_dbg_fa[k] &= ~VR_DBG_L_MASK;
                    vr_dbg_fa[k] |= c-'0';
                }
            }
            break;
        case '+':
            k = vr_dbg_fa[vr_dbg_select] & VR_DBG_L_MASK;
            if(k<7) {
                vr_dbg_fa[vr_dbg_select] &= ~VR_DBG_L_MASK;
                vr_dbg_fa[vr_dbg_select] |= ++k;
            }
            break;
        case '-':
            k = vr_dbg_fa[vr_dbg_select] & VR_DBG_L_MASK;
            if(k>0) {
                vr_dbg_fa[vr_dbg_select] &= ~VR_DBG_L_MASK;
                vr_dbg_fa[vr_dbg_select] |= --k;
            }
            break;
        case 'C':
        case 'c': // call toggle
            vr_dbg_fa[vr_dbg_select] ^= VR_DBG_CALL;
            break;
        case 'E':
        case 'e': // exit toggle
            vr_dbg_fa[vr_dbg_select] ^= VR_DBG_EXIT;
            break;
        case 'Q':
        case 'q':
        case EOF:
            return -1;
    }
    return vr_dbg_select;
} // vr_dbg_parse
// ---------------------------------------------------------------------------
// Show the array of debug control states using ncurses.

#define COL2 36

static void vr_dbg_show(void) {
unsigned n, n2; 
#if LINUX
//struct shmid_ds buf;
    clear();
    move(0, 1);
    printw("~~~~~~~~~~~~~~~~~~~~~~~~~ Debug Control ~~~~~~~~~~~~~~~~~~~~~~~~");
    clrtoeol();
    clrtobot();
    move(1, 1);
    printw(" i |hex value | name");
    move(1, COL2);
    printw(" i |hex value | name");
    move(2, 1);
    printw("---+----------+--------------");
    move(2, COL2);
    printw("---+----------+--------------");
    n2 = (VR_DBG_NUM_FA+1)/2;
    for(n=0; n<n2; n++) {
        move(3+n, 1);
        if(n == vr_dbg_select) {
             attron(A_REVERSE);
        }
        printw("%02d | %08X | %s",
            n, vr_dbg_fa[n], vr_dbg_fa_name[n]);
        if(n == vr_dbg_select) {
             attroff(A_REVERSE);
        }
        move(3+n, COL2);
        if(n+n2<VR_DBG_NUM_FA) {
            if(n+n2 == vr_dbg_select) {
                 attron(A_REVERSE);
            }
            printw("%02d | %08X | %s",
                n+n2, vr_dbg_fa[n+n2], vr_dbg_fa_name[n+n2]);
            if(n+n2 == vr_dbg_select) {
                 attroff(A_REVERSE);
            }
        } else {
            printw("   |          |",
                n+n2, vr_dbg_fa[n+n2], vr_dbg_fa_name[n+n2]);
        }
    }
    move(3+n2,    1); printw("---+----------+--------------");
    move(3+n2, COL2); printw("---+----------+--------------");
    #if 1
    move(4+n2,    1); printw("h)help,  q)quit   %s   ",
        vr_dbg_mode_str[*vr_dbg_mode]);
    #else
    shmctl(shmid, IPC_STAT, &buf);
    move(4+n2,    1); printw("h)help,  q)quit  nattch=%d    %s   ",
        buf.shm_nattch, vr_dbg_mode_str[*vr_dbg_mode]);
    #endif
    refresh();
#elif WIN32
#elif LIB
    vr_dbg_clear();
    printf("===+==========+========= Debug ===+==========+==============\n");
    printf(" i |hex value |  name           i |hex value |  name\n");
    printf("---+----------+--------------  ---+----------+--------------\n");
    n2 = (VR_DBG_NUM_FA+1)/2;
    for(n=0; n<n2; n++) {
        printf("%02d | %08X |%c %-12s",
            n, vr_dbg_fa[n], vr_dbg_fa_selected(n), vr_dbg_fa_name[n]);
        if(n+n2<VR_DBG_NUM_FA) {
            printf("  %02d | %08X |%c %s\n",
                n+n2, vr_dbg_fa[n+n2], vr_dbg_fa_selected(n+n2), vr_dbg_fa_name[n+n2]);
        } else {
            printf("     |          |\n");
        }
    }
    printf("===+==========+===================+==========+==============\n");
    printf("h)help,  q)quit   %s   \n", vr_dbg_mode_str[*vr_dbg_mode]);
#endif
} // vr_dbg_show
// ---------------------------------------------------------------------------
void vr_dbg_cli(void) {
    #if LINUX
    initscr();
    do vr_dbg_show();
    while(vr_dbg_parse() >= 0);
    endwin();
    #elif WIN32
    // TBD
    #elif LIB
    do vr_dbg_show();
    while(vr_dbg_parse() >= 0);
    #endif
} // vr_dbg_cli
// ---------------------------------------------------------------------------

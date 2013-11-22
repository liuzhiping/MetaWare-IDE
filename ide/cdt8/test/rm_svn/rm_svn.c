/* === rm_svn.c === 2008 May 12
 *
 * Program to recursively remove ".svn" or "CVS" directories
 */

#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <sys/param.h>
#include <sys/stat.h>

// If debug is non 0 execution will show what program does, but not do it.
static int debug=0;

static char Usage[] = "Usage:  rm_svn directory";

fail(char *msg) {
    fprintf(stderr,"*** %s\n", msg);
    exit(1);
} // fail

void indent(int i) {
    for(; i>0; i--)
        printf("  ");
} // indent

void rm_svn(char *dir, int sp) {
int i, offset;
char fn[MAXPATHLEN], rm_[MAXPATHLEN+8];
DIR *dir_ptr;
struct dirent *dp;
struct stat sbuf;
    if(debug) {
        indent(sp);
        printf("@ dir=%s\n", dir);
    }
    if(!(dir_ptr=opendir(dir)))
        fail("unable to open directory");
    for(dp=readdir(dir_ptr); dp!=NULL; dp=readdir(dir_ptr)) {
        if(debug>1) {
            indent(sp);
            printf("@@ d_name=%s\n", dp->d_name);
        }
        if(!strcmp(dp->d_name, ".") || !strcmp(dp->d_name, ".."))
            continue;
        /* --- build up full path name --- */
        strcpy(fn, dir);
        strcat(fn, "/");
        offset=((fn[1]=='/')&&(fn[0]='/'))?1:0;
        strcat(fn, dp->d_name);
#ifdef S_IFLNK
        if(lstat(fn, &sbuf)<0) {
#else
        if(stat(fn, &sbuf)<0) {
#endif
            printf("*** Cannot open %s\n", fn);
            continue;
        }
        /* --- is it a directory? --- */
        if(S_IFDIR == (sbuf.st_mode & S_IFMT)) { // yes
            if(!strcmp(dp->d_name, ".svn") ||
               !strcmp(dp->d_name, "CVS")) {
                sprintf(rm_, "rm -f -R %s", fn);
                if(debug) {
                    indent(sp);
                    printf("@@@ %s\n", rm_);
                } else {
                    system(rm_);
                }
            } else {
                rm_svn(fn, sp+1);
            }
        }
    }
    closedir(dir_ptr);
} // rm_svn

int main(int argc, char *argv[]) {
    if(2 != argc)
        fail(Usage);
    rm_svn(argv[1], 1);
    return 0;
} // main

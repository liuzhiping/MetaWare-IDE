/* === has_no_subdir.c === by: Prem Sobel, 2008 Jan 29
 *
 * Program to verify specified sub-directory is empty
 */

#include <stdio.h>
#include <dirent.h>
#include <sys/param.h>
#include <sys/types.h>
#include <sys/stat.h>

/************* command line switch definitions and variables ************
 *                                                                      *
 *  "has_no_subdir" requires a specified "directory"                    *
 *         normally returns: 0 if "directory" empty, 1 if not empty     * 
 *                                                                      *
 *  switches:                                                           *
 *     -t: reverse exit code: 0 if not empty, 1 if empty                *
 ************************************************************************/

char Usage[] = "Usage:  has_no_subdir [-t] directory";
char CantOpen[] = "Cannot open";

void fail(char *msg) {
    fprintf(stderr, "fail: %s\n", msg);
    exit(1);
} // fail

main(argc,argv) int argc; char *argv[]; {
int n, m, test=0;
struct stat sbuf;
char *directory=0;
DIR *dir_ptr;
struct dirent *dp;
//char type, is_link, *errmsg, cwd_pathname[MAXPATHLEN], link_to[MAXPATHLEN];
  /* --- examine command line arguments --- */
  for(n=1; n<argc; n++) {
     if(argv[n][0]=='-') /* --- examine command line switch(es) --- */
        for(m=1; argv[n][m]; m++) {
           switch(argv[n][m]) {
              /*
              case 'i': if(argv[n][m+1])
                            indent_spaces = atoi(&argv[n][m+1]);
                        else if(m=0,++n<argc)
                            indent_spaces = atoi(argv[n]);
                        goto L;
              */
              case 't': test=1; break;
              default:  fail(Usage);
           }
        }
     else { /* --- get directory name --- */
        if(directory)
           fail("More than one directory name given (may be missing quote marks)");
        directory = argv[n];
     }
     L: ;
  }
  if(!directory)
     fail(Usage);
  if(stat(directory, &sbuf)<0)
     fail("Directory read error");
  else if((sbuf.st_mode & S_IFMT) != S_IFDIR)
     fail("Not a directory");
  if(!(dir_ptr=opendir(directory)))
     fail("opendir() failed");
  for(dp=readdir(dir_ptr); dp!=NULL; dp=readdir(dir_ptr)) {
     if((strcmp(dp->d_name, ".")==0) || (strcmp(dp->d_name, "..")==0))
        continue;
     // directory is NOT empty
     printf("%s: directory \"%s\" is not empty\n", test?"pass":"fail", directory);
     return test ? 0 : 1;
  }
  closedir(dir_ptr);
  printf("%s: directory \"%s\" is empty\n", test?"fail":"pass", directory);
  return test ? 1 : 0;
} // main

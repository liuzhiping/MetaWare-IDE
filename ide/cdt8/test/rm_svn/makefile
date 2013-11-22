# FILE:  makefile
# DATE:  2008 May 12
# BY:    Prem Sobel

CC=gcc

CFLAGS=-g

OBJ=rm_svn.o

%.o : %.c
	$(CC) $(CFLAGS) -c $< -o $@

rm_svn:	$(OBJ)
		$(CC) $(CFLAGS) -o rm_svn $(OBJ)
		chmod +rx rm_svn

clean:	/bin/rm rm_svn *.o *~


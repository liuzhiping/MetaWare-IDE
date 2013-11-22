CC=hcac
CFLAGS=-g
OBJS =		Outside.o hello.o

LIBS =

TARGET =	Outside.exe

$(TARGET):	$(OBJS)
	$(CC) -o $(TARGET) $(OBJS) $(LIBS)
	
Outside.o:	Outside.cpp
	$(CC) -c -g Outside.cpp
	
hello.o:	../../hello.c
	$(CC) -c -g ../../hello.c
	
all:	$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET)

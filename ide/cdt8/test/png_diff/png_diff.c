/* png_diff.c
 * by Prem Sobel
 * 2008 Feb 12
 */

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <png.h>

#define iabs(i) (((i)<0) ? (-(i)) : (i))

static char line[1200];
static int  debug, show_pixels, show_diff_pixels, xor_bmp;
static char bm_fn[1200], bm_fn_len;
// -------------------------------------------------------------------
void error(char *msg) {
    printf("*** %s\n", msg);
    exit(1);
} // error
// -------------------------------------------------------------------
void *zalloc(int b) { void *p;
    p = malloc(b);
    if(!p) {
        sprintf(line, "out of memory: zalloc(%d)", b);
        error(line);
    }
    return p;
} // error
// -------------------------------------------------------------------
png_infop png_read(char *fn) {
char buf[8];
FILE *fp;
png_infop info;
png_structp png_ptr;
const int transforms = PNG_TRANSFORM_STRIP_ALPHA | 
              PNG_TRANSFORM_STRIP_16 | PNG_TRANSFORM_PACKING;
    // is fn a PNG file?
    errno = 0;
    fp = fopen(fn, "rb");
    if (fp == NULL) {
        sprintf(line, "%s : %s",  fn, strerror(errno));
        error(line);
    }
    if (fread(buf, 8, 1, fp) < 1) {
        sprintf(line, "%s: not a PNG file, %s", fn, strerror(errno));
        error(line);
    }
    if(fclose(fp)) {
        sprintf(line, "%s: not closable, %s", fn, strerror(errno));
        error(line);
    }
    if(png_sig_cmp((png_bytep)buf, (png_size_t) 0, 8)) {
        sprintf(line, "%s: not a PNG file", fn);
        error(line);
    }
    fp = fopen(fn, "rb");
    if (fp == NULL) {
        sprintf(line, "%s : %s",  fn, strerror(errno));
        error(line);
    }

    /* Apparently this bit creates and initialises a png_struct with
       the default error handling functions. */
    png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
    if (png_ptr == NULL) {
        err:
        fclose(fp);
        sprintf(line, "%s: error", fn);
        error(line);
    }

    /* Prepare the memory for image info. */
    info = png_create_info_struct(png_ptr);
    if (info == NULL) {
        png_destroy_read_struct(&png_ptr, png_infopp_NULL, png_infopp_NULL);
        goto err;
    }

    /* Setup the setjmp/longjmp error handling. */
    if (setjmp(png_jmpbuf(png_ptr))) {
        png_destroy_read_struct(&png_ptr, &info, png_infopp_NULL);
        goto err;
    }

    /* Initialise I/O. */
    png_init_io(png_ptr, fp);

    /* Read the PNG image's data in. */
    png_read_png(png_ptr, info, transforms, png_voidp_NULL);

    if(debug) {
        printf("%s: h=%u w=%u channels=%u\n",
            fn, (unsigned int)info->height, (unsigned int)info->width,
            (unsigned int)info->channels);
    }
    return info;
} // png_read
// -------------------------------------------------------------------
// return pixel png_img[r][c] - does not yet support 16 bit colors

int png_pixel(png_infop png, int r, int c) {
int pixel;
    if(1 == png->channels) {
        pixel = png->row_pointers[r][c];
    } else if(3 == png->channels) {
        pixel = (png->row_pointers[r][c*3]<<16) |
                (png->row_pointers[r][c*3+1]<<8) |
                png->row_pointers[r][c*3+2];
    } else
       pixel = 0xFFFFFFFF;
    if(show_pixels)
        printf("pixel[%02d][%02d] = %08X\n", r, c, pixel);
    return pixel;
} // png_pixel
// -------------------------------------------------------------------
static void write4(int i, FILE *fp) { // little endian
    fputc(i&0xFF, fp);
    fputc((i>>8)&0xFF, fp);
    fputc((i>>16)&0xFF, fp);
    fputc((i>>24)&0xFF, fp);
} // write4
// -------------------------------------------------------------------
static void write3(int i, FILE *fp) { // little endian
    fputc(i&0xFF, fp);
    fputc((i>>8)&0xFF, fp);
    fputc((i>>16)&0xFF, fp);
} // write3
// -------------------------------------------------------------------
static void write2(short s, FILE *fp) { // little endian
    fputc(s&0xFF, fp);
    fputc((s>>8)&0xFF, fp);
} // write2
// -------------------------------------------------------------------
static void set_bm_pixel(int *b, int w, int r, int c, int pixel) {
    b[r*w+c] = pixel;
} // set_bm_pixel
// -------------------------------------------------------------------
static int get_bm_pixel(int *b, int w, int r, int c) {
    return b[r*w+c];
} // get_bm_pixel
// -------------------------------------------------------------------
static void write_bmp(char *fn, int *bmp, int w, int h) {
FILE *fp;
int w4, size, xtra, r, c;
    fp = fopen(fn, "wb");
    if(!fp) {
        printf("@ Unable to write file: %s\n", fn);
    } else {
        // write BitMapFileHeader
        fputc('B', fp);
        fputc('M', fp);
        w4 = 3*w;
        xtra = 0;
        if(w4%4) {
            xtra = 4-(w4%4);
            w4 += xtra;
            if(debug)
                printf("@ pseudo w4=%d xtra=%d\n", w4, xtra);
        }
        size = 14+40+w4*h;
        write4(size, fp); // total size of file in bytes
        write4(0, fp); // reserved
        write4(54, fp); // offset to start of bitmap data
        write4(40, fp); // sizeof BitMapInfoHeader
        // write BitMapInfoHeader
        write4(w, fp); // width
        write4(h, fp); // height
        write2(1, fp); // num planes
        write2(24, fp); //bits per pixel
        write4(0, fp); // compression: none
        size = w4*h;
        write4(size, fp); // size of bitmap data (line rounded up %4)
        write4(0, fp);
        write4(0, fp);
        write4(0, fp);
        write4(0, fp);
        // write pixels
        for(r=0; r<h; r++) {
            for(c=0; c<w; c++) {
                write3(get_bm_pixel(bmp, w, r, c), fp);
            }
            // each line size must be devisible by 4 
            for(c=0; c<xtra; c++)
                fputc(0, fp);
        }
        // done
        fclose(fp);
    }
} // write_bmp
// -------------------------------------------------------------------
// returns number of initially non zero edge pixels
int bm_zero_edge(int *bmp, int w, int h) {
int chg=0, r, c, pixel;
    for(r=0; r<h; r++) {
        pixel = get_bm_pixel(bmp, w, r, 0);
        if(pixel) {
            chg++;
            set_bm_pixel(bmp, w, r, 0, 0);
        }
        pixel = get_bm_pixel(bmp, w, r, w-1);
        if(pixel) {
            chg++;
            set_bm_pixel(bmp, w, r, w-1, 0);
        }
    }
    for(c=1; c<w-1; c++) {
        pixel = get_bm_pixel(bmp, w, 0, c);
        if(pixel) {
            chg++;
            set_bm_pixel(bmp, w, r, 0, c);
        }
        pixel = get_bm_pixel(bmp, w, h-1, c);
        if(pixel) {
            chg++;
            set_bm_pixel(bmp, w, h-1, c, 0);
        }
    }
    return chg;
} // bm_zero_edge
// -------------------------------------------------------------------
// returns 0 if equivalent, 1 if not equivalent

void png_diff(png_infop png1, png_infop png2) {
int r, c, w1, h1, w2, h2, p1, p2, chg=0, dif;
int *b1, *b2, *bs, pixel, step, x, y;
    // compare sizes
    w1 = png1->width;
    h1 = png1->height;
    w2 = png1->width;
    h2 = png1->height;
    if((w1 == w2) && (h1 == h2)) {
        if(debug)
            printf("Sizes same: h=%d w=%d\n", h1, w1);
    } else {
        if(debug)
            printf("Sizes differ: h1=%d w1=%d : h2=%d w2=%d (using overlap)\n",
                h1, w1, h2, w2); 
        // find overlap region
        w1 = w1<w2 ? w1 : w2;
        h1 = h1<h2 ? h1 : h2;
    }
    // alocate array for xor.00
    b1 = (int *)zalloc(w1*h1*sizeof(int));
    // calculate xor.00
    for(r=0; r<h1; r++) {
        for(c=0; c<w1; c++) {
            p1 = png_pixel(png1, r, c);
            p2 = png_pixel(png2, r, c);
            if(p1 != p2) {
                chg++;
                dif = iabs((p1&0xFF0000) - (p2&0xFF0000)) |
                      iabs((p1&0xFF00) - (p2&0xFF00)) |
                      iabs((p1&0xFF) - (p2&0xFF));
                if(show_diff_pixels) {
                    printf("@ r=%d c=%d p1=%06X p2=%06X xor=%06X dif=%06X\n",
                        r, c, p1, p2, p1^p2, dif);
                }
                set_bm_pixel(b1, w1, r, c, dif^0xFFFFFF);
            } else {
                set_bm_pixel(b1, w1, r, c, 0);
            }
            
        }
    }
    if(!chg) {
        printf("Images identical\n");
        exit(0);
    } else {
        if(debug)
            printf("Images differ by %d pixels\n", chg);
    }
    if(xor_bmp) {
        write_bmp(bm_fn, b1, w1, h1);
    }
    // make sure edge pixels are 0
    chg = bm_zero_edge(b1, w1, h1);
    if(!chg) {
        if(debug)
            printf("XOR edge pixels all 0\n");
    } else {
        if(debug)
            printf("XOR edge pixels: %d were non zero\n", chg);
    }
    // allocate 2nd bmp, set edge to 0
    b2 = (int *)zalloc(w1*h1*sizeof(int));
    bm_zero_edge(b2, w1, h1);
    // how many steps to 0 XOR?
    for(step=1; step<99; step++) {
        chg = 0;
        for(r=1; r<h1-1; r++) {
            for(c=1; c<w1-1; c++) {
                pixel = get_bm_pixel(b1, w1, r, c);
                if(pixel) {
                    chg++;
                    // check 3x3 for 0
                    for(y=-1; y<=1; y++) {
                        for(x=-1; x<=1; x++) {
                            pixel = get_bm_pixel(b1, w1, r+y, c+x);
                            if(!pixel)
                                goto zero_pixel;
                        }
                    }
                }
                zero_pixel:
                set_bm_pixel(b2, w1, r, c, pixel);
            }
        }
        if(!chg) {
            printf("XOR zero after %d steps, images %s\n",
                step, step<10 ? "'same'" : "different");
            exit(step <10 ? 0 : 1);
        } else {
            if(debug)
                printf("XOR has %d non zero pixels after %d step(s)\n",
                    chg, step);
        }
        if(xor_bmp) {
            sprintf(&bm_fn[bm_fn_len+1], "%02d.bmp", step);
            write_bmp(bm_fn, b2, w1, h1);
        }
        // swap image pointers
        bs = b1;
        b1 = b2;
        b2 = bs;
    }
    if(debug)
        printf("images different\n");
    exit(1);
} // png_diff
// -------------------------------------------------------------------
static char *usage =
    "png_diff [-d] [-p] [-r] [-x bm_fn] png1 png2\n"
    "   -d : output info on files to stdout\n"
    "   -p : output pixels values to stdout\n"
    "   -r : output only differing pixel values to stdout\n"
    "   -x bm_fn : output XOR filename (without extension) in BMP format\n";

int main(int argc, char **argv) {
int n, m;
char *png_fn1=0, *png_fn2=0, *bfn=0;
png_infop png1, png2;
    /* --- examine command line arguments --- */
    for(n=1; n<argc; n++) {
        if(argv[n][0]=='-') {
            /* --- examine command line switch(es) --- */
            for(m=1;argv[n][m];m++) {
                switch(argv[n][m]) {
                    case 'x':
                        xor_bmp=1;
                        if(argv[n][m+1])
                            bfn = &argv[n][m+1];
                        else if(m=0,++n<argc)
                            bfn = argv[n];
                        else
                            error("missing bm_fn");
                        bm_fn_len = strlen(bfn);
                        sprintf(bm_fn, "%s.00.bmp", bfn);
                        goto L;
                    case 'd': debug=1; break;
                    case 'p': show_pixels=1; break;
                    case 'r': show_diff_pixels=1; break;
                    default:  error(usage);
                }
           }
        } else { /* --- get png file names --- */
            if(!png_fn1)
                png_fn1 = argv[n];
            else if(!png_fn2)
                png_fn2 = argv[n];
            else 
                error(usage);
        }
        L: ;
    }
    if(!png_fn1 || !png_fn2)
        error(usage);
    if(debug && !strcmp(png_fn1, png_fn2))
        printf("File names the same (will continue)!\n\n");

    // read and compare files
    png1 = png_read(png_fn1);
    png2 = png_read(png_fn2);
    png_diff(png1, png2);

    // clean up (execution never gets here)
    //png_free(png1);
    //png_free(png2);
    return 0;
} // main
// -------------------------------------------------------------------

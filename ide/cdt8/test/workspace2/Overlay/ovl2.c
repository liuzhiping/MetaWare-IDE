// {(
void _Overlay() will_evict() {
    int foil_inline[1024];
    printf("I am a single overlay that will cause all parents to be evicted while unwinding\n");
    return;
    }

void _Overlay(12) doit() {
    int foil_inline[1024];
    printf("I am a group overlay(12) entry doit and calling into will_evict!\n");
    will_evict();
    return;
    }

void _Overlay() doit0() {
    int foil_inline[1024];
    printf("I am a single overlay for doit0 and calling into doit!\n");
    doit();
    return;
    }

void doit1(void) {
    int foil_inline[1024];
    /* a call to an overlay function - cannot tail continue */
    printf("I am a pc-relative doit1 and calling doit0!\n");
    doit0();
    return;
    }

void _Overlay(12) doit2() {
    /*
     * - any call from within an overlay must go through loader (%r22)
     * - blink is meaningless since it was saved off onto
     *   alternate call-return stack. No need to save/restore.
     * - blink is used (for now) to load the token
     */
    int foil_inline[1024];
    /*
     * cannot tail continue this call. Must
     * go through return handler (%r23)
     */
    printf("I am a group overlay(12) entry doit2 and am calling doit1!\n");
    doit1();
    return;
    }

void main() {
    // create 2 memory regions for overlay manager to work with
    struct {
	unsigned x,y;
	} foo[] = {
	    {malloc(0x600),0x600},
	    {-1,-1}
	    };
    AOM_initialize(foo);
    printf("I'm main and calling doit2!\n");
    doit2();
    printf("doit2 just returned to me, main. Done.\n");
    return;
    }

// )}

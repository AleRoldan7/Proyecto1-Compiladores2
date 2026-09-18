#include <stdio.h>
#include <stdlib.h>

// ==== VARIABLES GLOBALES ====
int a;
int b;
int c;

// ==== TEMPORALES ====
int t0, t1, t2, t3, t4, t5;

// ==== FORWARD DECLARATIONS ====
int sumar();
void func_main();

// ==== FUNCIONES ====
int sumar(int x, int y) {
    t0 = x + y;
    return t0;
}

void func_main() {
    a = 5;
    b = 3;
    t1 = b * 2;
    t2 = a + t1;
    c = t2;
    if (a > b) goto L0;
    goto L1;
    L0:;
    t3 = c + 1;
    c = t3;
    goto L2;
    L1:;
    t4 = c - 1;
    c = t4;
    L2:;
    L3:;
    if (c < 100) goto L4;
    goto L5;
    L4:;
    t5 = c + 1;
    c = t5;
    goto L3;
    L5:;
    return;
}


int main() {
    func_main();
    return 0;
}

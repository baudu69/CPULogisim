LDvi R7 1000
LDvi R6 5
ST R6 R7
CALL f
LD R5 R7
STOP
f: LD R0 R7
    XOR R1 R1 R1
    JEQU R1 R0 fin
    SUBi R1 R0 1
    ADDi R7 R7 1
    ST R1 R7
    CALL f
   LD R1 R7
    SUBi R7 R7 1
    LD R0 R7
    MUL R0 R0 R1
    JMP push
fin: ADDi R0 R0 1
push: ST R0 R7
    RET
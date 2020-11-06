;Programme de Division
;Avec R0/R1=R2 et R0%R1=R3
start: LDvi R0 14
    LDvi R1 3
    LDvi R2 0
    LDvi R3 0
    CALL divi
divi: JPET R0 R1 zero
    SUB R0 R0 R1
    ADDi R2 R2 1
    JMP divi
zero: ADD R3 R3 R0
    STOP
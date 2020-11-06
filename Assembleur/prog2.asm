;Programme de calcul de puissance
;Forme R0 exposant R1 = R3
;R2 est une constante
start: LDvi R0 5
    LDvi R1 6
    LDvi R2 0
    LDvi R3 1
    CALL multi
    STOP
multi: MUL R3 R3 R0
    SUBi R1 R1 1
    JGRA R1 R2 multi
    RET
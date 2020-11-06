;Programme de calcul de factoriel
;R0 : entree prog
;R1 : Resultat
;R2 : Constant = a 1
start: LDvi R0 7
    LDvi R1 1
    LDvi R2 1
    CALL facto
    STOP
facto: MUL R1 R1 R0
    SUBi R0 R0 1
    JGRA R0 R2 facto
    RET
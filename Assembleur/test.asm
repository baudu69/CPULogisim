start:	LDvi R0 abcd
	CALL func
	STOP
func: ADDi R1 R0 2
	ADDi R1 R1 2
	ADDi R1 R1 2
	RET
package assembleur;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

/*

Syntaxe assembleur :
 - PAS de label seul sur une ligne
 - PAS de label avec le même nom qu'une instruction ou registre

*/
public class Main {

	private static void printHelp() {
		System.err.println("java -jar Assembleur.jar fichierIN fichierOUT");
	}

	public static void main(String[] args) {
		if (args.length == 2) {
			String[] source;
			try {
				source = readSourceFile(new File(args[0]));
			} catch (IOException e) {
				System.err.println("Erreur lors de la lecture du fichier source");
				e.printStackTrace();
				return;
			}
			int[] binCode = assemble(source);
			try {
				writeBinFile(binCode, new File(args[1]));
			} catch (IOException e) {
				System.err.println("Erreur lors de l'écriture du fichier binaire");
				e.printStackTrace();
			}
		} else {
			printHelp();
		}
	}

	private static String[] readSourceFile(File inFile) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));

		List<String> code = new ArrayList<>();

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.replaceAll("\\s*;.*$", ""); // enlève tous les commentaires
			line = line.replaceAll("(^\\s*)|(\\s*$)", ""); // enlève tous les espaces en début et fin de ligne

			if (!line.matches("^\\s*$")) { // si la ligne n'est pas vide
				code.add(line); // on l'ajoute à la liste
			}
		}

		return code.toArray(new String[]{});
	}

	private static final List<String> REGS = Arrays.asList("R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7");

	private static final List<String> ALU = Arrays.asList("ADD", "SUB", "AND", "OR", "XOR", "SR", "SL", "MUL");
	private static final List<String> ALUi = Arrays.asList("ADDi", "SUBi", "ANDi", "ORi", "XORi", "SRi", "SLi", "MULi");

	private static final List<String> MEM = Arrays.asList("LD", "LDai", "LDvi", "ST", "STai");

	private static final List<String> CTRL = Arrays.asList("JEQU", "JNEQ", "JPET", "JGRA", "JMP", "CALL", "RET");

	private static void checkReg(String reg) {
		if (!REGS.contains(reg)) {
			throw new RuntimeException("Registre '" + reg + "' inconnu");
		}
	}

	private static int[] assemble(String[] source) {
		int[] binCode = new int[source.length];

		Map<String, Integer> labelMap = new HashMap<>();

		// Récupère les adresses et enlève les labels
		for (int address = 0; address < source.length; address++) {
			String[] tokens = source[address].split("\\s+");

			if (tokens[0].matches("^.*:$")) {
				labelMap.put(tokens[0].substring(0, tokens[0].length() - 1), address);
			}
			source[address] = source[address].replaceFirst("^.*:\\s+", "");
		}

		// <editor-fold desc="Calcul labels">
		Stream<String> sortedLabels = labelMap.keySet().stream().sorted(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.length() - o1.length();
			}
		});

		sortedLabels.forEach(label -> {
			System.out.println(label + " -> " + Integer.toHexString(labelMap.get(label)));
			for (int i = 0; i < source.length; i++) {
				source[i] = source[i].replaceAll(label, String.valueOf(labelMap.get(label)));
			}
		});
		// </editor-fold>

		for (int i = 0; i < source.length; i++) {
			String[] tokens = source[i].split("\\s+");
			String instruction = tokens[0];
			int bin = 0;

			// <editor-fold desc="ALU">
			if (ALU.contains(instruction) || ALUi.contains(instruction)) {
				boolean immediate = ALUi.contains(instruction);

				int opcode;
				if (immediate) {
					opcode = ALUi.indexOf(instruction) | 0b1000;
				} else {
					opcode = ALU.indexOf(instruction);
				}
				bin |= opcode << 2;

				String DR = tokens[1];
				String SR1 = tokens[2];
				String arg2 = tokens[3];

				checkReg(DR);
				checkReg(SR1);

				bin |= REGS.indexOf(DR) << 6;
				bin |= REGS.indexOf(SR1) << 9;

				if (immediate) {
					int value = Integer.parseInt(arg2, 16);
					bin |= value << 16;
				} else {
					checkReg(arg2);
					bin |= REGS.indexOf(arg2) << 12;
				}
			}
			// </editor-fold>
			// <editor-fold desc="MEM">
			else if (MEM.contains(instruction)) {
				bin = 0b10;

				int opcode = 0;
				switch (instruction) {
					case "LD":
						opcode = 0b0000;
						break;
					case "LDai":
						opcode = 0b0001;
						break;
					case "LDvi":
						opcode = 0b1010;
						break;
					case "ST":
						opcode = 0b0100;
						break;
					case "STai":
						opcode = 0b0101;
						break;
				}

				bin |= opcode << 2;

				String R = tokens[1];
				checkReg(R);
				bin |= REGS.indexOf(R) << 6;


				switch (instruction) {
					case "LD":
					case "ST":
						String Raddress = tokens[2];
						checkReg(Raddress);
						bin |= REGS.indexOf(Raddress) << 9;
						break;
					case "LDvi":
					case "LDai":
					case "STvi":
						int value = Integer.parseInt(tokens[2], 16);
						bin |= value << 16;
						break;
				}
			}
			// </editor-fold>
			// <editor-fold desc="CTRL">
			else if (CTRL.contains(instruction)) {
				bin = 0b11;

				int opcode;
				switch (instruction) {
					case "CALL":
						opcode = 0b110;
						break;
					case "RET":
						opcode = 0b111;
						break;
					default:
						opcode = CTRL.indexOf(instruction);
						break;
				}
				bin |= opcode << 2;

				int jmpAddress;
				switch (instruction) {
					case "RET":
						break;
					case "JMP":
					case "CALL":
						jmpAddress = Integer.parseInt(tokens[1], 16);
						bin |= jmpAddress << 16;
						break;
					default:
						String R1 = tokens[1];
						String R2 = tokens[2];

						checkReg(R1);
						checkReg(R2);

						bin |= REGS.indexOf(R1) << 9;
						bin |= REGS.indexOf(R2) << 12;

						jmpAddress = Integer.parseInt(tokens[3], 16);
						bin |= jmpAddress << 16;
						break;
				}
			}
			// </editor-fold>
			if (instruction.equals("STOP")) {
				bin = 0b10011;
				bin |= i << 16;
			}

			binCode[i] = bin;
		}

		return binCode;
	}

	private static void writeBinFile(int[] binCode, File outFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

		writer.append("v2.0 raw\n");

		for (int j : binCode) {
			writer.append(Integer.toHexString(j)).append(" ");
			System.out.println(Integer.toBinaryString(j));
		}

		writer.flush();
		writer.close();
	}

}

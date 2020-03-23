package assembler;

import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class Parser 
{
	private File asm;
	private Scanner reader;
	private ArrayList<String> lines = new ArrayList<String>();
	private int[] instructionTypes;
	private String[] binaryLines;
	private int instructionCount = 0;
	private int incDataMemoryAddress = 16;
	private SymbolTable table = new SymbolTable();
	
	public static final int C_INSTRUCTION = 0;
	public static final int A_CONSTANT_INSTRUCTION = 1;
	public static final int A_LABEL_INSTRUCTION = 2;
	
	/*
	 * Constructor. Parses each instruction. Cuts comments, whitespace, blank lines, and identifies instruction labels.
	 */
	public Parser(String filename) throws FileNotFoundException
	{
		asm = new File(filename);
		reader = new Scanner(asm);
		
		// Pass #1: Parses each instruction from file. Cuts comments, white space, blank lines, and identifies
		// instruction labels.
		String current = "";
		while(reader.hasNextLine())
		{
			current = trim(reader.nextLine()); // nextLine() omits new line character
			if(!current.equals(""))            // Adds the line if != empty string.
			{
				if(isInstructionLabel(current))
					table.addEntry(current.substring(1, current.length() - 1), instructionCount);
				else
				{
					lines.add(current);
					instructionCount++;
				}
			}
		}
		reader.close();	
		
		// Pass #2: Classifies each instruction and adds A labels.
		instructionTypes = new int[instructionCount];
		parseInstructionTypes();
		
		// Pass #3: Translates each instruction
		binaryLines = new String[instructionCount];
		decode();
		
		// Final Step: Output binaryLines to file
		File hackFile = new File(filename.substring(0, filename.lastIndexOf(".")) + ".hack");
		PrintWriter writer = new PrintWriter(hackFile);
		for(String binaryLine: binaryLines)
			writer.println(binaryLine);
		
		writer.close();
	}
	
	/*
	 * Prints the program as it is currently stored in lines
	 */
	public String toString()
	{
		int index = 0;
		String parseString = "";
		for (String line : lines)
		{
			parseString += line + " Instruction Type: " + instructionTypes[index] + " Binary Equivalent: " + binaryLines[index] + "\n";
			index++;
		}
		parseString += "Instruction Count = " + instructionCount;
		return parseString;
	}
	
	/*
	 * Eliminates comments and spaces
	 */
	private String trim(String line)
	{
		line = line.replaceAll("//.*+", "");
		line = line.replaceAll("\\s", "");
		return line;
	}
	
	private boolean isInstructionLabel(String line)
	{
		
		if(line.matches("[(][A-Za-z_.$:][A-Za-z0-9_.$:]*+[)]"))
			return true;
		else if(line.matches("[(].*[)]"))
			return false;
		else
			return false;
	}
	
	
	private void parseInstructionTypes()
	{
		int index = 0;
		for(String line : lines)
		{
			if(line.matches("@[A-Za-z_.$:][A-Za-z0-9_.$:]*+"))    
			{
				addALabel(line.substring(1, line.length()));
				instructionTypes[index] = A_LABEL_INSTRUCTION;
			}
			else if(line.matches("@[0-9]++"))
				instructionTypes[index] = A_CONSTANT_INSTRUCTION;
			else
				instructionTypes[index] = C_INSTRUCTION;
			index++;
		}
	}
	
	private void addALabel(String label)
	{
		if(!table.contains(label))
		{
			table.addEntry(label, incDataMemoryAddress);
			incDataMemoryAddress++;
		}
	}
	
	private void decode()
	{
		int index = 0;
		int currentType;
		for(String line : lines)
		{
			currentType = instructionTypes[index];
			if (currentType == A_LABEL_INSTRUCTION) 
				decodeALabel(line, index);
			else if(currentType == A_CONSTANT_INSTRUCTION)
				decodeAConstant(line, index);
			else if(currentType == C_INSTRUCTION)
				decodeCInstruction(line, index);
			else
				binaryLines[index] = "placeholder";
			index++;
		}
	}
	
	private void decodeAConstant(String line, int index)
	{
		int constant = Integer.parseInt(line.substring(1, line.length()));
		binaryLines[index] = "0" + String.format("%15s", Integer.toBinaryString(constant)).replace(' ', '0');
		
	}
	
	private void decodeALabel(String line, int index)
	{
		String label = line.substring(1, line.length());
		int address = table.getAddress(label);
		binaryLines[index] = "0" + String.format("%15s", Integer.toBinaryString(address)).replace(' ', '0');
	}
	
	private void decodeCInstruction(String line, int index)
	{
		String preface = "111", comp = "0000000", dest = "000", jump = "000";
		
		if(line.matches("A?M?D?=.+"))
			dest = getDestCode(line.substring(0, line.indexOf("=")));
		
		int eqIndex = line.indexOf("=");
		int scIndex = line.indexOf(";");
		if(scIndex == -1)
			scIndex = line.length();
		
		comp = getCompCode(line.substring(eqIndex + 1, scIndex));
		
		if(line.matches(".+;J[GELNM][TQEP]"))
			jump = getJumpCode(line.substring(line.length() - 3, line.length()));
		
		binaryLines[index] = preface + comp + dest + jump;
	}
	
	private String getDestCode(String destSymbol)
	{
		// The supplied assembler does not allow these letters to appear
		// in different orders. A must come first, M second, D last.
		switch(destSymbol)
		{
			case "M":
				return "001";
			case "D":
				return "010";
			case "MD":
				return "011";
			case "A":
				return "100";
			case "AM":
				return "101";
			case "AD":
				return "110";
			case "AMD":
				return "111";
			default:
				return "000"; 
		}
	}
	
	private String getCompCode(String operation)
	{
		switch(operation)
		{
			case "0":
				return "0101010";
			case "1":
				return "0111111";
			case "-1":
				return "0111010";
			case "D":
				return "0001100";
			case "A":
				return "0110000";
			case "M":
				return "1110000";
			case "!D":
				return "0001101";
			case "!A":
				return "0110001";
			case "!M":
				return "1110001";
			case "-D":
				return "0001111";
			case "-A":
				return "0110011";
			case "-M":
				return "1110011";
			case "D+1": case "1+D":
				return "0011111";
			case "A+1": case "1+A":
				return "0110111";
			case "M+1": case "1+M":
				return "1110111";
			case "D-1":
				return "0001110";
			case "A-1":
				return "0110010";
			case "M-1":
				return "1110010";
			case "D+A": case "A+D":
				return "0000010";
			case "D+M": case "M+D":
				return "1000010";
			case "D-A":
				return "0010011";
			case "D-M":
				return "1010011";
			case "A-D":
				return "0000111";
			case "M-D":
				return "1000111";
			case "D&A": case "A&D":
				return "0000000";
			case "D&M": case "M&D":
				return "1000000";
			case "D|A": case "A|D":
				return "0010101";
			case "D|M": case "M|D":
				return "1010101";
			default:
				return "0000000"; 
			
		}
	}
	
	private String getJumpCode(String jumpSymbol)
	{
		switch(jumpSymbol)
		{
			case "JGT":
				return "001";
			case "JEQ":
				return "010";
			case "JGE":
				return "011";
			case "JLT":
				return "100";
			case "JNE":
				return "101";
			case "JLE":
				return "110";
			case "JMP":
				return "111";
			default:
				return "000";
		}
	}	
}
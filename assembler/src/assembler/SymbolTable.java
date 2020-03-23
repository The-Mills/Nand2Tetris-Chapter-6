package assembler;

import java.util.Map;
import java.util.HashMap;

public class SymbolTable 
{
	private Map<String, Integer> symboltable = new HashMap<String, Integer>();
	
	
	public SymbolTable()
	{
		symboltable.put("SP", 0);
		symboltable.put("LCL", 1);
		symboltable.put("ARG", 2);
		symboltable.put("THIS", 3);
		symboltable.put("THAT", 4);
		symboltable.put("R0", 0);
		symboltable.put("R1", 1);
		symboltable.put("R2", 2);
		symboltable.put("R3", 3);
		symboltable.put("R4", 4);
		symboltable.put("R5", 5);
		symboltable.put("R6", 6);
		symboltable.put("R7", 7);
		symboltable.put("R8", 8);
		symboltable.put("R9", 9);
		symboltable.put("R10", 10);
		symboltable.put("R11", 11);
		symboltable.put("R12", 12);
		symboltable.put("R13", 13);
		symboltable.put("R14", 14);
		symboltable.put("R15", 15);
		symboltable.put("SCREEN", 16384);
		symboltable.put("KBD", 24576);	
	}
	
	public void addEntry(String label, Integer address)
	{
		symboltable.put(label, address);
	}
	
	public int getAddress(String label)
	{
		return symboltable.get(label);
	}
	
	public boolean contains(String label)
	{
		return symboltable.containsKey(label);
	}
	
	public String toString()
	{
		return symboltable.toString();
	}
}

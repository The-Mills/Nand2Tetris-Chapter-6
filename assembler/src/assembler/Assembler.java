package assembler;

import java.io.FileNotFoundException;

public class Assembler 
{
	public static void main(String[] args) throws FileNotFoundException
	{
		Parser theParser = new Parser("Rect.asm");
		System.out.println(theParser);
	}	
}
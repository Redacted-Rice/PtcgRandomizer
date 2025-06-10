package redactedrice.ptcgr.compiler;

import java.io.IOException;
import java.util.Arrays;

import redactedrice.compiler.CompilerUtils;
import redactedrice.compiler.instructions.BasicInstruction;
import redactedrice.compiler.instructions.addressref.BlockBankLoadedAddress;
import redactedrice.compiler.instructions.basic.Rst;
import redactedrice.gbcframework.QueuedWriter;
import redactedrice.gbcframework.addressing.BankAddress;

public class Bank1Call extends BasicInstruction
{
	// TODO: integrate with call/farcall
	
	public static final int SIZE = 3;
	short value;

	public Bank1Call(short bank1Address)
	{
		// TODO: Check address
		super(SIZE);
		this.value = bank1Address;
	}
	
	// TODO: Support labels?
	public static Bank1Call create(String[] args)
	{		
		final String SUPPORT_STRING = "Bank1Call only supports (short): Given ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
		}
		
		try
		{
			return new Bank1Call(CompilerUtils.parseShortArg(args[0]));
		}
		catch (IllegalArgumentException iae) 
		{
			// The instruct doesn't fit - try the next one (if there is one)
			// Could throw here but kept to preserve the pattern being used for
			// the instructs to support more easily adding future ones without
			// forgetting to add the throw at the end
		}
		
		throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
	}
	
	@Override
	public void writeStaticBytes(QueuedWriter writer) throws IOException
	{
		// bankcall1 is in RST 18
		Rst.write(writer, (byte) 0x18);
		BlockBankLoadedAddress.write(writer, new BankAddress(value));
	}
}

package redactedrice.ptcgr.data;

import redactedrice.compiler.CodeBlock;
import redactedrice.compiler.instructions.basic.RawBytes;
import redactedrice.gbcframework.utils.ByteUtils;

public class ExistingCardEffect extends CardEffect
{
	public static final CardEffect NONE = new ExistingCardEffect((short) 0);
	
	short pointer;
	
	public ExistingCardEffect(short pointer) 
	{
		this.pointer = pointer;
	}

	@Override
	public CardEffect copy()
	{
		return new ExistingCardEffect(pointer);
	}

	@Override
	public void appendToCodeBlock(CodeBlock block)
	{
		block.appendInstruction(new RawBytes(ByteUtils.shortToLittleEndianBytes(pointer)));
	}

	@Override
	public String toString() 
	{
		return String.format("0x%x", pointer);
	}
}

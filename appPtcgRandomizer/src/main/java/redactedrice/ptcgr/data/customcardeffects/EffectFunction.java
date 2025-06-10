package redactedrice.ptcgr.data.customcardeffects;

import redactedrice.gbcframework.addressing.BankAddress;
import redactedrice.rompacker.MovableBlock;

public class EffectFunction
{
	private MovableBlock block;
	private short address;
	
	EffectFunction(MovableBlock block)
	{
		this.block = block;
		this.address = BankAddress.UNASSIGNED_ADDRESS;
	}

	EffectFunction(short address)
	{
		this.block = null;
		this.address = address;
	}
	
	public boolean isExistingFunction()
	{
		return block == null;
	}
	
	public MovableBlock getBlock()
	{
		return block;
	}
	
	public short getAddress()
	{
		return address;
	}
}

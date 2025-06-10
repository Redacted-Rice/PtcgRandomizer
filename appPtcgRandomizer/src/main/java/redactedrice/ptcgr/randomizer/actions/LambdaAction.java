package redactedrice.ptcgr.randomizer.actions;


import redactedrice.ptcgr.rom.Rom;

public class LambdaAction  extends Action {
	
	PerformLambda perform;
	
	public LambdaAction(String category, String name, String description, PerformLambda perform)
	{
		super (category, name, description);
		this.perform = perform;
	}
	
	public LambdaAction(String category, StringLambda name, StringLambda description, PerformLambda perform)
	{
		super (category, name, description);
		this.perform = perform;
	}

	@Override
	public void Perform(Rom rom) {
		perform.perform(rom);
	}
}

package redactedrice.ptcgr.randomizer.actions;


import redactedrice.ptcgr.rom.Rom;

public class LambdaAction {

    PerformLambda perform;

    public LambdaAction(String category, String name, String description, PerformLambda perform) {
        // super(category, name, description);
        this.perform = perform;
    }

    public LambdaAction(String category, StringLambda name, StringLambda description,
            PerformLambda perform) {
        // super(category, name, description);
        this.perform = perform;
    }

    protected LambdaAction(LambdaAction toCopy) {
        // super(toCopy);
        this.perform = toCopy.perform;
    }

    public LambdaAction copy() {
        return new LambdaAction(this);
    }

    public void perform(Rom rom) {
        perform.perform(rom);
    }
}

package redactedrice.ptcgr.compiler;


import java.io.IOException;
import java.util.Arrays;

import redactedrice.compiler.CompilerUtils;
import redactedrice.compiler.instructions.BasicInstruction;
import redactedrice.compiler.instructions.addressref.BlockBankLoadedAddress;
import redactedrice.compiler.instructions.basic.Rst;
import redactedrice.gbcframework.QueuedWriter;
import redactedrice.gbcframework.RomConstants;
import redactedrice.gbcframework.addressing.BankAddress;

// TODO: Does not support labels as its not really benificial. Labels are more about
// new blocks not assigning existing code to a label
public class Bank1Call extends BasicInstruction {
    public static final int SIZE = 3;
    short value;

    public Bank1Call(short bank1Address) {
        super(SIZE);
        validateBank1LoadedAddress(bank1Address);
        this.value = bank1Address;
    }

    private static void validateBank1LoadedAddress(short bank1Address) {
        int address = bank1Address & 0xFFFF;
        int bank1Start = RomConstants.BANK_SIZE;
        int bank1EndExclusive = RomConstants.BANK_SIZE * 2;
        if (address < bank1Start || address >= bank1EndExclusive) {
            throw new IllegalArgumentException("bank1call address must be a bank 1 loaded address "
                    + "between 0x" + Integer.toHexString(bank1Start) + " and 0x"
                    + Integer.toHexString(bank1EndExclusive - 1) + " but was 0x"
                    + Integer.toHexString(address));
        }
    }

    public static Bank1Call create(String[] args) {
        final String SUPPORT_STRING = "Bank1Call only supports (short): Given ";
        if (args.length != 1) {
            throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
        }

        try {
            return new Bank1Call(CompilerUtils.parseShortArg(args[0]));
        } catch (IllegalArgumentException iae) {
            // The instruct doesn't fit - try the next one (if there is one)
            // Could throw here but kept to preserve the pattern being used for
            // the instructs to support more easily adding future ones without
            // forgetting to add the throw at the end
        }

        throw new IllegalArgumentException(SUPPORT_STRING + Arrays.toString(args));
    }

    @Override
    public void writeStaticBytes(QueuedWriter writer) throws IOException {
        // bankcall1 is in RST 18
        Rst.write(writer, (byte) 0x18);
        BlockBankLoadedAddress.write(writer, new BankAddress(value));
    }
}

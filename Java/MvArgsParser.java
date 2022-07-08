package sg.edu.nus.comp.cs4218.impl.parser;

public class MvArgsParser extends ArgsParser {
    private final static char FLAG_IS_NO_OVERWRITE = 'n'; //NOPMD - suppressed LongVariable

    public MvArgsParser() {
        super();
        legalFlags.add(FLAG_IS_NO_OVERWRITE);
    }

    /**
     * Checks if the no overwrite option was provided in the command arguments
     *
     * @return true if and only if the "-n" option was provided
     */
    public boolean isNoOverwrite() {
        return flags.contains(FLAG_IS_NO_OVERWRITE);
    }

    /**
     * Returns the list of files/directory names from the command arguments.
     *
     * @return The list of file/directory names
     */
    public String[] getFileNames() {
        return nonFlagArgs.toArray(new String[0]);
    }
}
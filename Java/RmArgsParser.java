package sg.edu.nus.comp.cs4218.impl.parser;

public class RmArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE = 'r';
    private final static char FLAG_IS_EMPTY_FOLDER = 'd'; //NOPMD - suppressed LongVariable - Uses standard variable naming format for flags

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_EMPTY_FOLDER);
    }

    /**
     * Checks if the recursive option was provided in the command arguments.
     *
     * @return true if and only if the "-r" option was provided
     */
    public boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    /**
     * Checks if the "remove empty directories" option was provided in the command arguments.
     *
     * @return true if and only if the "-d" option was provided
     */
    public Boolean isEmptyFolder() {
        return flags.contains(FLAG_IS_EMPTY_FOLDER);
    }

    /**
     * Returns the list of fileNames from the command arguments.
     *
     * @return The list of fileNames
     */
    public String[] getFileNames() {
        return nonFlagArgs.toArray(new String[0]);
    }
}

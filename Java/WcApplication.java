package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.app.args.WcArguments;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.WcFormatUtils.formatCountResult;
import static sg.edu.nus.comp.cs4218.impl.util.WcFormatUtils.formatValue;

public class WcApplication implements WcInterface {

    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;
    private static final String WC_ERR_HEADER = "wc: ";

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws WcException {
        // Format: wc [-clw] [FILES]
        if (stdout == null) {
            throw new WcException(ERR_NULL_STREAMS);
        }
        WcArguments wcArgs = new WcArguments();
        wcArgs.parse(args);
        String result;

        try {
            if (wcArgs.getFiles().isEmpty()) {
                result = countFromStdin(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), stdin);
            } else if (wcArgs.hasFilesDash()) {
                result = countFromFileAndStdin(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), stdin, wcArgs.getFiles().toArray(new String[0]));
            } else {
                result = countFromFiles(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), wcArgs.getFiles().toArray(new String[0]));
            }
        } catch (Exception e) {
            // Will never happen
            throw new WcException(e, ERR_GENERAL); //NOPMD
        }
        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new WcException(e, ERR_WRITE_STREAM);//NOPMD
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param fileName Array of String of file names
     * @throws Exception
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords, //NOPMD
                                 String... fileName) throws Exception {
        if (fileName == null) {
            throw new Exception(ERR_GENERAL);
        }
        List<String> result = new ArrayList<>();
        long[] totalCount = new long[3];
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();

            if (hasFileErr(result, node)) {
                continue;
            }

            long[] count;
            try (InputStream input = IOUtils.openInputStream(file)) {
                count = getCountReport(input); // lines words bytes
            }
            // Update total count
            updateTotalCount(totalCount, count);

            // Format all output: " %7d %7d %7d %s"
            // Output in the following order: lines words bytes filename
            StringBuilder sb = formatCountResult(isBytes, isLines, isWords, count); //NOPMD - suppressed ShortVariable - unnecessary variable name is clear
            sb.append(formatValue(file));
            result.add(sb.toString());
        }

        // Print cumulative counts for all the files
        if (fileName.length > 1) {
            StringBuilder sb = formatCountResult(isBytes, isLines, isWords, totalCount); //NOPMD - suppressed ShortVariable - unnecessary variable name is clear
            sb.append(formatValue("total"));
            result.add(sb.toString());
        }
        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Returns Boolean value of whether given file/files has errors, and adds error to results
     *
     * @param result List of strings forming result
     * @param node   File object of file to be counted
     */
    private boolean hasFileErr(List<String> result, File node) {
        if (!node.exists()) {
            result.add(WC_ERR_HEADER + ERR_FILE_NOT_FOUND);
            return true;
        }
        if (node.isDirectory()) {
            result.add(WC_ERR_HEADER + ERR_IS_DIR);
            return true;
        }
        if (!node.canRead()) {
            result.add(WC_ERR_HEADER + ERR_NO_PERM);
            return true;
        }
        return false;
    }


    private void updateTotalCount(long[] totalCount, long[] currentCount) { //NOPMD - suppressed UseVarargs - need both long arrs
        totalCount[LINES_INDEX] += currentCount[LINES_INDEX];
        totalCount[WORDS_INDEX] += currentCount[WORDS_INDEX];
        totalCount[BYTES_INDEX] += currentCount[BYTES_INDEX];
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @throws Exception
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        long[] count = getCountReport(stdin); // lines words bytes;

        StringBuilder sb = formatCountResult(isBytes, isLines, isWords, count); //NOPMD - suppressed ShortVariable - unnecessary variable name is clear

        return sb.toString();
    }

    @Override
    public String countFromFileAndStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                        InputStream stdin, String... fileName) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        if (fileName == null) {
            throw new Exception(ERR_GENERAL);
        }

        long[] totalCount = new long[3];
        List<String> result = new ArrayList<>();
        boolean isStdinClosed = false;
        for (String file : fileName) {
            long[] count;
            if ("-".equals(file)) {
                if (isStdinClosed) {
                    result.add("wc: " + ERR_STREAM_CLOSED);
                    continue;
                }
                file = "";
                count = getCountReport(stdin);
                isStdinClosed = true;
            } else {
                File node = IOUtils.resolveFilePath(file).toFile();

                if (hasFileErr(result, node)) {
                    continue;
                }
                try (InputStream input = IOUtils.openInputStream(file)) {
                    count = getCountReport(input); // lines words bytes
                }
            }
            updateTotalCount(totalCount, count);
            StringBuilder sb = formatCountResult(isBytes, isLines, isWords, count); //NOPMD - suppressed ShortVariable - unnecessary variable name is clear
            sb.append(formatValue(file));
            result.add(sb.toString());
        }

        if (fileName.length > 1) {
            StringBuilder sb = formatCountResult(isBytes, isLines, isWords, totalCount); //NOPMD - suppressed ShortVariable - unnecessary variable name is clear
            sb.append(formatValue("total"));
            result.add(sb.toString());
        }

        return String.join(STRING_NEWLINE, result);
    }


    /**
     * Returns array containing the number of lines, words, and bytes based on data in InputStream.
     *
     * @param input An InputStream
     * @throws IOException
     */
    public long[] getCountReport(InputStream input) throws Exception {
        if (input == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        long[] result = new long[3]; // lines, words, bytes

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int inRead = 0;
        boolean inWord = false;
        while ((inRead = input.read(data, 0, data.length)) != -1) {
            for (int i = 0; i < inRead; ++i) {
                if (Character.isWhitespace(data[i])) {
                    // Use <newline> character here. (Ref: UNIX)
                    if (data[i] == '\n') {
                        ++result[LINES_INDEX];
                    }
                    if (inWord) {
                        ++result[WORDS_INDEX];
                    }

                    inWord = false;
                } else {
                    inWord = true;
                }
            }
            result[BYTES_INDEX] += inRead;
            buffer.write(data, 0, inRead);
        }
        buffer.flush();
        if (inWord) {
            ++result[WORDS_INDEX]; // To handle last word
        }

        return result;
    }
}

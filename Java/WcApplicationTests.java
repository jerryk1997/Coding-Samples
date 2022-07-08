package sg.edu.nus.comp.cs4218.impl.app;
//Assumption:
// only can have one stdin input for countFromFilesAndStdin
// Will never have all false values for flags


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.tests.mocks.MockByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.tests.util.TestFileUtil.createDirectories;
import static sg.edu.nus.comp.cs4218.tests.util.TestFileUtil.createFile;
import static sg.edu.nus.comp.cs4218.tests.util.WcStringConstants.*;

public class WcApplicationTests {
    WcApplication wcApp;
    private static final Charset CHARACTER_SET = StandardCharsets.UTF_8;
    private static final String PROVIDE_STDIN_ARGS = "provideStdinArgs";
    private static final String PROVIDE_BOOLEAN_ARGS = "provideBooleanCombinations";

    @TempDir
    Path workingDirectory;

    String origDirectory;

    @BeforeEach
    void setUp() throws IOException {
        wcApp = new WcApplication();
        origDirectory = Environment.currentDirectory;
        Environment.currentDirectory = workingDirectory.toString();
        initTestFiles();
    }

    @AfterEach
    void tearDown() {
        Environment.currentDirectory = origDirectory;
    }

    InputStream mockStdin(String input) {
        if (input == null) {
            return InputStream.nullInputStream();
        }
        return new ByteArrayInputStream(input.getBytes(CHARACTER_SET));
    }

    private String errMessageConstructor(String message) {
        return "wc: " + message;
    }

    private String getAnsFor(String input) {
        String ans = "";
        switch (input) {
            case SINGLE_LINE:
            case SINGLE_LINE_FILENAME:
                ans = SINGLE_LINE_ANS;
                break;
            case SINGLE_LINE_WITH_NEWLINE:
            case SINGLE_LINE_WITH_NEWLINE_FILENAME:
                ans = SINGLE_LINE_WITH_NEWLINE_ANS;
                break;

            case MULTI_NEWLINES:
            case MULTI_NEWLINES_FILENAME:
                ans = MULTI_NEWLINES_ANS;
                break;

            case SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS:
            case SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME:
                ans = SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_ANS;
                break;

            case MULTI_LINE:
            case MULTI_LINE_FILENAME:
                ans = MULTI_LINE_ANS;
                break;

            case MULTI_LINE_SPECIAL_CHARS:
            case MULTI_LINE_SPECIAL_CHARS_FILENAME:
                ans = MULTI_LINE_SPECIAL_CHARS_ANS;
                break;

            default:
                ans = "";
                break;
        }
        return ans;
    }

    private boolean checkAnsFor(String input, String result) {
        String ans;
        switch (input) {
            case NON_EXISTING_FILE:
                ans = ERR_FILE_NOT_FOUND_RESULT;
                return result.equals(ans);
            case DIRECTORY:
                ans = ERR_IS_DIR_RESULT;
                return result.equals(ans);

            case NO_PERMS_FILE:
                ans = ERR_NO_PERM_RESULT;
                return result.equals(ans);
            default:
                return false;
        }
    }

    MockByteArrayOutputStream mockStdout() {
        return new MockByteArrayOutputStream();
    }

    boolean checkAns(boolean isBytes, boolean isLines, boolean isWords, boolean isFile, boolean isRun, String input, String result) {
        String ans = getAnsFor(input);
        if (ans.isEmpty()) {
            return checkAnsFor(input, result);
        }

        ArrayList<String> ansList = new ArrayList<String>(Arrays.asList(ans.trim().split("\\s+")));
        ArrayList<String> resultList = new ArrayList<String>(Arrays.asList(result.trim().split("\\s+")));

        int offSet = 0;
        if (!isLines) {
            ansList.remove(0);
            offSet++;
        }

        if (!isWords) {
            ansList.remove(1 - offSet);
            offSet++;
        }

        if (!isBytes) {
            ansList.remove(2 - offSet);
            offSet++;
        }

        if (!isFile && resultList.size() != ansList.size() - 1) {
            return false;
        }
        StringBuilder sb = new StringBuilder(); //NOPMD - suppressed ShortVariable - unnecessary variable name is clear
        for (int i = 0; i < 3 - offSet; i++) {
            sb.append(String.format("\t%d", Integer.parseInt(ansList.get(i))));
        }
        if (isFile) {
            sb.append('\t').append(ansList.get(ansList.size() - 1));
        }
        if (isRun) {
            sb.append('\n');
        }
        return sb.toString().equals(result);
    }

    boolean checkAnsMulti(boolean isBytes, boolean isLines, boolean isWords, String fileNames, String result) {
        String[] fileNameArr = fileNames.split(" ");
        String[] resultArr = result.split("\n");
        for (int i = 0; i < resultArr.length - 1; i++) {
            resultArr[i] = resultArr[i] + "\n";
        }

        if (resultArr.length - 1 != fileNameArr.length) {
            return false;
        }

        for (int i = 0; i < resultArr.length - 1; i++) {
            boolean isFile = true;
            String file = fileNameArr[i];
            if ("-".equals(file)) {
                isFile = false;
            }
            boolean isCorrect = checkAns(isBytes, isLines, isWords, isFile, true, file, resultArr[i]);
            if (!isCorrect) {
                return false;
            }
        }

        return true;
    }

    void initTestFiles() {
        List<String[]> fileNameContentPair = List.of( //NOPMD - suppressed LongVariable - descriptive
                new String[]{SINGLE_LINE_FILENAME, SINGLE_LINE},
                new String[]{SINGLE_LINE_WITH_NEWLINE_FILENAME, SINGLE_LINE_WITH_NEWLINE},
                new String[]{MULTI_NEWLINES_FILENAME, MULTI_NEWLINES},
                new String[]{SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME, SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS},
                new String[]{MULTI_LINE_FILENAME, MULTI_LINE},
                new String[]{MULTI_LINE_SPECIAL_CHARS_FILENAME, MULTI_LINE_SPECIAL_CHARS}
        );

        for (String[] pair : fileNameContentPair) {
            createFile(workingDirectory, pair[0], pair[1]);
        }
        createDirectories(workingDirectory, Paths.get(DIRECTORY));
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_WRITE);
        createFile(workingDirectory, NO_PERMS_FILE, perms);
    }

    static Stream<Arguments> provideBooleanCombinations() {
        final Boolean[] options = {true, false};
        final ArrayList<Arguments> args = new ArrayList<>();
        for (final Boolean option1 : options) {
            for (final Boolean option2 : options) {
                for (final Boolean option3 : options) {
                    if (!option1 && !option2 && !option3) {
                        continue;
                    }
                    args.add(Arguments.of(option1, option2, option3));
                }
            }
        }
        return args.stream();
    }

    static Stream<Arguments> provideStdinArgs() {
        final ArrayList<Arguments> args = new ArrayList<>();
        final String[] inputs = {
                SINGLE_LINE,
                SINGLE_LINE_WITH_NEWLINE,
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS,
                MULTI_NEWLINES,
                MULTI_LINE,
                MULTI_LINE_SPECIAL_CHARS
        };
        for (final String input : inputs) {
            args.add(Arguments.of(input));
        }
        return args.stream();
    }

    @Test
    void run_NullStdout_ThrowsWcException() throws IOException {
        try (InputStream stdin = mockStdin("CS4218")) {
            Exception exception = assertThrows(WcException.class, () -> {
                wcApp.run(new String[]{"-l"}, stdin, null);
            });
            assertEquals(errMessageConstructor(ERR_NULL_STREAMS), exception.getMessage());
        }
    }

    @Test
    void run_EmptyArgsNullStdin_ThrowsWcException() {
        Exception exception = assertThrows(WcException.class, () -> {
            wcApp.run(null, null, mockStdout());
        });
        assertEquals(errMessageConstructor(ERR_GENERAL), exception.getMessage());
    }

    @Test
    void run_WithRelativePaths_HasCount() throws Exception {
        try (ByteArrayOutputStream stdout = mockStdout()) {
            String[] args = {SINGLE_LINE_FILENAME};
            wcApp.run(args, null, stdout);
            String ans = String.format("\t%d\t%d\t%d\t%s\n", 0, 6, 33, SINGLE_LINE_FILENAME); // NOPMD - Suppress AvoidDuplicateLiterals
            assertEquals(ans, stdout.toString());
        }
    }

    @Test
    void run_WithAbsPaths_HasCount() throws Exception {
        try (ByteArrayOutputStream stdout = mockStdout()) {
            String absPathString = workingDirectory.resolve(SINGLE_LINE_FILENAME).toString();
            String[] args = {absPathString};
            wcApp.run(args, null, stdout);
            String ans = String.format("\t%d\t%d\t%d\t%s\n", 0, 6, 33, absPathString);
            assertEquals(ans, stdout.toString());
        }
    }

    @Test
    void run_WithRelativeAndAbsPaths_HasCount() throws Exception {
        try (ByteArrayOutputStream stdout = mockStdout()) {
            String absPathString = workingDirectory.resolve(SINGLE_LINE_FILENAME).toString();
            String[] args = {SINGLE_LINE_FILENAME, absPathString};
            wcApp.run(args, null, stdout);
            String ans = String.format("\t%d\t%d\t%d\t%s\n", 0, 6, 33, SINGLE_LINE_FILENAME) +
                    String.format("\t%d\t%d\t%d\t%s\n", 0, 6, 33, absPathString) +
                    String.format("\t%d\t%d\t%d\t%s\n", 0, 12, 66, "total");
            assertEquals(ans, stdout.toString());
        }
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_STDIN_ARGS)
    void run_NoFlagsStdinInput_HasAllCount(String input) throws Exception {
        try (InputStream stdin = mockStdin(input);
             ByteArrayOutputStream stdout = mockStdout()) {
            wcApp.run(null, stdin, stdout);
            assertTrue(checkAns(true, true, true, false, true, input, stdout.toString()));
        }
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_STDIN_ARGS)
    void run_OneFlagStdinInput_OnlyHasByteCount(String input) throws Exception {
        try (InputStream stdin = mockStdin(input);
             ByteArrayOutputStream stdout = mockStdout()) {
            String[] flags = {"-c"};
            wcApp.run(flags, stdin, stdout);
            assertTrue(checkAns(true, false, false, false, true, input, stdout.toString()));
        }
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_STDIN_ARGS)
    void run_MultipleFlagStdinInput_HasWordAndLineCountOnly(String input) throws Exception {
        try (InputStream stdin = mockStdin(input);
             ByteArrayOutputStream stdout = mockStdout()) {
            String[] flags = {"-wl"};
            wcApp.run(flags, stdin, stdout);
            assertTrue(checkAns(false, true, true, false, true, input, stdout.toString()));
        }
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_STDIN_ARGS)
    void run_MultipleSeparateFlagsStdinInput_SeparateFlagsParsedCorrectly(String input) throws Exception {
        try (InputStream stdin = mockStdin(input);
             ByteArrayOutputStream stdout = mockStdout()) {
            String[] flags = {"-c", "-w"};
            wcApp.run(flags, stdin, stdout);
            assertTrue(checkAns(true, false, true, false, true, input, stdout.toString()));
        }
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_STDIN_ARGS)
    void run_InvalidFlagStdin_ThrowsWcException() throws IOException {
        try (InputStream stdin = mockStdin(MULTI_LINE);
             ByteArrayOutputStream stdout = mockStdout()) {
            String[] flags = {"-x"};
            Exception exception = assertThrows(WcException.class, () -> {
                wcApp.run(flags, stdin, stdout);
            });
            assertEquals(errMessageConstructor(ERR_INVALID_FLAG), exception.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_STDIN_ARGS)
    void run_ValidAndInvalidFlagStdin_ThrowsWcException() throws IOException {
        try (InputStream stdin = mockStdin(MULTI_LINE);
             ByteArrayOutputStream stdout = mockStdout()) {
            String[] flags = {"-wx"};
            Exception exception = assertThrows(WcException.class, () -> {
                wcApp.run(flags, stdin, stdout);
            });
            assertEquals(errMessageConstructor(ERR_INVALID_FLAG), exception.getMessage());
        }
    }

    @Test
    void run_MultipleSeparateFlagsFileInput_MultipleFlagsParsedCorrectlyWithFile() throws Exception {
        try (ByteArrayOutputStream stdout = mockStdout()) {
            String[] args = {"-c", "-l", MULTI_NEWLINES_FILENAME};
            wcApp.run(args, null, stdout);
            assertTrue(checkAns(true, true, false, true, true, MULTI_NEWLINES_FILENAME, stdout.toString()));
        }
    }

    @Test
    void run_MultipleFlagsMultipleFiles_AllFileCountsCorrectWithoutByteCount() throws Exception {
        try (ByteArrayOutputStream stdout = mockStdout()) {
            String[] args = {"-wl", SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME, MULTI_LINE_SPECIAL_CHARS_FILENAME, SINGLE_LINE_FILENAME};
            String fileNames = SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME + " " +
                    MULTI_LINE_SPECIAL_CHARS_FILENAME + " " +
                    SINGLE_LINE_FILENAME;
            wcApp.run(args, null, stdout);
            assertTrue(checkAnsMulti(false, true, true, fileNames, stdout.toString()));
        }
    }

    @Test
    void run_MultipleFlagsMultipleFiles_TotalCountCorrectWithoutByteCount() throws Exception {
        try (ByteArrayOutputStream stdout = mockStdout()) {
            String[] args = {"-wl", SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME, MULTI_LINE_SPECIAL_CHARS_FILENAME, SINGLE_LINE_FILENAME};
            String fileNames = SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME + " " +
                    MULTI_LINE_SPECIAL_CHARS_FILENAME + " " +
                    SINGLE_LINE_FILENAME;
            wcApp.run(args, null, stdout);
            String[] resultArr = stdout.toString().split("\n");
            String totalCount = resultArr[resultArr.length - 1] + "\n";
            String totalCountAns = String.format("\t%d\t%d\ttotal\n", 11, 136);
            assertEquals(totalCount, totalCountAns);
        }
    }

    @Test
    void run_NoFlagsMultipleFilesAndStdin_StdinIncludedCorrectly() throws Exception {
        try (InputStream stdin = mockStdin(MULTI_LINE_SPECIAL_CHARS);
             ByteArrayOutputStream stdout = mockStdout()) {
            String[] args = {SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME, "-", SINGLE_LINE_FILENAME};
            wcApp.run(args, stdin, stdout);
            String ans = SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_ANS + "\n" +
                    String.format("\t%d\t%d\t%d", 10, 128, 712) + "\n" +
                    SINGLE_LINE_ANS + "\n" +
                    String.format("\t%d\t%d\t%d\ttotal\n", 11, 136, 758);
            assertEquals(ans, stdout.toString());
        }
    }


    @Test
    void run_MultipleFlagsMultipleFilesAndStdin_StdinIncludedCorrectly() throws Exception {
        try (InputStream stdin = mockStdin(MULTI_NEWLINES);
             ByteArrayOutputStream stdout = mockStdout()) {
            String[] args = {"-c", "-l", "-", MULTI_LINE_FILENAME, MULTI_LINE_SPECIAL_CHARS_FILENAME};

            wcApp.run(args, stdin, stdout);
            String ans = String.format("\t%d\t%d", 14, 14) + "\n" +
                    String.format("\t%d\t%d\tmulti_line", 8, 565) + "\n" +
                    String.format("\t%d\t%d\tmulti_line_special_chars", 10, 712) + "\n" +
                    String.format("\t%d\t%d\ttotal\n", 32, 1291);
            assertEquals(ans, stdout.toString());
        }
    }

    @Test
    void countFromFile_NullFileNames_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            wcApp.countFromFiles(true, true, true, null);
        });
        assertEquals(ERR_GENERAL, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_BOOLEAN_ARGS)
    void countFromFiles_AllValidFlagsSingleFile_CorrectFileCountsIncluded(boolean isBytes, boolean isLines, boolean isWords) throws Exception {
        String[] fileNamesArr = {MULTI_LINE_FILENAME};
        String fileNames = MULTI_LINE_FILENAME;
        String result = wcApp.countFromFiles(isBytes, isLines, isWords, fileNamesArr);
        assertTrue(checkAns(isBytes, isLines, isWords, true, false, fileNames, result));
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_BOOLEAN_ARGS)
    void countFromFiles_AllValidFlagsMultiFile_AllFileCountsCorrectlyIncluded(boolean isBytes, boolean isLines, boolean isWords) throws Exception {
        String fileNames = SINGLE_LINE_FILENAME + " " +
                MULTI_NEWLINES_FILENAME + " " +
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME + " " +
                MULTI_LINE_SPECIAL_CHARS_FILENAME;
        String[] fileNamesArr = fileNames.split(" ");
        String result = wcApp.countFromFiles(isBytes, isLines, isWords, fileNamesArr);
        assertTrue(checkAnsMulti(isBytes, isLines, isWords, fileNames, result));
    }

    @Test
    void countFromFiles_MultiFile_TotalCountCorrect() throws Exception {
        String fileNames = SINGLE_LINE_FILENAME + " " +
                MULTI_LINE_FILENAME + " " +
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME + " " +
                MULTI_LINE_SPECIAL_CHARS_FILENAME;
        String[] fileNamesArr = fileNames.split(" ");
        String result = wcApp.countFromFiles(true, true, true, fileNamesArr);
        String[] resultArr = result.split("\n");
        String ans = String.format("\t%d\t%d\t%d\ttotal\n", 19, 236, 1323);
        String totalCount = resultArr[resultArr.length - 1] + "\n";
        assertEquals(ans, totalCount);
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_BOOLEAN_ARGS)
    void countFromFiles_AllValidFlagsMultiFileMissingFile_ErrMessageDisplayedOtherOutputCorrect(boolean isBytes, boolean isLines, boolean isWords) throws Exception {
        String fileNames = SINGLE_LINE_FILENAME + " " +
                MULTI_NEWLINES_FILENAME + " " +
                NON_EXISTING_FILE + " " +
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME + " " +
                MULTI_LINE_SPECIAL_CHARS_FILENAME;
        String[] fileNamesArr = fileNames.split(" ");
        String result = wcApp.countFromFiles(isBytes, isLines, isWords, fileNamesArr);
        assertTrue(checkAnsMulti(isBytes, isLines, isWords, fileNames, result));
    }

    @Test
    void countFromFiles_MultiFileHasDir_ErrMessageDisplayedOtherOutputCorrect() throws Exception {
        String fileNames = SINGLE_LINE_FILENAME + " " +
                MULTI_LINE_FILENAME + " " +
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME + " " +
                MULTI_LINE_SPECIAL_CHARS_FILENAME + " " +
                DIRECTORY;
        String[] fileNamesArr = fileNames.split(" ");
        String result = wcApp.countFromFiles(true, true, true, fileNamesArr);
        String[] resultArr = result.split("\n");
        String ans = String.format("\t%d\t%d\t%d\ttotal\n", 19, 236, 1323);
        String totalCount = resultArr[resultArr.length - 1] + "\n";
        assertEquals(ans, totalCount);
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_BOOLEAN_ARGS)
    void countFromFiles_AllFlagsMultiFileHasNoPerm_ErrMessageDisplayedOtherOutputCorrect(boolean isBytes, boolean isWords, boolean isLines) throws Exception {
        String fileNames = NO_PERMS_FILE + " " +
                SINGLE_LINE_FILENAME + " " +
                MULTI_LINE_FILENAME + " " +
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME + " " +
                MULTI_LINE_SPECIAL_CHARS_FILENAME;
        String[] fileNamesArr = fileNames.split(" ");
        String result = wcApp.countFromFiles(isBytes, isLines, isWords, fileNamesArr);
        assertTrue(checkAnsMulti(isBytes, isLines, isWords, fileNames, result));
    }

    @Test
    void countFromFiles_AllFileErrs_NormalExecution() throws Exception {
        String fileNames = NON_EXISTING_FILE + " " +
                DIRECTORY + " " +
                NO_PERMS_FILE;
        String[] fileNamesArr = fileNames.split(" ");
        String result = wcApp.countFromFiles(true, true, true, fileNamesArr);
        String ans = ERR_FILE_NOT_FOUND_RESULT +
                ERR_IS_DIR_RESULT +
                ERR_NO_PERM_RESULT +
                String.format("\t%d\t%d\t%d\ttotal", 0, 0, 0);
        assertEquals(ans, result);
    }

    @Test
    void countFromStdin_NullStdin_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            wcApp.countFromStdin(true, true, true, null);
        });
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_BOOLEAN_ARGS)
    void countFromStdin_AllValidFlags_AllCountsCorrectlyIncluded(boolean isBytes, boolean isLines, boolean isWords) throws Exception {
        try (InputStream stdin = mockStdin(MULTI_LINE_SPECIAL_CHARS)) {
            String result = wcApp.countFromStdin(isBytes, isLines, isWords, stdin);
            assertTrue(checkAns(isBytes, isLines, isWords, false, false, MULTI_LINE_SPECIAL_CHARS, result));
        }
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_STDIN_ARGS)
    void countFromStdin_NormalInput_NormalOutput(String input) throws Exception {
        try (InputStream stdin = mockStdin(input)) {
            String result = wcApp.countFromStdin(true, true, true, stdin);
            assertTrue(checkAns(true, true, true, false, false, input, result));
        }
    }

    @Test
    void countFromFileAndStdin_NullStdin_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            String[] fileNames = {SINGLE_LINE_FILENAME};
            wcApp.countFromFileAndStdin(true, true, true, null, fileNames);
        });

        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    @Test
    void countFromFileAndStdin_NullFiles_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            InputStream stdin = mockStdin("stdin");
            wcApp.countFromFileAndStdin(true, true, true, stdin, null);
        });

        assertEquals(ERR_GENERAL, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource(PROVIDE_BOOLEAN_ARGS)
    void countFromFileAndStdin_AllFlagsMultipleFileAndStdin_AllCountsCorrectlyIncludedWithStdin(boolean isBytes, boolean isLines, boolean isWords) throws Exception {
        String[] fileNames = {
                SINGLE_LINE_FILENAME,
                MULTI_LINE_FILENAME,
                MULTI_NEWLINES_FILENAME,
                MULTI_LINE_SPECIAL_CHARS_FILENAME,
                "-"
        };
        String fileNameString = SINGLE_LINE_FILENAME + " " +
                MULTI_LINE_FILENAME + " " +
                MULTI_NEWLINES_FILENAME + " " +
                MULTI_LINE_SPECIAL_CHARS_FILENAME + " " +
                SINGLE_LINE_WITH_NEWLINE_FILENAME;

        try (InputStream stdin = mockStdin(SINGLE_LINE_WITH_NEWLINE)) {
            String result = wcApp.countFromFileAndStdin(isBytes, isLines, isWords, stdin, fileNames);
            String[] resultArr = result.split("\n");
            resultArr[4] = resultArr[4] + "\t" + SINGLE_LINE_WITH_NEWLINE_FILENAME;
            result = String.join("\n", resultArr);
            assertTrue(checkAnsMulti(isBytes, isLines, isWords, fileNameString, result));
        }
    }

    @Test
    void countFromFilesAndStdin_MultipleFlagsMultipleFilesErrFilesAndStdin_ErrMessageCorrectlyShown() throws Exception {
        String[] fileNames = {NON_EXISTING_FILE,
                SINGLE_LINE_FILENAME,
                SINGLE_LINE_WITH_NEWLINE_FILENAME,
                DIRECTORY,
                MULTI_NEWLINES_FILENAME,
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME,
                "-",
                MULTI_LINE_SPECIAL_CHARS_FILENAME,
                NO_PERMS_FILE
        };

        String ans = ERR_FILE_NOT_FOUND_RESULT +
                SINGLE_LINE_ANS + "\n" +
                SINGLE_LINE_WITH_NEWLINE_ANS + "\n" +
                ERR_IS_DIR_RESULT +
                MULTI_NEWLINES_ANS + "\n" +
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_ANS + "\n" +
                String.format("\t%d\t%d\t%d\n", 8, 100, 565) +
                MULTI_LINE_SPECIAL_CHARS_ANS + "\n" +
                ERR_NO_PERM_RESULT +
                String.format("\t%d\t%d\t%d\ttotal", 34, 242, 1371);
        try (InputStream stdin = mockStdin(MULTI_LINE)) {
            String result = wcApp.countFromFileAndStdin(true, true, true, stdin, fileNames);
            assertEquals(ans, result);
        }
    }

    @Test
    void countFromFilesAndStdin_MultipleStdin_NoErrorsOnlyCountsFirst() throws Exception {
        String[] fileNames = {NON_EXISTING_FILE,
                SINGLE_LINE_FILENAME,
                "-",
                DIRECTORY,
                MULTI_NEWLINES_FILENAME,
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_FILENAME,
                "-",
                MULTI_LINE_SPECIAL_CHARS_FILENAME,
                NO_PERMS_FILE
        };

        String ans = ERR_FILE_NOT_FOUND_RESULT +
                SINGLE_LINE_ANS + "\n" +
                String.format("\t%d\t%d\t%d\n", 1, 6, 34) +
                ERR_IS_DIR_RESULT +
                MULTI_NEWLINES_ANS + "\n" +
                SINGLE_LINE_WITH_NEWLINE_TRAILING_WORDS_ANS + "\n" +
                errMessageConstructor(ERR_STREAM_CLOSED) + "\n" +
                MULTI_LINE_SPECIAL_CHARS_ANS + "\n" +
                ERR_NO_PERM_RESULT +
                String.format("\t%d\t%d\t%d\ttotal", 26, 142, 806);
        try (InputStream stdin = mockStdin(SINGLE_LINE_WITH_NEWLINE)) {
            String result = wcApp.countFromFileAndStdin(true, true, true, stdin, fileNames);
            assertEquals(ans, result);
        }
    }
}

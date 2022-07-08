package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sg.edu.nus.comp.cs4218.exception.EchoException;
import sg.edu.nus.comp.cs4218.tests.mocks.MockByteArrayOutputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

/*
Duplicate literals are just the same strings used for different tests.
*/
@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
public class EchoApplicationTest {
    private final static String EMPTY_STRING = "\n";
    private final static String SINGLE_WORD = "CS4218\n";
    private final static String MULTI_WORD = "CS4218 is the best mod\n";
    private final static String MULTI_WORD_SYMBOLS = "CS4218 is@the$best*mod\n";
    private final static String MULTI_SYMBOLS = "\u00D2 \u00D7 \u00E6\n";

    private final static String[] NULL_ARGS = null;
    private final static String[] EMPTY_ARGS = {""};
    private final static String[] SINGLE_WORD_ARG = {"CS4218"};
    private final static String[] MULTI_WORD_ARG = {"CS4218", "is", "the", "best", "mod"};
    private final static String[] MULTI_WORD_SYMBOLS_ARG = {"CS4218", "is@the$best*mod"};
    private final static String[] MULTI_SYMBOLS_ARG = {"\u00D2", "\u00D7", "\u00E6"};

    private final static String ERR_NULL_ARGS_MSG = "echo: " + ERR_NULL_ARGS;
    private final static String ERR_NO_OSTREAM_MSG = "echo: " + ERR_NO_OSTREAM;

    final static Charset CHARACTER_SET = StandardCharsets.UTF_8;

    private static EchoApplication echo;
    private static InputStream stdin;
    private static OutputStream stdout;

    private static InputStream mockStdin(String input) {
        if (input == null) {
            return InputStream.nullInputStream();
        }
        return new ByteArrayInputStream(input.getBytes(CHARACTER_SET));
    }

    MockByteArrayOutputStream mockStdout() {
        return new MockByteArrayOutputStream();
    }

    @BeforeEach
    void setUp() {
        echo = new EchoApplication();
    }

    boolean checkAns(String[] args, String result) {
        if (args.equals(EMPTY_ARGS)) {
            return result.equals(EMPTY_STRING);
        } else if (args.equals(SINGLE_WORD_ARG)) {
            return result.equals(SINGLE_WORD);
        } else if (args.equals(MULTI_WORD_ARG)) {
            return result.equals(MULTI_WORD);
        } else if (args.equals(MULTI_WORD_SYMBOLS_ARG)) {
            return result.equals(MULTI_WORD_SYMBOLS);
        } else if (args.equals(MULTI_SYMBOLS_ARG)) {
            return result.equals(MULTI_SYMBOLS);
        } else {
            return false;
        }
    }

    private static Stream<Arguments> provideArgs() {
        final String[][] args = {
                EMPTY_ARGS,
                SINGLE_WORD_ARG,
                MULTI_WORD_ARG,
                MULTI_WORD_SYMBOLS_ARG,
                MULTI_SYMBOLS_ARG
        };
        final ArrayList<Arguments> arguments = new ArrayList<>(args.length);
        for (final String[] arg : args) {
            arguments.add(Arguments.of((Object) arg));
        }
        return arguments.stream();
    }

    @Test
    void constructResult_NullArgs_ThrowsEchoException() {
        Exception exception = assertThrows(EchoException.class, () -> {
            echo.constructResult(NULL_ARGS);
        });
        assertEquals(ERR_NULL_ARGS_MSG, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideArgs")
    void constructResult_ValidArgs_ReturnConstructedResults(String... args) throws EchoException {
        String result = echo.constructResult(args);
        assertTrue(checkAns(args, result));
    }

    @Test
    void run_NullStdout_ThrowsEchoException() throws IOException {
        try (InputStream stdin = mockStdin("CS4218")) {
            Exception exception = assertThrows(EchoException.class, () -> {
                echo.run(EMPTY_ARGS, stdin, null);
            });

            assertEquals(ERR_NO_OSTREAM_MSG, exception.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("provideArgs")
    void run_NullStdinValidStdout_CorrectOutputWrittenToOutputStream(String... args) throws Exception {
        ByteArrayOutputStream stdout = mockStdout();
        echo.run(args, null, stdout);

        assertTrue(checkAns(args, stdout.toString(CHARACTER_SET)));

        stdout.close();
    }

    @ParameterizedTest
    @MethodSource("provideArgs")
    void run_ValidStdinValidStdout_CorrectOutputWrittenToOutputStream(String... args) throws Exception {

        try (InputStream stdin = mockStdin("CS4218"); ByteArrayOutputStream stdout = mockStdout()) {
            echo.run(args, stdin, stdout);
            assertTrue(checkAns(args, stdout.toString(CHARACTER_SET)));
        }
    }

    @Test
    void run_NullArgs_ThrowsEchoException() throws IOException {
        try (OutputStream stdout = mockStdout()) {
            Exception exception = assertThrows(EchoException.class, () -> {
                echo.run(NULL_ARGS, null, stdout);
            });

            assertEquals(ERR_NULL_ARGS_MSG, exception.getMessage());
        }
    }
}

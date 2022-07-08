package sg.edu.nus.comp.cs4218.tests.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.tests.util.AssertionUtil;
import sg.edu.nus.comp.cs4218.tests.util.TestFileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IORedirCmdSubstitutionIT {
    private final static String INPUT_FILE = "input.txt";
    private final static String INPUT_CONTENT = "hello world!!";
    private final static String OUTPUT_FILE = "output.txt"; // file used to test if output file is overwritten
    private final static String OUTPUT_CONTENT = "bye world!!";

    @TempDir
    static Path workingDirectory;

    static String origDirectory;

    @BeforeAll
    static void beforeAll() {
        origDirectory = Environment.currentDirectory;
        Environment.currentDirectory = workingDirectory.toString();
    }

    @AfterAll
    static void afterAll() {
        Environment.currentDirectory = origDirectory;
    }

    private void initFiles() {
        Path dir = Paths.get(Environment.currentDirectory);
        TestFileUtil.createFile(dir, INPUT_FILE, INPUT_CONTENT);
        TestFileUtil.createFile(dir, OUTPUT_FILE, OUTPUT_CONTENT);
    }

    @BeforeEach
    void setUp() {
        Environment.currentDirectory = workingDirectory.toString();
        initFiles();
    }

    @AfterEach
    void tearDown() {
        Environment.currentDirectory = origDirectory;
    }

    @Test
    void ioRedirCmdSub_CmdSubInputRedirFromCmdSub_Success() {
        String commandString = "`echo cat` < `echo input.txt`";
        String expected = INPUT_CONTENT + "\n";
        AssertionUtil.assertCommandEvaluatedMatches(expected, commandString);
    }

    @Test
    void ioRedirCmdSub_CmdSubOutputRedirToCmdSub_Success() throws IOException {
        String commandString = "cat `echo " + INPUT_FILE + "` > `echo output.txt`";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }

    @Test
    void ioRedirCmdSub_CmdSubInputRedirFromCmdSubOutputRedirToCmdSub_Success() throws IOException {
        String commandString = "`echo cat` < `echo input.txt` > `echo output.txt`";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }

    @Test
    void ioRedirCmdSub_CmbSubHasInputRedir_Success() throws IOException {
        String commandString = "echo \"`cat < input.txt`\" > output.txt";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }

    @Test
    void ioRedirCmdSub_CmbSubHasOutputRedir_Success() throws IOException {
        String commandString = "`cat input.txt > output.txt`";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }

    @Test
    void ioRedirCmdSub_CmbSubHasInputRedirError_ThrowsException() {
        String commandString = "echo \"`cat < non_exist.txt`\" > output.txt";
        AssertionUtil.assertShellException(commandString);
    }
}
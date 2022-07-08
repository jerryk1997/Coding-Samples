package sg.edu.nus.comp.cs4218.tests.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.tests.util.AssertionUtil;
import sg.edu.nus.comp.cs4218.tests.util.TestFileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IORedirSeqCmdIT {
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
        initFiles();
    }

    @AfterEach
    void tearDown() {
        TestFileUtil.purgeDirectory(workingDirectory);
    }

    @Test
    void ioRedirSeq_IORedCommandWithSeq_BothCommandsSucceed() throws IOException {
        String commandString = "cat < input.txt; cat < input.txt > output.txt";
        String expectedContent = INPUT_CONTENT + "\n";
        AssertionUtil.assertCommandEvaluatedMatches(expectedContent, commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }

    @Test
    void ioRedirSeq_FirstIORedCommandFails_SecondIORedCommandStillExecutes() {
        String commandString = "cat < nth.txt; cat < input.txt";
        String expected = "shell: No such file or directory\n" +
                INPUT_CONTENT + "\n";
        AssertionUtil.assertCommandEvaluatedMatches(expected, commandString);
    }
}

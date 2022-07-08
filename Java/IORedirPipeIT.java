package sg.edu.nus.comp.cs4218.tests.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.tests.util.AssertionUtil;
import sg.edu.nus.comp.cs4218.tests.util.TestFileUtil;
import sg.edu.nus.comp.cs4218.tests.util.TestHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IORedirPipeIT {
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

    @Test
    void ioRedirPipe_PipeToCommandInputRedir_CommandEvaluatedWithoutPipe() {
        String commandString = "cat output.txt | cat < input.txt";
        String expected = INPUT_CONTENT + "\n";
        AssertionUtil.assertCommandEvaluatedMatches(expected, commandString);
    }

    @Test
    void ioRedirPipe_PipeToCallOutputRedir_RedirectedOutputHasPipedInput() throws IOException {
        String commandString = "ls | cat > output.txt";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, TestHelper.joinWithTab("input.txt", "output.txt"));
    }

    @Test
    void ioRedirPipe_CommandOutputRedirPipe_NothingPipedToSecondCall() throws IOException {
        String commandString = "cat input.txt > output.txt | cat";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }

    @Test
    void ioRedirPipe_CommandInputRedirPipe_OutputPipedToSecondCall() throws IOException {
        String commandString = "cat < input.txt | cat > output.txt";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }

    @Test
    void ioRedirPipe_CommandInputRedirFails_ExecutionFailsAtIORedir() {
        String commandString = "cat < non_exist.txt | ls";
        AssertionUtil.assertShellException(commandString);
    }

    @Test
    void ioRedirPipe_CommandInputOutputRedirPipe_NothingPassedToPipe() throws IOException {
        String commandString = "cat < input.txt > output.txt | cat";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }

    @Test
    void ioRedirPipe_PipeToCommandInputOutputRedir_CommandEvaluatedWithoutPipedInput() throws IOException {
        String commandString = "ls | cat < input.txt > output.txt";
        AssertionUtil.assertCommandEvaluated(commandString);
        AssertionUtil.assertFileExistsAndContains(workingDirectory, OUTPUT_FILE, INPUT_CONTENT);
    }
}

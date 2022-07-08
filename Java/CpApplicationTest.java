package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.tests.mocks.MockByteArrayOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.tests.util.TestFileUtil.createDirectories;
import static sg.edu.nus.comp.cs4218.tests.util.TestFileUtil.createFile;

/*
Duplicate literals appear as file name inputs
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
public class CpApplicationTest {
    CpApplication cpApp;
    private static final String FILE_1_CONTENT = "Copy the content of source file(s) to destination file/folder. " +
            "Note that folder (directory) is a type of file.";
    private static final String FILE_2_CONTENT = "In the first form, copy the contents of source file to destination file.";
    private static final String FILE_3_CONTENT = "In the second form, copy contents of each of the named source file(s) " +
            "to the destination folder. Names of the files themselves are not changed.";
    private static final String FILE_4_CONTENT = "# copy “hello” (without quotes) folder and its contents to the folder “foo” (without quotes)\n" +
            "$ cp -r hello foo\n";
    private static final String FILE_1_NAME = "file1.txt";
    private static final String FILE_2_NAME = "file2.txt";
    private static final String FILE_3_NAME = "file3.txt";
    private static final String FILE_4_NAME = "file4.txt";

    @TempDir
    Path workingDirectory;

    String envPreviousDir;

    List<String> pathStrings = List.of(
            FILE_1_NAME,
            FILE_2_NAME,
            FILE_3_NAME,
            FILE_4_NAME,
            "nested_folder_1/folder_1/file1.txt",
            "nested_folder_1/folder_1/file2.txt",
            "nested_folder_1/folder_2/file1.txt",
            "nested_folder_1/folder_2/file2.txt",
            "nested_folder_1/file1.txt", //NOPMD - suppressed AvoidDuplicateLiterals
            "nested_folder_1/folder_3", //NOPMD - suppressed AvoidDuplicateLiterals
            "nested_folder_2/folder_1/file1.txt", //NOPMD - suppressed AvoidDuplicateLiterals
            "nested_folder_2/folder_1/file2.txt",
            "nested_folder_2/folder_1/file3.txt",
            "folder_1/file1.txt"
    );

    private void initTestFiles() throws IOException {
        for (String pathString : pathStrings) {
            Path path = Paths.get(pathString);
            Path parent = path.getParent();
            String fileName = path.getFileName().toString();
            if (parent == null) {
                parent = workingDirectory;
            } else if (!Files.exists(parent)) {
                createDirectories(workingDirectory, parent);
                parent = workingDirectory.resolve(parent);
            }

            String content = "";
            switch (fileName) { //NOPMD - suppressed SwitchStmtsShouldHaveDefault
                case FILE_1_NAME:
                    content = FILE_1_CONTENT;
                    break;
                case FILE_2_NAME:
                    content = FILE_2_CONTENT;
                    break;
                case FILE_3_NAME:
                    content = FILE_3_CONTENT;
                    break;
                case FILE_4_NAME:
                    content = FILE_4_CONTENT;
                    break;
            }
            if (content.isEmpty()) {
                createDirectories(parent, Paths.get(fileName));
                continue;
            }
            createFile(parent, fileName, content);

        }
    }

    MockByteArrayOutputStream mockStdout() {
        return new MockByteArrayOutputStream();
    }

    //used for debugging only
    private void directoryPrinter() throws IOException {
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                System.out.println(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file,
                                             final BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                return FileVisitResult.CONTINUE;
            }

        };
        System.out.println("===========================");
        Files.walkFileTree(Paths.get(Environment.currentDirectory), visitor);
        System.out.println("===========================");
    }

    private boolean checkFileCopy(Path file1, Path file2) throws IOException {
        if (!Files.exists(file1) || !Files.exists(file2)) {
            return false;
        }
        if (Files.size(file1) != Files.size(file2)) {
            return false;
        }

        byte[] file1ByteArr = Files.readAllBytes(file1);
        byte[] file2ByteArr = Files.readAllBytes(file2);
        return Arrays.equals(file1ByteArr, file2ByteArr);
    }

    private boolean checkDirectoryCopy(Path source, Path target) throws IOException {
        Path sourceDirParent = source.getParent();
        List<Path> allSourcePaths = new LinkedList<>();
        SimpleFileVisitor<Path> sourceVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                if (!dir.equals(target)) {
                    allSourcePaths.add(dir);
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(final Path file,
                                             final BasicFileAttributes attrs) {
                allSourcePaths.add(file);
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(source, sourceVisitor);

        for (Path path : allSourcePaths) {
            Path copiedPath = target.resolve(sourceDirParent.relativize(path));
            if (!Files.exists(copiedPath)) {
                return false;
            }

            if (Files.isDirectory(path) && !Files.isDirectory(copiedPath)) {
                return false;
            }

            if (Files.isDirectory(path) && Files.isDirectory(copiedPath)) {
                continue;
            }

            if (!checkFileCopy(path, copiedPath)) {
                return false;
            }
        }
        return true;
    }


    private boolean checkAllPaths(Path source, Path target, List<Path> paths) throws IOException {
        for (Path path : paths) {
            Path copiedPath = target.resolve(source.relativize(path));

            if (!Files.exists(copiedPath)) {
                return false;
            }

            if (Files.isDirectory(path) && !Files.isDirectory(copiedPath)) {
                return false;
            }

            if (Files.isDirectory(path) &&
                    Files.isDirectory(copiedPath) &&
                    !checkDirectoryCopy(path, copiedPath)) {
                return false;
            }

            if (!checkFileCopy(path, copiedPath)) {
                return false;
            }
        }
        return true;
    }

    private String getMessage(String name, String err) {
        return new CpException(name + " " + err).getMessage();
    }

    private String getMessage(String err) {
        return new CpException(err).getMessage();
    }

    @BeforeEach
    void setUp() throws IOException {
        cpApp = new CpApplication();
        envPreviousDir = Environment.currentDirectory;
        Environment.currentDirectory = workingDirectory.toString();

        initTestFiles();
    }

    @AfterEach
    void tearDown() {
        Environment.currentDirectory = envPreviousDir;
    }

    @ParameterizedTest
    @ValueSource(strings = {"non_existent.txt", "nested_folder_1/non_existent_folder", "non_existent_folder"})
    void cpSrcFileToDestFile_CopyNonExistentFileOrDirectory_ThrowsCpException(String input) {
        String sourceName = input;
        String destName = "nested_folder_1/folder_2/";
        Path sourcePath = IOUtils.resolveFilePath(sourceName);
        Path destPath = IOUtils.resolveFilePath(destName);

        assertFalse(Files.exists(sourcePath));
        assertTrue(Files.exists(destPath) && Files.isDirectory(destPath));

        Exception exception = assertThrows(CpException.class, () -> {
            cpApp.cpSrcFileToDestFile(false, sourceName, destName);
        });
        assertEquals(new CpException(input + " " + ERR_FILE_NOT_FOUND).getMessage(),
                exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nested_folder_1/folder_1/ nested_folder_1/file1.txt",
            "folder_1 nested_folder_1/folder_1/file2.txt",
    })
    void cpSrcFileToDestFile_CopyDirToFile_ThrowsCpException(String input) {
        String[] args = input.split(" ");
        String sourceName = args[0];
        String destName = args[1];
        Path sourcePath = IOUtils.resolveFilePath(sourceName);
        Path destPath = IOUtils.resolveFilePath(destName);

        assertTrue(Files.exists(sourcePath) && Files.isDirectory(sourcePath) &&
                Files.exists(destPath) && !Files.isDirectory(destPath));
        Exception exception = assertThrows(CpException.class, () -> {
            cpApp.cpSrcFileToDestFile(true, sourceName, destName);
        });
        assertEquals(getMessage(destName, ERR_IS_NOT_DIR), exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nested_folder_1/folder_1 nested_folder_1/folder_2",
            "folder_1 nested_folder_1/folder_3"
    })
    void cpSrcFileToDestFile_CopyDirToDirNoRecursiveFlag_ThrowsCpException(String input) {
        String[] args = input.split(" ");
        String sourceName = args[0];
        String destName = args[1];
        Path sourcePath = IOUtils.resolveFilePath(sourceName);
        Path destPath = IOUtils.resolveFilePath(destName);

        assertTrue(Files.exists(sourcePath) && Files.isDirectory(sourcePath) &&
                Files.exists(destPath) && Files.isDirectory(destPath));
        Exception exception = assertThrows(CpException.class, () -> {
            cpApp.cpSrcFileToDestFile(false, sourceName, destName);
        });
        assertEquals(getMessage(sourcePath.getFileName().toString() +
                " - Use recursive option -r or -R (not copied)"), exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            FILE_1_NAME + " " + FILE_2_NAME,
            "nested_folder_1/folder_1/file1.txt nested_folder_1/folder_1/file2.txt",
            "nested_folder_2/folder_1/file2.txt folder_1/file1.txt"
    })
    void cpSrcFileToDestFile_CopyFileToExistingFile_ExistingFileOverwritten(String input) throws IOException, CpException {
        String[] args = input.split(" ");
        String sourceName = args[0];
        String destName = args[1];
        Path sourcePath = IOUtils.resolveFilePath(sourceName);
        Path destPath = IOUtils.resolveFilePath(destName);

        assertTrue(Files.exists(destPath) && !Files.isDirectory(destPath));
        assertFalse(checkFileCopy(sourcePath, destPath));
        cpApp.cpSrcFileToDestFile(false, sourceName, destName);
        assertTrue(checkFileCopy(sourcePath, destPath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nested_folder_1/folder_1/file1.txt nested_folder_1/folder_3/thisone.txt",
            FILE_4_NAME + " file5.txt",
            "nested_folder_2/folder_1/file3.txt folder_1/file78.txt"
    })
    void cpSrcFileToDestFile_CopyFileToNonExistingFile_FileCreated(String input) throws IOException, CpException {
        String[] args = input.split(" ");
        String sourceName = args[0];
        String destName = args[1];
        Path sourcePath = IOUtils.resolveFilePath(sourceName);
        Path destPath = IOUtils.resolveFilePath(destName);

        assertFalse(Files.exists(destPath));
        assertTrue(Files.exists(sourcePath));

        cpApp.cpSrcFileToDestFile(true, sourceName, destName);

        assertTrue(Files.exists(destPath) && !Files.isDirectory(destPath));
        assertTrue(checkFileCopy(sourcePath, destPath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nested_folder_1/folder_2/file2.txt folder_1",
            FILE_4_NAME + " nested_folder_1/folder_3",
            "nested_folder_2/folder_1/file3.txt nested_folder_1/folder_2"
    })
    void cpSrcFileToDestFile_CopyFileToDir_SameNameFileCreated(String input) throws IOException, CpException {
        String[] args = input.split(" ");
        String sourceName = args[0];
        String destName = args[1];
        Path sourcePath = IOUtils.resolveFilePath(sourceName);
        Path destPath = IOUtils.resolveFilePath(destName);
        Path newDestPath = destPath.resolve(sourcePath.getFileName());

        assertFalse(Files.exists(newDestPath));
        cpApp.cpSrcFileToDestFile(false, sourceName, destName);
        assertTrue(Files.exists(newDestPath));
        assertTrue(checkFileCopy(sourcePath, newDestPath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nested_folder_1/folder_3 nested_folder_2",
            "nested_folder_1/folder_3 nested_folder_2/folder_1",
            "nested_folder_1/folder_3 currentWorkingDirectory"
    })
    void cpSrcFileToDestFile_CopyEmptyDirToDir_DirCreated(String input) throws IOException, CpException {
        String[] args = input.split(" ");
        String sourceName = args[0];
        String destName = args[1];
        Path sourcePath = IOUtils.resolveFilePath(sourceName);
        Path destPath;
        if ("currentWorkingDirectory".equals(destName)) {
            destName = Environment.currentDirectory;
            destPath = Paths.get(Environment.currentDirectory);
        } else {
            destPath = IOUtils.resolveFilePath(destName);
        }
        Path newDestPath = destPath.resolve(sourcePath.getFileName());

        assertFalse(Files.exists(newDestPath));
        cpApp.cpSrcFileToDestFile(true, sourceName, destName);
        assertTrue(Files.exists(newDestPath) && Files.isDirectory(newDestPath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "nested_folder_2 nested_folder_1",
            "nested_folder_1/folder_1 nested_folder_1/folder_2",
            "nested_folder_2/folder_1 folder_1",
            "currentWorkingDirectory folder_1"
    })
    void cpSrcFileToDestFile_CopyNonEmptyDirToDir_DirAndFilesCreated(String input) throws IOException, CpException {
        String[] args = input.split(" ");
        String sourceName = args[0];
        String destName = args[1];
        Path sourcePath;
        if ("currentWorkingDirectory".equals(sourceName)) {
            sourceName = Environment.currentDirectory;
            sourcePath = Paths.get(Environment.currentDirectory);
        } else {
            sourcePath = IOUtils.resolveFilePath(sourceName);
        }
        Path destPath = IOUtils.resolveFilePath(destName);

        assertFalse(checkDirectoryCopy(sourcePath, destPath));
        cpApp.cpSrcFileToDestFile(true, sourceName, destName);
        boolean isCorrect = checkDirectoryCopy(sourcePath, destPath);
        assertTrue(isCorrect);
    }

    @Test
    void cpSrcFileToDestFile_CopyNonEmptyDirToDir_DuplicateDirNotOverwritten() throws IOException, CpException {
        String sourceName = "nested_folder_2/folder_1";
        String destName = "nested_folder_1";
        Path sourcePath = IOUtils.resolveFilePath(sourceName);
        Path destPath = IOUtils.resolveFilePath(destName);

        cpApp.cpSrcFileToDestFile(true, sourceName, destName);

        assertFalse(checkDirectoryCopy(sourcePath, destPath));
    }

    @Test
    void cpFilesToFolder_DestNotADir_ThrowsCpException() {
        String[] sourceName = {
                "nested_folder_1/folder_1/file1.txt",
                "nested_folder_1/folder_1/file2.txt",
                "nested_folder_1/folder_2/file1.txt",
                "nested_folder_1/folder_2/file2.txt",
        };
        String destName = "nested_folder_1/file1.txt";
        Exception exception = assertThrows(CpException.class, () -> {
            cpApp.cpFilesToFolder(false, destName, sourceName);
        });
        assertEquals(getMessage(destName, ERR_IS_NOT_DIR), exception.getMessage());
    }

    @Test
    void cpFilesToFolder_CopyMultipleFilesToDir_ExistingOverwrittenOthersCreated() throws IOException, CpException {
        String[] sourceNames = {
                "nested_folder_2/folder_1/file1.txt",
                "nested_folder_2/folder_1/file2.txt",
                "nested_folder_2/folder_1/file3.txt"
        };
        Path newFolder = IOUtils.resolveFilePath("folder_2");
        String content = "to be overwritten";
        createDirectories(Paths.get(Environment.currentDirectory), Paths.get("folder_2"));
        createFile(newFolder, "file1.txt", content);

        assertFalse(checkFileCopy(IOUtils.resolveFilePath("nested_folder_2/folder_1/file1.txt"),
                IOUtils.resolveFilePath("folder_2/file1.txt")));
        assertFalse(Files.exists(IOUtils.resolveFilePath("folder_2/file2.txt")));
        assertFalse(Files.exists(IOUtils.resolveFilePath("folder_2/file3.txt")));

        cpApp.cpFilesToFolder(false, "folder_2", sourceNames);

        assertTrue(checkFileCopy(IOUtils.resolveFilePath("nested_folder_2/folder_1/file1.txt"),
                IOUtils.resolveFilePath("folder_2/file1.txt")));
        assertTrue(checkFileCopy(IOUtils.resolveFilePath("folder_2/file2.txt"),
                IOUtils.resolveFilePath("folder_2/file2.txt")));
        assertTrue(checkFileCopy(IOUtils.resolveFilePath("folder_2/file3.txt"),
                IOUtils.resolveFilePath("folder_2/file3.txt")));
    }

    @Test
    void cpFilesToFolder_MultipleFilesAndFoldersWrongFlag_OnlyFilesCopied() throws IOException, CpException {
        String[] sourceNames = {
                "nested_folder_2/folder_1",
                "nested_folder_1/file1.txt",
        };
        String destName = "nested_folder_1/folder_3";
        cpApp.cpFilesToFolder(false, destName, sourceNames);
        Path destFolder = IOUtils.resolveFilePath(destName);
        Path fileToCopy1 = destFolder.resolve("folder_1");
        Path fileToCopy2 = destFolder.resolve("file1.txt");
        assertFalse(Files.exists(fileToCopy1));
        assertTrue(Files.exists(fileToCopy2));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-r",
            "-R"
    })
    void run_FlagsAndArgsParsedCorrectly_CorrectCopyOperations(String flag) throws CpException, IOException {
        String[] args = {
                flag,
                "nested_folder_1/folder_1", //NOPMD - suppressed AvoidDuplicateLiterals
                "nested_folder_1/folder_3"
        };
        ByteArrayOutputStream outputStream = mockStdout();
        cpApp.run(args, null, outputStream);
        Path sourcePath = IOUtils.resolveFilePath("nested_folder_1/folder_1");
        Path destPath = IOUtils.resolveFilePath("nested_folder_1/folder_3");
        assertTrue(checkDirectoryCopy(sourcePath, destPath));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-x",
            "-T",
            "-q"
    })
    void run_IllegalFlags_CorrectCopyOperations(String flag) {
        String[] args = {
                flag,
                "nested_folder_1/folder_1",
                "nested_folder_1/folder_3"
        };
        ByteArrayOutputStream outputStream = mockStdout();
        Exception exception = assertThrows(CpException.class, () -> {
            cpApp.run(args, null, outputStream);
        });
        assertEquals(getMessage(ILLEGAL_FLAG_MSG + flag.substring(1)), exception.getMessage());
    }

    @Test
    void run_SkippedExistingDirectories_PrintSkippedDirectories() throws CpException {
        String[] args = {
                "-r",
                "nested_folder_1/folder_1",
                "nested_folder_1/folder_2",
                "nested_folder_1/file1.txt",
                "nested_folder_1/folder_3",
                "nested_folder_2"
        };
        ByteArrayOutputStream stdout = mockStdout();
        cpApp.run(args, null, stdout);
        assertEquals("folder_1 " + ERR_DIR_EXISTS + "\n", stdout.toString());
    }

    @Test
    void run_NullStdout_ThrowsCpException() {
        String[] args = {
                "-R",
                "nested_folder_1/folder_1/file1.txt",
                "nested_folder_1/folder_1/file2.txt",
                "nested_folder_1/folder_2/file1.txt",
                "nested_folder_1/folder_2/file2.txt"
        };
        Exception exception = assertThrows(CpException.class, () -> {
            cpApp.run(args, null, null);
        });
        assertEquals(getMessage(ERR_NO_OSTREAM), exception.getMessage());
    }

    @Test
    void run_LessThan2FileArgs_ThrowsCpException() throws IOException {
        String[] args = {
                "file1.txt"
        };
        try (OutputStream stdout = mockStdout()) {
            Exception exception = assertThrows(CpException.class, () -> {
                cpApp.run(args, null, stdout);
            });
            assertEquals(getMessage(ERR_NO_ARGS), exception.getMessage());
        }
    }

    @Test
    void run_DestFileNoWritePerms_ThrowsException() {
        String[] args = {
                FILE_1_NAME,
                "no_perms.txt"
        };
        Set<PosixFilePermission> perms = new HashSet<>();
        createFile(workingDirectory, "no_perms.txt", perms);
        Exception exception = assertThrows(CpException.class, () -> {
            cpApp.run(args, null, mockStdout());
        });
    }

    @Test
    void run_SourceFileNoWritePerms_Success() throws CpException {
        String[] args = {
                "no_perms.txt",
                "no_perms_copy.txt"
        };
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        createFile(workingDirectory, "no_perms.txt", perms);
        assertFalse(Files.exists(IOUtils.resolveFilePath("no_perms_copy.txt")));
        cpApp.run(args, null, mockStdout());
        assertTrue(Files.exists(IOUtils.resolveFilePath("no_perms_copy.txt")));
    }

    @Test
    void run_SourceArgsIsAbsolute_FileCopied() throws CpException, IOException {
        String srcFile = IOUtils.resolveFilePath(FILE_1_NAME).toString();
        String destFile = "file5.txt";
        Path srcPath = Paths.get(srcFile);
        Path destPath = IOUtils.resolveFilePath(destFile);
        String[] args = {
                srcFile,
                destFile
        };
        assertFalse(Files.exists(destPath));
        cpApp.run(args, null, mockStdout());
        assertTrue(Files.exists(destPath));
        assertTrue(checkFileCopy(srcPath, destPath));

    }

    @Test
    void run_DestArgsIsAbsolute_FileCopied() throws CpException, IOException {
        String srcFile = FILE_1_NAME;
        String destFile = IOUtils.resolveFilePath("file5.txt").toString();
        Path srcPath = IOUtils.resolveFilePath(FILE_1_NAME);
        Path destPath = Paths.get(destFile);
        String[] args = {
                srcFile,
                destFile
        };
        assertFalse(Files.exists(destPath));
        cpApp.run(args, null, mockStdout());
        assertTrue(Files.exists(destPath));
        assertTrue(checkFileCopy(srcPath, destPath));

    }

    @Test
    void run_AllArgsAreAbsolute_FileCopied() throws CpException, IOException {
        String srcFile = IOUtils.resolveFilePath(FILE_1_NAME).toString();
        String destFile = IOUtils.resolveFilePath("file5.txt").toString();
        Path srcPath = Paths.get(srcFile);
        Path destPath = Paths.get(destFile);
        String[] args = {
                srcFile,
                destFile
        };
        assertFalse(Files.exists(destPath));
        cpApp.run(args, null, mockStdout());
        assertTrue(Files.exists(destPath));
        assertTrue(checkFileCopy(srcPath, destPath));

    }
}
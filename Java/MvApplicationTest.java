package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;
import sg.edu.nus.comp.cs4218.tests.mocks.MockByteArrayOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.tests.util.TestFileUtil.createDirectories;
import static sg.edu.nus.comp.cs4218.tests.util.TestFileUtil.createFile;

/*
Duplicate literals are used as file name inputs
 */
@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
public class MvApplicationTest {
    private static final String FILE_11_CONTENT = "Copy the content of source file(s) to destination file/folder. ";
    private static final String FILE_12_CONTENT = "Note that folder (directory) is a type of file.";
    private static final String FILE_13_CONTENT = "In the first form, copy the contents of ";
    private static final String FILE_14_CONTENT = "source file to destination file.";
    private static final String FILE_21_CONTENT = "In the second form, copy contents of each of the named source file(s) ";
    private static final String FILE_22_CONTENT = "to the destination folder. Names of the files themselves are not changed.";
    private static final String FILE_23_CONTENT = "# copy “hello” (without quotes) folder and its contents to the folder “foo” (without quotes)";
    private static final String FILE_24_CONTENT = "$ cp -r hello foo";
    private static final String FILE_1_NAME = "file1.txt";
    private static final String FILE_2_NAME = "file2.txt";
    private static final String FILE_3_NAME = "file3.txt";
    private static final String FILE_4_NAME = "file4.txt";
    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String FOLDER_3 = "folder3";
    private static final String NON_EXIST = "non_exist.txt";

    MvApplication mvApp;

    @TempDir
    Path workingDirectory;

    String envPreviousDir;

    List<String> pathStrings = List.of(
            "folder1/file1.txt",
            "folder1/file2.txt",
            "folder1/file3.txt",
            "folder1/file4.txt",
            "folder2/file1.txt",
            "folder2/file2.txt",
            "folder2/file3.txt",
            "folder2/file4.txt",
            "folder3"
    );

    private void initTestFiles() { //NOPMD - suppressed ExcessiveMethodLength
        for (int i = 1; i <= 3; i++) {
            String folderName = "folder" + i;
            createDirectories(Paths.get(Environment.currentDirectory),
                    Paths.get(folderName));
        }

        for (String pathString : pathStrings) {
            if ("folder3".equals(pathString)) {
                createDirectories(Paths.get(Environment.currentDirectory), Paths.get(pathString));
                break;
            }
            Path path = Paths.get(pathString);
            Path parent = path.getParent();
            Path file = path.getFileName();
            String content = "";

            if (parent.toString().equals("folder1")) {
                switch (file.toString()) { //NOPMD - suppressed SwitchStmtsShouldHaveDefault
                    case FILE_1_NAME:
                        content = FILE_11_CONTENT;
                        break;
                    case FILE_2_NAME:
                        content = FILE_12_CONTENT;
                        break;
                    case FILE_3_NAME:
                        content = FILE_13_CONTENT;
                        break;
                    case FILE_4_NAME:
                        content = FILE_14_CONTENT;
                        break;
                }
            } else if (parent.toString().equals("folder2")) {
                switch (file.toString()) { //NOPMD - suppressed SwitchStmtsShouldHaveDefault
                    case FILE_1_NAME:
                        content = FILE_21_CONTENT;
                        break;
                    case FILE_2_NAME:
                        content = FILE_22_CONTENT;
                        break;
                    case FILE_3_NAME:
                        content = FILE_23_CONTENT;
                        break;
                    case FILE_4_NAME:
                        content = FILE_24_CONTENT;
                        break;
                }
            }

            Path fullDirPath = IOUtils.resolveFilePath(parent.toString());
            createFile(fullDirPath, file.toString(), content);
        }
    }

    MockByteArrayOutputStream mockStdout() {
        return new MockByteArrayOutputStream();
    }

    //Method only used for debugging
    private void directoryPrinter() throws IOException {
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                System.out.println(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file,
                                             final BasicFileAttributes attrs) {
                System.out.println(file);
                return FileVisitResult.CONTINUE;
            }

        };
        Files.walkFileTree(Paths.get(Environment.currentDirectory), visitor);
    }

    private boolean checkFileContent(Path path, String expectedContent) throws Exception {
        InputStream input = new FileInputStream(path.toString()); //NOPMD - suppressed CloseResource - closed below
        List<String> lines = IOUtils.getLinesFromInputStream(input);
        input.close();

        String fileContent = String.join(STRING_NEWLINE, lines);
        return expectedContent.equals(fileContent);
    }

    private boolean checkDirMove(String folder, Path parent) throws Exception {
        boolean isCorrect = false;
        for (int i = 1; i <= 4; i++) {
            Path fullFilePath = parent.resolve(constructFileString(
                    folder, "file" + i + ".txt"
            ));
            if (folder.equals(FOLDER_1)) {
                switch (i) {
                    case 1:
                        isCorrect = checkFileContent(fullFilePath, FILE_11_CONTENT);
                        break;
                    case 2:
                        isCorrect = checkFileContent(fullFilePath, FILE_12_CONTENT);
                        break;
                    case 3:
                        isCorrect = checkFileContent(fullFilePath, FILE_13_CONTENT);
                        break;
                    case 4:
                        isCorrect = checkFileContent(fullFilePath, FILE_14_CONTENT);
                        break;
                    default:
                        isCorrect = false;
                        break;
                }
            } else {
                switch (i) {
                    case 1:
                        isCorrect = checkFileContent(fullFilePath, FILE_21_CONTENT);
                        break;
                    case 2:
                        isCorrect = checkFileContent(fullFilePath, FILE_22_CONTENT);
                        break;
                    case 3:
                        isCorrect = checkFileContent(fullFilePath, FILE_23_CONTENT);
                        break;
                    case 4:
                        isCorrect = checkFileContent(fullFilePath, FILE_24_CONTENT);
                        break;
                    default:
                        isCorrect = false;
                        break;
                }
            }
            if (!isCorrect) {
                return false;
            }
        }
        return true;
    }

    private String constructFileString(String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirstArg = true;
        for (String s : args) {
            if (!isFirstArg) {
                stringBuilder.append(StringUtils.fileSeparator());
            }
            isFirstArg = false;
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    @BeforeEach
    void setUp() throws IOException {
        mvApp = new MvApplication(); //TODO comment out
        envPreviousDir = Environment.currentDirectory;
        Environment.currentDirectory = workingDirectory.toString();

        initTestFiles();
    }

    @AfterEach
    void tearDown() {
        Environment.currentDirectory = envPreviousDir;
    }

    @Test
    void mvSrcFileToDestFile_MissingSrcFile_ThrowsException() {
        assertThrows(Exception.class, () -> {
            mvApp.mvSrcFileToDestFile(true, NON_EXIST, FOLDER_1);
        });
    }

    @Test
    void mvSrcFileToDestFile_DestFileExistNoOverwrite_ThrowsException() {
        String srcFile = constructFileString(FOLDER_1, FILE_1_NAME);
        String destFile = constructFileString(FOLDER_2, FILE_2_NAME);
        assertThrows(Exception.class, () -> {
            mvApp.mvSrcFileToDestFile(false, srcFile, destFile);
        });
    }

    @Test
    void mvSrcFileToDestFile_MoveFileToExistingFile_FileMovedOverwrittenExistingFile() throws Exception {
        String srcFile = constructFileString(FOLDER_1, FILE_1_NAME);
        String destFile = constructFileString(FOLDER_2, FILE_2_NAME);
        Path srcFilePath = Paths.get(Environment.currentDirectory).resolve(Paths.get(srcFile));
        Path destFilePath = IOUtils.resolveFilePath(destFile);
        String srcContent = FILE_11_CONTENT;
        String destContent = FILE_22_CONTENT;

        assertTrue(checkFileContent(srcFilePath, srcContent));
        assertTrue(checkFileContent(destFilePath, destContent));

        mvApp.mvSrcFileToDestFile(true, srcFile, destFile);

        assertFalse(Files.exists(srcFilePath));
        assertTrue(checkFileContent(destFilePath, srcContent));
    }

    @Test
    void mvSrcFileToDestFile_MoveFileToNonExistingFile_FileMovedFileCreated() throws Exception {
        String srcFile = constructFileString(FOLDER_1, FILE_3_NAME);
        String destFile = constructFileString(FOLDER_3, FILE_1_NAME);
        Path srcPath = IOUtils.resolveFilePath(srcFile);
        Path destPath = IOUtils.resolveFilePath(destFile);

        assertFalse(Files.exists(destPath));

        mvApp.mvSrcFileToDestFile(false, srcFile, destFile);

        assertTrue(Files.exists(destPath));
        assertTrue(checkFileContent(destPath, FILE_13_CONTENT));
        assertFalse(Files.exists(srcPath));
    }

    @Test
    void mvSrcFileToDestFile_MoveDirToFile_ThrowsException() {
        String srcFile = FOLDER_1;
        String destFile = constructFileString(FOLDER_2, FILE_2_NAME);

        assertThrows(Exception.class, () -> {
            mvApp.mvSrcFileToDestFile(true, srcFile, destFile);
        });
    }

    @Test
    void mvSrcFileToDestFile_MoveDirToDir_OldDirRemovedNewDirCreated() throws Exception {
        String srcFile = FOLDER_1;
        String destFile = FOLDER_2;
        Path srcPath = IOUtils.resolveFilePath(srcFile);
        Path destPath = IOUtils.resolveFilePath(destFile);

        mvApp.mvSrcFileToDestFile(true, srcFile, destFile);
        Path newPathToFolder = IOUtils.resolveFilePath(constructFileString(FOLDER_2, FOLDER_1));
        assertTrue(Files.exists(newPathToFolder));
        assertFalse(Files.exists(srcPath));
        assertTrue(checkDirMove(FOLDER_1, destPath));
    }


    @Test
    void mvSrcFileToDestFile_MoveFileUpOneLevel_FileMoved() throws Exception {
        String srcFile = constructFileString(FOLDER_1, FILE_4_NAME);
        String destFile = Environment.currentDirectory;

        mvApp.mvSrcFileToDestFile(true, srcFile, destFile);
        assertTrue(checkFileContent(
                IOUtils.resolveFilePath(
                        constructFileString(Environment.currentDirectory, FILE_4_NAME)),
                FILE_14_CONTENT
        ));
        assertFalse(Files.exists(
                IOUtils.resolveFilePath(constructFileString(
                        FOLDER_1, FILE_4_NAME)
                )));
    }

    @Test
    void mvSrcFileToDestFile_MoveDirToSameDir_ThrowsException() {
        assertThrows(Exception.class, () -> {
            mvApp.mvSrcFileToDestFile(true, FOLDER_1, FOLDER_1);
        });
    }

    @Test
    void mvSrcFileToDestFile_MoveFileToSameDirUsingPeriod_ThrowsException() {
        Environment.currentDirectory = workingDirectory.resolve(FOLDER_1).toString();

        assertThrows(MvException.class, () -> {
            mvApp.mvSrcFileToDestFile(true, FILE_1_NAME, ".");
        });
    }

    @Test
    void mvSrcFileToDestFile_MoveFileUpOneDirUsingPeriod_FileMoved() throws MvException, IOException {
        Environment.currentDirectory = workingDirectory.resolve(FOLDER_1).toString();
        mvApp.mvSrcFileToDestFile(true, FILE_1_NAME, "..");
        Environment.currentDirectory = workingDirectory.toString();
        Path toBeCreated = IOUtils.resolveFilePath(FILE_1_NAME);
        Path toBeMoved = IOUtils.resolveFilePath(constructFileString(FOLDER_1, FILE_1_NAME));
        assertTrue(Files.exists(toBeCreated));
        assertFalse(Files.exists(toBeMoved));
    }

    @Test
    void mvFilesToFolder_DestFolderNonExistent_ThrowsException() {
        String[] srcFiles = {
                constructFileString(FOLDER_1, FILE_1_NAME),
                constructFileString(FOLDER_1, FILE_2_NAME),
                constructFileString(FOLDER_1, FILE_3_NAME),
        };
        String destFile = "no_folder";
        assertThrows(Exception.class, () -> {
            mvApp.mvFilesToFolder(true, destFile, srcFiles);
        });
    }

    @Test
    void mvFilesToFolder_DestFolderContainsSameNameFilesNoOverwrite_FilesNotOverwritten() throws Exception {
        String[] srcFiles = {
                constructFileString(FOLDER_1, FILE_1_NAME),
                constructFileString(FOLDER_1, FILE_2_NAME),
                constructFileString(FOLDER_1, FILE_3_NAME),
        };
        String destFile = FOLDER_2;
        mvApp.mvFilesToFolder(false, destFile, srcFiles);
        Path folder2File1 = IOUtils.resolveFilePath(FOLDER_2).resolve(FILE_1_NAME);
        Path folder2File2 = IOUtils.resolveFilePath(FOLDER_2).resolve(FILE_2_NAME);
        Path folder2File3 = IOUtils.resolveFilePath(FOLDER_2).resolve(FILE_3_NAME);
        assertTrue(checkFileContent(folder2File1, FILE_21_CONTENT));
        assertTrue(checkFileContent(folder2File2, FILE_22_CONTENT));
        assertTrue(checkFileContent(folder2File3, FILE_23_CONTENT));
    }

    @Test
    void mvFilesToFolder_DestFolderContainsSameNameFilesIsOverwrite_OverwrittenFiles() throws Exception {
        String[] srcFiles = {
                constructFileString(FOLDER_1, FILE_1_NAME),
                constructFileString(FOLDER_1, FILE_2_NAME),
                constructFileString(FOLDER_1, FILE_3_NAME),
        };
        String destFile = FOLDER_2;
        mvApp.mvFilesToFolder(true, destFile, srcFiles);
        Path file1Path = IOUtils.resolveFilePath(constructFileString(FOLDER_2, FILE_1_NAME));
        Path file2Path = IOUtils.resolveFilePath(constructFileString(FOLDER_2, FILE_2_NAME));
        Path file3Path = IOUtils.resolveFilePath(constructFileString(FOLDER_2, FILE_3_NAME));
        Path file4Path = IOUtils.resolveFilePath(constructFileString(FOLDER_2, FILE_4_NAME));

        assertTrue(checkFileContent(file1Path, FILE_11_CONTENT));
        assertTrue(checkFileContent(file2Path, FILE_12_CONTENT));
        assertTrue(checkFileContent(file3Path, FILE_13_CONTENT));
        assertTrue(checkFileContent(file4Path, FILE_24_CONTENT));

        for (String file : srcFiles) {
            assertFalse(Files.exists(IOUtils.resolveFilePath(file)));
        }
    }


    @Test
    void mvFilesToFolder_MultipleFilesSomeMissing_ExistingFilesMovedSuccessfully() throws Exception {
        String[] srcFiles = {
                constructFileString(FOLDER_2, FILE_3_NAME),
                constructFileString(FOLDER_1, FILE_2_NAME),
                NON_EXIST
        };
        String destFiles = FOLDER_3;
        mvApp.mvFilesToFolder(true, destFiles, srcFiles);
        Path expectedRemoved1 = IOUtils.resolveFilePath(FOLDER_2).resolve(FILE_3_NAME);
        Path expectedRemoved2 = IOUtils.resolveFilePath(FOLDER_1).resolve(FILE_2_NAME);
        Path destPath = IOUtils.resolveFilePath(FOLDER_3);
        Path expectedCreated1 = destPath.resolve(FILE_3_NAME);
        Path expectedCreated2 = destPath.resolve(FILE_2_NAME);
        assertTrue(Files.exists(expectedCreated1));
        assertTrue(Files.exists(expectedCreated2));
        assertTrue(checkFileContent(expectedCreated1, FILE_23_CONTENT));
        assertTrue(checkFileContent(expectedCreated2, FILE_12_CONTENT));
        assertFalse(Files.exists(expectedRemoved1));
        assertFalse(Files.exists(expectedRemoved2));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-N",
            "-R"
    })
    void run_InvalidFlags_ThrowsException(String flag) {
        String[] args = {
                flag,
                constructFileString(FOLDER_1, FILE_4_NAME),
                constructFileString(FOLDER_2, FILE_4_NAME)
        };
        ByteArrayOutputStream stdout = mockStdout();
        assertThrows(Exception.class, () -> {
            mvApp.run(args, null, stdout);
        });
    }

    @Test
    void run_ValidFlagsNoOverWriteMoveFileToFile_ThrowsException() {
        String[] args = {
                "-n",
                constructFileString(FOLDER_1, FILE_4_NAME),
                constructFileString(FOLDER_2, FILE_4_NAME)
        };
        assertThrows(Exception.class, () -> {
            mvApp.run(args, null, mockStdout());
        });
    }

    @Test
    void run_NullStdout_ThrowsException() {
        String[] args = {
                "-n",
                constructFileString(FOLDER_1, FILE_4_NAME),
                constructFileString(FOLDER_2, FILE_4_NAME)
        };
        assertThrows(Exception.class, () -> {
            mvApp.run(args, null, null);
        });
    }

    @Test
    void run_MoveMultipleFilesToEmptyDir_FilesMoved() throws Exception {
        String[] args = {
                constructFileString(FOLDER_2, FILE_4_NAME),
                constructFileString(FOLDER_2, FILE_3_NAME),
                FOLDER_3
        };
        mvApp.run(args, null, mockStdout());
        assertTrue(checkFileContent(
                IOUtils.resolveFilePath(constructFileString(FOLDER_3, FILE_4_NAME)),
                FILE_24_CONTENT
        ));
        assertTrue(checkFileContent(
                IOUtils.resolveFilePath(constructFileString(FOLDER_3, FILE_3_NAME)),
                FILE_23_CONTENT
        ));
        for (int i = 0; i < 2; i++) {
            assertFalse(Files.exists(
                    IOUtils.resolveFilePath(args[i])
            ));
        }
    }

    @Test
    void run_LessThanOneFileArg_ThrowsException() {
        String[] args = {
                "-n",
                constructFileString(FOLDER_1, FILE_1_NAME)
        };

        assertThrows(Exception.class, () -> {
            mvApp.run(args, null, mockStdout());
        });
    }

    @Test
    void run_DestFileNoWritePerms_ThrowsException() {
        String[] args = {
                constructFileString(FOLDER_1, FILE_1_NAME),
                "no_perms.txt"
        };
        Set<PosixFilePermission> perms = new HashSet<>();
        createFile(workingDirectory, "no_perms.txt", perms);
        Exception exception = assertThrows(MvException.class, () -> {
            mvApp.run(args, null, mockStdout());
        });
    }

    @Test
    void run_SourceFileNoWritePerms_Success() throws MvException {
        String[] args = {
                "no_perms.txt",
                "no_perms_copy.txt"
        };
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        createFile(workingDirectory, "no_perms.txt", perms);
        assertFalse(Files.exists(IOUtils.resolveFilePath("no_perms_copy.txt")));
        mvApp.run(args, null, mockStdout());
        assertTrue(Files.exists(IOUtils.resolveFilePath("no_perms_copy.txt")));
    }

    @Test
    void run_SourceArgIsAbsolute_FileMoved() throws Exception {
        String srcFile = IOUtils.resolveFilePath(constructFileString(FOLDER_1, FILE_1_NAME)).toString();
        String destFile = "file5.txt";
        String[] args = {"-n", srcFile, destFile};
        mvApp.run(args, null, mockStdout());
        assertTrue(Files.exists(IOUtils.resolveFilePath(destFile)));
        assertFalse(Files.exists(IOUtils.resolveFilePath(srcFile)));
    }

    @Test
    void run_DestArgIsAbsolute_FileMoved() throws Exception {
        String srcFile = constructFileString(FOLDER_1, FILE_1_NAME);
        String destFile = IOUtils.resolveFilePath("file5.txt").toString();
        String[] args = {"-n", srcFile, destFile};
        mvApp.run(args, null, mockStdout());
        assertTrue(Files.exists(IOUtils.resolveFilePath(destFile)));
        assertFalse(Files.exists(IOUtils.resolveFilePath(srcFile)));
    }

    @Test
    void run_AllArgsAreAbsolute_FileMoved() throws Exception {
        String srcFile = IOUtils.resolveFilePath(constructFileString(FOLDER_1, FILE_1_NAME)).toString();
        String destFile = IOUtils.resolveFilePath("file5.txt").toString();
        String[] args = {"-n", srcFile, destFile};
        mvApp.run(args, null, mockStdout());
        assertTrue(Files.exists(IOUtils.resolveFilePath(destFile)));
        assertFalse(Files.exists(IOUtils.resolveFilePath(srcFile)));
    }
}

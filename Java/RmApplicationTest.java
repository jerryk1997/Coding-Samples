package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.RmException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/*
Duplicate literals appear as explicit file/folder paths in test cases
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RmApplicationTest {
    RmApplication app;

    String envPreviousDir;

    @TempDir
    Path workingDirectory;

    @BeforeEach
    void setUp() throws IOException {
        app = new RmApplication();
        envPreviousDir = Environment.currentDirectory;
        Environment.currentDirectory = workingDirectory.toString();

        initTestFiles();
    }

    @AfterEach
    void tearDown() {
        Environment.currentDirectory = envPreviousDir;
    }

    Path createFolder(Path root, Path path) throws IOException {
        final Path folder = root.resolve(path);
        final File object = folder.toFile();
        if (object.exists() || folder.toFile().mkdirs()) {
            return folder;
        }
        throw new IOException("Couldn't make folder: " + folder);
    }

    Path createFile(Path root, Path path) throws IOException {
        final Path file = root.resolve(path);
        if (file.toFile().createNewFile()) {
            return file;
        }
        throw new IOException("Couldn't make file: " + file);
    }

    protected static boolean doesPathExist(Path path) {
        return path.toFile().exists();
    }

    protected boolean doesPathExist(String path) {
        return doesPathExist(workingDirectory.resolve(path));
    }

    private static Stream<Arguments> provideBooleanCombinations() {
        final Boolean[] options = {null, true, false};
        final ArrayList<Arguments> arguments = new ArrayList<>(options.length * options.length);
        for (final Boolean o1 : options) {
            for (final Boolean o2 : options) {
                arguments.add(Arguments.of(o1, o2));
            }
        }
        return arguments.stream();
    }

    static String[] makeRunArguments(Boolean isEmptyFolder, Boolean isRecursive, String... fileNames) {
        final ArrayList<String> args = new ArrayList<>(2 + fileNames.length);

        if (Boolean.TRUE.equals(isEmptyFolder)) {
            args.add("-d");
        }
        if (Boolean.TRUE.equals(isRecursive)) {
            args.add("-r");
        }

        args.addAll(List.of(fileNames));

        return args.toArray(new String[]{});
    }

    void initTestFiles() throws IOException {
        final List<String> pathStrings = List.of(
                "existing-file.txt",
                "another-file.txt",
                "flat-folder-empty/",
                "flat-folder-with-two-files/existing-file.txt",
                "flat-folder-with-two-files/another-file.txt",
                "nested-folder/folder-empty/",
                "nested-folder/folder-with-two-files/existing-file.txt",
                "nested-folder/folder-with-two-files/another-file.txt"
        );
        for (final String pathString : pathStrings) {
            final Path path = Path.of(pathString);
            if (pathString.endsWith("/")) {
                createFolder(workingDirectory, path);
            } else {
                Path parent = path.getParent();
                if (parent == null) {
                    parent = workingDirectory;
                } else {
                    parent = createFolder(workingDirectory, parent);
                }
                createFile(parent, path.getFileName());
            }
        }
    }

    @Test
    void run_InvalidOption_ThrowsRmException() {
        final String invalidOption = "-x";
        final String[] args = {invalidOption, "existing-file.txt"};

        assertThrows(RmException.class, () -> {
            app.run(args, null, null);
        });
    }

    @ParameterizedTest
    @MethodSource(value = "provideBooleanCombinations")
    void run_MissingPath_ThrowsRmException(Boolean isEmptyFolder, Boolean isRecursive) {
        final String missingPath = "missing-path";
        assertFalse(doesPathExist(missingPath), "Test case is broken");

        final String[] args = makeRunArguments(isEmptyFolder, isRecursive, missingPath);

        assertThrows(RmException.class, () -> {
            app.run(args, null, null);
        });

        assertFalse(doesPathExist(missingPath));
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void run_ExistingFile_FileRemoved(Boolean isEmptyFolder, Boolean isRecursive) {
        final String toDelete = "flat-folder-with-two-files/existing-file.txt";
        final String otherFile = "flat-folder-with-two-files/another-file.txt";

        final String[] args = makeRunArguments(isEmptyFolder, isRecursive, toDelete);

        assertDoesNotThrow(() -> {
            app.run(args, null, null);
        });

        assertFalse(doesPathExist(toDelete));
        assertTrue(doesPathExist(otherFile));
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void run_MultipleExistingFiles_FilesRemoved(Boolean isEmptyFolder, Boolean isRecursive) {
        final String[] files = {
                "existing-file.txt",
                "another-file.txt",
                "flat-folder-with-two-files/existing-file.txt",
                "flat-folder-with-two-files/another-file.txt",
                "nested-folder/folder-with-two-files/existing-file.txt",
                "nested-folder/folder-with-two-files/another-file.txt"
        };

        final String[] args = makeRunArguments(isEmptyFolder, isRecursive, files);

        assertDoesNotThrow(() -> {
            app.run(args, null, null);
        });

        for (final String file : files) {
            assertFalse(doesPathExist(file));
        }
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void run_MissingPathThenExistingFile_ThrowsRmExceptionOnMissingPathSkippedExistingFileRemains(Boolean isEmptyFolder, Boolean isRecursive) {
        final String[] files = {
                "missing-path",
                "existing-file.txt"
        };
        assertFalse(doesPathExist("missing-path"), "Test case is broken");

        final String[] args = makeRunArguments(isEmptyFolder, isRecursive, files);

        assertThrows(RmException.class, () -> {
            app.run(args, null, null);
        });

        assertTrue(doesPathExist(files[1]), "rm unexpectedly deleted the existing file");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "flat-folder-empty",
            "nested-folder/folder-empty"
    })
    void run_EmptyDirectoryIsEmptyFolderTrue_DirectoryRemoved(String path) {
        final String[] args = makeRunArguments(true, false, path);

        assertDoesNotThrow(() -> {
            app.run(args, null, null);
        });

        assertFalse(doesPathExist(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "flat-folder-with-two-files",
            "nested-folder",
            "nested-folder/folder-with-two-files"
    })
    void run_NotEmptyDirectoryIsEmptyFolderTrue_ThrowsRmException(String path) {
        final String[] args = makeRunArguments(true, false, path);

        assertThrows(RmException.class, () -> {
            app.run(args, null, null);
        });
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void run_IsRecursiveTrueIsEmptyFolderIgnoredNestedDirectory_DirectoryRecursivelyRemoved(Boolean isEmptyFolder) {
        final String folder = "nested-folder";
        final String[] args = makeRunArguments(isEmptyFolder, true, folder);

        assertDoesNotThrow(() -> {
            app.run(args, null, null);
        });

        assertFalse(doesPathExist(folder));
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void remove_NullFileNames_ThrowsRmException(Boolean isEmptyFolder, Boolean isRecursive) {
        final String[] fileNames = null;

        assertThrows(RmException.class, () -> {
            app.remove(isEmptyFolder, isRecursive, fileNames);
        });

        assertTrue(doesPathExist(workingDirectory));
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void remove_EmptyFileNames_ThrowsRmException(Boolean isEmptyFolder, Boolean isRecursive) {
        final String[] fileNames = {};

        assertThrows(RmException.class, () -> {
            app.remove(isEmptyFolder, isRecursive, fileNames);
        });

        assertTrue(doesPathExist(workingDirectory));
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void remove_BlankFileName_DoesNothing(Boolean isEmptyFolder, Boolean isRecursive) {
        final String fileName = "";

        assertDoesNotThrow(() -> {
            app.remove(isEmptyFolder, isRecursive, fileName);
        });

        assertTrue(doesPathExist(workingDirectory));
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void remove_MissingPath_ThrowsRmException(Boolean isEmptyFolder, Boolean isRecursive) {
        final String missingPath = "missing-path";
        assertFalse(doesPathExist(missingPath), "Test case is broken");

        assertThrows(RmException.class, () -> {
            app.remove(isEmptyFolder, isRecursive, missingPath);
        });

        assertFalse(doesPathExist(missingPath));
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void remove_ExistingFile_FileRemoved(Boolean isEmptyFolder, Boolean isRecursive) {
        final String toDelete = "nested-folder/folder-with-two-files/existing-file.txt";
        final String otherFile = "nested-folder/folder-with-two-files/another-file.txt";

        assertDoesNotThrow(() -> {
            app.remove(isEmptyFolder, isRecursive, toDelete);
        });

        assertFalse(doesPathExist(toDelete));
        assertTrue(doesPathExist(otherFile));
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void remove_MultipleExistingFiles_FilesRemoved(Boolean isEmptyFolder, Boolean isRecursive) {
        final String[] files = {
                "existing-file.txt",
                "another-file.txt",
                "flat-folder-with-two-files/existing-file.txt",
                "flat-folder-with-two-files/another-file.txt",
                "nested-folder/folder-with-two-files/existing-file.txt",
                "nested-folder/folder-with-two-files/another-file.txt"
        };

        assertDoesNotThrow(() -> {
            app.remove(isEmptyFolder, isRecursive, files);
        });

        for (final String file : files) {
            assertFalse(doesPathExist(file));
        }
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void remove_MissingPathThenExistingFile_ThrowsRmExceptionOnMissingPathSkippedExistingFileRemains(Boolean isEmptyFolder, Boolean isRecursive) {
        final String[] files = {
                "missing-path",
                "existing-file.txt"
        };
        assertFalse(doesPathExist("missing-path"), "Test case is broken");

        assertThrows(RmException.class, () -> {
            app.remove(isEmptyFolder, isRecursive, files);
        });

        assertTrue(doesPathExist(files[1]), "rm unexpectedly deleted the existing file");
    }

    @ParameterizedTest
    @MethodSource("provideBooleanCombinations")
    void remove_ExistingFileAbsolutePath_FileRemoved(Boolean isEmptyFolder, Boolean isRecursive) {
        final String existingFile = "nested-folder/folder-with-two-files/existing-file.txt";
        final String otherFile = "nested-folder/folder-with-two-files/another-file.txt";

        final Path absolutePath = workingDirectory.resolve(existingFile).toAbsolutePath();

        assertDoesNotThrow(() -> {
            app.remove(isEmptyFolder, isRecursive, absolutePath.toString());
        });

        assertFalse(doesPathExist(absolutePath));
        assertTrue(doesPathExist(otherFile));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "flat-folder-empty",
            "nested-folder/folder-empty"
    })
    void remove_EmptyDirectoryIsEmptyFolderTrue_DirectoryRemoved(String path) {
        assertDoesNotThrow(() -> {
            app.remove(true, false, path);
        });
        assertFalse(doesPathExist(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "flat-folder-with-two-files",
            "nested-folder",
            "nested-folder/folder-with-two-files"
    })
    void remove_NotEmptyDirectoryIsEmptyFolderTrue_ThrowsRmException(String path) {
        assertThrows(RmException.class, () -> {
            app.remove(true, false, path);
        });

        assertTrue(doesPathExist(path));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {true, false})
    void remove_IsRecursiveTrueIsEmptyFolderIgnoredNestedDirectory_DirectoryRecursivelyRemoved(Boolean isEmptyFolder) {
        final String folder = "nested-folder";

        assertDoesNotThrow(() -> {
            app.remove(isEmptyFolder, true, folder);
        });

        assertFalse(doesPathExist(folder));
    }

    @Test
    void remove_DotFileName_ThrowsRmException() {
        final String currentDirectory = ".";

        assertThrows(RmException.class, () -> {
            app.remove(false, false, currentDirectory);
        });
    }

    @Test
    void remove_DotFileNameEmptyWorkingDirectoryIsEmptyFolderTrue_WorkingDirectoryDeleted() {
        final String prevWorkingDir = Environment.currentDirectory;
        Environment.currentDirectory = workingDirectory.resolve("flat-folder-empty").toString();

        final String currentDirectory = ".";

        assertDoesNotThrow(() -> {
            app.remove(true, false, currentDirectory);
        });

        assertFalse(doesPathExist(Environment.currentDirectory));
        assertTrue(doesPathExist(prevWorkingDir));
        Environment.currentDirectory = prevWorkingDir;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "flat-folder-empty",
            "flat-folder-with-two-files",
            "nested-folder/folder-empty",
            "nested-folder/folder-with-two-files",
    })
    void remove_DotFileNameWorkingDirectoryIsRecursiveTrue_WorkingDirectoryDeleted(String tempWorkingDir) {
        final String prevWorkingDir = Environment.currentDirectory;
        Environment.currentDirectory = workingDirectory.resolve(tempWorkingDir).toString();

        final String currentDirectory = ".";

        assertDoesNotThrow(() -> {
            app.remove(false, true, currentDirectory);
        });

        assertFalse(doesPathExist(Environment.currentDirectory));
        assertTrue(doesPathExist(prevWorkingDir));
        Environment.currentDirectory = prevWorkingDir;
    }
}

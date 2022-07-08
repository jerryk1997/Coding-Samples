package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class MvApplication implements MvInterface {
    private final List<String> ERROR_MESSAGES = new ArrayList<>();

    enum OperationType {
        DIRECTORY_TO_DIRECTORY,
        FILE_TO_DIRECTORY,
        FILE_TO_FILE
    }

    /**
     * Runs the mv application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws MvException {
        try {
            if (stdout == null) {
                throw new MvException(ERR_NO_OSTREAM);
            }

            MvArgsParser parser = new MvArgsParser();
            parser.parse(args);

            boolean isOverwrite = !parser.isNoOverwrite();
            String[] fileArgs = parser.getFileNames();

            if (fileArgs.length <= 1) {
                throw new MvException(ERR_NO_ARGS);
            } else if (fileArgs.length == 2) {
                mvSrcFileToDestFile(isOverwrite, fileArgs[0], fileArgs[1]);
            } else {
                mvFilesToFolder(isOverwrite,
                        fileArgs[fileArgs.length - 1],
                        Arrays.copyOf(fileArgs, fileArgs.length - 1));
            }
        } catch (MvException mve) {
            throw mve;
        } catch (Exception e) {
            throw new MvException(e, e.getMessage());
        }
        if (!ERROR_MESSAGES.isEmpty()) {
            //Prints error from moving some files when moving multiple files.
            //Since error will not cause moving of files to fail
            for (String message : ERROR_MESSAGES) {
                try {
                    stdout.write(message.getBytes());
                    stdout.write(STRING_NEWLINE.getBytes());
                } catch (IOException e) {
                    throw new MvException(ERR_WRITE_STREAM); // NOPMD - suppress PreserveStackTrace
                }
            }
        }
    }

    /**
     * renames the file named by the source operand to the destination path named by the target operand
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param srcFile     of path to source file
     * @param destFile    of path to destination file
     */
    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException, IOException {
        OperationType operationType = getOperationType(srcFile, destFile);
        Path sourcePath = IOUtils.resolveFilePath(srcFile);
        Path sourceParent = sourcePath.getParent();
        if (sourceParent == null) {
            sourceParent = Paths.get(Environment.currentDirectory);
        }
        Path destPath = IOUtils.resolveFilePath(destFile);

        if (sourcePath.equals(destPath)) {
            throw new MvException(sourcePath.getFileName().toString() +
                    " is identical to " +
                    destPath.getFileName().toString() +
                    ". (not moved)");
        }

        switch (operationType) {
            case DIRECTORY_TO_DIRECTORY:
                SimpleFileVisitor<Path> copier = getCopyDirectoryToDirectoryVisitor(isOverwrite, destPath, sourcePath, sourceParent);
                Files.walkFileTree(sourcePath, copier);
                SimpleFileVisitor<Path> deleter = getDirectoryDeleterVisitor();
                Files.walkFileTree(sourcePath, deleter);
                break;

            case FILE_TO_DIRECTORY:
                moveFile(isOverwrite, srcFile, destFile, true);
                break;

            case FILE_TO_FILE:
                moveFile(isOverwrite, srcFile, destFile, false);
                break;
        }
        return "";
    }

    /**
     * move files to destination folder
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param destFolder  of path to destination folder
     * @param fileName    Array of String of file names
     */
    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException, IOException {
        Path destPath = IOUtils.resolveFilePath(destFolder);
        if (!Files.isDirectory(destPath)) {
            throw new MvException(destFolder + " - " + ERR_IS_NOT_DIR);
        }
        for (String file : fileName) {
            try {
                mvSrcFileToDestFile(isOverwrite, file, destFolder);
            } catch (MvException mve) {
                ERROR_MESSAGES.add(mve.getMessage());
            }
        }
        return "";
    }

    private void moveFile(boolean isOverwrite, String srcFile, String destFile, boolean destIsFolder) throws IOException, MvException {
        Path srcPath = IOUtils.resolveFilePath(srcFile);
        Path destPath = IOUtils.resolveFilePath(destFile);
        if (destIsFolder) {
            //move file to folder append file name to folder path
            destPath = destPath.resolve(srcPath.getFileName());
        }

        boolean canWrite = Files.isWritable(destPath);
        if (Files.exists(destPath) && !canWrite && isOverwrite) {
            throw new MvException(ERR_NO_PERM);
        }
        if (isOverwrite) {
            try {
                Files.copy(srcPath, destPath, REPLACE_EXISTING);
            } catch (FileAlreadyExistsException fae) {
                throw new MvException(fae, fae.getMessage());
            }
        } else {
            try {
                Files.copy(srcPath, destPath);
            } catch (FileAlreadyExistsException fae) {
                throw new MvException(srcPath.getFileName().toString() + " - " + ERR_FILE_EXISTS); // NOPMD - suppress PreserveStackTrace
            }
        }
        Files.delete(srcPath);

    }

    /**
     * Returns the type of copying being done (e.g. file to file)
     *
     * @param srcFile  Path to source file
     * @param destFile Path to destination file
     * @return OperationType enum type representing  type of operation
     */
    private OperationType getOperationType(String srcFile, String destFile) throws MvException, IOException {
        Path sourcePath = IOUtils.resolveFilePath(srcFile);
        String sourceFileName = sourcePath.getFileName().toString();
        Path destPath = IOUtils.resolveFilePath(destFile);
        boolean destIsDirectory = Files.isDirectory(destPath);
        boolean srcIsDirectory = Files.isDirectory(sourcePath);

        //srcfile should match directly to a file or folder
        if (!Files.exists(sourcePath)) {
            throw new MvException(srcFile + " " + ERR_FILE_NOT_FOUND);
        }

        if (srcIsDirectory && !Files.exists(destPath)) {
            //throw new MvException(destFile + " " + ERR_FILE_NOT_FOUND);
            Files.createDirectories(destPath);
            return OperationType.DIRECTORY_TO_DIRECTORY;
        } else if (srcIsDirectory && !destIsDirectory) {
            throw new MvException(destFile + " " + ERR_IS_NOT_DIR);
        } else if (srcIsDirectory && isPresentIn(srcFile, destFile)) {
            throw new MvException("Cannot move parent directory to child directory");
        } else if (srcIsDirectory && !isPresentIn(srcFile, destFile)) {
            return OperationType.DIRECTORY_TO_DIRECTORY;
        }
        if (destIsDirectory) {
            if (".".equals(destFile)) {
                destPath = destPath.getParent();
            } else if ("..".equals(destFile)) {
                destPath = destPath.getParent().getParent();
            }
            if (destPath.equals(sourcePath.getParent())) {
                throw new MvException(srcFile + " - " + ERR_FILE_EXISTS);
            }
            return OperationType.FILE_TO_DIRECTORY;
        }

        return OperationType.FILE_TO_FILE;
    }

    /**
     * Returns true if dir1 contains dir2
     *
     * @param dir1 Directory containing dir2
     * @param dir2 Directory contained within dir1
     * @return True if dir1 contains dir2
     */
    private boolean isPresentIn(String dir1, String dir2) {
        String dir1Path = IOUtils.resolveFilePath(dir1).toString();
        String dir2Path = IOUtils.resolveFilePath(dir2).toString();
        return dir2Path.startsWith(dir1Path);
    }

    /**
     * Returns a SimpleFileVisitor to copy a whole directory into another directory
     * If source directory contains target directory, target directory is ignored
     *
     * @param target Path to target directory to copy
     * @param source Path to source directory to copy to
     * @param parent Parent directory of the source
     * @return SimpleFileVisitor to copy whole directory into another directory
     */
    private SimpleFileVisitor<Path> getCopyDirectoryToDirectoryVisitor(boolean isOverwrite, Path target, Path source, Path parent) {
        return new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                if (target.equals(source)) { //target and source is the same directory do nothing
                    return FileVisitResult.TERMINATE;
                }
                //remove path before source folder to append to target folder path
                Path directoryPathToCreate = target.resolve(parent.relativize(dir));
                try { //Attempt to create folder
                    Files.createDirectory(directoryPathToCreate);
                } catch (FileAlreadyExistsException fae) { //Folder already exists
                    if (isOverwrite) { //Delete existing folder and create new
                        SimpleFileVisitor<Path> dirDeleteVisitor = getDirectoryDeleterVisitor();
                        Files.walkFileTree(directoryPathToCreate, dirDeleteVisitor);
                        Files.createDirectory(directoryPathToCreate);
                        return FileVisitResult.CONTINUE;
                    }
                    printToStdout(parent.relativize(dir) + " " + ERR_DIR_EXISTS);
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file,
                                             final BasicFileAttributes attrs) throws IOException {
                Files.copy(file,
                        target.resolve(parent.relativize(file)), REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        };
    }

    /**
     * @return File visitor to delete entire directories
     */
    private SimpleFileVisitor<Path> getDirectoryDeleterVisitor() {
        return new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file,
                                             final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        };
    }

    private void printToStdout(String message) throws IOException {
        ERROR_MESSAGES.add(message);
    }
}

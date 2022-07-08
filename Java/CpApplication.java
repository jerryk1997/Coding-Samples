package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CpApplication implements CpInterface {
    private final LinkedList<String> errorMessages = new LinkedList<>();

    enum OperationType {
        DIRECTORY_TO_DIRECTORY,
        FILE_TO_DIRECTORY,
        FILE_TO_FILE,
    }

    /**
     * @param args   Array of arguments for the application
     * @param stdin  An InputStream, unused
     * @param stdout An InputStream, unused
     * @throws CpException if there are invalid flags or if there is a problem specified files to destination
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CpException {
        try {
            if (stdout == null) {
                throw new CpException(ERR_NO_OSTREAM);
            }
            final CpArgsParser parser = new CpArgsParser();
            parser.parse(args);

            String[] fileArgs = parser.getFileNames();


            if (fileArgs.length < 2) {
                throw new CpException(ERR_NO_ARGS);
            } else if (fileArgs.length == 2) {
                cpSrcFileToDestFile(parser.isRecursive(), fileArgs[0], fileArgs[1]);
            } else {
                cpFilesToFolder(parser.isRecursive(), parser.getDestination(), parser.getSourceFiles());
            }
        } catch (CpException cpe) {
            throw cpe;
        } catch (Exception e) {
            throw new CpException(e, e.getMessage());
        }

        if (!errorMessages.isEmpty()) {
            for (String message : errorMessages) {
                try {
                    stdout.write(message.getBytes());
                    stdout.write(STRING_NEWLINE.getBytes());
                } catch (IOException e) {
                    throw new CpException(e, ERR_WRITE_STREAM);
                }
            }
        }
    }

    /**
     * copy content of source file to destination file
     *
     * @param isRecursive Copy folders (directories) recursively
     * @param srcFile     of path to source file
     * @param destFile    of path to destination file
     * @throws Exception
     */
    @Override
    public String cpSrcFileToDestFile(Boolean isRecursive, String srcFile, String destFile) throws CpException, IOException {
        OperationType operationType = getOperationType(isRecursive, srcFile, destFile);
        Path sourcePath = IOUtils.resolveFilePath(srcFile);
        Path sourceParent = sourcePath.getParent();
        if (sourceParent == null) {
            sourceParent = Paths.get(Environment.currentDirectory);
        }
        Path destPath = IOUtils.resolveFilePath(destFile);

        if (sourcePath.equals(destPath)) {
            String fileNameSrc = sourcePath.getFileName().toString();
            String fileNameDest = destPath.getFileName().toString();
            throw new CpException(fileNameSrc + " and " + fileNameDest + " are identical (not copied)");
        }

        if (Files.exists(destPath) && !Files.isWritable(destPath)) {
            throw new CpException(destPath.getFileName().toString() + " - " + ERR_NO_PERM);
        }

        switch (operationType) {
            case DIRECTORY_TO_DIRECTORY:
                SimpleFileVisitor<Path> copier = getCopyDirectoryToDirectoryVisitor(destPath, sourceParent);
                Files.walkFileTree(sourcePath, copier);
                break;

            case FILE_TO_DIRECTORY:
                Files.copy(sourcePath, destPath.resolve(sourcePath.getFileName()), REPLACE_EXISTING);
                break;

            case FILE_TO_FILE:
                Files.copy(sourcePath, destPath, REPLACE_EXISTING);
                break;
            default:
                throw new CpException("Invalid operation type");
        }
        return "";
    }

    /**
     * Returns the type of copying being done (e.g. file to file)
     *
     * @param isRecursive Copy folders (directories) recursively
     * @param srcFile     Path to source file
     * @param destFile    Path to destination file
     * @return String representation of type of operation
     */
    private OperationType getOperationType(boolean isRecursive, String srcFile, String destFile) throws CpException, IOException {
        Path sourcePath = IOUtils.resolveFilePath(srcFile);
        Path destPath = IOUtils.resolveFilePath(destFile);
        boolean destIsDirectory = Files.isDirectory(destPath);
        boolean srcIsDirectory = Files.isDirectory(sourcePath);

        //No wildcard srcfile should match directly to a file or folder
        if (!Files.exists(sourcePath)) {
            throw new CpException(srcFile + " " + ERR_FILE_NOT_FOUND);
        }

        if (srcIsDirectory && !isRecursive) {
            throw new CpException(sourcePath.getFileName().toString() + " - Use recursive option -r or -R (not copied)");
        } else if (srcIsDirectory && !Files.exists(destPath)) {
            //throw new CpException(destFile + " " + ERR_FILE_NOT_FOUND);
            Files.createDirectory(destPath);
            return OperationType.DIRECTORY_TO_DIRECTORY;
        } else if (srcIsDirectory && !destIsDirectory) {
            throw new CpException(destFile + " " + ERR_IS_NOT_DIR);
        } else if (srcIsDirectory) {
            return OperationType.DIRECTORY_TO_DIRECTORY;
        }

        if (Files.exists(destPath) && !Files.isWritable(destPath)) {
            throw new CpException(ERR_NO_PERM);
        }

        if (destIsDirectory) {
            return OperationType.FILE_TO_DIRECTORY;
        }

        return OperationType.FILE_TO_FILE;
    }

    /**
     * copy files to destination folder
     *
     * @param isRecursive Copy folders (directories) recursively
     * @param destFolder  of path to destination folder
     * @param fileName    Array of String of file names
     */
    @Override
    public String cpFilesToFolder(Boolean isRecursive, String destFolder, String... fileName) throws CpException, IOException {
        Path destPath = IOUtils.resolveFilePath(destFolder);
        if (!Files.isDirectory(destPath)) {
            throw new CpException(destFolder + " " + ERR_IS_NOT_DIR);
        }
        for (String file : fileName) {
            try {
                cpSrcFileToDestFile(isRecursive, file, destFolder);
            } catch (CpException cpe) { //one copy error should not short circuit the whole operation
                errorMessages.add(cpe.getMessage());
            }
        }
        return "";
    }

    /**
     * Returns a SimpleFileVisitor to copy a whole directory into another directory
     * If source directory contains target directory, target directory is ignored
     *
     * @param target Path to target directory to copy
     * @param parent Parent directory of the source
     * @return SimpleFileVisitor to copy whole directory into another directory
     */
    private SimpleFileVisitor<Path> getCopyDirectoryToDirectoryVisitor(Path target, Path parent) {
        return new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                if (dir.equals(target)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }


                Path directoryPathToCreate = target.resolve(parent.relativize(dir));
                try {
                    Files.createDirectory(directoryPathToCreate);
                } catch (FileAlreadyExistsException fae) {
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

    private void printToStdout(String message) {
        errorMessages.add(message);
    }
}

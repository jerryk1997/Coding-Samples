package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;

public class RmApplication implements RmInterface {
    /**
     * Runs the rm application.
     *
     * @param args   Array of arguments for the application
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws RmException If there are invalid args or there is a problem removing the specified file names
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws RmException {
        try {
            final RmArgsParser argsParser = new RmArgsParser();
            argsParser.parse(args);

            remove(argsParser.isEmptyFolder(), argsParser.isRecursive(), argsParser.getFileNames());
        } catch (RmException rme) {
            throw rme;
        } catch (Exception exc) {
            throw new RmException(exc.getMessage(), exc);
        }
    }

    /**
     * Remove the file. (It does not remove folder by default)
     *
     * @param isEmptyFolder Boolean option to delete a folder only if it is empty
     * @param isRecursive   Boolean option to recursively delete the folder contents (traversing
     *                      through all folders inside the specified folder)
     * @param fileNames     Array of String of file names
     * @throws RmException If there is a problem removing the specified file names
     */
    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileNames) throws RmException {
        if (fileNames == null || fileNames.length == 0) {
            throw new RmException(ERR_NO_ARGS);
        }
        isEmptyFolder = Objects.requireNonNullElse(isEmptyFolder, false); //NOPMD - suppressed AvoidReassigningParameters - Don't remove empty directories if null is provided
        isRecursive = Objects.requireNonNullElse(isRecursive, false); //NOPMD - suppressed AvoidReassigningParameters - Assume not recursive if null is provided

        try {
            for (final String fileName : fileNames) {
                if (fileName.isEmpty()) {
                    continue;
                }

                final Path path = convertToAbsolutePath(fileName);
                final File object = path.toFile();
                if (!object.exists()) { //NOPMD - suppressed ConfusingTernary - Condition checks that the object does not exist
                    throw new RmException(fileName + ": No such file or directory");
                } else if (object.isFile()) {
                    delete(path);
                } else if (object.isDirectory()) {
                    if (isRecursive) {
                        Files.walkFileTree(path, rmRecursively);
                    } else if (isEmptyFolder && isEmptyDirectory(path)) {
                        delete(path);
                    } else {
                        throw new RmException(fileName + ": is a directory");
                    }
                }
            }
        } catch (RmException rme) {
            throw rme;
        } catch (Exception exc) {
            throw new RmException(exc.getMessage(), exc);
        }
    }

    /**
     * Deletes a file.
     *
     * @param path The path to the file to delete
     * @throws IOException If an I/O error occurs
     */
    private static void delete(Path path) throws IOException {
        Files.delete(path);
    }

    /**
     * Converts filename to absolute path, if initially was relative path
     *
     * @param fileName supplied by user
     * @return a Path of the absolute path of the filename
     */
    private Path convertToAbsolutePath(String fileName) {
        return IOUtils.resolveFilePath(fileName).normalize().toAbsolutePath();
    }

    /**
     * Checks if a directory is empty.
     *
     * @param path The path to the directory to check
     * @return true if and only if the directory is empty
     * @throws IOException If an I/O error occurs
     */
    private static boolean isEmptyDirectory(Path path) throws IOException {
        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
            return !directoryStream.iterator().hasNext();
        }
    }

    /**
     * A SimpleFileVisitor that recursively deletes all files and folders.
     */
    // PMD gives an "error while parsing" for the following block of code, may need to comment it out to get PMD to run
    private static final SimpleFileVisitor<Path> rmRecursively = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            RmApplication.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            RmApplication.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };
}

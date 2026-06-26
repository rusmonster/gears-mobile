package app.constructor.csdk.zip.api

/** Packages a collection of files into a single zip archive. */
interface ZipPacker {

    /**
     * Creates a zip archive at [outputPath] containing all files listed in [inputFilePaths].
     *
     * Each file is stored using its filename as the zip entry name, so callers must ensure
     * that no two paths share the same filename.
     *
     * @param inputFilePaths Absolute paths of the files to include. All files must exist;
     *   passing a path that does not exist on disk is a programming error and will throw.
     * @param outputPath Absolute path where the zip archive will be written.
     */
    fun pack(inputFilePaths: List<String>, outputPath: String)
}

### SDF-4: Develop the SDFApplication class
- **Given** a user has installed the Smart Duplicate Finder application,
- **When** the user runs the application with command-line arguments specifying the mode and paths to scan,
- **Then** the SDFApplication class should parse these arguments and initiate the scanning process with the specified parameters.

### SDF-5: Implement the DuplicateFinder class
- **Given** the application is executed with valid directory paths,
- **When** the DuplicateFinder class is invoked to scan these directories,
- **Then** it should identify and group duplicate files based on their content, disregarding identical files in different locations as duplicates.

### SDF-6: Create HashingStrategy and AdaptiveHashing
- **Given** a file needs to be hashed during the scanning process,
- **When** the AdaptiveHashing class is used based on the HashingStrategy interface,
- **Then** it should select an appropriate hashing algorithm based on the file's characteristics (e.g., size, type) to efficiently compute its hash.

### SDF-7: Develop DirectoryComparator and FileComparator
- **Given** two directories or files need to be compared,
- **When** the DirectoryComparator or FileComparator is invoked,
- **Then** it should accurately determine whether the directories contain the same files or if the files are identical, respectively, based on their content hashes.

### SDF-8: Implement ReportGenerator
- **Given** the scanning process has identified duplicates and unique files,
- **When** the ReportGenerator is invoked,
- **Then** it should format and output the results, categorizing findings into duplicates, uniques, and directories containing duplicates, in a clear and readable format.

### Utility Services

### SDF-9: Develop FileUtils
- **Given** file operations like reading or hashing are required,
- **When** methods from FileUtils are utilized,
- **Then** they should perform the necessary file operations efficiently and return the expected outcomes, such as file contents or hash values.

### SDF-10: Implement Logger
- **Given** the application needs to log information, warnings, or errors,
- **When** the Logger utility is used throughout the application,
- **Then** it should log messages appropriately based on the log level (info, warning, error) and the verbose mode setting, ensuring important information is recorded.

### SDF-11: Create ErrorHandler
- **Given** an error occurs during the application's execution (e.g., invalid arguments, inaccessible directories),
- **When** the ErrorHandler is invoked,
- **Then** it should provide a user-friendly error message, suggesting possible actions to resolve the issue, and log the error details for debugging purposes.

# Smart Duplicate Finder (SDF)

## Overview
SDF is a robust tool designed for efficient duplicate file detection. It employs a layered approach and integrates cryptographic checks to ensure accurate identification of duplicates.

## Features
- **Intelligent Directory Comparison**: Streamlines duplicate detection by focusing on entire directories before individual files.
- **Adaptive File Hashing**: Utilizes different hashing strategies based on file size and type for optimized performance.
- **Cryptographic Verification**: Enhances reliability of duplicate detection with cryptographic hash functions.

## Getting Started

### Prerequisites
- Java JDK 17+
- Gradle

### Installation
Clone the repo and navigate to the project directory:
```bash
git clone https://github.com/patelgr/SmartDuplicateFinder.git
cd SmartDuplicateFinder
```
Build the project with Gradle:
```bash
gradle build
```

### Usage
**Dry Run Mode**: High-Level Directory Comparison (Dry Run Mode):

```bash
java -jar build/libs/sdf.jar -mode dryrun -path "/path/to/directory"
```
This command identifies duplicate directories within the specified path but doesn't report individual file duplicates.

**Fine-Grained File Comparison**: Performs in-depth file comparison.
```bash
java -jar build/libs/sdf.jar -mode detailed -path "/path/to/directory"
```
This command performs a comprehensive analysis, comparing individual files and reporting all duplicate matches.


### Error Handling:

SDF will exit with an error code and provide a message if it encounters issues like invalid arguments, inaccessible directories, or unexpected file system errors.

#### Real-World Scenarios:

Decluttering Downloads Folder: Use SDF to identify and remove duplicate downloaded files, freeing up disk space.
Backing Up Important Data: Ensure data integrity by comparing backup directories with the original source using SDF.
Managing Large Media Collections: Streamline media organization by efficiently detecting duplicate photos, music files, or videos.
Command Options:

### Help
-h or --help: Displays help information about available options and usage.
-v or --verbose: Enables verbose output with additional details during execution.

## Contributing
Contributions are welcome. Please fork the repository, make your changes, and submit a pull request.

## License
Distributed under Creative Commons Attribution-NonCommercial-NoDerivs 4.0 International. Visit [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/) for more information.

## Potential Algorithm Alternatives:

While the current approach is effective, consider exploring alternative algorithms for specific scenarios:

- **Locality-Sensitive Hashing (LSH)**: Efficient for identifying similar but not identical files, potentially useful for media comparisons.
- **Simhash**: Suitable for large datasets, offering efficient approximate similarity detection.
- https://github.com/dynatrace-oss/hash4j
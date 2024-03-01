# Smart Duplicate Finder (SDF)

## Overview
Smart Duplicate Finder (SDF) is an advanced tool designed to identify and report duplicate files and directories across specified storage areas. Utilizing state-of-the-art hashing and cryptographic techniques alongside intelligent scanning strategies, SDF offers precise detection with optimized performance, ensuring minimal false positives.

## Features
- **Multiple Directory Scanning**: Allows specifying multiple directories for scanning, focusing the analysis on targeted areas without scanning the entire filesystem.
- **Adaptive Hashing Mechanism**: Adapts hashing strategies based on file characteristics, balancing efficiency and effectiveness in duplicate detection.
- **Cryptographic Integrity Checks**: Uses cryptographic hash functions to ensure the accuracy of identified duplicates, safeguarding against false matches.
- **Intelligent Directory Analysis**: Employs directory-level analysis to quickly pinpoint potential duplicates, reducing the need for exhaustive file-by-file comparisons.
- **Aggregated Duplicate Reporting**: Identifies file duplicates first, then aggregates these to report directory-level duplicates, minimizing report redundancy and focusing on significant duplicates.

## Getting Started

### Prerequisites
- Java JDK 17 or newer
- Gradle

### Installation
Clone the repository and build the project using Gradle to set up SDF:

```bash
git clone https://github.com/patelgr/SmartDuplicateFinder.git
cd SmartDuplicateFinder
gradle build
```

### Usage
SDF can be operated in different modes for varying levels of analysis:

- **Dry Run Mode**: Offers an overview of potential duplicate directories, ideal for a preliminary scan.
    ```bash
    java -jar build/libs/sdf.jar -mode dryrun -paths "/path/to/scan1,/path/to/scan2"
    ```

- **Detailed Mode**: Performs a thorough file and directory comparison, identifying all duplicates within the specified paths.
    ```bash
    java -jar build/libs/sdf.jar -mode detailed -paths "/path/to/scan1,/path/to/scan2"
    ```

Paths should be comma-separated for scanning multiple directories.

### Error Handling
SDF gracefully manages errors, providing clear messages for issues like invalid arguments, inaccessible paths, or filesystem errors.

### Real-World Applications
- **Download Folder Organization**: Quickly clean up your download folder by eliminating redundant files.
- **Data Backup Verification**: Ensure the integrity of your backups by comparing them against the original data.
- **Media Library Cleanup**: Organize large media libraries efficiently by removing duplicate content.

### Command-Line Options
- `-h` or `--help`: Displays help information with all available commands.
- `-v` or `--verbose`: Activates verbose mode for comprehensive operation logs.

## Contributing
We welcome contributions to SDF. Please fork the repository, make your changes, and submit a pull request for review.

## License
SDF is available under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. Full license details are available at [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/).

## Exploring Further
For different scenarios or environments, you might want to explore alternative algorithms such as Locality-Sensitive Hashing (LSH) for large media files or Simhash for vast datasets. Visit [Hash4j](https://github.com/dynatrace-oss/hash4j) for more on these algorithms.

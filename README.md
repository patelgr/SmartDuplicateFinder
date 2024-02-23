# Smart Duplicate Finder (SDF)

## Overview
Smart Duplicate Finder (SDF) is a sophisticated tool designed to accurately identify and report duplicate files across your storage. Leveraging advanced hashing and cryptographic techniques, SDF ensures precise detection while optimizing performance.

## Features
- **Intelligent Directory Analysis**: Prioritizes directory-level comparisons to quickly identify potential duplicates before diving into file-level analysis.
- **Adaptive Hashing Mechanism**: Dynamically adjusts hashing strategies based on file characteristics, ensuring efficient and effective comparison.
- **Cryptographic Integrity Checks**: Employs cryptographic hash functions to guarantee the accuracy of duplicate detection, minimizing false positives.

## Getting Started

### Prerequisites
- Java JDK 17 or newer
- Gradle

### Installation
To get started with SDF, clone the repository and build the project using Gradle:

```bash
git clone https://github.com/patelgr/SmartDuplicateFinder.git
cd SmartDuplicateFinder
gradle build
```

### Usage
SDF can be run in different modes depending on the level of analysis required:

- **Dry Run Mode**: Provides a high-level overview of duplicate directories without delving into file-specific duplicates.
    ```bash
    java -jar build/libs/sdf.jar -mode dryrun -path "/path/to/scan"
    ```

- **Detailed Mode**: Conducts an in-depth comparison, identifying all duplicate files within the specified directory.
    ```bash
    java -jar build/libs/sdf.jar -mode detailed -path "/path/to/scan"
    ```

### Error Handling
SDF is designed to handle errors gracefully, providing meaningful messages for issues such as invalid arguments, inaccessible directories, or file system errors.

### Real-World Applications
- **Organizing Downloads**: Quickly declutter your downloads folder by removing redundant files.
- **Data Backup Verification**: Use SDF to compare backup directories against the original data, ensuring backup integrity.
- **Media Library Management**: Efficiently organize large collections of photos, music, and videos by removing duplicates.

### Command-Line Options
- `-h` or `--help`: Displays the help menu with information about all available commands.
- `-v` or `--verbose`: Enables verbose mode for detailed operation logs.

## Contributing
Contributions to SDF are highly encouraged. Please fork the repository, commit your changes, and submit a pull request for review.

## License
SDF is distributed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. Full license details can be found at [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/).

## Exploring Further
For environments or use cases requiring different approaches, consider investigating alternative algorithms such as:
- **Locality-Sensitive Hashing (LSH)**: Ideal for detecting similar content, particularly in large media files.
- **Simhash**: Offers efficient similarity detection suitable for vast datasets, often used in document fingerprinting.

Visit [Hash4j](https://github.com/dynatrace-oss/hash4j) for more information on implementing these algorithms.

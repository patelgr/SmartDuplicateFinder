### Story 1: User Initiates Directory Scan
**Given** a user has accessed the SDF web interface  
**When** the user selects one or multiple directories for scanning and initiates the scan  
**Then** the system starts scanning the selected directories, and the user sees a confirmation message with a real-time progress indicator on the web interface.

### Story 2: Display Real-Time Scanning Results
**Given** a directory scan is in progress  
**When** new duplicates are identified or the status of the scan changes  
**Then** the web interface updates in real-time to show the current progress, including the number of files scanned, the number of duplicates found, and any other relevant statistics.

### Story 3: User Views Detailed Duplicate Information
**Given** the scan has identified duplicate files or directories  
**When** the user clicks on a summary entry in the results table  
**Then** the system displays a detailed comparison of the duplicate files or directories, including file sizes, paths, and potentially a side-by-side content comparison for files.

### Story 4: Real-Time Updates via WebSocket
**Given** a user is viewing the scan results page  
**When** the backend process identifies new duplicates or updates the scan status  
**Then** the frontend receives these updates in real-time through a WebSocket connection and updates the UI accordingly without requiring a page refresh.

### Story 5: User Manages Duplicate Files
**Given** the user is presented with a list of duplicate files  
**When** the user selects specific duplicates for deletion or preservation  
**Then** the system performs the chosen file management actions and updates the database and UI to reflect these changes.

### Story 6: Scan Completion and Summary Report
**Given** a scan has completed  
**When** all files in the selected directories have been processed  
**Then** the system generates a summary report of the scan, including the total number of duplicates found, space that can be freed, and provides options for downloading or sharing the report.

### Story 7: Error Handling and User Notifications
**Given** an error occurs during the scan (e.g., inaccessible directory, read permission error)  
**When** the system encounters such an error  
**Then** the user is notified through the web interface with a clear, non-technical description of the issue and suggested actions.

### Story 8: User Configures Scan Settings
**Given** a user wants to customize the scan parameters  
**When** the user accesses the settings page and selects their preferences (e.g., scan depth, hashing method)  
**Then** the system updates the scan configurations according to the user's selections and uses these settings for future scans.


### Landing Page User Story

**Story 9: User Accesses the Landing Page**

**Given** a user wants to use the Smart Duplicate Finder  
**When** the user navigates to the SDF web application  
**Then** the user is presented with a landing page that includes:
- A brief introduction to the SDF tool and its benefits.
- Instructions on how to start a scan, including selecting directories.
- Optional settings for the scan, such as scan depth or specific hashing methods.
- A "Start Scan" button to initiate the scanning process.

### Comparison Page User Story

**Story 10: User Reviews and Manages Duplicate Files on the Comparison Page**

**Given** the scan has completed and the user wants to review the duplicates  
**When** the user selects a specific set of duplicates from the results summary  
**Then** the user is taken to a comparison page that includes:
- Detailed information about the duplicates, including file names, sizes, and paths.
- A side-by-side comparison view for duplicate files, showing content differences if applicable.
- Options to mark duplicates for deletion or preservation.
- A "Perform Action" button to execute the selected file management tasks.

### Additional Considerations for the Comparison Page

- **Real-Time Updates:** If the backend continues to find duplicates after the initial results are displayed, the comparison page should update in real-time to reflect new findings.
- **Navigation:** Users should be able to easily navigate back to the summary results and select another set of duplicates for detailed review.
- **Bulk Actions:** For efficiency, provide options to select and manage multiple duplicates at once, such as "Select All" or "Delete All Except One".


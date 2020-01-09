# WSL Recycler
## Synopsis
A utility to recycle files rather than to delete them permanently from the disk.

## Note:
##### Active development of this project has been ceased in favor of the project named 'Trash' (https://github.com/srv-code/Trash).
This program seems not to be working reliably, especially in Windows Subsystem for Linux (WSL), as java.io.File#renameTo won't work across heterogeneous file systems.  
Solutions can be to use java.nio classes as they work platform independently.  
But its always better to use a native solution like using shell scripts for building this kind of small utilities, development time will be much lesser and guaranteed to work reliably.  
A similar utility program have been developed using a Bourne-Again shell script with optimised working architecture. 
Active development should be carried out to this project named Trash. Link: https://github.com/srv-code/Trash

## Features
- Option to enable verbose mode i.e. to show the information about each major steps being carried out.
- Option to flush the trash bin i.e. to delete all trashed data permanently.
- Option to show the log file containing the information about the parent directories from where the each set of files are trashed which is useful to restore them back again.
- Option to restore back the trashed set of files back to their parent directories.

### Default behavior
- Verbose mode is disabled.
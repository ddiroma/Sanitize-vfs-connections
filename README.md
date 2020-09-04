# Sanitize-vfs-connections

Ensure the the .bat or .sh script is run from this directory as it will use ./target/RemoveEmbeddedVFSConnections-1.0-SNAPSHOT.jar

**USAGE FOR WINDOWS:**

 USAGE: .\SanitizeVFSConnections.bat [OPTION] [FILE|DIRECTORY]
 
 
 [OPTION]: only 1 required
         
         -f:        designates next argument is a single file
         -d:        designates next argument is a single directory (no recursion desired)
         -r:        designates next argument is a single directory (recursion desired^)
     --help:	       shows this help message

 [FILE^|DIRECTORY]: Absolute path to file or directory
 
       file:        if -f precedes, a single file using an absolute path surrounded with quotation marks. File type must be .ktr or .kjb.
       directory:   if -d or -r precedes, a single directory using absolute path surrounded with quotation marks


 Examples:
 
    .\sanitizeVFSConnections.sh -f "C:\Users\username\Documents\directory\file.ktr"   sanitizes file.ktr only
    .\sanitizeVFSConnections.sh -d "\Users\username\Documents\directory"              sanitizes any .ktr and .kjb file in the directory
    .\sanitizeVFSConnections.sh -r "\Users\username\Documents\directory"              sanitizes any .ktr and .kjb file in the directory and is recursive




**USAGE FOR MAC/LINUX:**

USAGE: ./sanitizeVFSConnections.sh [OPTION] [FILE|DIRECTORY]

[OPTION]: only 1 required

    -f:          designates next argument is a single file
    -d:          designates next argument is a single directory (no recursion desired)
    -r:          designates next argument is a single directory (recursion desired)
    --help:		    shows this help message

[FILE|DIRECTORY]: Absolute path to file or directory

    file:           if -f precedes, a single file using an absolute path. File type must be .ktr or .kjb.
    directory:      if -d or -r precedes, a single directory using absolute path


Examples:

    ./sanitizeVFSConnections.sh -f /Users/username/Documents/directory/file.ktr     sanitizes file.ktr only
    ./sanitizeVFSConnections.sh -d /Users/username/Documents/directory/             sanitizes any .ktr and .kjb file in <directory>
    ./sanitizeVFSConnections.sh -r /Users/username/Documents/directory/             sanitizes any .ktr and .kjb file in <directory> and is recursive
 
 

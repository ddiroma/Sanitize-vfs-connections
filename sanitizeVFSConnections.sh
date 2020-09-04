#!/bin/sh

# *****************************************************************************
#
# Pentaho Data Integration
#
# Copyright (C) 2020 by Hitachi Vantara : http://www.hitachivantara.com
#
# *****************************************************************************
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# *****************************************************************************

_PENTAHO_JAVA_HOME=$JAVA_HOME
_PENTAHO_JAVA=$_PENTAHO_JAVA_HOME/bin/java

show_usage() {
echo "
VFS Connection Sanitation tool:

USAGE: ./sanitizeVFSConnections.sh [OPTION] [FILE|DIRECTORY]

[OPTION]: only 1 required
    -f:             designates next argument is a single file
    -d:             designates next argument is a single directory (no recursion desired)
    -r:             designates next argument is a single directory (recursion desired)
--help:		    shows this help message

[FILE|DIRECTORY]: Absolute path to file or directory
file:           if -f precedes, a single file using an absolute path. File type must be .ktr or .kjb.
directory:      if -d or -r precedes, a single directory using absolute path


Examples:
./sanitizeVFSConnections.sh -f /Users/username/Documents/directory/file.ktr     sanitizes file.ktr only
./sanitizeVFSConnections.sh -d /Users/username/Documents/directory/             sanitizes any .ktr and .kjb file in <directory>
./sanitizeVFSConnections.sh -r /Users/username/Documents/directory/             sanitizes any .ktr and .kjb file in <directory> and is recursive
";
}

DIR=`pwd`

if [ "$#" == 1 ] && [ "$1" == "--help" ]; then
  show_usage;
  exit 0;
fi

if [ "$#" -ne 2 ]; then
  show_usage;
  exit 1;
fi

if [ "$1" != "-f" ] && [ "$1" != "-d" ] && [ "$1" != "-r" ]; then
  echo "WARNING: Incorrect Arguments";
  show_usage;
  exit 1;
fi

"$_PENTAHO_JAVA" -Xmx2048m -classpath "$DIR/target/*" com.pentaho.embeddedmetastore.util.EmbeddedMetastoreUtil $@

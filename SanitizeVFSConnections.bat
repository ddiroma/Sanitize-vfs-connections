@echo off
REM *****************************************************************************
REM
REM Pentaho Data Integration
REM
REM Copyright (C) 2020 by Hitachi Vantara : http://www.hitachivantara.com
REM
REM *****************************************************************************
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with
REM the License. You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM
REM *****************************************************************************

set _PENTAHO_JAVA_HOME=%JAVA_HOME%
set _PENTAHO_JAVA=%_PENTAHO_JAVA_HOME%\bin\java
set dir="%~dp2%~nx2"

REM if user enters help
if [%1] EQU [--help] call :show_usage && EXIT /B 0

REM if user enters more than 2 arguments
if [%1] NEQ [] if [%dir%] NEQ [""] if [%3] NEQ [] call :show_usage && EXIT /B 1

REM if user enters no arguments
if [%1] EQU [] call :show_usage && EXIT /B 1

REM if user enters only 1 argument
if [%1] NEQ [] if [%dir%] EQU [""] call :show_usage && EXIT /B 1

REM if user enters no quotations around directory (2nd argument)
if [%dir%] NEQ ["%~2"] call :show_usage && EXIT /B 1

REM if user's 1st argument is no -f, -d, or -r
if [%1] NEQ [-f] if [%1] NEQ [-d] if [%1] NEQ [-r] call :show_usage && EXIT /B 1


"%_PENTAHO_JAVA%" -Xmx2048m -classpath "%~dp0*" com.pentaho.embeddedmetastore.util.EmbeddedMetastoreUtil %1 %dir%
EXIT /B 0


:show_usage
echo VFS Connection Sanitation tool:
echo.
echo USAGE: .\SanitizeVFSConnections.bat [OPTION] [FILE^|DIRECTORY]
echo.
echo [OPTION]: only 1 required
echo         -f:        designates next argument is a single file
echo         -d:        designates next argument is a single directory (no recursion desired^)
echo         -r:        designates next argument is a single directory (recursion desired^)
echo     --help:	shows this help message
echo.
echo [FILE^|DIRECTORY]: Absolute path to file or directory
echo       file:        if -f precedes, a single file using an absolute path surrounded with quotation marks. File type must be .ktr or .kjb.
echo  directory:        if -d or -r precedes, a single directory using absolute path surrounded with quotation marks
echo.
echo.
echo Examples:
echo .\sanitizeVFSConnections.sh -f "C:\Users\username\Documents\directory\file.ktr"     sanitizes file.ktr only
echo .\sanitizeVFSConnections.sh -d "\Users\username\Documents\directory"             sanitizes any .ktr and .kjb file in the directory
echo .\sanitizeVFSConnections.sh -r "\Users\username\Documents\directory"             sanitizes any .ktr and .kjb file in the directory and is recursive
EXIT /B 0

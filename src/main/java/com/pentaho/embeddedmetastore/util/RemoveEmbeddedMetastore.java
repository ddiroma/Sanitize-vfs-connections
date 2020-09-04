/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.pentaho.embeddedmetastore.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class RemoveEmbeddedMetastore {
  private static Logger logger = Logger.getLogger( RemoveEmbeddedMetastore.class.getName() );
  private static final String KTR = ".ktr";
  private static final String KJB = ".kjb";
  private static final String PATH_SEP_LINUX = "/";
  private static final String PATH_SEP_WINDOWS = "\\";
  private static final String BACKUP_EXT = ".bak";

  private boolean isWindows = false;
  private List<String> allDirectories = new ArrayList<>();  // List of all directories examined
  private List<String> allFiles = new ArrayList<>();        // List of all files examined
  private List<String> sanitizedFiles = new ArrayList<>();  // List of any files that are sanitized
  private List<String> unchangedFiles = new ArrayList<>();  // List of any files that remained unchanged
  private List<String> backupFiles = new ArrayList<>();     // List of all files where a backup was created .bak
  private List<String> skippedFiles = new ArrayList<>();    // List of any file that was skipped (non .ktr or .kjb)
  private List<String> problemFiles = new ArrayList<>();    // List of any file that had a problem while sanitizing

  /**
   * Checks file type of File file. If .ktr or .kjb, returns true. Else returns false
   * @param file - file to check type
   * @return - true if .ktr or .kjb
   */
  private boolean isCorrectFileType( File file ) {
    String filename = file.getName();
    if ( filename.endsWith( KTR ) || filename.endsWith( KJB ) ) {
      logger.info( "File " + file.getPath() + " is a .ktr or .kjb....proceeding." );
      return true;
    }
    skippedFiles.add( file.getPath() );
    logger.info( "Skipping this file. It is not of type " + KTR + " or " + KJB + ": " + file.getPath() );
    return false;
  }

  /**
   * Called if only single file is entered
   * @param file - file
   */
  void handleFile( File file ) {
    allFiles.add( file.getPath() );
    if ( isCorrectFileType( file ) && copyFile( file ) ) {
      parseFile( file );
    }
  }

  /**
   * Called if only single directory is entered
   * @param directory - directory
   * @param isRecursive - true to recurse, false to not
   */
  void handleDirectory( File directory, boolean isRecursive ) {
    // Check for null on listFiles
    if ( directory.listFiles() == null ) {
      return;
    }
    List<File> files = new ArrayList<>( Arrays.asList( directory.listFiles() ) );
    allDirectories.add( directory.getPath() );
    for ( File f : files ) {
      if ( f.isFile() ) {
        allFiles.add( f.getPath() );
        if ( isCorrectFileType( f ) && copyFile( f ) ) {
          parseFile( f );
        }
      } else if ( f.isDirectory() && isRecursive ) {
        handleDirectory( f, true );
      }
    }
  }

  /**
   * Parse the xml file to remove VFS connection information
   * @param file - file to parse
   */
  private void parseFile( File file ) {
    logger.info( "Parsing file: " + file.getName() );
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    List<Node> nodesToRemove = new ArrayList<>();
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.parse( file );
      document.getDocumentElement().normalize();
      Element documentElement = document.getDocumentElement();
      // Add name space to root element as attribute
      documentElement.setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
      NodeList nList = document.getElementsByTagName( "attributes" );
      for ( int k = 0; k < nList.getLength(); k++ ) {
        NodeList aList = nList.item( k ).getChildNodes();
        for ( int i = 0; i < aList.getLength(); i++ ) {
          NodeList groupNodes = aList.item( i ).getChildNodes();
          for ( int j = 0; j < groupNodes.getLength(); j++ ) {
            Node childNode = groupNodes.item( j );
            if ( childNode.getNodeName().equalsIgnoreCase( "name" )
              && childNode.getTextContent().contains( "Embedded MetaStore Elements" ) ) {
              nodesToRemove.add( childNode.getParentNode() );
            }
          }
        }
      }
      if ( nodesToRemove.isEmpty() ) {
        logger.info( "File did not contain VFS Connection information so it was not touched: " + file.getPath() );
        unchangedFiles.add( file.getPath() );
        return; // no need to rewrite file
      }

      for ( Node node : nodesToRemove ) {
        Node parent = node.getParentNode();
        parent.removeChild( node );
      }
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource( document );
      StreamResult result = new StreamResult( new File( file.getPath() ) );

      transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
      transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
      transformer.setOutputProperty( OutputKeys.STANDALONE, "yes" );
      transformer.transform( source, result );
      sanitizedFiles.add( file.getPath() );
    } catch ( Exception e ) {
      problemFiles.add( file.getPath() );
      logger.warning( "There was an error correcting this file and it may have been corrupted. Please see the "
        + "backup file: " + file.getPath() + BACKUP_EXT );
    }
  }

  /**
   * Makes backup file with .bak extension
   * @param file - file to copy
   * @return - true if successful copy, false otherwise
   */
  private boolean copyFile( File file ) {
    logger.info( "Copying this file before parsing: " + file.getPath() );
    try {
      File newFile = ( new File( file.getParent()
        + ( this.isWindows ? PATH_SEP_WINDOWS : PATH_SEP_LINUX ) + file.getName() + BACKUP_EXT ) );
      Files.copy( file.toPath(), newFile.toPath() );
      logger.info( "File: " + file.getPath() + " copied to: " + newFile.getPath() );
      backupFiles.add( newFile.getPath() );
      return true;
    } catch ( Exception e ) {
      skippedFiles.add( file.getPath() );
      logger.warning( "SKIPPING COPY-->Unable to copy:" + file.getPath() );
    }
    return false;
  }

  /**
   * Shows final results from util
   */
  void showResultSummary() {
    String msg = "\n\n\n\n\nHere is a summary of what happened:\n\n";

    // Directories if directory entered:
    if ( !allDirectories.isEmpty() ) {
      msg += appendListsToOutput( "LIST OF ALL DIRECTORIES", allDirectories );
    }

    // All files uncensored
    msg += appendListsToOutput( "LIST OF ALL FILES WHETHER TOUCHED OR NOT", allFiles );

    // Unchanged Files:
    msg += appendListsToOutput( "LIST OF UNCHANGED FILES", unchangedFiles );

    // Skipped Files:
    msg += appendListsToOutput( "LIST OF SKIPPED FILES", skippedFiles );

    // Problem Files:
    msg += appendListsToOutput( "LIST OF PROBLEM FILES TO REFER TO THEIR CORRESPONDING BACKUP FILE (.bak)", problemFiles );

    // Backed-up Files:
    msg += appendListsToOutput( "LIST OF BACK-UP FILES", backupFiles );

    // Changed Files:
    msg += appendListsToOutput( "LIST OF SANITIZED FILES", sanitizedFiles );
    logger.info( msg );
  }

  /**
   * Formats output for all file/directory lists
   * @param headerText - Text to print with header of each list
   * @param list - list for output
   * @return - formatted output of list with header
   */
  private String appendListsToOutput( String headerText, List<String> list ) {
    StringBuilder sb = new StringBuilder();
    sb.append( "\n\n*********** " );
    sb.append( headerText );
    sb.append( " ***********\n" );
    for ( int i = 0; i < list.size(); i++ ) {
      sb.append( ( i + 1 ) );
      sb.append( ". " );
      sb.append( list.get( i ) );
      sb.append( "\n" );
    }
    return sb.toString();
  }

  void setWindows( boolean windows ) {
    isWindows = windows;
  }
}

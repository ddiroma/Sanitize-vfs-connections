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

import java.io.File;
import java.util.logging.Logger;

public class EmbeddedMetastoreUtil {
  private static Logger logger = Logger.getLogger( EmbeddedMetastoreUtil.class.getName() );

  public static void main( String [] args ) {
    if ( args == null || args.length != 2 ) {
      logger.warning( "Arguments are incorrect, exiting system without sanitizing anything" );
      System.exit( 1 );
    }
    String recursive = args[0];
    String strFile = args[1];

    String notice = "Arguments- Type: " + recursive + " File/Directory: " + strFile;
    logger.info( notice );

    File file = new File( strFile );
    if ( file.exists() && file.isFile() ) {
      RemoveEmbeddedMetastore removeEmbeddedMetastore = new RemoveEmbeddedMetastore();
      removeEmbeddedMetastore.setWindows( isWindows() );
      removeEmbeddedMetastore.handleFile( file );
      removeEmbeddedMetastore.showResultSummary();
    } else if ( file.exists() && file.isDirectory() ) {
      boolean isRecursive = false;
      if ( "-r".equals( recursive ) ) {
        isRecursive = true;
      }
      RemoveEmbeddedMetastore removeEmbeddedMetastore = new RemoveEmbeddedMetastore();
      removeEmbeddedMetastore.setWindows( isWindows() );
      removeEmbeddedMetastore.handleDirectory( file, isRecursive );
      removeEmbeddedMetastore.showResultSummary();
    } else {
      logger.warning( "File or Directory does not exist: " + file.getPath() );
    }
  }

  /**
   * Determine OS
   * @return - true if windows, otherwise false
   */
  private static boolean isWindows() {
    return System.getProperty( "os.name" ).startsWith( "Windows" );
  }
}

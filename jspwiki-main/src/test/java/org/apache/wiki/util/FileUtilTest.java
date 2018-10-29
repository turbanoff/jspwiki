/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */

package org.apache.wiki.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.apache.wiki.TestEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FileUtilTest
{

    @BeforeAll
    public static void setUp()
    {
        Properties props = TestEngine.getTestProperties();
        PropertyConfigurator.configure(props);
    }

    /**
     *  This test actually checks if your JDK is misbehaving.  On my own Debian
     *  machine, changing the system to use UTF-8 suddenly broke Java, and I put
     *  in this test to check for its brokenness.  If your tests suddenly stop
     *  running, check if this one is Assertions.failing too.  If it is, your platform is
     *  broken.  If it's not, seek for the bug in your code.
     */
    @Test
    public void testJDKString()
        throws Exception
    {
        String src = "abc\u00e4\u00e5\u00a6";

        String res = new String( src.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);

        Assertions.assertEquals( src, res );
    }

    @Test
    public void testReadContentsLatin1()
        throws Exception
    {
        String src = "abc\u00e4\u00e5\u00a6";

        String res = FileUtil.readContents( new ByteArrayInputStream( src.getBytes(StandardCharsets.ISO_8859_1) ),
                                            "ISO-8859-1" );

        Assertions.assertEquals( src, res );
    }

    /**
     *  Check that fallbacks to ISO-Latin1 still work.
     */
    @Test
    public void testReadContentsLatin1_2()
        throws Exception
    {
        String src = "abc\u00e4\u00e5\u00a6def";

        String res = FileUtil.readContents( new ByteArrayInputStream( src.getBytes(StandardCharsets.ISO_8859_1) ),
                                            "UTF-8" );

        Assertions.assertEquals( src, res );
    }

    /**
       ISO Latin 1 from a pipe.

       FIXME: Works only on UNIX systems now.
    */
    @Test
    public void testReadContentsFromPipe()
        throws Exception
    {
        String src = "abc\n123456\n\nfoobar.\n";

        // Make a very long string.
        for( int i = 0; i < 10; i++ )
        {
            src += src;
        }

        src += "\u00e4\u00e5\u00a6";

        File f = FileUtil.newTmpFile( src, "ISO-8859-1" );

        String[] envp = {};

        try
        {
            Process process = Runtime.getRuntime().exec( "cat "+f.getAbsolutePath(), envp, f.getParentFile() );

            String result = FileUtil.readContents( process.getInputStream(), "UTF-8" );

            f.delete();

            Assertions.assertEquals( src,
                          result );
        }
        catch( IOException e ) {}
    }

    @Test
    public void testReadContentsReader()
        throws IOException
    {
        String data = "ABCDEF";

        String result = FileUtil.readContents( new StringReader( data ) );

        Assertions.assertEquals( data, result );
    }

}

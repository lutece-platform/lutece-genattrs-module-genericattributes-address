/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.genericattributes.modules.address.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * The scripts of the module run in the front office, whose theme does not load jQuery: a call to the jQuery API stops the
 * script with « $ is not defined » (the « Base adresse » geolocation question of forms).
 */
public class AddressScriptsWithoutJQueryTest
{
    private static final String [ ] ROOTS = {
            "webapp/WEB-INF/templates", "webapp/js/plugins/genericattributes/modules/address"
    };

    /** A call to the jQuery API: $( ... ), $.function, jQuery( ... ) or jQuery.function. The FreeMarker ${ } is not one. */
    private static final Pattern JQUERY = Pattern.compile( "(?<![\\w$])(?:\\$|jQuery)\\s*(?:\\(|\\.\\w)" );

    /**
     * Lists the templates and the scripts of the module.
     *
     * @return the files
     * @throws IOException
     *             if a tree cannot be read
     */
    private static List<Path> files( ) throws IOException
    {
        List<Path> listFiles = new ArrayList<>( );
        for ( String strRoot : ROOTS )
        {
            try ( Stream<Path> stream = Files.walk( Path.of( strRoot ) ) )
            {
                listFiles.addAll( stream.filter( p -> p.toString( ).endsWith( ".html" ) || p.toString( ).endsWith( ".js" ) ).sorted( )
                        .collect( Collectors.toList( ) ) );
            }
        }
        return listFiles;
    }

    /**
     * No template nor script of the module calls jQuery.
     *
     * @throws IOException
     *             if a file cannot be read
     */
    @Test
    public void testNoScriptCallsJQuery( ) throws IOException
    {
        List<Path> listFiles = files( );
        assertTrue( listFiles.stream( ).anyMatch( p -> p.toString( ).endsWith( "AddressTemplate.html" ) ), "AddressTemplate.html not found" );
        assertTrue( listFiles.stream( ).anyMatch( p -> p.toString( ).endsWith( "genericattributes_address.js" ) ), "genericattributes_address.js not found" );

        List<String> listFailures = new ArrayList<>( );
        for ( Path path : listFiles )
        {
            List<String> listLines = Files.readAllLines( path, StandardCharsets.UTF_8 );
            for ( int i = 0; i < listLines.size( ); i++ )
            {
                if ( JQUERY.matcher( listLines.get( i ) ).find( ) )
                {
                    listFailures.add( path + ":" + ( i + 1 ) + " " + listLines.get( i ).trim( ) );
                }
            }
        }
        if ( !listFailures.isEmpty( ) )
        {
            fail( "jQuery is not loaded in the front office:\n  " + String.join( "\n  ", listFailures ) );
        }
    }
}

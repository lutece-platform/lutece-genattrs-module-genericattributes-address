/*
 * Copyright (c) 2002-2017, Mairie de Paris
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

import fr.paris.lutece.plugins.address.business.axis.InvalidParameterException;
import fr.paris.lutece.plugins.address.business.jaxb.Adresse;
import fr.paris.lutece.plugins.address.service.AddressServiceProvider;
import fr.paris.lutece.plugins.address.util.LibraryAddressUtils;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * 
 * GenericAttributesAddressJspBean : for AJAX addresses search.
 * 
 */
public class GenericAttributesAddressJspBean
{
    /**
     * For error logs (pmd fix)
     */
    private static final String CONSTANT_LOG_PREFIX = "GenericAttributesAddressJspBean : ";

    /**
     * Address parameter
     */
    private static final String PARAMETER_ADDRESS = "address";
    /**
     * Id address parameter
     */
    private static final String PARAMETER_ID_ADDRESS = "id_address";
    /**
     * Id entry parameter
     */
    private static final String PARAMETER_ID_ENTRY = "id_entry";

    private static final String MESSAGE_REMOTE_EXCEPTION = "module.genericattributes.address.message.remoteException";
    private static final String MESSAGE_SELECT_AN_ADDRESS = "module.genericattributes.address.message.selectAddress";
    private static final String MESSAGE_ADDRESS_NOT_FOUND = "module.genericattributes.address.message.addressNotFound";
    private static final String MESSAGE_TOO_MANY_ADDRESSES = "module.genericattributes.address.message.tooManyAddresses";

    private static final String CONSTANT_DOT = ".";

    /**
     * Finds the address
     * @param request the request
     * @return {@link SearchAddress}
     * @see SearchAddress
     * @see AddressServiceProvider#searchAddress(HttpServletRequest, String)
     */
    public String searchAddress( HttpServletRequest request )
    {
        String strAddress = request.getParameter( PARAMETER_ADDRESS );
        String strIdEntry = request.getParameter( PARAMETER_ID_ENTRY );
        SearchAddress searchAddress = new SearchAddress( );

        searchAddress.setIdEntry( LibraryAddressUtils.parseInt( strIdEntry ) );

        if ( StringUtils.isBlank( strAddress ) )
        {
            //searchAddress.setMessage(I18nService.getLocalizedString( MESSAGE_SELECT_AN_ADDRESS, request.getLocale(  ) ) );
            return searchAddress.toJSONString( request.getLocale( ) );
        }

        try
        {
            ReferenceList refList = AddressServiceProvider.searchAddress( request, strAddress );

            if ( refList != null && refList.size( ) > 0 )
            {
                // fill addresses
                searchAddress.setAddresses( refList );
                if ( refList.size( ) == 1 )
                {
                    setSingleAddressInfos( request, LibraryAddressUtils.parseLong( refList.get( 0 ).getCode( ) ),
                            searchAddress );
                }
            }
            /*
             * else
             * {
             * searchAddress.setMessage( I18nService.getLocalizedString(
             * MESSAGE_ADDRESS_NOT_FOUND, request.getLocale( ) ) );
             * }
             */
        }
        catch ( InvalidParameterException e )
        {
            AppLogService.error( CONSTANT_LOG_PREFIX + e.getMessage( ), e );

            // set an error message
            String strMessage = e.getFaultString( );//I18nService.getLocalizedString( MESSAGE_REMOTE_EXCEPTION, request.getLocale(  ) );
            if ( StringUtils.isNotBlank( strMessage ) && !strMessage.endsWith( CONSTANT_DOT ) )
            {
                strMessage += CONSTANT_DOT;
            }
            searchAddress.setMessage( strMessage
                    + I18nService.getLocalizedString( MESSAGE_TOO_MANY_ADDRESSES, request.getLocale( ) ) );
        }
        catch ( RemoteException e )
        {
            AppLogService.error( CONSTANT_LOG_PREFIX + e.getMessage( ), e );

            // set an error message
            String strMessage = I18nService.getLocalizedString( MESSAGE_REMOTE_EXCEPTION, request.getLocale( ) );
            searchAddress.setMessage( strMessage );
        }

        if ( AppLogService.isDebugEnabled( ) )
        {
            AppLogService.debug( searchAddress.toJSONString( request.getLocale( ) ) );
        }

        return searchAddress.toJSONString( request.getLocale( ) );
    }

    /**
     * Gets the address info
     * @param request the request
     * @return the JSON
     */
    public String getAddressInfo( HttpServletRequest request )
    {
        SearchAddress searchAddress = new SearchAddress( );

        String strIdAddress = request.getParameter( PARAMETER_ID_ADDRESS );

        if ( StringUtils.isBlank( strIdAddress ) )
        {
            searchAddress
                    .setMessage( I18nService.getLocalizedString( MESSAGE_SELECT_AN_ADDRESS, request.getLocale( ) ) );
        }

        long lIdAddress = LibraryAddressUtils.parseLong( strIdAddress );

        if ( lIdAddress == -1 )
        {
            searchAddress
                    .setMessage( I18nService.getLocalizedString( MESSAGE_SELECT_AN_ADDRESS, request.getLocale( ) ) );
        }

        try
        {
            setSingleAddressInfos( request, lIdAddress, searchAddress );
        }
        catch ( InvalidParameterException e )
        {
            AppLogService.error( CONSTANT_LOG_PREFIX + e.getMessage( ), e );

            // set an error message
            String strMessage = e.getFaultString( );//I18nService.getLocalizedString( MESSAGE_REMOTE_EXCEPTION, request.getLocale(  ) );
            searchAddress.setMessage( strMessage );
        }
        catch ( RemoteException e )
        {
            AppLogService.error( CONSTANT_LOG_PREFIX + e.getMessage( ), e );

            // set an error message
            String strMessage = e.getMessage( );
            searchAddress.setMessage( strMessage );
        }

        if ( AppLogService.isDebugEnabled( ) )
        {
            AppLogService.debug( searchAddress.toJSONString( request.getLocale( ) ) );
        }

        return searchAddress.toJSONString( request.getLocale( ) );
    }

    /**
     * Fills the searchAddress object
     * @param request the request
     * @param lIdAddress the address id
     * @param searchAddress the SearchAddress object to fill
     * @throws RemoteException when an exception with the webservice occurs
     */
    private void setSingleAddressInfos( HttpServletRequest request, long lIdAddress, SearchAddress searchAddress )
            throws RemoteException
    {
        Adresse address = AddressServiceProvider.getAdresseInfo( request, lIdAddress, false );
        if ( address == null )
        {
            searchAddress
                    .setMessage( I18nService.getLocalizedString( MESSAGE_ADDRESS_NOT_FOUND, request.getLocale( ) ) );
        }
        else
        {
            searchAddress.setX( address.getGeoX( ) );
            searchAddress.setY( address.getGeoY( ) );
            searchAddress.setIdAddress( address.getIadresse( ) );

            ReferenceItem refItem = new ReferenceItem( );
            refItem.setCode( Long.toString( lIdAddress ) );
            refItem.setName( LibraryAddressUtils.normalizeAddress( address ) );

            searchAddress.setAddressLabel( refItem.getName( ) );

            ReferenceList refList = new ReferenceList( );
            refList.add( refItem );

            searchAddress.setAddresses( refList );
        }
    }

    /**
     * SearchAddress. Use for JSON compatibility : <br>
     * 
     * <pre>
     * {
     *  "noResult" : "false",
     *  "singleResult" : "false",
     *  "listAddresses" : [{"12345", "217 rue de Bercy"},
     *                 {"98765", "216, rue de Bercy"}],
     *  "message" : "",
     *  "error" : "false",
     *  "address" : "",
     *  "html" : "[...]"
     * }
     * </pre>
     * 
     * http://www.json.org/
     */
    public static class SearchAddress
    {
        private static final String TEMPLATE_ADDRESS_LIST = "admin/plugins/genericattributes/modules/address/ajax/AddressList.html";
        private static final String TEMPLATE_SINGLE_ADDRESS = "admin/plugins/genericattributes/modules/address/ajax/SingleAddress.html";
        private static final String TEMPLATE_NO_RESULT = "admin/plugins/genericattributes/modules/address/ajax/NoResult.html";

        private static final String MARK_ADDRESS = "address";
        private static final String MARK_LIST_ADDRESSES = "listAddresses";
        private static final String MARK_ID_ENTRY = "id_entry";

        private static final String JSON_HTML = "html";
        private static final String JSON_X = "x";
        private static final String JSON_Y = "y";
        private static final String JSON_NO_RESULT = "noResult";
        private static final String JSON_SINGLE_RESULT = "singleResult";
        private static final String JSON_MESSAGE = "message";
        private static final String JSON_ERROR = "error";
        private static final String JSON_ADDRESS_LABEL = "addressLabel";
        private static final String JSON_ID_ADDRESS = "idAddress";

        private ReferenceList _refListAddresses;
        private String _strAddressLabel;
        private String _strMessage;
        private float _fX;
        private float _fY;
        private int _nIdEntry;
        private BigInteger _nIdAddress;

        /**
         * address
         * @return address
         */
        public String getAddressLabel( )
        {
            return _strAddressLabel;
        }

        /**
         * Sets the address
         * @param strAddressLabel the new address
         */
        public void setAddressLabel( String strAddressLabel )
        {
            _strAddressLabel = strAddressLabel;
        }

        /**
         * Entry id
         * @return the entry id
         */
        public int getIdEntry( )
        {
            return _nIdEntry;
        }

        /**
         * Sets the entry id
         * @param nIdEntry the new entry id
         */
        public void setIdEntry( int nIdEntry )
        {
            _nIdEntry = nIdEntry;
        }

        /**
         * <code>true</code> if no result, <code>false</code> otherwise
         * @return <code>true</code> if no result, <code>false</code> otherwise
         */
        public boolean isNoResult( )
        {
            return _refListAddresses == null || ( _refListAddresses.size( ) == 0 );
        }

        /**
         * <code>true</code> if there is one and only one result ,
         * <code>false</code> otherwise
         * @return <code>true</code> if there is one and only one result,
         *         <code>false</code> otherwise
         */
        public boolean isSingleResult( )
        {
            return ( _refListAddresses != null ) && ( _refListAddresses.size( ) == 1 );
        }

        /**
         * Sets the addresses
         * @param refListAddresses the addresses
         */
        public void setAddresses( ReferenceList refListAddresses )
        {
            _refListAddresses = refListAddresses;
        }

        /**
         * Return matching addresses
         * @return the addresses
         */
        public ReferenceList getAddresses( )
        {
            return _refListAddresses;
        }

        /**
         * Sets an error message
         * @param strMessage an error message
         */
        public void setMessage( String strMessage )
        {
            _strMessage = strMessage;
        }

        /**
         * Returns the error message
         * @return the error message, <code>null</code> otherwise.
         */
        public String getMessage( )
        {
            return _strMessage;
        }

        /**
         * <code>true</code> if the error message is not blank,
         * <code>false</code> otherwise
         * @return <code>true</code> if the error message is not blank,
         *         <code>false</code> otherwise
         */
        public boolean isError( )
        {
            return StringUtils.isNotBlank( _strMessage );
        }

        /**
         * X
         * @return x
         */
        public float getX( )
        {
            return _fX;
        }

        /**
         * Sets the x
         * @param fX the x
         */
        public void setX( float fX )
        {
            _fX = fX;
        }

        /**
         * Sets the y
         * @param fY the y
         */
        public void setY( float fY )
        {
            _fY = fY;
        }

        /**
         * Y
         * @return y
         */
        public float getY( )
        {
            return _fY;
        }

        /**
         * @return the nIdAddress
         */
        public BigInteger getIdAddress( )
        {
            return _nIdAddress;
        }

        /**
         * @param nIdAddress the IdAddress to set
         */
        public void setIdAddress( BigInteger nIdAddress )
        {
            _nIdAddress = nIdAddress;
        }

        /**
         * 
         * @param locale the locale
         * @return json representation
         * @see JSONObject#toString()
         */
        public String toJSONString( Locale locale )
        {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = mapper.createObjectNode();

            
            jsonObject.put( JSON_X, getX( ) );
            jsonObject.put( JSON_Y, getY( ) );
            jsonObject.put( JSON_ERROR, isError( ) );
            jsonObject.put( JSON_MESSAGE, getMessage( ) );
            jsonObject.put( JSON_SINGLE_RESULT, isSingleResult( ) );
            jsonObject.put( JSON_NO_RESULT, isNoResult( ) );
            jsonObject.put( JSON_ADDRESS_LABEL, getAddressLabel( ) );
            jsonObject.put( JSON_ID_ADDRESS, getIdAddress( ) );

            HtmlTemplate template;
            if ( isNoResult( ) )
            {
                template = AppTemplateService.getTemplate( TEMPLATE_NO_RESULT, locale );
            }
            else if ( isSingleResult( ) )
            {
                Map<String, Object> model = new HashMap<String, Object>( );
                model.put( MARK_ADDRESS, getAddresses( ).get( 0 ) );
                model.put( MARK_ID_ENTRY, getIdEntry( ) );
                template = AppTemplateService.getTemplate( TEMPLATE_SINGLE_ADDRESS, locale, model );
            }
            else
            {
                Map<String, Object> model = new HashMap<String, Object>( );
                model.put( MARK_LIST_ADDRESSES, getAddresses( ) );
                model.put( MARK_ID_ENTRY, getIdEntry( ) );
                template = AppTemplateService.getTemplate( TEMPLATE_ADDRESS_LIST, locale, model );
            }

            jsonObject.put( JSON_HTML, template.getHtml( ) );

            
            return jsonObject.toString( );
        }
    }
}

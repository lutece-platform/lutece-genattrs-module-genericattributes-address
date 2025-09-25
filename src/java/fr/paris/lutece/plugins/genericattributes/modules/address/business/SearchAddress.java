package fr.paris.lutece.plugins.genericattributes.modules.address.business;
;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.plugins.address.business.Address;
import fr.paris.lutece.plugins.address.service.AddressServiceProvider;
import fr.paris.lutece.plugins.genericattributes.modules.address.service.GenericAttributesAddressPlugin;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.util.html.HtmlTemplate;
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
public class SearchAddress
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
    private String _strIdAddress;

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
    public String getIdAddress( )
    {
        return _strIdAddress;
    }

    /**
     * @param nIdAddress the IdAddress to set
     */
    public void setIdAddress( String strIdAddress )
    {
        _strIdAddress = strIdAddress;
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

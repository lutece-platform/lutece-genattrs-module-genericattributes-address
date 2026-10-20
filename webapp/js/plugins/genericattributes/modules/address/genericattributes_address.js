/**
 * Checks the address
 * @param id_entry entry id
 * @return void
 */
 function checkAddress( id_entry ){
	var div_results = document.getElementById(id_entry + "_address_results");
	div_results.innerHTML = '';
	var addressValue = document.getElementById( id_entry + '_address').value;
	// set x, y and idAddress to 0
	document.getElementById(id_entry + "_x").value = 0;
	document.getElementById(id_entry + "_y").value = 0;
	document.getElementById(id_entry + "_idAddress").value = 0;
	if ( addressValue != null && addressValue != '' ){
		$.getJSON(
				document.getElementsByTagName('base')[0].href + "jsp/site/plugins/genericattributes/modules/address/SearchAddress.jsp",
				{
					 "address" : addressValue,
					 "id_entry" : id_entry
				},
				function ( data )
				{
					if ( data.error )					{
						displayError( data.message, id_entry );
					} else {
						displayResult( data, id_entry );
					}
				}
		);
	}
}

function displayResult( data, id_entry ){
var div_results = document.getElementById(id_entry + "_address_results");
if ( data.singleResult ){
	document.getElementById(id_entry + "_address").value = data.addressLabel;
	document.getElementById(id_entry + "_x").value = data.x;
	document.getElementById(id_entry + "_y").value = data.y;
	document.getElementById(id_entry + "_idAddress").value = data.idAddress;
}	
$('#'+id_entry + "_address_results").html(data.html);
announceAddress( id_entry, div_results ? div_results.textContent : '' );
}

/**
 * Announces a result or an error to the screen readers through the status region of the entry, as text
 * @param id_entry entry id
 * @param message the text to announce
 * @return void
 */
function announceAddress( id_entry, message ){
	var status = document.getElementById( id_entry + "_address_status" );
	if ( status ){
		status.textContent = ( message || '' ).replace( /\s+/g, ' ' ).trim( );
	}
}

/**
 * Displays an error message, as text
 * @param message the message
 * @param id_entry entry id
 * @return void
 */
function displayError( message, id_entry ){
	$('#'+id_entry + "_address_results").text(message);
	announceAddress( id_entry, message );
}
/**
 * Put the geolocation
 * @param address the address
 * @return void
 */
function selectAddress( id_entry, code_address ){
var div_results = document.getElementById(id_entry + "_address_results");
div_results.innerHTML = '';
$.getJSON( document.getElementsByTagName('base')[0].href + "jsp/site/plugins/genericattributes/modules/address/FindAddressInfos.jsp",
	{ "id_address" : code_address,	"id_entry" : id_entry },
	function ( data ){
		if ( data.error ){
			displayError( data.message, id_entry );
		} else {
			displayResult( data, id_entry );
		}
	});
}

/**
 * Selects an address of the list (ajax/AddressList.html): the radio button holds the code of the address in its value
 * and the entry id in data-address-entry. One listener for the page, whatever the number of address entries.
 */
if ( !window.genericAttributesAddressSelect ){
	window.genericAttributesAddressSelect = true;
	document.addEventListener( 'click', function( event ){
		var radio = event.target.closest ? event.target.closest( 'input[data-address-entry]' ) : null;
		if ( radio ){
			selectAddress( radio.getAttribute( 'data-address-entry' ), radio.value );
		}
	});
}

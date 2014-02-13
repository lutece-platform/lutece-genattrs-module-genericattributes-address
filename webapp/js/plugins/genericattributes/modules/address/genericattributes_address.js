/**
 * Checks the address
 * @param id_entry entry id
 * @return void
 */
function checkAddress( id_entry )
{
	document.getElementById( id_entry + '_button_check' ).disabled = 'disabled';
	var div_results = document.getElementById(id_entry + "_address_results");
	div_results.innerHTML = '';
	var addressValue = document.getElementById( id_entry + '_address').value;
	// set x, y and idAddress to 0
	document.getElementById(id_entry + "_x").value = 0;
	document.getElementById(id_entry + "_y").value = 0;
	document.getElementById(id_entry + "_idAddress").value = 0;
	
	if ( addressValue != null && addressValue != '' )
	{
		$.getJSON(
				document.getElementsByTagName('base')[0].href + "jsp/site/plugins/genericattributes/modules/address/SearchAddress.jsp",
				{
					 "address" : addressValue,
					 "id_entry" : id_entry
				},
				function ( data )
				{
					if ( data.error )
					{
						displayError( data.message, id_entry );
					}
					else
					{
						displayResult( data, id_entry );
					}
				}
		);
	}
	else
	{
		document.getElementById( id_entry + '_button_check' ).disabled = '';
	}
}

function displayResult( data, id_entry )
{
	var div_results = document.getElementById(id_entry + "_address_results");
	
	if ( data.singleResult )
	{
		document.getElementById(id_entry + "_address").value = data.addressLabel;
		document.getElementById(id_entry + "_x").value = data.x;
		document.getElementById(id_entry + "_y").value = data.y;
		document.getElementById(id_entry + "_idAddress").value = data.idAddress;
	}
	
	$('#'+id_entry + "_address_results").html(data.html);
	document.getElementById( id_entry + '_button_check' ).disabled = '';
}

function displayError( message, id_entry )
{
	$('#'+id_entry + "_address_results").html(message);
	document.getElementById( id_entry + '_button_check' ).disabled = '';
}

/**
 * Put the geolocation
 * @param address the address
 * @return void
 */
function selectAddress( id_entry, code_address )
{
	var div_results = document.getElementById(id_entry + "_address_results");
	div_results.innerHTML = '';
	$.getJSON(
			document.getElementsByTagName('base')[0].href + "jsp/site/plugins/genericattributes/modules/address/FindAddressInfos.jsp",
			{
				 "id_address" : code_address,
				 "id_entry" : id_entry
			},
			function ( data )
			{
				if ( data.error )
				{
					displayError( data.message, id_entry );
				}
				else
				{
					displayResult( data, id_entry );
				}
			}
	);
}

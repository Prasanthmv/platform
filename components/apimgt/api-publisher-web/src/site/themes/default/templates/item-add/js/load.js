function loadTiers(row) {

    var target = document.getElementById("tier");

    jagg.post("/site/blocks/item-add/ajax/add.jag", { action:"getTiers" },
        function (result) {
            if (!result.error) {
                var arr = [];
                $('.getThrottlingTier',row).html('');
                $('.postThrottlingTier',row).html('');
                $('.putThrottlingTier',row).html('');
                $('.deleteThrottlingTier',row).html('');
                $('.optionsThrottlingTier',row).html('');

                for (var i = 0; i < result.tiers.length; i++) {
                    arr.push(result.tiers[i].tierName);
                    var k = result.tiers.length - i -1;
                    $('.getThrottlingTier',row).append($('<option value="'+result.tiers[k].tierName+'" title="'+result.tiers[k].tierDescription+'">'+result.tiers[k].tierName+'</option>'));
                    $('.postThrottlingTier',row).append($('<option value="'+result.tiers[k].tierName+'" title="'+result.tiers[k].tierDescription+'">'+result.tiers[k].tierName+'</option>'));
                    $('.putThrottlingTier',row).append($('<option value="'+result.tiers[k].tierName+'" title="'+result.tiers[k].tierDescription+'">'+result.tiers[k].tierName+'</option>'));
                    $('.deleteThrottlingTier',row).append($('<option value="'+result.tiers[k].tierName+'" title="'+result.tiers[k].tierDescription+'">'+result.tiers[k].tierName+'</option>'));
                    $('.optionsThrottlingTier',row).append($('<option value="'+result.tiers[k].tierName+'" title="'+result.tiers[k].tierDescription+'">'+result.tiers[k].tierName+'</option>'));
                }
                for (var j = 0; j < arr.length; j++) {
                    option = new Option(arr[j], arr[j]);
                    target.options[j] = option;
                    target.options[j].title = result.tiers[j].tierDescription;

                    if (j == 0) {
                        target.options[j].selected = 'selected';
                        $("#tiersHelp").html(result.tiers[0].tierDescription);
                        var tierArr = [];
                        tierArr.push(target.options[j].value);
                        $('<input>').attr('type', 'hidden')
                            .attr('name', 'tiersCollection')
                            .attr('id', 'tiersCollection')
                            .attr('value', tierArr)
                            .appendTo('#addAPIForm');
                    }
                }
            }
        }, "json");
}


$(document).ready(function() {

    $('.transports_check_http').change(function(){
        if($(this).is(":checked")){
            $(http_checked).val($(this).val());
        }else{
            $(http_checked).val("");
        }
    });

    $('.transports_check_https').change(function(){
            if($(this).is(":checked")){
                $(https_checked).val($(this).val());
            }else{
                $(https_checked).val("");
            }
        });

    $("select[name='tier']").change(function() {
        // multipleValues will be an array
        var multipleValues = $(this).val() || [];
        var countLength = $('#tiersCollection').length;
        if (countLength == 0) {

            $('<input>').attr('type', 'hidden')
                    .attr('name', 'tiersCollection')
                    .attr('id', 'tiersCollection')
                    .attr('value', multipleValues)
                    .appendTo('#addAPIForm');
        } else {
            $('#tiersCollection').attr('value', multipleValues);

        }

    });

    $("#clearThumb").on("click", function () {
        $('#apiThumb-container').html('<input type="file" class="input-xlarge" name="apiThumb" />');
    });

    var v = $("#addAPIForm").validate({
        submitHandler: function(form) {
            //Adding custom validation for the resource url UI
            if(validateResourceTable() != ""){
                return;
            }


            $('#saveMessage').show();
            $('#saveButtons').hide();
            $(form).ajaxSubmit({
                success:function(responseText, statusText, xhr, $form) {
                    if (!responseText.error) {
                        var current = window.location.pathname;
                        if (current.indexOf(".jag") >= 0) {
                            location.href = "index.jag";
                        } else {
                            location.href = 'site/pages/index.jag';
                        }
                    } else {
                        if (responseText.message == "timeout") {
                            if (ssoEnabled) {
                                var currentLoc = window.location.pathname;
                                if (currentLoc.indexOf(".jag") >= 0) {
                                    location.href = "add.jag";
                                } else {
                                    location.href = 'site/pages/add.jag';
                                }
                            } else {
                                jagg.showLogin();
                            }
                        } else {
                            jagg.message({content:responseText.message,type:"error"});
                        }
                        $('#saveMessage').hide();
                        $('#saveButtons').show();
                    }
                }, dataType: 'json'
            });
        }
    });


});

function getContextValue() {
    var context = $('#context').val();
    var version = $('#version').val();

    if (context == "" && version != "") {
        $('#contextForUrl').html("/{context}/" + version);
        $('#contextForUrlDefault').html("/{context}/" + version);
    }
    if (context != "" && version == "") {
        if (context.charAt(0) != "/") {
            context = "/" + context;
        }
        $('#contextForUrl').html(context + "/{version}");
        $('#contextForUrlDefault').html(context + "/{version}");
    }
    if (context != "" && version != "") {
        if (context.charAt(0) != "/") {
            context = "/" + context;
        }
        $('.contextForUrl').html(context + "/" + version);
    }
}

function showHideRoles(){
	var visibility = $('#visibility').find(":selected").val();
	
	if (visibility == "public" || visibility == "controlled"){
		$('#rolesDiv').hide();
	} else{
		$('#rolesDiv').show();
	}
	if (visibility == "controlled") {
		$('#allowTenantsDiv').show();
	} else {
		$('#allowTenantsDiv').hide();
	}
}

function showUTProductionURL(){
	var endpointType = $('#endpointType').find(":selected").val();
	if(endpointType == "secured"){
		$('#credentials').show();
	}
	else{
		$('#credentials').hide();
	}
	
}

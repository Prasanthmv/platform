$(document).ready(function () {


    $('.app-key-generate-button').click(function () {
        var elem = $(this);
        var i = elem.attr("iteration");
        var keyType = elem.attr("data-keytype");
        var authoDomains;
        var domainsDiv;
        var regen;
        var link;
        var validityTime;
        if (keyType == 'PRODUCTION') {
            authoDomains = $('#accessallowdomains' + i).attr('value');
            domainsDiv = 'allowDomainDiv'+ i;
            regen = 'table #accessallowdomainsRegen'+i;
            link = 'prodLink' + i;
            validityTime=$('#validityTime'+i).attr('value');
        } else {
            authoDomains = $('#allowDomainsSand' + i).attr('value');
            domainsDiv = 'allowDomainDivSand'+ i;
            regen = 'table #allowDomainsSandRegen'+i;
            link = 'sandLink' + i;
            validityTime=$('#validityTimeSand'+i).attr('value');
        }

        jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
            action:"generateApplicationKey",
            application:elem.attr("data-application"),
            keytype:elem.attr("data-keytype"),
            callbackUrl:elem.attr("data-callbackUrl"),
            authorizedDomains:authoDomains,
            validityTime:validityTime,
        }, function (result) {
            if (!result.error) {
                $('table .consumerKey', $(elem).parent().parent()).html(result.data.key.consumerKey);
                $('table .accessToken', $(elem).parent().parent()).html(result.data.key.accessToken);
                $('table .consumerSecret', $(elem).parent().parent()).html(result.data.key.consumerSecret);
                $(regen, $(elem).parent().parent()).html(result.data.key.accessallowdomains);
                if(result.data.key.accessallowdomains == ""){
                    $('.prodAuthorizedDomains', $(elem).parent().parent().parent()).html("All");
                }else{
                    $('.prodAuthorizedDomains', $(elem).parent().parent().parent()).html(result.data.key.accessallowdomains);
                }
                var regenerateOption=result.data.key.enableRegenarate;
                $('.app-key-generate-button', $(elem).parent()).parent().hide();
                var inputId;
                var domainResultVal = result.data.key.accessallowdomains;
                if (domainResultVal == '') {
                    domainResultVal = 'ALL';
                }
                if (keyType == 'PRODUCTION') {
                    if (regenerateOption) {
                    $('.key-table-header', $(elem).parent().parent().parent()).html('').html(i18n.t('titles.production')+ '<a onclick="toggleKey(this)" class="show-hide-key pull-right"><i class="icon-arrow-up icon-white"></i>'+ i18n.t('titles.hideKeys')+'</a>').show();
                    }else{
                    $('.key-table-header', $(elem).parent().parent().parent()).html('').html(i18n.t('titles.production')+ '<a onclick="toggleKey(this)" class="show-hide-key pull-right"><i class="icon-arrow-up icon-white"></i>'+ i18n.t('titles.hideKeys')+'</a>').show();
                    }
                    inputId = $('#prodOldAccessToken' + i);
                    $('#accessallowdomainsRegen' + i).val(domainResultVal);
                } else {
                    if (regenerateOption) {
                    $('.key-table-header', $(elem).parent().parent().parent()).html('').html(i18n.t('titles.sandbox')+' <a onclick="toggleKey(this)" class="show-hide-key pull-right"><i class="icon-arrow-up icon-white"></i>'+ i18n.t('titles.hideKeys')+'</a>').show();
                    }else{
                    $('.key-table-header', $(elem).parent().parent().parent()).html('').html(i18n.t('titles.sandbox')+' <a onclick="toggleKey(this)" class="show-hide-key pull-right"><i class="icon-arrow-up icon-white"></i>'+ i18n.t('titles.hideKeys')+'</a>').show();
                    }
                    inputId = $('#sandOldAccessToken' + i);
                    $('#allowDomainsSandRegen' + i).val(domainResultVal);
                }
                $('.table', $(elem).parent().parent()).show();
                document.getElementById(domainsDiv).style.display="none";
                document.getElementById(link).style.display="none";
                $('.info-msg', $(elem).parent().parent().parent()).html(i18n.t('resultMsgs.showKey')).hide();

                inputId.val(result.data.key.accessToken);
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");

        $(this).html(i18n.t('info.wait'));
    });

       $('.key-table-content textarea').focus(function() {
        var $this = $(this);
        $this.select();

        // Work around Chrome's little problem
        $this.mouseup(function() {
            // Prevent further mouseup intervention
            $this.unbind("mouseup");
            return false;
        });
    });

});

var regenerate=function(appName,keyType,i,btn,div,clientId,clientSecret) {
    //$('.show-hide-key pull-right').attr('disabled');
    $(btn).prev().show();
    $(btn).hide();
    var elem = $(this);
    var divId;
    var oldAccessToken;
    var inputId;
    var authorizedDomainsTemp;
    var clientId;
    var clientSecret;
    var validityTime;
    if (keyType == 'PRODUCTION') {
        inputId=$('#prodOldAccessToken'+i);
        divId = 'prodTable'+i;
        authorizedDomainsTemp = $('#accessallowdomainsRegen'+i).attr('value');
        validityTime=$('#refreshProdValidityTime'+i).attr('value');
    } else {
        inputId= $('#sandOldAccessToken'+i);
        sandDomains=$('#prodOldAccessToken'+i);
        divId = "sandTable"+i;
        authorizedDomainsTemp = $('#allowDomainsSandRegen'+i).attr('value');
        validityTime=$('#refreshSandValidityTime'+i).attr('value');
    }
    oldAccessToken=inputId.val();
    jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
        action:"refreshToken",
        application:appName,
        keytype:keyType,
        oldAccessToken:oldAccessToken,
        authorizedDomains:authorizedDomainsTemp,
        clientId:clientId,
        clientSecret:clientSecret,
        validityTime:validityTime
    }, function (result) {
        if (!result.error) {

            $('#'+divId+' .accessToken').text(result.data.key.accessToken);
            var regenerateOption=result.data.key.enableRegenarate;
            $('.app-key-generate-button', $(elem).parent()).hide();
            if (!regenerateOption) {
            $('.show-hide-key',$(elem).parent().parent().parent()).html('<i class="icon-arrow-up icon-white"></i> Hide Keys ').show();
            }else{
            $('.show-hide-key',$(elem).parent().parent().parent()).html('<i class="icon-refresh"></i> ' + i18n.t('titles.regenerate')+' <div class="pull-right" style="padding:0 5px;"> | </div> <i class="icon-arrow-up icon-white"></i>'+i18n.t('titles.hideKeys')+' ').show();
            }
            $('.table-striped', $(elem).parent().parent().parent()).show();
            $('.info-msg', $(elem).parent().parent().parent()).html(i18n.t('resultMsgs.showKey')).hide();
            inputId.val(result.data.key.accessToken);
            var domainResultVal = result.data.key.accessallowdomains;
            if (domainResultVal == '') {
                domainResultVal = 'ALL';
            }
            if (keyType == 'PRODUCTION') {
                $('#accessallowdomainsRegen' + i).val(domainResultVal);
            } else {
                $('#allowDomainsSandRegen' + i).val(domainResultVal);
            }

        } else {
            jagg.message({content:result.message,type:"error"});
        }
        $(btn).prev().hide();
        $('#' + div).hide();
        if (regenerateOption) {
            $(btn).show();
            $('#' + div).show();
        }



    }, "json");

    $(this).html(i18n.t('info.wait'));

}


var updateAccessAllowDomains = function(appName, keyType, i, btn) {
    var elem = $(btn);
    var divId;
    var oldAccessToken;
    var inputId;
    var authorizedDomainsEdit;
    elem.next().hide();
    $(btn).html('Updating..').attr('disabled','disabled');
    if (keyType == 'PRODUCTION') {
        inputId = $('#prodOldAccessToken' + i);
        divId = 'prodTable' + i;
        authorizedDomainsEdit = $('#accessallowdomainsRegen' + i).attr('value');
    } else {
        inputId = $('#sandOldAccessToken' + i);
        divId = "sandTable" + i;
        authorizedDomainsEdit = $('#allowDomainsSandRegen' + i).attr('value');
    }
    oldAccessToken = inputId.val();
    jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
        action:"updateAccessAllowDomains",
        application:appName,
        keytype:keyType,
        oldAccessToken:oldAccessToken,
        authorizedDomains:authorizedDomainsEdit
    }, function (result) {
        if (!result.error) {
            elem.html('Update Domains').removeAttr('disabled');
            elem.next().show();
            setTimeout(function(){elem.next().hide()},3000);
            if(authorizedDomainsEdit == ""){
                $('.prodAuthorizedDomains',elem.parent().parent()).html("ALL");
            }else{
                $('.prodAuthorizedDomains',elem.parent().parent()).html(authorizedDomainsEdit);
            }
        } else {
            jagg.message({content:result.message,type:"error"});
        }
    }, "json");
}

function toggleKey(toggleButton){
    var keyTable = $(toggleButton).parent().parent();

    if($('table',keyTable).is(":visible")){
        $('table',keyTable).hide();
        $('.info-msg',keyTable).show();
        $('.oauth-title',keyTable).hide();
        if(!$('.allowDomainDiv',keyTable).attr('.data-value') == "havetohide"){
            $('.allowDomainDiv',keyTable).show();
        }
        $(toggleButton).html('<i class="icon-arrow-down icon-white"></i>'+ i18n.t('titles.showKeys')+'');
    }else{
        $('table',keyTable).show();
        $('.info-msg',keyTable).hide();
        $('.oauth-title',keyTable).show();
        $('.allowDomainDiv',keyTable).hide();
        $(toggleButton).html('<i class="icon-arrow-up icon-white"></i>'+ i18n.t('titles.hideKeys')+'');
    }
}
function collapseKeys(index,type,link){

    if(type == 'super'){
        if($('#appDetails'+index+'_super').is(":visible")){
            $('i',link).removeClass('icon-minus').addClass('icon-plus');
        }else{
            $('i',link).removeClass('icon-plus').addClass('icon-minus');
        }
        $('#appDetails'+index+'_super').toggle();
    }else{
        if($('#appDetails'+index).is(":visible")){
            $('i',link).removeClass('icon-minus').addClass('icon-plus');
        }else{
            $('i',link).removeClass('icon-plus').addClass('icon-minus');
        }

        $('#appDetails'+index).toggle();
    }
}

function toggleAutSection(link){
    if($(link).next().is(":visible")){
        $(link).next().hide();
        $(link).html('Edit');
    }else{
        $(link).next().show();
        $(link).html('Hide');
        $('textarea',$(link).next()).focus();
    }
}

function toggleTokenTimeSection(link){
    if($(link).parent().next().is(":visible")){
        $(link).parent().next().hide();
        $(link).html('Edit');
    }else{
        $(link).parent().next().show();
        $(link).html('Hide');
        $('.token-validity-input',$(link).parent().next()).focus();
    }
}

function removeSubscription(apiName, version, provider, applicationId, delLink) {
    $('#messageModal').html($('#confirmation-data').html());
    $('#messageModal h3.modal-title').html(i18n.t('confirm.delete'));
    $('#messageModal div.modal-body').html('\n\n'+i18n.t('confirm.unsubscribeMsg') +'<b>"' + apiName+'-'+version + '</b>"?');
    $('#messageModal a.btn-primary').html(i18n.t('info.yes'));
    $('#messageModal a.btn-other').html(i18n.t('info.no'));
    $('#messageModal a.btn-primary').click(function() {
    jagg.post("/site/blocks/subscription/subscription-remove/ajax/subscription-remove.jag", {
        action:"removeSubscription",
        name:apiName,
        version:version,
        provider:provider,
        applicationId:applicationId
    }, function (result) {
        if (!result.error) {
            $('#messageModal').modal("hide");
            $(delLink).parent().parent().parent().parent().parent().remove();
            var master = $($(delLink).attr('data-master-id'));
            if($('.keyListPadding',master).length == 0){
                location.reload();
            }
        } else {

            jagg.message({content:result.message,type:"error"});
        }
    }, "json"); });
    $('#messageModal a.btn-other').click(function() {
        return;
    });
    $('#messageModal').modal();

}
function toggleHelp(icon){
    var theHelpBlock = $('.help-block',$(icon).parent().parent());
    if(theHelpBlock.is(':visible')) { theHelpBlock.hide(); } else { theHelpBlock.show(); }
}
var login = login || {};
(function () {
    var loginbox = login.loginbox || (login.loginbox = {});

    loginbox.login = function (username, password, url) {
        jagg.post("/site/blocks/user/login/ajax/login.jag", { action:"login", username:username, password:password },
                 function (result) {
                     if (result.error == false) {
                         if (redirectToHTTPS && redirectToHTTPS != "") {
                             window.location.href = redirectToHTTPS;
                         } else if(url){
                             window.location.href = url;
                         }else{
                             window.location.reload();
                         }
                     } else {
                         $('#messageModal').modal('hide');
                         jagg.message(result.message);
                     }
                 }, "json");
    };

    loginbox.logout = function () {
        jagg.post("/site/blocks/user/login/ajax/login.jag", {action:"logout"}, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                jagg.message(result.message);
            }
        }, "json");
    };



}());


$(document).ready(function () {
    var registerEventsForLogin = function(){
        $('#mainLoginForm input').die();
         $('#mainLoginForm input').keydown(function(event) {
         if (event.which == 13) {
                var goto_url =$.cookie("goto_url");
                event.preventDefault();
                login.loginbox.login($("#username").val(), $("#password").val(), goto_url);

            }
        });

        $('#loginBtn').die();
         $('#loginBtn').click(
            function() {
                var goto_url = $.cookie("goto_url");
                login.loginbox.login($("#username").val(), $("#password").val(), goto_url);
            }
         );
    };
    var showLoginForm = function(event){
        if(event != undefined){
            event.preventDefault();
        }
        if(!isSecure){
            $('#loginRedirectForm').submit();
            return;
        }

        $('#messageModal').html($('#login-data').html());
        $('#messageModal').modal('show');
        $.cookie("goto_url",$(this).attr("href"));
        $('#username').focus();

         registerEventsForLogin();
    };



    $("#logout-link").click(function () {
        login.loginbox.logout();
    });

    $(".need-login").click(showLoginForm);
    $('#login-link').click(showLoginForm);

    if(isSecure && showLogin){
        showLogin = false;
        showLoginForm();
    }

});
//Theme Selection Logic
function applyTheme(elm){
    $('#themeToApply').val(elm.title);
    $('#themeSelectForm').submit();
}



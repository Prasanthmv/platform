var jagg = jagg || {};

(function () {
    jagg.post = function () {
        var args = Array.prototype.slice.call(arguments);
        args[0] = this.site.context + args[0];
        $.post.apply(this, args);
    };

   jagg.messageDisplay = function (params) {
        $('#messageModal').html($('#confirmation-data').html());
        if(params.title == undefined){
            $('#messageModal h3.modal-title').html('API Store');
        }else{
            $('#messageModal h3.modal-title').html(params.title);
        }
        $('#messageModal div.modal-body').html(params.content);
        if(params.buttons != undefined){
            $('#messageModal a.btn-primary').hide();
            for(var i=0;i<params.buttons.length;i++){
                $('#messageModal div.modal-footer').append($('<a class="btn '+params.buttons[i].cssClass+'">'+params.buttons[i].name+'</a>').click(params.buttons[i].cbk));
            }
        }else{
            $('#messageModal a.btn-primary').html('OK').click(function() {
                $('#messageModal').modal('hide');
            });
        }
        $('#messageModal a.btn-other').hide();
        $('#messageModal').modal();
    };
     /*
    usage
    Show info dialog
    jagg.message({content:'foo',type:'info'});

    Show warning
    dialog jagg.message({content:'foo',type:'warning'});

    Show error dialog
    jagg.message({content:'foo',type:'error'});

    Show confirm dialog
    jagg.message({content:'foo',type:'confirm',okCallback:function(){},cancelCallback:function(){}});
     */
    jagg.message = function(params){
        if(params.type == "custom"){
            jagg.messageDisplay(params);
            return;
        }else if(params.type = "confirm"){
            if( params.title == undefined ){ params.title = "API Store"}
            jagg.messageDisplay({content:params.content,title:params.title ,buttons:[
                {name:"Yes",cssClass:"btn btn-primary",cbk:function() {
                    $('#messageModal').modal('hide');
                    if(typeof params.okCallback == "function") {params.okCallback()};
                }},
                {name:"No",cssClass:"btn",cbk:function() {
                    $('#messageModal').modal('hide');
                    if(typeof params.cancelCallback  == "function") {params.cancelCallback()};
                }}
            ]
            });
            return;
        }
        params.content = '<table><tr><td><img src="'+siteRoot+'/images/'+params.type+'.png" align="center" hspace="10" /></td><td><span class="messageText">'+params.content+'</span></td></tr></table>';
        jagg.messageDisplay({content:params.content,title:"API Store - "+params.type,buttons:[
            {name:"OK",cssClass:"btn btn-primary",cbk:function() {
                $('#messageModal').modal('hide');
                if(params.cbk && typeof params.cbk == "function")
	                    params.cbk();
            }}
        ]
        });
    };

    jagg.initStars = function (elem, saveCallback, removeCallback, data) {
        $('.dynamic-rating-stars a', elem).each(function () {
            $(this).mouseover(function () {
                        var rating = $('a', $(this).parent()).index(this) + 1;
                        $('.selected-rating', $(this).parent().parent()).html(rating);
                        $('a', $(this).parent()).each(function (index) {
                            if (index < rating) {
                                $(this).removeClass("star-0").addClass("star-1");
                            } else {
                                $(this).removeClass("star-1").addClass("star-0");
                            }
                        });
                    }).click(function () {
                        var rating = $('a', $(this).parent()).index(this) + 1;
                        $(this).parent().parent().data("rating", rating);
                        saveCallback(rating, $(".dynamic-rating", elem).data("rating-meta"));
                    }).mouseleave(function () {
                        var rating = $(this).parent().parent().data("rating");
                        rating = rating || 0;
                        $('.selected-rating', $(this).parent().parent()).html(rating);
                        $('a', $(this).parent()).each(function (index) {
                            if (index < rating) {
                                $(this).removeClass("star-0").addClass("star-1");
                            } else {
                                $(this).removeClass("star-1").addClass("star-0");
                            }
                        });
                    });
        });

        $(".dynamic-rating", elem).data("rating-meta", data).data("rating", $(".selected-rating", elem).text());

        $(".remove-rating", elem).click(function () {
            removeCallback($(".dynamic-rating", elem).data("rating-meta", data));
        });
    };
}());
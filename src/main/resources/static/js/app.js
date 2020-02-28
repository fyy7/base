$(function () {



    // 初始化设置

    var default_set = {
        win_w: window.innerWidth,
        win_h: window.innerHeight

    }

    $("#Sliding_layer").css({ "top": default_set.win_h + 20, "height": default_set.win_h });
    $("#Popup_layer").css({ "top": 0, "height": default_set.win_h });

    $(".mainbody").css("height", default_set.win_h - 130)
    $(".mainbody__cardnum").css({ "height": $(".mainbody__card").innerHeight() - 55, "line-height": $(".mainbody__card").innerHeight() - 55 + 'px' })
    var rk_height = $(".mainbody__chartbg").height();
    $(".mainbody__fastenter li").css({ height: (rk_height - 36) / 3 })
    
    $(window).resize(function () {
    	 $(".mainbody__cardnum").css({ "height": $(".mainbody__card").innerHeight() - 55, "line-height": $(".mainbody__card").innerHeight() - 55 + 'px' })
         var rk_height = $(".mainbody__chartbg").height();
         $(".mainbody__fastenter li").css({ height: (rk_height - 36) / 3 })

         $(".mainbody").css("height", window.innerHeight - 130)
         
        $("#Sliding_layer").css({ "height": window.innerHeight });
        $("#Popup_layer").css({ "height": window.innerHeight });

    })

    // 滑出
    function show_layout(url) {
       // $("#Sliding_layer").animate({ "top": "0" }, 500);
        $("#Sliding_layer ").find("iframe").attr("src", url);
        $("#Sliding_layer ").find("iframe").load(function () {
            $(document).scrollTop(0)
            $("#Sliding_layer").animate({ "top": "0" }, 500);
            // $("#Sliding_layer ").css({ "display":"block"});


        })
    }

    $("#monitor").click(function () {//do?action=goto&page=workflow/instance/my_instance_main
        //show_layout("./side_jyjk.html");
        show_layout("do?action=goto&page=dataexchange/index/side_jyjk");

    })
    $("#statistical").click(function () {
       // show_layout("./side_tjfx.html");
    	 show_layout("do?action=goto&page=dataexchange/index/side_tjfx");
    })
    $("#bank").click(function () {
       // show_layout("./side_bank.html");
        show_layout("do?action=goto&page=dataexchange/index/side_bank");
    })
    $("#warning").click(function () {
        //show_layout("./side_yujing.html");
    	show_layout("do?action=goto&page=dataexchange/index/side_yujing");
    })

    // 关闭滑出
    $(".Layout__hid").click(function () {

        $("#Sliding_layer", window.parent.document).animate({ "top": default_set.win_h + 20 }, 500);

    })


    // 弹出


    function Popup_box(url) {
        $("#Popup_layer").fadeIn(300);
        $("#Popup_layer ").find("iframe").attr("src", url);

        $("#Sliding_layer ", window.parent.document).css("z-index", 20)
    }

    $("#Jy_tb").click(function () {
    	// Popup_box("./alert_jiaoyi.html");
        Popup_box("do?action=goto&page=dataexchange/index/alert_jiaoyi");

    })
    
    $("#Tj_tb1").click(function () {
    	 // Popup_box("./alert_tj_01.html");
       Popup_box("do?action=goto&page=dataexchange/index/alert_tj_01");

    })
    $("#Tj_tb2").click(function () {
    	// Popup_box("./alert_tj_02.html");
       Popup_box("do?action=goto&page=dataexchange/index/alert_tj_02");

    })
    $("#Yh_tb").click(function () {
    	//Popup_box("./alert_bank.html");
       Popup_box("do?action=goto&page=dataexchange/index/alert_bank");

    })


    // 关闭弹出
    $(".Layout__close").click(function () {
        $("#Popup_layer").fadeOut(300, function () {
            $("#Popup_layer ").find("iframe").attr("src", "");
        });
        $("#Sliding_layer", window.parent.document).css("z-index", 10)
    })






})

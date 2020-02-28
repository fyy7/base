
/**
 * 基于WebUploader的通用批量文件上传模块。
 * 依赖：jquery、百度webuploader
 * 文件上送到服务端时将带上参数：name、chunk、chunks
 *
 * @param options   配置参数
   {
        debugMode: true|false,                          // 调试模式
        containerId: "uploaderContainer",               // 文件列表div容器ID，如：<div id="uploaderContainer"></div>
        filePickerId: "webuploaderBtn",                 // 选择文件按钮（div）ID，如：<div id="webuploaderBtn">选择图片</div>
        serverUrl: "webuploder-upload",                 // 文件上传服务器地址，服务器需返回json格式数据
        chunkSize: 1024,                                // 文件分片上传分片大小（k）
        showMsgFn: function (msg) {                     // 显示消息的函数
            alert(msg);
        },
        beforeUpload: function () {                     // 上传之前触发事件，传参：空
            console.log("before");
        },
        afterUpload: function (successUploadInfos) {    // 上传结束触发事件，传参：successUploadInfos（array，包含服务器响应等相关信息）
            console.log("after");
        }
    }

 * @returns {Uploader}
 *
 * @author  yanhang
 */
function CommonWebUploader(options) {

    var debugMode = options.debugMode || false;

    function Uploader(options) {
        this.containerId = options.containerId;
        this.filePickerId = options.filePickerId;
        this.serverUrl = options.serverUrl;
        this.chunkSize = options.chunkSize || 10 * 1024 * 1024;
        this.fileNumLimit = options.fileNumLimit || 99;
        this.fileSingleSizeLimit = options.fileSingleSizeLimit || 1024 * 1024 * 1024;
        this.showMsgFn = options.showMsgFn || alert;
        this.beforeUpload = options.beforeUpload || function () {};
        this.afterUpload = options.afterUpload || function (arg) {};
        this.debugMode = options.debugMode || false;

        this.successUploadInfos = new Array();
        this.uploadCallBack = null;

        // webupload上传时错误提示（error事件里的errType提示定义）
        this.errorMsgDef = {
            F_DUPLICATE: "文件已存在",
            F_EXCEED_SIZE: "文件大小超出限制",
			Q_EXCEED_NUM_LIMIT: "文件数量超出限制",
            Q_TYPE_DENIED: "文件格式不正确",
			YT_NO_FILES_FOR_UPLOAD: "请选择待上传的文件"
        };

        var base64Icon_file = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAADZ0lEQVRoQ+2ZX0gUURTGv3NX0SxNRy0KiiLH/iAUKBQSpETUQ/qQFb0E+VK4i/0FX1WoBykKzF31qeglqCAieglCH5LoIQopQmcXhEoFdVeoUMuZEzO4Nruuu4szzK6487Rw557z/c43d3buPYRVflG0fsnrP8HgIyAcSgUbAX5iHphT6dWvy/JEIg2LAEXdgZOCtYsM1CWa5MQ4AwEw7oY8si9ePgOgqDdwmFTtNYB1AH4z8IZY++iE0OgcDFEKwkECKvUxIjo11VT2fDktBoDkUwYAVDPQzzTfON20dyQV4s05i7v9HczcQoTA1PjcPrRX/ImliUq9nzeolPNTHxSgqkl32YdUiw/nl7zKOAibwbQ96Cn7FhOg8L5SI1zo06sfcsu1icSX+PyV8yrnJ7rPjnFy4Z4Rh6kHNP8u5N4zGB2XkgHI7/panC2yegEcA1Bgh7gVxvjBjBchj+wJz08IIHn91SDW10jaXMyYCHnkTcYij+fAwvoYArAVwHsmcZNnxdvpazunnaaROpUCZOE4gCeGcODllFuujwtQ3K2cZ8YjAKPClV01eWnHmNPCo/OV9I5s0dS/owaEEA1xASSfoi+iqyB6GGwqa0y1+MW3k2/4MUDniKg5LkCRT+kjoAastQc9u9vSBsA71AYSrbquDEAqXJEyDqSi7KaclhzQ/zec1j/dLPebc1oG0L+dnIQIuuWIjdfaBnCy8svlsuRABsCGCmQcsKGIlkJkHLBUPhsmr20HjC2o0Bz9nIjei1hyILyHtuFJSDqE7Z8S5EJr0tltuDH6vMqSAzbosRwiA2C5hBYDZBywWEDL0yMd6BnaLzTxCcCXoFuuMEdfFedC+b1DJdmqCPeizgbd8tMwRCwAyauchsAZK2XMm8m98P36tpmVxohwQA8i+RT9wNQQZW5yLAtAWIRciYi82dw8WwEWIPi/GB7UVLqi/2FFHy3qDrCgAysRHp6zfibnlu0ABoRXuQPCDf23pqI2FoAV4XbNXfIImQOXdAXKVUIla9rYqgQwwxR7hzuYqCVdj9eZ4VnSqTcDSN3DDWB6lq4NDiZRFxdAh1l8E6Vdi4l7ptzlTQkBCn1KvQA/AEiyaxFajRPR5Esm2MZOZZfLxbdBdDTt2qzJAES8pRxsdJvzkksNxmp0/wNsjC/mRL35KQAAAABJRU5ErkJggg==";
        var base64Icon_compress = "";
        var base64Icon_doc = "";
        var base64Icon_pic = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAFVklEQVRoQ+1ae4hUZRT/nTvrgzB37x0XeijoundHCyRJCCpCKgN7CEYPiVU0rXbuqLFWSNFD+yfRQtHuN7tgDzURAkUqsixJCCHBqMSinTvji5Io57FKmY73O3FndmbvzsOdRzs7K35/3fnuefx+95zzPYcwwhuNcPy4ugg0maHbFKLZYNlUj5Fh8pxhSu5P+KefzODLRkAVoc0EWlGPwAtg2hcz9Aed/hQBVVhHCLh9hIBPw2S5NhbwrSFNWI8D+LgP/AWALAbH6pTM9e4PLenyFFJNyySCkSLFEPGAHqhT8ClYqrAsAlpT6cO8hFRhfUPAbHdY6pzAALzXCNQ6WrkZU1YEmjaeaMLo5GSSHhujcTr+3NTeEUNADVp+YogBgAlrYn59bS1JVBQBzQztAdH8QkBJUe6Pdkw9UCsSZRPwdkXuYym/Lg6QQvKi545E55RELUiUTUATkSWAfD8Njk5C4YdSjxL7Adycns7l/Kjh21uXBFQReolA69PzBO+KBdqech41EfoJoBl9oDtjhr6pLgl4uyKPspS7XeA6U6tVUt7I9EnmmYlA2491SaDZPDbOpjE9AG4qCJCwMebXV9UCfN9SovyZWDPDd4L4UAGQZ2OG3lwteGcfMsrj+bvxkjwdXqlfvJK9sos4Y0ztjjQqkl+RjDYCJ4kRiQb0lysFr70bmsMKtRPhXjAm9tk5TYRXo359RzG7FROoFGghPc0Mvwni14ra7FvrF3o/7AQ003obhBcGguOjrhEtPeBB3hM3fN/mkhhWAt6gZTKn9x7pRkelzc8nVugHJ4jjPoa9gYFHMm8V2NPOGtOcASTbqibQtMVK7x0AOI5LTS3NDH0AosUu+cMepva/Aq3hTN+E7pM3Sjt5xDXiHbZxYW6vMSOercWc/UvJq9EmYc0joDO7+UmFGR+RgrdiHfovRYl0Hxml2Y1OUT6ZkWHgoEKyPer3/Z6XIsHIw8TyU1f/7pihP1YVAVVEOggyWATkz1DwRCES2mZrPDdgBwHzXLr7Lo8e235u2aSi+27N7FnjnigJWB41dNOxUXYKacJ6HYB7yXyOAWdf6j7FyCPRbJ64wUZyO4jmuHJ+z3X/jmn/bdWkC4OlnldYn7jq4Q8b8q5ew3e8LAJeEQoyqMPl7JACz9KzRkuPGgwvIOZdrndZEo2ip8XDtA1Ed2fLlbEzGtDbBwOeea91WbdA4qtMPTCwI27oi0oi0Gz+Oc5G7x4Qsl+PQXsvy+Sy88unR7NOgtbTYLyXS4IltudEaGvM0J8pFXxGzmuGFzHxtqwe0wIm7nAfQuQVscfTsNOW8gsALS6HRQF4RWgFgzYPIAHc2l+wvCVutK0sF3yWhLAEA/6+3yEGzlyRgLt4HCVirBtsyaCKyGqCXJcLkpjXRwNtqysF7+g1ilNqAy4dYGCm89sZwYoTyENAL8b8re+UAkAToW6Ank3L0j9ge4Nz9FeK7mAyqgjPJfDneXIs1w5MIZcEgRZHjdb+/BvMC4BmM9yalDwRDehN+PUfSlApWUQTljMSOiNifytCIMGSF8aXt31WsvUaCWrC+hLAA/1FnRMBBsIELIwZ+nc1wlSWmwldkVlSSucEZHxKMS8CV1jGluVpCIVLmgeG0H/Vpq8RqPoTVmkgLwKasJzbGeeWxmmbYobeWaWPIVXXhHUM/TN9J2mmtRSErVmvRB9C2qeGFEWFxpmUFmeUzKg751GpSz5NhH4FyFeh3WFRY6TXWK5rVtdV07BAKt0pA9/HDX2WozHgpl4N9iyAVCYT5NjSzdVS0nOeFeqJ+6dmVwlX118Navkt/y9fIz4C/wHGoiyALk61twAAAABJRU5ErkJggg==";

        var $container = $("#" + this.containerId);
        $container.addClass("uploader-file-container");

        var _this = this;

        var webuploader = WebUploader.create({
            pick: {
                id: "#" + this.filePickerId
            },
            chunked: true,
            chunkSize: this.chunkSize,
            fileNumLimit: this.fileNumLimit,
            fileSingleSizeLimit: this.fileSingleSizeLimit,
            server: this.serverUrl,
            runtimeOrder: "html5",
            thumb: {
                width: 200,
                height: 200,
                // 是否允许放大，如果想要生成小图的时候不失真，此选项应该设置为false.
                allowMagnify: true
            },

            // 禁掉全局的拖拽功能。这样不会出现图片拖进页面的时候，把图片打开。
            disableGlobalDnd: true
        });

        // 添加文件回调
        webuploader.on("fileQueued", function (file) {
            var fileId = file.id;
            var fileName = file.name;
            var fileExt = file.ext;
            var thumblrId = fileId + "__thumblr";

            var _html = '';
            _html += '<div id="' + fileId + '" class="uploader-file-item" style="display: none;">';
            _html += '	<div class="uploader-file-thumblr">';
            _html += '		<img id="' + fileId + '__thumblr" src="">';
            _html += '	</div>';
            _html += '	<div class="uploader-file-info">';
            _html += '		<div id="' + fileId + '__name" class="uploader-file-name">' + fileName + '</div>';
            _html += '		<div class="uploader-file-progress"><div id="' + fileId + '__progress_bar" class="uploader-file-progress-bar"></div></div>';
            _html += '	</div>';
            _html += '	<div class="uploader-file-operate">';
            _html += '		<span id="' + fileId + '__status" class="uploader-file-status">待上传</span>';
            _html += '		<span id="' + fileId + '__remove_btn" class="uploader-file-remove" onclick="window[\'UPLOADER_' + _this.containerId + '\'].removeFile(\'' + fileId + '\')">&#x2716;</span>';
            _html += '	</div>';
            _html += '</div>';

            $container.append(_html);
            $("#" + fileId).show(200);

            var $thumblr = $("#" + thumblrId);
            if (fileExt == "png" || fileExt == "jpg" || fileExt == "jpeg" || fileExt == "gif" || fileExt == "bmp") {
                webuploader.makeThumb(file, function(error, ret) {
                    if (error) {
                        $thumblr.attr("src", base64Icon_pic);
                    } else {
                        $thumblr.attr("src", ret);
                    }
                });

                bindEnlargeEvent(thumblrId);
            } else {
                $thumblr.attr("src", base64Icon_file);
            }

            log("add file: " + file.id + " " + file.name);
        });

        webuploader.on("fileDequeued", function (file) {
            log("remove file: " + file.id + " " + file.name);
        });

        // 每个分片上送后回调函数。服务端正常响应且回调函数返回true则将触发"uploadSuccess"事件，返回false将触发"uploadError"事件；服务端请求异常则将触发"uploadError"事件。
        webuploader.on("uploadAccept", function (fileChunk, ret) {
            log("uploadAccept: ");
            log(fileChunk);  // 分片信息
            log(ret);
            log("uploadAccept.");
            return ret && ret.success;
        });

        // 上传进度
        webuploader.on("uploadProgress", function (file, percentage) {
            var fileId = file.id;

            var $progress = $("#" + fileId + "__progress_bar");
            $progress.width(percentage * $("#" + fileId + " .uploader-file-progress").width());
            //$progress.html((percentage * 100).toFixed(1) + "%");

            var $status = $("#" + fileId + "__status");
            $status.html("上传中...");

            log("uploadProgress: " + file.name + "  " + file.size + "  " + percentage);
        });

        // 一个文件上送生命周期内调用一次
        webuploader.on("uploadError", function (file, reason) {
            var fileId = file.id;
            var $status = $("#" + fileId + "__status");
            $status.removeClass("uploader-file-success");
            $status.addClass("uploader-file-fail");
            $status.html("上传失败");

            var $progress = $("#" + fileId + "__progress");
            $progress.removeClass("uploader-file-success");
            $progress.addClass("uploader-file-fail");

            log("uploadError: " + file.name + "  " + file.size + "  "  + reason);
            log(reason);
        });

        // 一个文件上送生命周期内调用一次
        webuploader.on("uploadSuccess", function (file, response) {
            var fileId = file.id;
            var $status = $("#" + fileId + "__status");
            $status.removeClass("uploader-file-fail");
            $status.addClass("uploader-file-success");
            $status.html("上传完成");

            var $progress = $("#" + fileId + "__progress");
            $progress.removeClass("uploader-file-fail");
            $progress.addClass("uploader-file-success");

            // 获取所有分片请求的response
            var allResponse = new Array();
            if (file && file.blocks) {
                for (var idx in file.blocks) {
                    var _response = file.blocks[idx].response;
                    if (_response) {
                        allResponse.push(_response);
                    }
                }
            }

            _this.successUploadInfos.push({
                id: file.id,
                name: file.name,
                ret: allResponse
            });

            log("uploadSuccess: " + file.name + "  " + file.size + "  "  + response);
            log(response);
        });

        // 单文件上传结束时触发
        webuploader.on("uploadComplete", function (file) {
            log("uploadComplete: " + file.name + "  " + file.size);
        });

        // 所有文件上传结束时触发
        webuploader.on("uploadFinished", function () {
			log(_this.successUploadInfos);
            if (_this.uploadCallBack) {
				_this.uploadCallBack(_this.successUploadInfos);
            }
            _this.afterUpload(_this.successUploadInfos);
        });

        webuploader.on("error", function (errType) {
            var msg = _this.errorMsgDef[errType.toUpperCase()];
            if (msg && msg != "") {
				_this.showMsgFn(msg);
            }

            console.log("ERROR [" + errType + "]: " + msg);
        });

        this.webuploader = webuploader;
    }

	/**
	 * 返回当前webuploader实例
	 * @returns {*}
	 */
	Uploader.prototype.getUploader = function () {
		return this.webuploader;
	}

    Uploader.prototype.setUploaderOption = function (key, val) {
        this.webuploader.option(key, val);
    }

    Uploader.prototype.getUploaderOption = function (key) {
        return this.webuploader.option(key);
    }

    Uploader.prototype.setErrorMsg = function (errType, msg) {
        this.errorMsgDef[errType] = msg;
    }

    Uploader.prototype.removeFile = function (fileId) {
        this.webuploader.removeFile(fileId);
        $("#" + fileId).hide(200, function () {
            $("#" + fileId).remove();
        });

        for (var idx in this.successUploadInfos) {
            var item = this.successUploadInfos[idx];
            if (item.id == fileId) {
                this.successUploadInfos.splice(idx, 1);
            }
        }
    }

    Uploader.prototype.upload = function (uploadCallBack) {
        // if ($("#" + containerId + " .uploader-file-item").length == 0) {
        if (this.webuploader.getFiles("inited").length == 0) {
            this._showErrorMsg("YT_NO_FILES_FOR_UPLOAD");
            // return;
        }

        this.uploadCallBack = uploadCallBack;

        this.beforeUpload();

        this.webuploader.upload();
    }

    /**
     * 获取得到的上传的结果
     * @returns {...*[] | any[]}
     */
    Uploader.prototype.getUploadInfos = function () {
        return this.successUploadInfos;
    }

	Uploader.prototype.hasFileForUpload = function () {
		return this.webuploader.getFiles("inited").length > 0;
	}

	Uploader.prototype.getStats = function () {
		return this.webuploader.getStats();
	}

	Uploader.prototype._showErrorMsg = function (errorCode) {
		var msg = this.errorMsgDef[errorCode.toUpperCase()];
		if (msg && msg != "") {
			this.showMsgFn(msg);
		}

		console.log("ERROR [" + errorCode + "]: " + msg);
	}

    // 绑定鼠标滑过悬浮放大显示图片事件，className为加载在需要绑定事件的img元素的class名称
    function bindEnlargeEvent(imageId) {
        if ($("#__webuploader_enlargeImage").length == 0) {
            $("<div id='__webuploader_enlargeImage' style='position: absolute; display: none; z-index: 999; border: 0px solid #f4f4f4;'></div>").appendTo("body");;
        }

        var $image = $("#" + imageId);
        $image.mousemove(function(event) {
            event = event || window.event;
            var enlargeWidth = 200;
            var enlargeHeight = 200;
            var enlargeImage = $("#__webuploader_enlargeImage");
            enlargeImage.css("display", "block");
            enlargeImage.html("<img src='" + event.target.src + "' style='width: 200px; height: 200px;'/>");

            enlargeImage.css("top", document.body.scrollTop + event.clientY + 10 + "px");
            var left = (document.body.clientWidth - (document.body.scrollLeft + event.clientX)) > enlargeWidth ? (document.body.scrollLeft + event.clientX) : (document.body.scrollLeft + event.clientX - enlargeWidth - 10);
            enlargeImage.css("left", left + "px");
        });

        $image.mouseout(function(event) {
            var enlargeImage = $("#__webuploader_enlargeImage");
            enlargeImage.html("");
            enlargeImage.css("display", "none");
        });
    }

    function log(msg) {
        if (debugMode) {
            console.log(msg);
        }
    }

    // 实例化
    var uploader = new Uploader(options);
    window["UPLOADER_" + uploader.containerId] = uploader;

    return uploader;
}
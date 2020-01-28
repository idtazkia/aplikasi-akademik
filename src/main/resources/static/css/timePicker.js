/**
 * @author Hunter
 *
 * var timePicker = new HunterTimePicker({
 *	  target: '#targetID',		   目标元素ID（如：文本框，该值为选择的时间）
 *	  callback: function(value){   回调函数，选择城市后调用，Time_id为选择的城市的ID
 *		  alert(value);
 *	  }
 * });
 *
 * timePicker.init();
 */

var HunterTimePicker = function(options) {
    this.template = $('<div class="Hunter-time-picker" id="Hunter_time_picker"><div class="Hunter-wrap"><ul class="Hunter-wrap" id="Hunter_time_wrap"></ul></div></div>');
    this.time_wrap = $('#Hunter_time_wrap', this.template);
    this.settings = {
        "target": $(options.target),
        "callback": options.callback || '',
    };
};

var dates = {
    hour: ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'],
    minute: ['00', '05', '10', '15', '20', '25', '30', '35', '40', '45', '50', '55'],
};

HunterTimePicker.prototype = {
    init: function() {
        var that = this;

        $(window).click(function(event) {
            that.template.remove();
        });

        that.targetEvent();
    },

    buildTimePicker: function() {
        var that = this;
        that.buildHourTpl();
        that.hourEvent();
        that.minuteEvent();
        that.cleanBtnEvent();
    },

    buildHourTpl: function() {
        var that = this;

        var hour_html = '<p>小时</p>';
        for(var i = 0; i < dates.hour.length; i++) {
            var temp = that.settings.target.val().split(":")[0];
            if(dates.hour[i] == temp) {
                hour_html += '<li class="Hunter-hour active" data-hour="' + dates.hour[i] + '"><ul class="Hunter-minute-wrap"></ul><div class="Hunter-hour-name">' + dates.hour[i] + '</div></li>';
            } else {
                hour_html += '<li class="Hunter-hour" data-hour="' + dates.hour[i] + '"><ul class="Hunter-minute-wrap"></ul><div class="Hunter-hour-name">' + dates.hour[i] + '</div></li>';
            }
        }

        hour_html += '<li class="Hunter-clean"><input type="button" class="Hunter-clean-btn" id="Hunter_clean_btn" value="清 空"></li>'

        that.time_wrap.html(hour_html);
    },

    buildMinuteTpl: function(cur_time) {
        var that = this;

        var poi = cur_time.position();
        var minute_html = '<p>分钟</p>';
        var temp = that.settings.target.val().split(":")[1];
        for(var j = 0; j < dates.minute.length; j++) {
            if(dates.minute[j] == temp) {
                minute_html += '<li class="Hunter-minute active" data-minute="' + dates.minute[j] + '">' + dates.minute[j] + '</li>';
            } else {
                minute_html += '<li class="Hunter-minute" data-minute="' + dates.minute[j] + '">' + dates.minute[j] + '</li>';
            }
        }
        cur_time.find('.Hunter-minute-wrap').html(minute_html).css('left', '-' + (poi.left - 37) + 'px').show();
    },

    hourEvent: function() {
        var that = this;

        that.time_wrap.on('click', '.Hunter-hour', function(event) {
            event.preventDefault();
            event.stopPropagation();
            var _this = $(this);

            that.time_wrap.find('.Hunter-hour').removeClass('active');
            that.time_wrap.find('.Hunter-hour-name').removeClass('active');
            that.time_wrap.find('.Hunter-minute-wrap').hide().children().remove();

            _this.addClass('active');
            _this.find('.Hunter-hour').addClass('active');

            var hourValue = _this.data('hour');
            var temp = that.settings.target.val().split(":");
            if(temp.length > 1) {
                that.settings.target.val(hourValue + ":" + temp[1]);
            } else {
                that.settings.target.val(hourValue + ":00");
            }
            that.buildMinuteTpl(_this);

            return false;
        });
    },

    minuteEvent: function() {
        var that = this;

        that.time_wrap.on('click', '.Hunter-minute', function(event) {
            event.preventDefault();
            event.stopPropagation();
            /* Act on the event */
            var _this = $(this);

            var minuteValue = _this.data('minute');
            var temp = that.settings.target.val().split(":")[0] + ":" + minuteValue;
            that.settings.target.val(temp);
            that.template.remove();

            if(that.settings.callback) that.settings.callback(temp);

            return false;
        });
    },

    cleanBtnEvent: function() {
        var that = this;

        that.time_wrap.on('click', '#Hunter_clean_btn', function(event) {
            event.preventDefault();
            event.stopPropagation();
            /* Act on the event */
            that.settings.target.val('');
            that.template.remove();

            if(that.settings.callback) that.settings.callback(0);

            return false;
        });
    },

    targetEvent: function() {
        var that = this;

        that.settings.target.click(function(event) {
            event.stopPropagation();
            that.template.remove();
            var _this = $(this);
            that.buildTimePicker();

            var offset = _this.offset();
            var top = offset.top + _this.outerHeight() + 15;

            that.template.css({
                'left': offset.left,
                'top': top
            });

            $('body').append(that.template);

            return false;
        });
    }
};
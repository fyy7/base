/**
 * 绘制图表
 * 
 * @param divId
 * @param option
 * @returns
 */
function drawchart(divId, option) {
	echarts.dispose(document.getElementById(divId));
	var myChart = echarts.init(document.getElementById(divId));
	myChart.setOption(option);
	/* 数据钻取 */

	return myChart;
}

/**
 * 
 * @param divId
 * @param myOptions
 *            myOptions = { titleText ： '销售量统计', xName : '月份', yName : '销售量',
 *            data ：[{ "name" : "1月", "value" : "5" }] }
 * @returns
 */
function drawBarAndLine(divId, myOptions) {
	var xArray = new Array();
	var colors = new Array();
	var data = myOptions.seriesData ? myOptions.seriesData
			: (myOptions.series ? (myOptions.series.data ? myOptions.series.data
					: (myOptions.series.length > 0 && myOptions.series[0].data ? myOptions.series[0].data
							: null))
					: null);
	if (data != null) {
		for (var i = 0; i < data.length; i++) {
			colors[i] = 'rgb(' + Math.round(Math.random() * 255) + ','
					+ Math.round(Math.random() * 255) + ','
					+ Math.round(Math.random() * 255) + ')';
			xArray.push(data[i].name);
		}
	}
	var option = $.extend({
		// 标题

		title : {
			left : '10%'
		},
		legend : {
			show : true
		},
		// 工具栏
		toolbox : {
			show : true,
			right : '10%',
			feature : {
				/*magicType : {
					show : true,
					type : [ 'bar', 'line' ],
				},*/
				restore : {
					show : true
				},
				saveAsImage : {
					show : true
				}
			}
		},
		// x轴属性
		xAxis : {
			data : xArray,
			boundaryGap : true,
			axisLine : {
				lineStyle : {
					color : '#bababa'
				}
			},
			splitLine:{
				show:true
			}
		},
		// y轴属性
		yAxis : {},
		axisLabel : {
			color : '#bababa',
		},
		
		// 图表位置
		grid : {
			left : '10%',
			bottom : '20%',
			right : '10%',
			top : '20%'
		},
		// 图表类型
		series : {
			type : 'bar',
			label : {
				show : true,
				position : 'top',
			},
			itemStyle : {
				color : function(params) {
					return colors[params.dataIndex];
				}
			}
		},
		tooltip : {
			trigger : 'axis',
		}
	}, myOptions);
	/* 自定义属性 */
	// 标题 titleText
	if (!myOptions.title) {
		option.title.text = myOptions.titleText ? myOptions.titleText
				: option.title.text;
		/*option.series.itemStyle.color = "#fff"*/
	}
	if (!myOptions.series) {
		// 数据 data
		option.series.data = myOptions.seriesData ? myOptions.seriesData
				: option.series.data;
		option.series.type = myOptions.seriesType ? myOptions.seriesType
				: option.series.type;
		option.xAxis.boundaryGap = myOptions.seriesType ? (myOptions.seriesType == 'line' ? false
				: true)
				: true;
		/* 项数不同颜色 */
		if (!myOptions.differentColor) {
			delete option.series.itemStyle.color;
		}
	} else {
		option.xAxis.boundaryGap = myOptions.series.type ? (myOptions.series.type == 'line' ? false
				: true)
				: (myOptions.series.length > 0 ? (myOptions.series[0].type == 'line' ? false
						: true)
						: true);
	}
	if (!myOptions.toolbox && myOptions.noMagicType) {
		delete option.toolbox.feature.magicType;
	}
	// x轴名称 xName
	if (!myOptions.xAxis) {
		option.xAxis.name = myOptions.xName ? myOptions.xName
				: option.xAxis.name;
	}
	// y轴民称 yName
	if (!myOptions.yAxis) {
		option.yAxis.name = myOptions.yName ? myOptions.yName
				: option.yAxis.name;
	}
	/* 横向展示 */
	if (myOptions.showHorizontal) {
		var temp = option.xAxis;
		option.xAxis = option.yAxis;
		option.yAxis = temp;
	}
	return drawchart(divId, option);
}
/**
 * 
 * @param divId
 * @param myOptions
 *            myOptions = { titleText ： '销售量统计', data ：[{ "name" : "1月", "value" :
 *            "5" }] }
 * @returns
 */
function drawPie(divId, myOptions) {
	/* 隐藏值为0的数据 */
	var data = myOptions.seriesData ? myOptions.seriesData
			: (myOptions.series ? (myOptions.series.data ? myOptions.series.data
					: (myOptions.series.length == 1 && myOptions.series[0].data ? myOptions.series[0].data
							: null))
					: null);
	var selected = '{';
	if (data != null && !myOptions.selectAllLegend) {
		for ( var i in data) {
			if (data[i].value == 0) {
				selected += ('"' + data[i].name + '":false,');
			}
		}
	}
	selected += '}';
	var option = $
			.extend(
					{
						// 标题
						title : {
							right : 'center'
						},
						// 工具栏
						toolbox : {
							show : true,
							right : '10%',
							feature : {
								restore : {
									show : true
								},
								saveAsImage : {
									show : true
								}
							}
						},
						legend : {
							type : 'scroll',
							orient : 'vertical',
							x : 'left',
							selected : eval("(" + selected + ")"),
							formatter : function(name) {
								return (name.length > 9 ? (name.slice(0, 9) + "...")
										: name);
							},
							tooltip : {
								show : true
							},
						},
						// 图表类型
						series : {
							type : 'pie',
							radius : '50%',
							center : [ '50%', '65%' ],
							selectedMode : 'single',
							selectedOffset : 10,
							label : {
								show : true,
								formatter : function(params) {
									var name = params.data.name;
									return (name.length > 9 ? (name.slice(0, 9) + "...")
											: name)
											+ '(' + params.percent + '%)';
								},
							}
						},
					}, myOptions);
	/* 自定义属性 */
	if (!myOptions.title) {
		option.title.text = myOptions.titleText ? myOptions.titleText
				: option.title.text;
	}
	if (!myOptions.series) {
		option.series.data = myOptions.seriesData ? myOptions.seriesData
				: option.series.data;
	}
	if (!myOptions.legend) {
		option.legend.height = myOptions.legendHeight ? myOptions.legendHeight
				: option.legend.height;
	}
	return drawchart(divId, option);
}

function drill(params) {

}

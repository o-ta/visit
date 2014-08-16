/**
 * scheduleのテーブル描画
 */
// テーブル描画
function draw_table(date) {
	var tbody = $('#sc_tbody').empty();//表を表示する部分を空にする
	
	$('#loading').css({'visibility':'visible'});
	$.getJSON('/admin/js/schedule.jsp', {
		"date" : date
	},
	function(work) {
		var jdg = 0;//要素判定
		
		$.each(work, function(i, work_item) {
			var tr = $('<tr />');
			
			var empty = "-";
			
			var work_id = work_item.work_id;// 業務id
			var staff_name = work_item.staff_name;// スタッフ名
			var user_name = work_item.user_name;// ユーザ名
			var schedule_time = work_item.schedule_time;// 開始予定時間
			var aim = work_item.aim;// 手段
			var walk = status2sign(work_item.walk);// 歩行
			var talk = status2sign(work_item.talk);// 話
			var move = status2sign(work_item.move);// 移動
			var sleep = status2sign(work_item.sleep);// 睡眠
			var eat = status2sign(work_item.eat);// 食事

			// 空があり得るのでnullチェック
			var start_time;// 開始時間
			var end_time;// 終了時間
			var photo;// 写真
			var status;

			if (work_item.start_time != null) {
				start_time = work_item.start_time;
			} else {
				start_time = empty;
			}
			if (work_item.start_time != end_time) {
				end_time = work_item.end_time;
			} else {
				end_time = empty;
			}
			//写真部分
			if (work_item.url != null) {
				photo = $('<a />').addClass("fb").attr({
					href : work_item.url
				}).append($('<img />').attr({
					src : "img/schedule/photo.png",
					title : "日報写真",
					alt : "日報写真"
				}));
			} else {
				photo = $('<img />').attr({
					src : "img/schedule/photo_empty.png",
					title : "日報写真無し",
					alt : "日報写真無し"
				});
			}
			//日報閲覧部分
			if (work_item.status == 1) {
				status = $('<a />').addClass("fb").attr({
					href : "report.html?work_id=" + work_id
				}).append($('<img />').attr({
					src : "img/schedule/dayRepo.png",
					title : "日報表示",
					alt : "日報表示"
				}));
			} else{
				status = $('<img />').attr({
					src : "img/schedule/dayRepo_empty.png",
					title : "日報無し",
					alt : "日報無し"
				});
			}
			
			tr.append($('<td />').addClass("_id").text(work_id));
			tr.append($('<td />').text(staff_name));
			tr.append($('<td />').text(user_name));
			tr.append($('<td />').text(schedule_time));
			tr.append($('<td />').text(start_time));
			tr.append($('<td />').text(end_time));
			tr.append($('<td />').text(aim));
			tr.append($('<td />').text(walk));
			tr.append($('<td />').text(talk));
			tr.append($('<td />').text(move));
			tr.append($('<td />').text(sleep));
			tr.append($('<td />').text(eat));
			tr.append($('<td />').html(photo));
			tr.append($('<td />').html(status));
			
			
			//描画
			tbody.append(tr);
			jdg++;

		});
		
		
		//要素が空ならその趣旨を表示
		if(jdg == 0){
			var tr = $('<td colspan="14">データがありません</td>');
			tbody.append(tr);
		}
		
		
		//すべての描画終了
		//now Loading..もやめる
		$('#loading').css({'visibility':'hidden'});
	});
}

//ステータスを記号に変更
function status2sign(status){
	if(status == -1){//指定なし
		return "-";
	}else if(status == 0){//良
		return "◎";
	}else if(status == 1){//通常
		return "◯";
	}else if(status == 2){//不良
		return "☓";
	}else{
		return "？";
	}
}
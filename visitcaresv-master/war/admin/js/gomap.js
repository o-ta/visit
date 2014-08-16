/*　マップ描画部分 */
//<![CDATA[
var map;
var MarkerArray = new google.maps.MVCArray();

function initialize() {

	var latlng = new google.maps.LatLng(33.2365, 131.607);
	var opts = {
		zoom : 15,
		center : latlng,
		mapTypeId : google.maps.MapTypeId.ROADMAP
	};
	map = new google.maps.Map(document.getElementById("gmap"), opts);
	getStaffData();
}

function getStaffData() {
	
	$.ajaxSetup({
		scriptCharset : 'UTF-8',
		cache : false
	});
	$.getJSON('/api/v1/request/staff.jsp?', {
		'staff_id' : -1,
		'location' : true
	}, function(data, status) {
		ClearAllIcon();
		var $dl = $('<dl />').addClass("staff_list");
		$.each(data, function() {
			var staff_id = this.staff_id;
			var staff_name = this.staff_name;
			var latitude = this.latitude;
			var longitude = this.longitude;

			$('<dt />').append(
					$('<a />').text("ID:" + staff_id + staff_name).click(
							function() {
								// クリックされた時の処理
								// GoogleMapの中央に表示
								map.panTo(new google.maps.LatLng(latitude,
										longitude));
							}).css("cursor", "pointer").append(
							$('<img />').attr({
								src : 'img/character/' + staff_id + '.png',
								title : staff_name,
								alt : staff_name
							}))).appendTo($dl);
			$('<dd/>').append("緯度：" + latitude).appendTo($dl);
			$('<dd/>').append("経度：" + longitude).appendTo($dl);
			$('<dd/>').append("更新時間：" + this.last_update_time).appendTo($dl);

			// マップのマーカーを生成
			var marker = makeMarker(latitude, longitude, staff_name, staff_id);

		});
		$('#staff_now').empty().append($dl);
		// $("#testest").text("現在地情報を取得しました");
	});

	// マーカーを作成して返す
	function makeMarker(lat, lng, name, image_id) {
		var image = 'img/character/' + image_id + '.png';
		var myLatLng = new google.maps.LatLng(lat, lng);
		var Marker = new google.maps.Marker({
			position : myLatLng,
			map : map,
			icon : image,
			title : name
		});
		MarkerArray.push(Marker);
	}

	function ClearAllIcon() {
		MarkerArray.forEach(function(marker, idx) {
			marker.setMap(null);
		});
	}

}

// ]]>

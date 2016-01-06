function time_str_to_array(str)
{
	var start_times = [0, 0, 0, 0, 0, 0];
	var durations = [0, 0, 0, 0, 0, 0];

	var strings_by_day = str.split("/");
	strings_by_day.forEach(function(day_str) {
		var day_index = ["월", "화", "수", "목", "금", "토"].indexOf(day_str[0]);
		var time_str = day_str.substring(2, day_str.length - 1);
		var start_time = Number(time_str.split('-')[0]);
		var duration = Number(time_str.split('-')[1]);

		if(start_times[day_index] == 0) {
			start_times[day_index] = start_time;
			durations[day_index] = duration;
		} else {
			durations[day_index] += duration;
		}
	})

	var class_times = [];
	for (var i = 0; i < 6; i++) {
		class_times.push({start: start_times[i], len:durations[i]});
	}
	return class_times;
}

function insert_course(lines, year, semester, next)
{
	var async = require('async');
	var Lecture = require('../../model/_lecture');

	var cnt = 0, err_cnt = 0;
	/*For those who are not familiar with async.each
	async.each(elements, 
		funcForEachElement,
		funcEverythingIsDone
		)
	*/
	async.each(lines, function(line, callback) {
		var components = line.split(";");
		if (components.length == 1) {
			callback();
			return;
		}
		var lecture = new Lecture({
			year: Number(year),
			semester: semester,
			classification: components[0],
			department: components[1],
			academic_year: components[2],
			course_number: components[3],
			lecture_number: components[4],
			course_title: components[5],
			credit: Number(components[6]),
			class_time: components[7],
			location: components[8],
			instructor: components[9],
			quota: Number(components[10]),
			enrollment: Number(components[11]),
			remark: components[12],
			category: components[13]
		})
		lecture.save(function (err, lecture) {
			cnt++
			if (err) {
				console.log("Error with " + components)
				console.log(err)
				err_cnt++
			}
			process.stdout.write("Inserting " + cnt + "th course\r");
			callback();
		})
	}, function(err) {
		console.log("INSERT COMPLETE with " + eval(cnt-err_cnt) + " success and "+ err_cnt + " errors")
		next();
	})
}

module.exports = { time_str_to_array: time_str_to_array, insert_course: insert_course};
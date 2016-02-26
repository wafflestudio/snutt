var fs = require('fs');
var Lecture = require('../model/lecture');
var Tags = require('../model/tagList');
var updateLectures = require('./update_lectures').insert_course;

if (process.argv.length != 4) {
	console.log("Invalid arguments");
	console.log("usage: $ node import_txt.js 2016 1");
	process.exit(1);
}

var year = Number(process.argv[2]);
var semester = process.argv[3];
var datapath = __dirname + "/txt/"+year+"_"+semester+".txt";

fs.readFile(datapath, function (err, data) {
	if (err) {
		console.log('DATA LOAD FAILED: ' + year + '_' + semester);
		process.exit(1);
	}
	console.log("Importing " + year + " " + semester);

	var lines = data.toString().split("\n");
	var header = lines.slice(0, 3);
	var courses = lines.slice(3);

	if (year != header[0].split("/")[0].trim() ||
		semester != header[0].split("/")[1].trim()) {
		console.log("Textfile does not match with given parameter");
		process.exit(1);
	}
	var semesterIndex = ['1', 'S', '2', 'W'].indexOf(semester) + 1;
	//delete existing coursebook of input semester before update

	Tags.remove({ year: year, semester: semesterIndex}, function(err) {
		if (err)
			console.log(err);
		else {
			console.log("removed existing tags for this semester");
			Lecture.remove({ year: year, semester: semesterIndex}, function(err) {
				if (err)
					console.log(err);
				else {
					console.log("removed existing coursebook for this semester");
					updateLectures(courses, year, semesterIndex, function(){
						process.exit(0);
					})
				}
			});
		}
	});
});

var fs = require('fs');
var config = require('../../config')
var Lecture = require('../../model/_lecture')
var util = require('./import_txt_utils')

var time_str_to_array = util.time_str_to_array;
var insert_course = util.insert_course;

if (process.argv.length != 4) {
	console.log("Invalid arguments")
	console.log("usage: $ node importTxt.js 2015 S");
	process.exit(1);
}

var year = Number(process.argv[2])
var semester = process.argv[3]
var datapath = config.snutt.ROOT_DATA_PATH + "/txt/"+year+"_"+semester+".txt";

fs.readFile(datapath, function (err, data) {
	if (err) {
		console.log('DATA LOAD FAILED: ' + year + '_' + semester);
		process.exit(1);
	}
	console.log("Importing " + year + " " + semester)

	var lines = data.toString().split("\n");
	var header = lines.slice(0, 3);
	var courses = lines.slice(3);

	if (year != header[0].split("/")[0].trim() || 
		semester != header[0].split("/")[1].trim()) {
		console.log("Textfile does not match with given parameter")
		process.exit(1);
	}
	var updated_time = header[1];

	//delete existing courserbook of input semester before update
	Lecture.remove({ year: year, semester: semester}, function(err) {
		if (err) 
			console.log(err)
		else {
			console.log("removed existing coursebook for this semester")
			insert_course(courses, year, semester, function(){
				process.exit(0);
			})
		}
	});
})
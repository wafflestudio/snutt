// Requires
var _ = require("underscore");
var fs = require("fs");
var config = require("../config.js");
var utils = require("../utils.js");
var mongoose = require('mongoose');

var safeString = utils.safeString;
var increasingOrderInclusion = utils.increasingOrderInclusion;

var stringToObject = utils.stringToObject;
var objectToString = utils.objectToString;

var Lecture = require("./lecture.js").Lecture;

/* In-memory&File system style lecture model */
function NaiveLectureModel() {
    this.coursebook = {};
    this.lastCoursebookInfo = {
        year: 2000,
        semester: 1,
        updated_time: "2000-01-01 00:00:00"
    };
}

if (!(global.hasOwnProperty('NaiveLectureModel_userdata_cnt'))) {
    global.NaiveLectureModel_userdata_cnt = _.max([
        _.max(
			_.reject(
				_.map(
					fs.readdirSync(config.snutt.USER_TIMETABLE_PATH),
                    function (num) { return parseInt(num); }
					),
                isNaN
				)
			),
        0]);
	console.log("User data cnt INIT");
    console.log(_.map(fs.readdirSync(config.snutt.USER_TIMETABLE_PATH),
                             function (num) { return parseInt(num); }));
}


/* DB initialization for logging users' search query requests 
 * TODO make the db address shared across all modules */

mongoose.connect('mongodb://localhost/snuttdb');
var db = mongoose.connection;
db.on ('error', function () { 
	console.error.bind(console, 'connection error! ');
	throw "Failed to open DB";
});

var queryLogSchema = mongoose.Schema ({
	lastQueryTime: { type: Date, default: Date.now },
	count: { type: Number, min: 1, default: 1 },
	year: { type: Number, min: 2000, max: 2999 },
	semester: { type: String },
	type: { type: String },
	body: String
});
var QueryLog = mongoose.model('QueryLog', queryLogSchema); 


NaiveLectureModel.prototype = {
    init: function () {
        this._load_data(2012, '2');
        this._load_data(2012, 'W');
        this._load_data(2013, '1');
        this._load_data(2013, 'S');
        this._load_data(2013, '2');
        this._load_data(2013, 'W');
        this._load_data(2014, '1');
        this._load_data(2014, 'S');
		this._load_data(2014, '2');
		this._load_data(2014, 'W');
        this._load_data(2015, '1');
    },
    save: function (lectures, year, semester, callback) {
        /* This function saves this lecture and return a string id identifying lectures.
         *
         * Arguments -
         *     lectures: Lecture array
         *     year
         *     semester
         *     callback: a function that takes exactly two argument,
         *     the former of which is `error' object and the later of which is 
         *     the id of userdata.
         */
        global.NaiveLectureModel_userdata_cnt += 1;
        var id = String(global.NaiveLectureModel_userdata_cnt);

        var filepath = config.snutt.USER_TIMETABLE_PATH + '/' + id,
            content = objectToString({
                year: year,
                semester: semester,
                lectures: lectures
            });

        fs.writeFile(filepath, content, function (err) {
            return callback(err, id);
        });
    },
    load: function (id, callback) {
        /*
         * schema of content, which passed to `callback' as an argument, is
         * {
         *     year: string
         *     semester: string
         *     lectures: Lecture array
         * }
         *
         */
        var filepath = config.snutt.USER_TIMETABLE_PATH + '/' + id;
        fs.readFile(filepath, function (read_err, content) {
            if (read_err) {
                callback(read_err, null);
            } else {
                try {
                    var obj = stringToObject(content);
                    callback(read_err, obj);
                } catch (parse_err) {
                    callback(parse_err, null);
                }
            }
        });
    },
    getLastCoursebookInfo: function () {
        //{20121:{year:2012, semester:'S', updated_time:"2012-01-01 00:00"}]
        return this.lastCoursebookInfo;
    },
    getCoursebookInfo: function () {
        //현재 저장된 수강편람 정보를 리턴
        var hash, result = [], coursebook = this.coursebook;

        for (hash in coursebook) {
            if (coursebook.hasOwnProperty(hash)) {
                result.push({
                    year: coursebook[hash].year,
                    semester: coursebook[hash].semester,
                    updated_time: coursebook[hash].updated_time
                });
            }
        }
        function semesterToNumber(s) {
            if (s === '1') {
                return 1;
            } else if (s === 'S') {
                return 2;
            } else if (s === '2') {
                return 3;
            } else if (s === 'W') {
                return 4;
            } else {
                return 5;
            }
        }

        function sortFun(a, b) {
            var a_s = semesterToNumber(a.semester),
                b_s = semesterToNumber(b.semester),
                a_y = a.year,
                b_y = b.year;

            if (a_y > b_y)
                return -1;
            else if(a_y < b_y)
                return 1;
            else if(a_s > b_s)
                return -1;
            else if(a_s < b_s)
                return 1;
            else
                return 0;
        }
        result.sort(sortFun);
        return result;
    },
    getLectures: function (options) {
        /*
         * Query for several Lecture objects which meets condition given as arguments
         * 
         * Arguments -
         *     options: an option object with below keys
         *         year
         *         semester
         *         type: "course_title" | "instructor" | "course_number" | "class_time"
         *         filter: An filter object
         *             [academic_year]: dest academic_year
         *             [credit]: dest credit(학점)
         *             [basics]: dest basics(학문의 기초)
         *             [core]: dest core(핵심교양)
         *             [etc]: dest etc(기타)
         *
         *             Note that filter of basics, core and etc is associated with one anothor by OR-relation, which infers that condition will be met if any of those filters is met.
         *
         *         [page]:
         *             page count
         *             default value is 1 if not given
         *
         *         [per_page]:
         *             how many lectures is in a page at most.
         *             default value is 30 if not given 
         *         
         * Returns - an object with below keys
         *     lectures: Lecture array
         *     page
         *     per_page
         *     query:
         *         a reference to the options argument.
         */
        var key = safeString(options.year) + safeString(options.semester);
        var coursebook = this.coursebook;
        if (!coursebook[key])
            return {lectures: [],
                    page: 1,
                    per_page: options.per_page,
                    query: options};

        var lectures = coursebook[key].lectures;
        var page = options.page || 1;
        var per_page = options.per_page || 30;
        var filter = options.filter;
        var result = {lectures: [], page: page, per_page: per_page, options: options};
        var skip_count, i;

        //교과목명으로 검색
        if (options.type === "course_title") {
            var title = safeString(options.query_text).trim();

			//Log it!
			QueryLog.findOne ({
				year: options.year,
				semester: options.semester,
				body: title
			}, 
			function (err, prevQuery) {
				if (err) console.error(err);
				if (prevQuery == undefined) {
					var currQuery = new QueryLog ({
						year: options.year,
						semester: options.semester,
						type: options.type,
						body: title
					});
					currQuery.save(function (err) {
						if (err) return console.error(err);
						//console.log('new log: '+currQuery.body);
					});
				}
				else { 
					prevQuery.count++;
					prevQuery.lastQueryTime = Date.now();
					prevQuery.save(function (err) {
						if (err) return console.error(err);
						//console.log(prevQuery.body+' is hit '+prevQuery.count+' times!');
					});
				}
			}
			);
													
            skip_count = 0;
            for (i = 0; i < lectures.length; i++) {
                if (increasingOrderInclusion(lectures[i].course_title, title) && filter_check(lectures[i], filter)){
                    if (skip_count < per_page * (page-1))
                        skip_count++;
                    else {
                        result.lectures.push(lectures[i]);
                    }
                }
                if (result.lectures.length >= per_page) break;
            }
            return result;
        } else if (options.type === "instructor") {
            //교수명으로 검색
            var instructor = safeString(options.query_text).replace(/ /g, "").toLowerCase();
            skip_count = 0;
            for (i=0;i<lectures.length;i++){
                var lecture_instructor = safeString(lectures[i].instructor).replace(/ /g, "").toLowerCase();
                if (lecture_instructor.indexOf(instructor) !== -1 && filter_check(lectures[i], filter)){
                    if (skip_count < per_page * (page-1))
                        skip_count++;
                    else{
                        result.lectures.push(lectures[i]);
                    }
                }
                if (result.lectures.length >= per_page) break;
            }
            return result;
        } else if (options.type === "course_number") {
            //교과목번호로 검색
            var course_number = safeString(options.query_text).replace(/ /g, "").toLowerCase();
            skip_count = 0;
            for (i = 0; i < lectures.length; i++){
                var lecture_course_number = (safeString(lectures[i].course_number)+safeString(lectures[i].lecture_number)).replace(/ /g, "").toLowerCase();
                if (lecture_course_number.indexOf(course_number) !== -1 && filter_check(lectures[i], filter)){
                    if (skip_count < per_page * (page-1))
                        skip_count++;
                    else{
                        result.lectures.push(lectures[i]);
                    }
                }
                if (result.lectures.length >= per_page) break;
            }
            return result;
        } else if (options.type === "class_time") {
            //수업교시로 검색
            var class_times = safeString(options.query_text).replace(/ /g, "").split(',');
            skip_count = 0;
            for (i=0; i<lectures.length; i++) {
                var lecture_class_times = safeString(lectures[i].class_time).split(',');
                //강의시간 사이에 검색시간이 존재하면 추가
                if (class_times_check(lecture_class_times, class_times) &&
                    filter_check(lectures[i], filter)) {

                    if (skip_count < per_page * (page-1))
                        skip_count++;
                    else{
                        result.lectures.push(lectures[i]);
                    }
                }
                if (result.lectures.length >= per_page) break;
            }
            return result;
        } else if (options.type === "department") {
            //개설학과로 검색
            var department = safeString(options.query_text);
            skip_count = 0;
            for (i=0;i<lectures.length;i++) {
                if (increasingOrderInclusion(lectures[i].department, department) && filter_check(lectures[i], filter)){
                    if (skip_count < per_page * (page-1))
                        skip_count++;
                    else {
                        //비고에 개설학과 추가
                        result.lectures.push(lectures[i]);
                    }
                }
                if (result.lectures.length >= per_page) break;
            }
            return result;
        }
    },
    _load_data: function (year, semester) {
        /* Used Internally. DO NOT CALL IT OUTSIDE OF THIS MODULE. */
        var hash = year + semester;
        var datapath = config.snutt.ROOT_DATA_PATH + "/txt/"+year+"_"+semester+".txt";
        var coursebook = this.coursebook;
        var lastCoursebookInfo = this.lastCoursebookInfo;

        console.log(datapath);
        fs.readFile(datapath, function(err, data) {
            if (err) {
                console.log('DATA LOAD FAILED : ' + year + "_" + semester);
                return;
            }
            var lines = data.toString().split("\n");
            var lectures = [];
            year = lines[0].split("/")[0].trim();
            semester = lines[0].split("/")[1].trim();
            var updated_time = lines[1];
            var header = lines[2].split(";");
            for (var i=3;i<lines.length;i++){
                var line = lines[i];
                var options = {};
                var components = line.split(";");
                for (var j=0;j<components.length;j++){
                    options[header[j]] = components[j];
                }
                if (safeString(options.category).indexOf('core') !== -1)
                    options.classification = "핵교";
                lectures.push(new Lecture(options));
            }
            coursebook[hash] = {
                lectures : lectures,
                year : year,
                semester : semester,
                updated_time : updated_time
            };
            if (lastCoursebookInfo.updated_time < updated_time) {
                lastCoursebookInfo.year = year;
                lastCoursebookInfo.semester = semester;
                lastCoursebookInfo.updated_time = updated_time;
            }

            console.log('LOAD COMPLETE : ' + year + "_" + semester);
        });
    }
};


function filter_check(lecture, filter) {
    if (!filter)
        return true;

    var academic_filter= function (academic_year) {
        return (academic_year === "1" && lecture.academic_year === "1학년") ||
               (academic_year === "2" && lecture.academic_year === "2학년") ||
               (academic_year === "3" && lecture.academic_year === "3학년") ||
               (academic_year === "4" && (lecture.academic_year === "4학년" || lecture.academic_year === "5학년")) ||
               (academic_year === "5" && (lecture.academic_year === "석사" || lecture.academic_year === "박사" || lecture.academic_year === "석박사"));
    }
    var credit_filter = function (credit) {
        return (credit === "1" && lecture.credit === "1") ||
               (credit === "2" && lecture.credit === "2") ||
               (credit === "3" && lecture.credit === "3") ||
               (credit === "4" && lecture.credit === "4") ||
               (credit === "5" && parseInt(lecture.credit) >= 5);
    }
    //학년
    if (filter.hasOwnProperty('academic_year') && !_.any(filter.academic_year, academic_filter)) {
        return false;
    }
    //학점
    if (filter.hasOwnProperty('credit') && !_.any(filter.credit, credit_filter)) {
        return false;
    }

    //학문의기초, 핵심교양, 기타는 OR 연산
    if (filter.hasOwnProperty('basics') || filter.hasOwnProperty('core') ||  filter.hasOwnProperty('etc')) {
        var same_as_category = function (x) {
            return x === lecture.category;
        };
        var etc_check = function (etc) {
            return (etc === "teaching" && lecture.classification === "교직") ||
                   (etc === "exercise" && lecture.category === "normal_exercise") ||
                   (etc === "etc" && lecture.category  === "normal_exercise" &&
                    safeString(lecture.category).indexOf('normal') !== -1);
        };
        //학문의 기초
        //핵심교양
        //기타
        return filter.hasOwnProperty('basics') && _.any(filter.basics, same_as_category) ||
            filter.hasOwnProperty('core') && _.any(filter.core, same_as_category) ||
           filter.hasOwnProperty('etc') && _.any(filter.etc, etc_check);
    }
    return true;
}

//[월3-2, 수3-2], [월3, 화3]
function class_times_check(lecture_class_times, search_class_times) {
    //search_class_time이 lecture_class_time 사이에 존재하는가.
    //월3-2, 월4
    function single_time_check(args) {
        var lecture_class_time = args[0];
        var search_class_time = args[1];

        if (search_class_time === "")
            return true;
        var lecture_wday = lecture_class_time[0];
        var lecture_start_time = parseFloat(lecture_class_time.replace(/[()]/g, "").split("-")[0].slice(1));
        var lecture_duration = parseFloat(lecture_class_time.replace(/[()]/g, "").split("-")[1]);
        var search_wday = search_class_time[0];
        var search_time = parseFloat(search_class_time.slice(1));
        if (isNaN(search_time) && search_class_time.length !== 1)
            return false; //입력방식 오류
        if (isNaN(search_time))
            return (lecture_wday === search_wday);

        return (lecture_wday === search_wday && (lecture_start_time <= search_time && search_time < lecture_start_time + lecture_duration));
    }
    return _.any(_.zip(lecture_class_times, search_class_times), single_time_check);
}

module.exports.NaiveLectureModel = NaiveLectureModel;

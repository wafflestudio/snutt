import mongoose = require('mongoose');
import Timetable from './model/Timetable';
import UserLecture from './/model/UserLecture';
import AbstractTimetable from './model/AbstractTimetable';
import InvalidLectureUpdateRequestError from './error/InvalidLectureUpdateRequestError';
import TimetableNotFoundError from './error/TimetableNotFoundError';
import UserLectureNotFoundError from './error/UserLectureNotFoundError';
import ObjectUtil = require('@app/core/common/util/ObjectUtil');

export const NUMBER_OF_THEME = 6

let userLectureSchema = new mongoose.Schema({
  classification: String,                           // 교과 구분
  department: String,                               // 학부
  academic_year: String,                            // 학년
  course_title: {type: String, required: true},   // 과목명
  credit: Number,                                   // 학점
  class_time: String,
  class_time_json: [
    {day: Number, start: Number, len: Number, place: String}
  ],
  class_time_mask: {type: [Number], required: true, default: [0, 0, 0, 0, 0, 0, 0]},
  instructor: String,                               // 강사
  quota: Number,                                    // 정원
  enrollment: Number,                               // 신청인원
  remark: String,                                   // 비고
  category: String,
  course_number: String,
  lecture_number: String,
  created_at: Date,
  updated_at: Date,
  color: {fg: String, bg: String},
  colorIndex: {type: Number, required: true, default: 0}
});

let TimetableSchema = new mongoose.Schema({
  user_id: {type: mongoose.Schema.Types.ObjectId, ref: 'User'},
  year: {type: Number, required: true},
  semester: {type: Number, required: true, min: 1, max: 4},
  title: {type: String, required: true},
  lecture_list: [userLectureSchema],
  updated_at: Date,
  theme: {type: Number, required: true, default: 0, min: 0, max:NUMBER_OF_THEME-1}
});

TimetableSchema.index({user_id: 1})

TimetableSchema.pre('save', function (next) {
  this.updated_at = Date.now();
  next();
});

let mongooseModel = mongoose.model('Timetable', TimetableSchema, 'timetables');

export async function findByUserIdAndSemesterAndTitle(userId: string, year: number, semester: number, title: string): Promise<Timetable> {
  let doc = await mongooseModel.findOne({
    user_id: userId,
    year: year,
    semester: semester,
    title: title
  }).exec();
  return fromMongoose(doc);
}

export async function findByUserIdAndMongooseId(userId: string, mongooseId: string): Promise<Timetable> {
  var doc = await mongooseModel.findOne({'user_id': userId, '_id': mongooseId}).exec();
  return fromMongoose(doc);
}

export async function findByUserIdAndSemester(userId: string, year: number, semester: number): Promise<Timetable[]> {
  let docs = await mongooseModel.find({'user_id': userId, 'year': year, 'semester': semester}).exec();
  return docs.map(fromMongoose);
}

export async function findBySemester(year: number, semester: number): Promise<Timetable[]> {
  let docs: any = await mongooseModel.find({'year': year, 'semester': semester}).lean().exec();
  return docs.map(fromMongoose);
}

export async function findAbstractListByUserId(userId: string): Promise<AbstractTimetable[]> {
  const docs = await mongooseModel.aggregate([{
    $match: {'user_id': userId}
    }, {
      $unwind: {
        path: "$lecture_list",
        preserveNullAndEmptyArrays: true
      }
    }, {
      $group: {
        _id: "$_id",
        year: {$first: "$year"},
        semester: {$first: "$semester"},
        title: {$first: "$title"},
        updated_at: {$first: "$updated_at"},
        total_credit: {
          $sum: {
            $cond: [{
              $or: [{$not: "$lecture_list"},
                {$not: "$lecture_list.credit"}
              ]
            }, 0, "$lecture_list.credit"]
          }
        }
      }
    },
      {$sort: {_id:1}}
  ]).exec()
  return docs
}

export async function findHavingLecture(year: number, semester: number, courseNumber: string, lectureNumber: string): Promise<Timetable[]> {
  let docs = await mongooseModel.find(
    {
      year: year,
      semester: semester,
      lecture_list: {
        $elemMatch: {
          course_number: courseNumber,
          lecture_number: lectureNumber
        }
      }
    }).exec();

  return docs.map(fromMongoose);
}

export async function findRecentByUserId(userId: string): Promise<Timetable> {
  let doc = await mongooseModel.findOne({'user_id': userId}).sort({updated_at: -1}).exec();
  return fromMongoose(doc);
}

function makeUserLecturePartialUpdateQuery(lecture: any) {
  var updateMap = {};
  let lectureCopy = ObjectUtil.deepCopy(lecture);
  ObjectUtil.deleteObjectId(lectureCopy);
  for (var field in lectureCopy) {
    updateMap['lecture_list.$.' + field] = lectureCopy[field];
  }
  return {
    $set: updateMap
  };
}

export async function partialUpdateUserLecture(tableId: string, lecture: any): Promise<void> {
  if (!lecture || !lecture._id) {
    throw new InvalidLectureUpdateRequestError(lecture);
  }
  let updateQuery = makeUserLecturePartialUpdateQuery(lecture);
  let newMongooseDocument: any = await mongooseModel.findOneAndUpdate(
    {"_id": tableId, "lecture_list._id": lecture._id},
    updateQuery,
    {new: true}).exec();

  if (!newMongooseDocument.lecture_list.id(lecture._id)) {
    throw new UserLectureNotFoundError();
  }
}

export async function deleteLectureWithUserId(userId: string, tableId: string, lectureId: string): Promise<void> {
  let document = await mongooseModel.findOneAndUpdate(
    {'_id': tableId, 'user_id': userId},
    {$pull: {lecture_list: {_id: lectureId}}}, {new: true})
    .exec();
  if (!document) throw new TimetableNotFoundError();
}

export async function deleteLecture(tableId: string, lectureId: string): Promise<void> {
  let document = await mongooseModel.findOneAndUpdate(
    {'_id': tableId},
    {$pull: {lecture_list: {_id: lectureId}}}, {new: true})
    .exec();
  if (!document) throw new TimetableNotFoundError();
}

export async function deleteLectureByCourseNumber(tableId: string, courseNumber: string, lectureNumber: string): Promise<void> {
  let document = await mongooseModel.findOneAndUpdate(
    {'_id': tableId},
    {$pull: {lecture_list: {course_number: courseNumber, lecture_number: lectureNumber}}}, {new: true})
    .exec();
  if (!document) throw new TimetableNotFoundError();
}

export async function deleteByUserIdAndMongooseId(userId: string, tableId: string): Promise<void> {
  let document = await mongooseModel.findOneAndRemove({'user_id': userId, '_id': tableId}).lean().exec();
  if (!document) throw new TimetableNotFoundError();
}

export async function updateTitleByUserId(tableId: string, userId: string, title: string): Promise<void> {
  let document = await mongooseModel.findOneAndUpdate(
    {'_id': tableId, 'user_id': userId},
    {$set: {title: title}}, {new: true})
    .exec();
  if (!document) throw new TimetableNotFoundError();
}

export async function updateThemeByUserId(tableId: string, userId: string, theme: number): Promise<void> {
  let document = await mongooseModel.findOneAndUpdate(
    {'_id': tableId, 'user_id': userId},
    {$set: {theme: theme}}, {new: true})
    .exec();
  if (!document) throw new TimetableNotFoundError();
}

export async function insert(table: Timetable): Promise<Timetable> {
  let doc = new mongooseModel({
    user_id: table.user_id,
    year: table.year,
    semester: table.semester,
    title: table.title,
    lecture_list: table.lecture_list,
    updated_at: table.updated_at
  });
  await doc.save();
  return fromMongoose(doc);
}

export async function insertUserLecture(tableId: string, lecture: UserLecture): Promise<void> {
  ObjectUtil.deleteObjectId(lecture);
  let document = await mongooseModel.findOne({'_id': tableId}).exec();
  document['lecture_list'].push(lecture);
  await document.save();
}

export async function updateUpdatedAt(tableId: string, updatedAt: number): Promise<void> {
  let document = await mongooseModel.findOneAndUpdate(
    {'_id': tableId},
    {$set: {updated_at: updatedAt}}, {new: true})
    .exec();
  if (!document) throw new TimetableNotFoundError();
}

function fromMongoose(mongooseDoc): Timetable {
  if (!mongooseDoc) return null;

  let lecture_list = undefined;
  if (mongooseDoc.lecture_list) {
    lecture_list = [];
    for (let i = 0; i < mongooseDoc.lecture_list.length; i++) {
      lecture_list.push(lectureFromMongoose(mongooseDoc.lecture_list[i]));
    }
  }

  return {
    _id: mongooseDoc._id,
    user_id: mongooseDoc.user_id,
    year: mongooseDoc.year,
    semester: mongooseDoc.semester,
    title: mongooseDoc.title,
    lecture_list: lecture_list,
    theme: mongooseDoc.theme,
    updated_at: mongooseDoc.updated_at
  }
}

function lectureFromMongoose(mongooseDoc): UserLecture {
  return {
    _id: mongooseDoc._id,
    classification: mongooseDoc.classification,                           // 교과 구분
    department: mongooseDoc.department,                               // 학부
    academic_year: mongooseDoc.academic_year,                            // 학년
    course_title: mongooseDoc.course_title,   // 과목명
    credit: mongooseDoc.credit,                                   // 학점
    class_time: mongooseDoc.class_time,
    class_time_json: mongooseDoc.class_time_json,
    class_time_mask: mongooseDoc.class_time_mask,
    instructor: mongooseDoc.instructor,                               // 강사
    quota: mongooseDoc.quota,                                    // 정원
    remark: mongooseDoc.remark,                                   // 비고
    category: mongooseDoc.category,
    course_number: mongooseDoc.course_number,   // 교과목 번호
    lecture_number: mongooseDoc.lecture_number,  // 강좌 번호
    created_at: mongooseDoc.created_at,
    updated_at: mongooseDoc.updated_at,
    color: mongooseDoc.color,
    colorIndex: mongooseDoc.colorIndex,
  }
}

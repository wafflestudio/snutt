/*
 * model/lecture.js
 * Lecture는 수강편람 상의 강의
 * UserLecture는 유저 시간표 상의 강의
 */
import mongoose = require('mongoose');

import RefLecture from './model/RefLecture';
import RefLectrureNotFoundError from './error/RefLectureNotFoundError';
import {Time} from "@app/core/timetable/model/Time";

let refLectureSchema = new mongoose.Schema({
  classification: String,                           // 교과 구분
  department: String,                               // 학부
  academic_year: String,                            // 학년
  course_title: { type: String, required: true },   // 과목명
  credit: Number,                                   // 학점
  class_time: String,
  real_class_time: String,
  class_time_json: [
    { day : Number, place : String, startMinute: Number, endMinute: Number }
  ],
  class_time_mask: { type: [ Number ], required: true, default: [0,0,0,0,0,0,0] },
  instructor: String,                               // 강사
  quota: Number,                                    // 정원
  freshmanQuota: Number,                            // 신입생정원
  enrollment: Number,                               // 신청인원
  remark: String,                                   // 비고
  category: String,
  year: { type: Number, required: true },           // 연도
  semester: { type: Number, required: true },       // 학기
  course_number: { type: String, required: true},   // 교과목 번호
  lecture_number: { type: String, required: true},  // 강좌 번호
});

refLectureSchema.index({ year: 1, semester: 1});
refLectureSchema.index({ course_number: 1, lecture_number: 1 })

let mongooseModel = mongoose.model('Lecture', refLectureSchema, 'lectures');

export async function queryWithCourseTitle(
  query, courseTitle: string, limit: number, offset: number): Promise<RefLecture[]> {
  let firstChar = courseTitle.slice(0, 1);
  let docs = await mongooseModel.aggregate([
    { $match: query },
    {
      $addFields: {
        _firstChar: { $substrCP: ["$course_title", 0, 1] },
        _lastChar: { $substrCP: ["$course_title", { $subtract: [{ $strLenCP: "$course_title" }, 1] }, 1] },
        _courseTitleLengthExceptWhiteSpace : {
          $reduce: {
            input: {
              $split: ["$course_title", " "]
            },
            initialValue: 0,
            in: { $add: [
              "$$value",
              {
                $strLenCP: "$$this"
              }
            ]}
          }
        },
      }
    },
    {
      $addFields: {
        _firstCharMatches : {
          $cond: {
            if: {
              $eq: [
                "$_firstChar",
                firstChar
              ]
            },
            then: 0,
            else: 1
          }
        },
        _endsWithNumber : {
          $cond: {
            if: {
              $and: [
                {
                  $gte: [ { $strcasecmp: ["$_lastChar", "0"] }, 0 ]
                },
                {
                  $lte: [ { $strcasecmp: ["$_lastChar", "9"] }, 0 ]
                }
              ]
            },
            then: 0,
            else: 1
          }
        }
      }
    },
    {
      $sort: {
        _firstCharMatches: 1,
        _courseTitleLengthExceptWhiteSpace: 1,
        _endsWithNumber: 1,
        course_title: 1
      }
    },
    { $skip: offset },
    { $limit: limit }
  ]).exec();
  return docs.map(fromMongoose);
}

export async function query(query, limit, offset): Promise<RefLecture[]> {
  let docs = await mongooseModel.find(query).sort('course_title')
    .skip(offset)
    .limit(limit)
    .exec();
  return docs.map(fromMongoose);
}

export async function findByCourseNumber
    (year: number, semester: number, courseNumber: string, lectureNumber: string): Promise<RefLecture> {
  let doc = await mongooseModel.findOne({'year': year, 'semester': semester,
    'course_number': courseNumber, 'lecture_number': lectureNumber}).lean()
    .exec();
    return fromMongoose(doc);
}

export async function findByMongooseId(id: any): Promise<RefLecture> {
  let doc = await mongooseModel.findOne({'_id': id}).exec();
  return fromMongoose(doc);
}

export async function findBySemester(year: number, semester: number): Promise<RefLecture[]> {
  let docs: any = await mongooseModel.find({year : year, semester : semester}).lean().exec();
  return docs.map(fromMongoose);
}

export async function deleteBySemester(year: number, semester: number): Promise<void> {
  await mongooseModel.remove({ year: year, semester: semester}).exec();
}

export async function deleteByLectureId(lectureId: string): Promise<void> {
  await mongooseModel.remove({ _id: lectureId }).exec();
}

export async function insertAll(lectures: RefLecture[]): Promise<number> {
  let docs = await mongooseModel.insertMany(lectures);
  return docs.length;
}

export async function partialUpdateRefLecture(lectureId: string, lecture: any): Promise<RefLecture> {
  let newMongooseDocument: any = await mongooseModel.findOneAndUpdate(
    { "_id": lectureId },
    { $set: lecture },
    {new: true}).exec();

  if (!newMongooseDocument) {
    throw new RefLectrureNotFoundError();
  }

  return fromMongoose(newMongooseDocument);
}

function fromMongoose(mongooseDoc): RefLecture {
  let classTime = (typeof mongooseDoc.class_time_json.toObject !== "undefined" ?
    mongooseDoc.class_time_json.toObject() : mongooseDoc.class_time_json).map(json => {
    let startTime = new Time(json.startMinute)
    let endTime = new Time(json.endMinute)
    let start = Math.floor((startTime.getDecimalHour() - 8) * 2) / 2
    let end = Math.ceil((endTime.getDecimalHour() - 8) * 2) / 2
    return {
      ...json,
      start_time: startTime.toHourMinuteFormat(),
      end_time: endTime.toHourMinuteFormat(),
      start: start,
      len: end - start
    }
  })
  if (mongooseDoc === null) return null;
  return {
    _id: mongooseDoc._id,
    classification: mongooseDoc.classification,                           // 교과 구분
    department: mongooseDoc.department,                               // 학부
    academic_year: mongooseDoc.academic_year,                            // 학년
    course_title: mongooseDoc.course_title,   // 과목명
    credit: mongooseDoc.credit,                                   // 학점
    class_time: mongooseDoc.class_time,
    real_class_time: mongooseDoc.real_class_time,
    class_time_json: classTime,
    class_time_mask: mongooseDoc.class_time_mask,
    instructor: mongooseDoc.instructor,                               // 강사
    quota: mongooseDoc.quota,                                    // 정원
    freshmanQuota: mongooseDoc.freshmanQuota,                    // 신입생정원
    remark: mongooseDoc.remark,                                   // 비고
    category: mongooseDoc.category,
    year: mongooseDoc.year,           // 연도
    semester: mongooseDoc.semester,       // 학기
    course_number: mongooseDoc.course_number,   // 교과목 번호
    lecture_number: mongooseDoc.lecture_number,  // 강좌 번호
  }
}

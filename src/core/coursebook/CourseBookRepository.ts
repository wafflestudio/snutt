import mongoose = require('mongoose');
import CourseBook from './model/CourseBook';

var CourseBookSchema = new mongoose.Schema({
  year: { type: Number, required: true },
  semester: { type: Number, required: true },
  updated_at: Date
});

let mongooseModel = mongoose.model('CourseBook', CourseBookSchema, 'coursebooks');

export async function findAll(): Promise<CourseBook[]> {
    let docs = await mongooseModel
        .find({}, '-_id year semester updated_at')
        .sort([["year", -1], ["semester", -1]])
        .exec();
    return docs.map(fromMongoose);
}

export async function findRecent(): Promise<CourseBook | null> {
    let doc = await mongooseModel
        .findOne({}, '-_id year semester updated_at')
        .sort([["year", -1], ["semester", -1]])
        .exec();
    return fromMongoose(doc);
}

export async function findLastTwoSemesters(): Promise<CourseBook[] | null> {
    let docs = await mongooseModel
        .find({}, '-_id year semester updated_at')
        .sort([["year", -1], ["semester", -1]])
        .skip(1)
        .limit(2)
        .exec();
    return docs.map(fromMongoose);
}

export async function findByYearAndSemester(year: number, semester: number): Promise<CourseBook | null> {
    let doc = await mongooseModel
        .findOne({year: year, semester: semester})
        .exec();
    return fromMongoose(doc);
}

export async function insert(courseBook: CourseBook): Promise<void> {
    await new mongooseModel(courseBook).save();
}

export async function update(courseBook: CourseBook): Promise<void> {
    await mongooseModel.updateOne(
        {year: courseBook.year, semester: courseBook.semester},courseBook).exec();
}

function fromMongoose(mongooseDoc): CourseBook | null {
    if (mongooseDoc === null) return null;
    return {
        year: mongooseDoc.year,
        semester: mongooseDoc.semester,
        updated_at: mongooseDoc.updated_at
    }
}

import RefLectureRepository = require('./RefLectureRepository');
import RefLecture from './model/RefLecture';
import ObjectUtil = require('@app/core/common/util/ObjectUtil');
import LectureService = require('@app/core/lecture/LectureService');

export function query(query: any, limit: number, offset: number): Promise<RefLecture[]> {
    return RefLectureRepository.query(query, limit, offset);
}

export function queryWithCourseTitle(query: any, courseTitle: string, limit: number, offset: number): Promise<RefLecture[]> {
    return RefLectureRepository.queryWithCourseTitle(query, courseTitle, limit, offset);
}

export function getByMongooseId(mongooseId: string): Promise<RefLecture> {
    return RefLectureRepository.findByMongooseId(mongooseId);
}

export function getByCourseNumber(year: number, semester: number, courseNumber: string, lectureNumber: string): Promise<RefLecture> {
    return RefLectureRepository.findByCourseNumber(year, semester, courseNumber, lectureNumber);
}

export function getBySemester(year: number, semester: number): Promise<RefLecture[]> {
    return RefLectureRepository.findBySemester(year, semester);
}

export function addAll(lectures: RefLecture[]): Promise<number> {
    return RefLectureRepository.insertAll(lectures);
}

export function removeBySemester(year: number, semester: number): Promise<void> {
    return RefLectureRepository.deleteBySemester(year, semester);
}

export function remove(lectureId: string): Promise<void> {
    return RefLectureRepository.deleteByLectureId(lectureId);
}

export function partialModifiy(lectureId: string, lecture: any): Promise<RefLecture> {
    let lectureCopy = ObjectUtil.deepCopy(lecture);
    ObjectUtil.deleteObjectId(lectureCopy);
    return RefLectureRepository.partialUpdateRefLecture(lectureId, lectureCopy);
}

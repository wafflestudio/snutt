import request = require('request-promise-native');
import retry = require('async-retry');
import winston = require('winston');
import ExcelUtil = require('@app/batch/coursebook/excel/ExcelUtil');
import RefLecture from '@app/core/lecture/model/RefLecture';
import SugangSnuLectureService = require('@app/batch/coursebook/sugangsnu/SugangSnuLectureService');
import SugangSnuLectureCategoryService = require('@app/batch/coursebook/sugangsnu/SugangSnuLectureCategoryService');
let logger = winston.loggers.get('default');

const SEMESTER_1 = 1;
const SEMESTER_S = 2;
const SEMESTER_2 = 3;
const SEMESTER_W = 4;

const semesterString = {
    [SEMESTER_1]: "1",
    [SEMESTER_S]: "S",
    [SEMESTER_2]: "2",
    [SEMESTER_W]: "W",
}

const semesterQueryString = {
    [SEMESTER_1]: "U000200001U000300001",
    [SEMESTER_S]: "U000200001U000300002",
    [SEMESTER_2]: "U000200002U000300001",
    [SEMESTER_W]: "U000200002U000300002",
}

export async function getRefLectureList(year: number, semester: number): Promise<RefLecture[]> {
    let ret: RefLecture[] = [];

    if (!(await isCoursebookOpened(year, semester))) {
        return ret;
    }

    // apply의 경우 두번째 인자의 개수가 너무 많을 경우 fail할 수 있음
    // lectureCategory = null일 경우 getRefLectureListForCategory 안에서 교양 영역 강좌는 필터링
    ret.push.apply(ret, await getRefLectureListForCategory(year, semester, null));
    for (let category of SugangSnuLectureCategoryService.lectureCategoryList) {
        ret.push.apply(ret, await getRefLectureListForCategory(year, semester, category));
    }
    return ret;
}

/**
 * 수강스누 2.0부터는 없는 학기의 편람 엑셀 URL을 호출해도
 * 결과가 응답되고, 강좌 데이터가 들어 있다
 *
 * 따라서 수강 편람 존재 여부를 확인하기 위해
 * 검색 조건 API를 호출하여, 현재 수강 학기와 비교한다.
 */
async function isCoursebookOpened(year: number, semester: number): Promise<boolean> {
    return retry(bail => {
        return request.post(makeIsCoursebookOpenedUrl(year, semester), {
            resolveWithFullResponse: true,
            timeout: 1000,
            headers: {
                "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.80 Safari/537.36",
                "Referrer": "https://sugang.snu.ac.kr/sugang/cc/cc100InterfaceExcel.action"
            },
            json: true
        }).then(function(response: request.FullResponse) {
            if (response.statusCode >= 400) {
                logger.warn("status code " + response.statusCode);
                return Promise.resolve(false);
            }
            let latestYear = parseInt(response.body.currSchyy);
            let currShtmFg = response.body.currShtmFg;
            let currDetaShtmFg = response.body.currDetaShtmFg;
            let latestSemesterQueryString = currShtmFg + currDetaShtmFg;
            let latestSemester = -1;
            for (let semester in semesterQueryString) {
                if (semesterQueryString[semester] === latestSemesterQueryString) {
                    latestSemester = parseInt(semester);
                    break;
                }
            }

            if (latestSemester < 0) {
                return Promise.reject("Semester not found for " + currShtmFg + " " + currDetaShtmFg);
            }

            return Promise.resolve(
                (year < latestYear) ||
                (year == latestYear && semester <= latestSemester)
            );
        });
    }, {
        retries: 2,
        maxTimeout: 60000,
        onRetry: function(err) {
            logger.error("Retry triggered by: " + err)
        }
    });
}

const SUGANG_SNU_SEARCH_CONDITION_BASEPATH = 'https://sugang.snu.ac.kr/sugang/cc/cc100ajax.action?';
function makeIsCoursebookOpenedUrl(year: number, semester: number): string {
    let params = {
        openUpDeptCd: "",
        openDeptCd: "",
        srchOpenSchyy: year,
        srchOpenShtm: semesterQueryString[semester]
    };

    let retarr = [];
    for (let key in params) {
        retarr.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
    }
    let ret = SUGANG_SNU_SEARCH_CONDITION_BASEPATH + retarr.join('&');
    logger.debug(ret);
    return ret;
}

async function getRefLectureListForCategory(year: number, semester: number, lectureCategory: number): Promise<RefLecture[]> {
    let fileBuffer: Buffer = await getCoursebookExcelFileForCategory(year, semester, lectureCategory);
    if (fileBuffer.byteLength == 0) {
        logger.info("No response");
        return [];
    }
    let sheet = ExcelUtil.getFirstSheetFromBuffer(fileBuffer);
    return SugangSnuLectureService.getRefLectureListFromExcelSheet(sheet, year, semester, lectureCategory);;
}

function getCoursebookExcelFileForCategory(year: number, semester: number, lectureCategory: number): Promise<Buffer> {
    return retry(bail => {
        return request.post(makeCoursebookExcelFileUrl(year, semester, lectureCategory), {
            encoding: null, // return as binary
            resolveWithFullResponse: true,
            timeout: 10000,
            headers: {
                "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.80 Safari/537.36",
                "Referrer": "https://sugang.snu.ac.kr/sugang/cc/cc100InterfaceExcel.action"
            }
        }).then(function(response: request.FullResponse) {
            if (response.statusCode >= 400) {
                logger.warn("status code " + response.statusCode);
                return Promise.resolve(new Buffer(0));
            }
            if (!response.headers["content-disposition"]) {
                logger.warn("No content-disposition found");
                return Promise.resolve(new Buffer(0));
            }
            return response.body;
        });
    }, {
        retries: 5,
        maxTimeout: 60000
    });
}

const SUGANG_SNU_BASEPATH = "https://sugang.snu.ac.kr/sugang/cc/cc100InterfaceExcel.action?";
function makeCoursebookExcelFileUrl(year: number, semester: number, lectureCategory: number): string {
    let params = {
        seeMore: "더보기",
        srchBdNo: "",
        srchCamp: "",
        srchCptnCorsFg: "",
        srchCurrPage: 1,
        srchExcept: "",
        // 추가됨
        srchGenrlRemoteLtYn: "",
        srchIsEngSbjt: "",
        // 추가됨
        srchIsPendingCourse: "",
        srchLanguage: "ko",
        srchLsnProgType: "",
        srchMrksApprMthdChgPosbYn: "",
        srchMrksGvMthd: "",
        srchOpenUpDeptCd: "",
        srchOpenMjCd: "",
        srchOpenPntMax: "",
        srchOpenPntMin: "",
        srchOpenSbjtDayNm: "",
        srchOpenSbjtNm: "",
        srchOpenSbjtTm: "",
        srchOpenSbjtTmNm: "",
        srchOpenSchyy: year,
        srchOpenShtm: semesterQueryString[semester],
        srchOpenShyr: "",
        srchOpenSubmattCorsFg: "",
        srchOpenSubmattFgCd1: "",
        srchOpenSubmattFgCd2: "",
        srchOpenSubmattFgCd3: "",
        srchOpenSubmattFgCd4: "",
        srchOpenSubmattFgCd5: "",
        srchOpenSubmattFgCd6: "",
        srchOpenSubmattFgCd7: "",
        srchOpenSubmattFgCd8: "",
        // 추가됨
        srchOpenSubmattFgCd9: "",
        srchOpenDeptCd: "",
        srchOpenUpSbjtFldCd: "",
        srchPageSize: 9999,
        srchProfNm: "",
        srchSbjtCd: "",
        srchSbjtNm: "",
        srchTlsnAplyCapaCntMax: "",
        srchTlsnAplyCapaCntMin: "",
        srchTlsnRcntMax: "",
        srchTlsnRcntMin: "",
        workType: "EX",
        // srchIsAplyAvailable: "", // 사라짐
    }

    if (lectureCategory === null) {
        params["srchOpenSbjtFldCd"] = "";
        logger.info("Fetching " + year + "-" + semesterString[semester]);
    } else {
        params["srchOpenUpSbjtFldCd"] = SugangSnuLectureCategoryService.getLectureUpperCategory(lectureCategory);
        params["srchOpenSbjtFldCd"] = lectureCategory;
        logger.info("Fetching " + SugangSnuLectureCategoryService.getLectureCategoryString(lectureCategory));
    }

    let retarr = [];
    for (let key in params) {
        retarr.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
    }
    let ret = SUGANG_SNU_BASEPATH + retarr.join('&');
    logger.debug(ret);
    return ret;
}

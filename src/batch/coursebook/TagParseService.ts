import RefLecture from '@app/core/lecture/model/RefLecture';
import TagListEtcTagService = require('@app/core/taglist/TagListEtcTagService');
import winston = require('winston');
var logger = winston.loggers.get('default');

export type TagStruct = {
  classification : string[],
  department : string[],
  academic_year : string[],
  credit : string[],
  instructor : string[],
  category : string[],
  etc: string[]
};

export function parseTagFromLectureList(lines:RefLecture[]): TagStruct {
  var tags: TagStruct = {
    classification : [],
    department : [],
    academic_year : [],
    credit : [],
    instructor : [],
    category : [],
    etc: TagListEtcTagService.getEtcTagList()
  };
  for (let i=0; i<lines.length; i++) {
    var line = lines[i];

    var new_tag = {
      classification : line.classification,
      department : line.department,
      academic_year : line.academic_year,
      credit : line.credit+'학점',
      instructor : line.instructor,
      category : line.category,
    };

    for (let key in new_tag) {
      if (tags.hasOwnProperty(key)){
        if (!tags[key].includes(new_tag[key])) {
          // 한글자인 태그는 버린다.
          if (new_tag[key].length < 2) continue;
          tags[key].push(new_tag[key]);
        }
      }
    }
  }

  for (var key in tags) {
    if (tags.hasOwnProperty(key)) {
      if (key === 'credit') {
        tags[key].sort((a, b) => parseInt(a) - parseInt(b));
      } else {
        tags[key].sort();
      }
    }
  }
  return tags;
}

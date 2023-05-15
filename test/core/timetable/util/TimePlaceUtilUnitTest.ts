import assert = require('assert');
import TimePlaceUtil = require('@app/core/timetable/util/TimePlaceUtil');
import TimePlace from '@app/core/timetable/model/TimePlace';

describe('TimePlaceUtilUnitTest', function () {
  it("timeJsonToMask__success__emptyJson", async function () {
    assert.deepEqual([0, 0, 0, 0, 0, 0, 0], TimePlaceUtil.timeJsonToMask([]));
  })

  it("timeJsonToMask__success", async function () {
    assert.deepEqual([0, parseInt("00011" + "11000" + "00000" + "00000" + "00000" + "00000", 2), 0,
        parseInt("00011" + "11000" + "00000" + "00000" + "00000" + "00000", 2), 0, 0, 0],
      TimePlaceUtil.timeJsonToMask([{day: 1, start: 1.5, len: 2, place: '220-317', start_time: null, end_time: null, startMinute: null, endMinute: null},
        {day: 3, start: 1.5, len: 2, place: '220-317', start_time: null, end_time: null, startMinute: null, endMinute: null}]));
    assert.deepEqual([0, parseInt("00011" + "11100" + "00000" + "00000" + "00000" + "00000", 2), 0,
        parseInt("00011" + "11100" + "00000" + "00000" + "00000" + "00000", 2), 0, 0, 0],
      TimePlaceUtil.timeJsonToMask([{day: 1, start: 1.5, len: 2.5, place: '220-317', start_time: null, end_time: null, startMinute: null, endMinute: null},
        {day: 3, start: 1.5, len: 2.5, place: '220-317', start_time: null, end_time: null, startMinute: null, endMinute: null}]));
    assert.deepEqual([0, parseInt("00001" + "11100" + "00000" + "00000" + "00000" + "00000", 2), 0,
        parseInt("00001" + "11100" + "00000" + "00000" + "00000" + "00000", 2), 0, 0, 0],
      TimePlaceUtil.timeJsonToMask([{day: 1, start: 2, len: 2, place: '220-317', start_time: null, end_time: null, startMinute: null, endMinute: null},
        {day: 3, start: 2, len: 2, place: '220-317', start_time: null, end_time: null, startMinute: null, endMinute: null}]));
    assert.deepEqual([0, parseInt("00000" + "00000" + "00000" + "00000" + "11111" + "11111", 2), 0,
        parseInt("00000" + "00000" + "00000" + "00000" + "11111" + "11111", 2), 0, 0, 0],
      TimePlaceUtil.timeJsonToMask([{day: 1, start: 10, len: 5, place: '220-317', start_time: null, end_time: null, startMinute: null, endMinute: null},
        {day: 3, start: 10, len: 5, place: '220-317', start_time: null, end_time: null, startMinute: null, endMinute: null}]));
    assert.deepEqual([0, parseInt("00000" + "00000" + "00000" + "00000" + "00000" + "01100", 2), 0,
        parseInt("00000" + "00000" + "00000" + "00000" + "00000" + "01100", 2), 0, 0, 0],
      TimePlaceUtil.timeJsonToMask([{day: 1, start: 13, len: 1, place: "302-308", start_time: null, end_time: null, startMinute: null, endMinute: null},
        {day: 3, start: 13, len: 1, place: "302-308", start_time: null, end_time: null, startMinute: null, endMinute: null}]));
    assert.deepEqual([0, parseInt("00000" + "00000" + "00000" + "00000" + "00000" + "00000", 2), 0,
        parseInt("00000" + "00000" + "00000" + "00000" + "00000" + "00000", 2), 0, 0, 0],
      TimePlaceUtil.timeJsonToMask([{day: 1, start: -1, len: 1, place: "302-308", start_time: null, end_time: null, startMinute: null, endMinute: null},
        {day: 3, start: 15, len: 1, place: "302-308", start_time: null, end_time: null, startMinute: null, endMinute: null}]));
  });
});

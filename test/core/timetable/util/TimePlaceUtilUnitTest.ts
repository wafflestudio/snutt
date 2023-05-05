import assert = require('assert');
import TimePlaceUtil = require('@app/core/timetable/util/TimePlaceUtil');
import TimePlace from '@app/core/timetable/model/TimePlace';

describe('TimePlaceUtilUnitTest', function () {
  it("timeAndPlaceToJson__success__emptyTimeString", async function () {
    assert.deepEqual([], TimePlaceUtil.timeAndPlaceToJson('', '', ''));
  })

  it("timeAndPlaceToJson__success__coursesInDifferentDay", async function () {
    assert.deepEqual([{day: 1, start: 1, len: 2, start_time: '09:00', end_time: '10:50', place: '220-317', startMinute: 540, endMinute: 650},
        {day: 3, start: 1, len: 2, start_time: '09:00', end_time: '10:50', place: '220-317', startMinute: 540, endMinute: 650}],
      TimePlaceUtil.timeAndPlaceToJson('화(1-2)/목(1-2)', '220-317/220-317', '화(09:00~10:50)/목(09:00~10:50)'));
  })

  it("timeAndPlaceToJson__success__onlyOneLocation", async function () {
    assert.deepEqual([{day: 1, start: 1, len: 2, start_time: '09:00', end_time: '10:50', place: '220-317', startMinute: 540, endMinute: 650},
        {day: 3, start: 1, len: 2, start_time: '09:00', end_time: '10:50', place: '220-317', startMinute: 540, endMinute: 650}],
      TimePlaceUtil.timeAndPlaceToJson('화(1-2)/목(1-2)', '220-317', '화(09:00~10:50)/목(09:00~10:50)'));
  })

  it("timeAndPlaceToJson__success__emptyLocation", async function () {
    assert.deepEqual([{day: 1, start: 1, len: 2, start_time: '09:00', end_time: '10:50', place: '', startMinute: 540, endMinute: 650},
        {day: 3, start: 1, len: 2, start_time: '09:00', end_time: '10:50', place: '', startMinute: 540, endMinute: 650}],
      TimePlaceUtil.timeAndPlaceToJson('화(1-2)/목(1-2)', '/', '화(09:00~10:50)/목(09:00~10:50)'));
  })

  it("timeAndPlaceToJson__success__noLocation", async function () {
    assert.deepEqual([{day: 1, start: 1, len: 2, start_time: '09:00', end_time: '10:50', place: '', startMinute: 540, endMinute: 650},
        {day: 3, start: 1, len: 2, start_time: '09:00', end_time: '10:50', place: '', startMinute: 540, endMinute: 650}],
      TimePlaceUtil.timeAndPlaceToJson('화(1-2)/목(1-2)', '', '화(09:00~10:50)/목(09:00~10:50)'));
  })

  it("timeAndPlaceToJson__success__floatingPointTime", async function () {
    assert.deepEqual([{day: 1, start: 1.5, len: 2, place: '220-317', start_time: '09:30', end_time: '11:20', startMinute: 570, endMinute: 680},
        {day: 3, start: 1.5, len: 2, place: '220-317', start_time: '09:30', end_time: '11:20', startMinute: 570, endMinute: 680}],
      TimePlaceUtil.timeAndPlaceToJson('화(1.5-2)/목(1.5-2)', '220-317/220-317', '화(09:30~11:20)/목(09:30~11:20)'));
  })

  it("timeAndPlaceToJson__success__coursesInSameDay", async function () {
    assert.deepEqual([{day: 1, start: 3, len: 1, place: '302-208', start_time: '11:00', end_time: '11:50', startMinute: 660, endMinute: 710},
        {day: 3, start: 3, len: 1, place: '302-208', start_time: '11:00', end_time: '11:50', startMinute: 660, endMinute: 710},
        {day: 3, start: 11, len: 2, place: '302-310-2', start_time: '19:00', end_time: '20:50', startMinute: 1140, endMinute: 1250}],
      TimePlaceUtil.timeAndPlaceToJson('화(3-1)/목(3-1)/목(11-2)', '302-208/302-208/302-310-2', '화(11:00~11:50)/목(11:00~11:50)/목(19:00~20:50)'));
  })

  it("timeAndPlaceToJson__success__doNotMergeContinuousCourseButDiffLocation", async function () {
    assert.deepEqual([
        {day: 3, start: 9, len: 2, place: '220-317', start_time: '17:00', end_time: '18:50', startMinute: 1020, endMinute: 1130},
        {day: 3, start: 11, len: 2, place: '220-316', start_time: '19:00', end_time: '20:50', startMinute: 1140, endMinute: 1250}
      ],
      TimePlaceUtil.timeAndPlaceToJson('목(9-2)/목(11-2)', '220-317/220-316', '화(17:00~18:50)/목(19:00~20:50)'));
  })

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

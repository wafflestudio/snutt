var assert = require('assert')
var timeAndPlaceToJson = require('../data/update_lectures.js').timeAndPlaceToJson
var timeJsonToMask = require('../data/update_lectures').timeJsonToMask

describe('TimeConverter', function() {
	describe('empty time', function() {
		it('should be converted to empty array', function() {
			assert.deepEqual([], timeAndPlaceToJson('',''))
		})
	})
  describe('courses in different day', function() {
    it('should be converted into different element', function() {
      assert.deepEqual([{day: 1, start:1, len:2, place: '220-317'},
        {day:3, start:1, len:2, place: '220-317'}],
        timeAndPlaceToJson('화(1-2)/목(1-2)', '220-317/220-317'))
    })
  })
  describe('courses starts at decimal time', function() {
    it('should be converted correctly', function() {
      assert.deepEqual([{day: 1, start:1.5, len:2, place: '220-317'},
        {day:3, start:1.5, len:2, place: '220-317'}],
        timeAndPlaceToJson('화(1.5-2)/목(1.5-2)', '220-317/220-317'))
    })
  })
  describe('courses in same day', function() {
    it('should be stored seperately', function() {
      assert.deepEqual([{day: 1, start:3, len:1, place: '302-208'},
        {day: 3, start:3, len:1, place: '302-208'},
        {day: 3, start:11, len:2, place: '302-310-2'}],
        timeAndPlaceToJson('화(3-1)/목(3-1)/목(11-2)', '302-208/302-208/302-310-2'))
    })
  })
  describe('two continuous courses', function() {
    it('should be merged', function() {
      assert.deepEqual([{day: 3, start: 9, len: 4, place: '220-317'}],
        timeAndPlaceToJson('목(9-2)/목(11-2)', '220-317/220-317'))
    })
  })
  describe('course held in two classroom', function() {
    it('should be merged', function() {
      assert.deepEqual([
        {day: 0, start: 3, len: 1.5, place: '500-L302'},
        {day: 2, start: 3, len: 1.5, place: '500-L302'},
        {day: 4, start: 3, len: 2, place: '020-103/020-104'}
      ],
        timeAndPlaceToJson('월(3-1.5)/수(3-1.5)/금(3-2)/금(3-2)', '500-L302/500-L302/020-103/020-104'))
    })
  })
})

describe('TimeMaskConverter', function() {
  describe('empty timeJson', function() {
    it('should be converted to array with six zeros', function() {
      assert.deepEqual([0, 0, 0, 0, 0, 0], timeJsonToMask([]))
    })
  })
  describe('timeJson with decimal start & length', function() {
    it('should be converted correctly', function() {
      assert.deepEqual([0, parseInt("00011"+"11000"+"00000"+"00000"+"00000"+"0", 2), 0,
        parseInt("00011"+"11000"+"00000"+"00000"+"00000"+"0", 2), 0, 0],
        timeJsonToMask([{day: 1, start:1.5, len:2, place: '220-317'},
        {day:3, start:1.5, len:2, place: '220-317'}]))
      assert.deepEqual([0, parseInt("00011"+"11100"+"00000"+"00000"+"00000"+"0", 2), 0,
        parseInt("00011"+"11100"+"00000"+"00000"+"00000"+"0", 2), 0, 0],
        timeJsonToMask([{day: 1, start:1.5, len:2.5, place: '220-317'},
        {day:3, start:1.5, len:2.5, place: '220-317'}]))
      assert.deepEqual([0, parseInt("00001"+"11100"+"00000"+"00000"+"00000"+"0", 2), 0,
        parseInt("00001"+"11100"+"00000"+"00000"+"00000"+"0", 2), 0, 0],
        timeJsonToMask([{day: 1, start:2, len:2, place: '220-317'},
        {day:3, start:2, len:2, place: '220-317'}]))
    })
  })
})
